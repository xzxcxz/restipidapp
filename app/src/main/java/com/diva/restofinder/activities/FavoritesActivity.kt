package com.diva.restofinder.activities

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.diva.restofinder.R
import com.diva.restofinder.adapter.MainAdapter
import com.diva.restofinder.db.FavoriteRestaurantDao
import com.diva.restofinder.model.RestaurantDataDto
import com.diva.restofinder.model.RestaurantResponseDto
import com.diva.restofinder.utils.OnMainAdapterCallback
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class FavoritesActivity : AppCompatActivity() {

    private var mainAdapter: MainAdapter? = null
    private var restaurants = listOf<RestaurantDataDto>()

    private var mProgressBar: ProgressDialog? = null

    @Inject
    lateinit var favoriteRestaurantDao: FavoriteRestaurantDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites)

        mProgressBar = ProgressDialog(this)
        mProgressBar?.setTitle("Please wait")
        mProgressBar?.setCancelable(false)
        mProgressBar?.setMessage("Showing data...")

        loadFavorites()

        // method to show recyclerview and attach adapter.
        showRecyclerRestaurant()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadFavorites() {
        CoroutineScope(Dispatchers.IO).launch {
            restaurants = favoriteRestaurantDao.getAll()

            withContext(Dispatchers.Main) {
                showRecyclerRestaurant()
                mainAdapter?.notifyDataSetChanged()
            }
        }
    }

    private fun showRecyclerRestaurant() {
        mainAdapter = MainAdapter(this, restaurants)

        rvRestaurantsNearby.layoutManager = LinearLayoutManager(this)
        rvRestaurantsNearby.setHasFixedSize(true)
        rvRestaurantsNearby.adapter = mainAdapter

        mainAdapter?.setOnItemClickCallback(object : OnMainAdapterCallback {
            override fun onItemMainClicked(restaurantResponseDto: RestaurantResponseDto) {
                val intent = Intent(this@FavoritesActivity, DetailRestaurantActivity::class.java)
                intent.putExtra(DetailRestaurantActivity.DETAIL, restaurantResponseDto.restaurant)
                intent.putExtra(DetailRestaurantActivity.IS_SAVED, true)
                startActivity(intent)
            }

            override fun onItemMainClicked(restaurant: RestaurantDataDto) {
                val intent = Intent(this@FavoritesActivity, DetailRestaurantActivity::class.java)
                intent.putExtra(DetailRestaurantActivity.DETAIL, restaurant)
                intent.putExtra(DetailRestaurantActivity.IS_SAVED, true)
                startActivity(intent)
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun onFavoriteClicked(
                restaurantResponseDto: RestaurantResponseDto,
                isFavorite: Boolean
            ) {
                if (!isFavorite) {
                    restaurantResponseDto.restaurant.let {
                        CoroutineScope(Dispatchers.IO).launch {
                            favoriteRestaurantDao.removeFromFavorites(it)

                            val restaurantDetailToRemove =
                                favoriteRestaurantDao.getRestaurantDetailById(it.id)
                            favoriteRestaurantDao.removeRestaurantDetail(restaurantDetailToRemove)

                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    this@FavoritesActivity,
                                    "Removed from favorites!",
                                    Toast.LENGTH_SHORT
                                ).show()

                                loadFavorites()
                                mainAdapter?.notifyDataSetChanged()
                            }
                        }
                    }
                }
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun onFavoriteClicked(restaurant: RestaurantDataDto, isFavorite: Boolean) {
                if (!isFavorite) {
                    restaurant.let {
                        CoroutineScope(Dispatchers.IO).launch {
                            favoriteRestaurantDao.removeFromFavorites(it)

                            val restaurantDetailToRemove =
                                favoriteRestaurantDao.getRestaurantDetailById(it.id)
                            favoriteRestaurantDao.removeRestaurantDetail(restaurantDetailToRemove)

                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    this@FavoritesActivity,
                                    "Removed from favorites!",
                                    Toast.LENGTH_SHORT
                                ).show()

                                loadFavorites()
                                mainAdapter?.notifyDataSetChanged()
                            }
                        }
                    }
                }
            }
        })
    }
}