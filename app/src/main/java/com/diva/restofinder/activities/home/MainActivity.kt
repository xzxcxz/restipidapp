package com.diva.restofinder.activities.home

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.diva.restofinder.R
import com.diva.restofinder.activities.DetailRestaurantActivity
import com.diva.restofinder.activities.FavoritesActivity
import com.diva.restofinder.adapter.MainAdapter
import com.diva.restofinder.adapter.MainAdapterHorizontal
import com.diva.restofinder.db.FavoriteRestaurantDao
import com.diva.restofinder.model.CollectionDataDto
import com.diva.restofinder.model.RestaurantDataDto
import com.diva.restofinder.model.RestaurantResponseDto
import com.diva.restofinder.networking.ZomatoAPI
import com.diva.restofinder.utils.OnMainAdapterCallback
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), LocationListener {

    private lateinit var mainAdapterHorizontal: MainAdapterHorizontal
    private lateinit var mainAdapter: MainAdapter
    private var mProgressBar: ProgressDialog? = null
    private val collections: MutableList<CollectionDataDto> = ArrayList()
    private var restaurants: MutableList<RestaurantResponseDto> = ArrayList()
    private var lat: Double = 0.0
    private var lng: Double = 0.0

    private var permissionArrays = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    @Inject
    lateinit var zomatoAPI: ZomatoAPI

    @Inject
    lateinit var favoriteRestaurantDao: FavoriteRestaurantDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val myVersion = Build.VERSION.SDK_INT
        if (myVersion > Build.VERSION_CODES.LOLLIPOP_MR1 &&
            !checkIfAlreadyHavePermission() &&
            !checkIfAlreadyHavePermission2()
        ) {
            requestPermissions(permissionArrays, 101)
        }

        mProgressBar = ProgressDialog(this)
        mProgressBar?.setTitle("Please wait")
        mProgressBar?.setCancelable(false)
        mProgressBar?.setMessage("Showing data...")

        // method to show recyclerview and attach adapter.
        showRecyclerRestaurant()

        searchResto.queryHint = "Search restaurant"
        searchResto.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                setSearchRestaurant(query)
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (newText.isBlank()) getLatLong()
                return false
            }
        })

        val searchPlateId = searchResto.context
            .resources.getIdentifier("android:id/search_plate", null, null)
        val searchPlate = searchResto.findViewById<View>(searchPlateId)
        searchPlate?.setBackgroundColor(Color.TRANSPARENT)

        // method get location (latitude and longitude)
        getLatLong()
    }

    private fun showRecyclerRestaurant() {
        mainAdapterHorizontal = MainAdapterHorizontal(this, collections)
        mainAdapter = MainAdapter(this, restaurants)

        rvRestaurantsNearby.layoutManager = LinearLayoutManager(this)
        rvRestaurantsNearby.setHasFixedSize(true)
        rvRestaurantsNearby.adapter = mainAdapter

        mainAdapter.setOnItemClickCallback(object : OnMainAdapterCallback {
            override fun onItemMainClicked(restaurantResponseDto: RestaurantResponseDto) {
                val intent = Intent(this@MainActivity, DetailRestaurantActivity::class.java)
                intent.putExtra(DetailRestaurantActivity.DETAIL, restaurantResponseDto.restaurant)
                intent.putExtra(DetailRestaurantActivity.IS_SAVED, false)
                startActivity(intent)
            }

            override fun onItemMainClicked(restaurant: RestaurantDataDto) {
                val intent = Intent(this@MainActivity, DetailRestaurantActivity::class.java)
                intent.putExtra(DetailRestaurantActivity.DETAIL, restaurant)
                intent.putExtra(DetailRestaurantActivity.IS_SAVED, false)
                startActivity(intent)
            }

            override fun onFavoriteClicked(
                restaurantResponseDto: RestaurantResponseDto,
                isFavorite: Boolean
            ) {
                if (isFavorite) {
                    restaurantResponseDto.restaurant.let {
                        CoroutineScope(Dispatchers.IO).launch {
                            favoriteRestaurantDao.addToFavorites(it)
                            val response = withContext(Dispatchers.IO) {
                                zomatoAPI.getRestaurantDetail(it.id)
                            }



                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    this@MainActivity,
                                    "Added to favorites!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            if (response.isSuccessful) {

                                response.body()?.let { res ->

                                    Glide.with(this@MainActivity)
                                        .asBitmap()
                                        .load(res.thumb)
                                        .transform(CenterCrop(), RoundedCorners(25))
                                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                                        .into(object : CustomTarget<Bitmap>() {
                                            override fun onResourceReady(
                                                resource: Bitmap,
                                                transition: Transition<in Bitmap>?
                                            ) {
                                                res.thumbBitmap = resource
                                            }

                                            override fun onLoadCleared(placeholder: Drawable?) {
                                                // TODO("Not yet implemented")
                                            }
                                        })

                                    favoriteRestaurantDao.addRestaurantDetail(res.copy(roomId = res.id))
                                }

                            } else {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Cannot fetch restaurant detail!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    }
                } else {
                    restaurantResponseDto.restaurant.let {
                        CoroutineScope(Dispatchers.IO).launch {
                            favoriteRestaurantDao.removeFromFavorites(it)

                            val restaurantDetailToRemove =
                                favoriteRestaurantDao.getRestaurantDetailById(it.id)
                            favoriteRestaurantDao.removeRestaurantDetail(restaurantDetailToRemove)

                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    this@MainActivity,
                                    "Removed from favorites!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
            }

            override fun onFavoriteClicked(restaurant: RestaurantDataDto, isFavorite: Boolean) {
                if (isFavorite) {
                    restaurant.let {
                        CoroutineScope(Dispatchers.IO).launch {
                            favoriteRestaurantDao.addToFavorites(it)
                            val response = withContext(Dispatchers.IO) {
                                zomatoAPI.getRestaurantDetail(it.id)
                            }

                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    this@MainActivity,
                                    "Added to favorites!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            if (response.isSuccessful) {

                                response.body()?.let { res ->
                                    favoriteRestaurantDao.addRestaurantDetail(res.copy(roomId = res.id))
                                }

                            } else {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Cannot fetch restaurant detail!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    }
                } else {
                    restaurant.let {
                        CoroutineScope(Dispatchers.IO).launch {
                            favoriteRestaurantDao.removeFromFavorites(it)

                            val restaurantDetailToRemove =
                                favoriteRestaurantDao.getRestaurantDetailById(it.id)
                            favoriteRestaurantDao.removeRestaurantDetail(restaurantDetailToRemove)

                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    this@MainActivity,
                                    "Removed from favorites!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
            }
        })
    }

    private fun getLatLong() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                115
            )
            return
        }


        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        val criteria = Criteria()
        val provider = locationManager.getBestProvider(criteria, true)
        val location = locationManager.getLastKnownLocation(provider.toString())

        if(lat > 0.0 && lng > 0.0) {
            // method to get list of restaurants at first run of application.
            getListCollection()

            // method to get list of restaurants at first run of application.
            getListRestaurant()
            return
        }

        if (location != null) {
            onLocationChanged(location)

            // method to get list of restaurants at first run of application.
            getListCollection()

            // method to get list of restaurants at first run of application.
            getListRestaurant()

        } else {
            locationManager.requestLocationUpdates(provider.toString(), 20000, 0f, this)
        }
    }

    override fun onLocationChanged(location: Location) {
        lng = location.longitude
        lat = location.latitude
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setSearchRestaurant(query: String) {
        mProgressBar?.show()
        CoroutineScope(Dispatchers.IO).launch {

            val response = withContext(Dispatchers.IO) {
                zomatoAPI.searchRestaurant(
                    query,
                    lat.toString(),
                    lng.toString()
                )
            }

            withContext(Dispatchers.Main) {
                mProgressBar?.dismiss()
            }

            if (response.isSuccessful) {
                response.body()?.let { res ->
                    if (restaurants.isNotEmpty()) restaurants.clear()
                    res.restaurants.forEach { item ->
                        item.restaurant.let { data ->
                            restaurants.add(
                                RestaurantResponseDto(
                                    RestaurantDataDto(
                                        roomId = data.id,
                                        id = data.id,
                                        name = data.name,
                                        thumbRestaurant = data.thumbRestaurant,
                                        url = data.url,
                                        isFavorite = favoriteRestaurantDao.isRestaurantExisting(data.id) == 1,
                                        restaurantLocation = data.restaurantLocation,
                                        rating = data.rating
                                    )
                                )
                            )
                        }


                    }

                    withContext(Dispatchers.Main) {
                        showRecyclerRestaurant()
                        mainAdapter.notifyDataSetChanged()
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    mProgressBar?.dismiss()
                    Toast.makeText(this@MainActivity, "No internet connection!", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getListCollection() {
        mProgressBar?.show()

        CoroutineScope(Dispatchers.IO).launch {

            val response = withContext(Dispatchers.IO) {
                zomatoAPI.getCollection(
                    lat.toString(),
                    lng.toString()
                )
            }

            withContext(Dispatchers.Main) {
                mProgressBar?.dismiss()
            }

            if (response.isSuccessful) {
                response.body()?.let { res ->
                    if (restaurants.isNotEmpty()) restaurants.clear()

                    res.collectionEntities.forEach { item ->
                        collections.add(
                            CollectionDataDto(
                                collectionId = item.collection.collectionId,
                                description = item.collection.description,
                                imageUrl = item.collection.imageUrl,
                                resCount = item.collection.resCount,
                                shareUrl = item.collection.shareUrl,
                                title = item.collection.title,
                                url = item.collection.url
                            )
                        )
                    }


                    withContext(Dispatchers.Main) {
                        mainAdapterHorizontal.notifyDataSetChanged()
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    mProgressBar?.dismiss()
                    Toast.makeText(this@MainActivity, "No internet connection!", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getListRestaurant() {
        mProgressBar?.show()

        CoroutineScope(Dispatchers.IO).launch {
            val response = withContext(Dispatchers.IO) {
                zomatoAPI.getGeocode(
                    lat.toString(),
                    lng.toString()
                )
            }

            withContext(Dispatchers.Main) {
                mProgressBar?.dismiss()
            }

            if (restaurants.isNotEmpty()) restaurants.clear()

            if (response.isSuccessful) {

                response.body()?.let { geocode ->
                    geocode.nearbyRestaurant.forEach { item ->

                        item.restaurant.let { data ->

                            restaurants.add(
                                RestaurantResponseDto(
                                    RestaurantDataDto(
                                        roomId = data.id,
                                        id = data.id,
                                        name = data.name,
                                        thumbRestaurant = data.thumbRestaurant,
                                        url = data.url,
                                        isFavorite = favoriteRestaurantDao.isRestaurantExisting(
                                            data.id
                                        ) == 1,
                                        restaurantLocation = data.restaurantLocation,
                                        rating = data.rating
                                    )
                                )
                            )
                        }

                    }

                }


                withContext(Dispatchers.Main) {
                    // Log.d("MainActivity", restaurants.toString())
                    showRecyclerRestaurant()
                    mainAdapter.notifyDataSetChanged()
                }
            } else {
                withContext(Dispatchers.Main) {
                    mProgressBar?.dismiss()
                    Toast.makeText(
                        this@MainActivity,
                        "No internet connection!",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }

        }
    }

    private fun checkIfAlreadyHavePermission(): Boolean {
        val result =
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun checkIfAlreadyHavePermission2(): Boolean {
        val result =
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        return result == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        for (grantResult in grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                val intent = intent
                finish()
                startActivity(intent)
            } else {
                getLatLong()
            }
        }
    }

    override fun onStatusChanged(s: String, i: Int, bundle: Bundle) {}
    override fun onProviderEnabled(s: String) {}
    override fun onProviderDisabled(s: String) {}

    companion object {
        fun setWindowFlag(activity: Activity, bits: Int, on: Boolean) {
            val window = activity.window
            val layoutParams = window.attributes
            if (on) {
                layoutParams.flags = layoutParams.flags or bits
            } else {
                layoutParams.flags = layoutParams.flags and bits.inv()
            }
            window.attributes = layoutParams
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.drawer_menu, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.myFavorites) {
            startActivity(Intent(this, FavoritesActivity::class.java))
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}