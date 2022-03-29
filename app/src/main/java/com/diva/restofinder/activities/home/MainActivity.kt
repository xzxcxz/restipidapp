package com.diva.restofinder.activities.home

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.SearchView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
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
import com.google.android.material.navigation.NavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), LocationListener {

    private var mainAdapterHorizontal: MainAdapterHorizontal? = null
    private var mainAdapter: MainAdapter? = null
    private var mProgressBar: ProgressDialog? = null
    private val collections: MutableList<CollectionDataDto> = ArrayList()
    private var restaurants: MutableList<RestaurantResponseDto> = ArrayList()
    private var lat: Double? = null
    private var lng: Double? = null

    private var permissionArrays = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private lateinit var actionBarToggle: ActionBarDrawerToggle
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView

    @Inject
    lateinit var zomatoAPI: ZomatoAPI

    @Inject
    lateinit var favoriteRestaurantDao: FavoriteRestaurantDao

    @SuppressLint("ObsoleteSdkInt")
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        if (Build.VERSION.SDK_INT >= 21) {
            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false)
            window.statusBarColor = resources.getColor(R.color.colorPrimary)
        }

        val myVersion = Build.VERSION.SDK_INT
        if (myVersion > Build.VERSION_CODES.LOLLIPOP_MR1 &&
            !checkIfAlreadyHavePermission() &&
            !checkIfAlreadyHavePermission2()
        ) {
            requestPermissions(permissionArrays, 101)
        }

        drawerLayout = findViewById(R.id.drawerLayout)
        navView = findViewById(R.id.navView)

        actionBarToggle = ActionBarDrawerToggle(this, drawerLayout, 0, 0)
        drawerLayout.addDrawerListener(actionBarToggle)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        actionBarToggle.syncState()


        navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.myFavorites -> {
//                    startActivity(Intent(this@MainActivity, FavoritesActivity::class.java))
                    Toast.makeText(
                        applicationContext,
                        "Going to my favorites",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            true
        }

        mProgressBar = ProgressDialog(this)
        mProgressBar?.setTitle("Please wait")
        mProgressBar?.setCancelable(false)
        mProgressBar?.setMessage("Showing data...")

        searchResto.queryHint = "Search restaurant"
        searchResto.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                setSearchRestaurant(query)
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (newText == "") getListRestaurant()
                return false
            }
        })

        val searchPlateId = searchResto.context
            .resources.getIdentifier("android:id/search_plate", null, null)
        val searchPlate = searchResto.findViewById<View>(searchPlateId)
        searchPlate?.setBackgroundColor(Color.TRANSPARENT)

        // method to show recyclerview and attach adapter.
        showRecyclerRestaurant()

        // method get location (latitude and longitude)
        getLatLong()
    }

    private fun showRecyclerRestaurant() {
        mainAdapterHorizontal = MainAdapterHorizontal(this, collections)
        mainAdapter = MainAdapter(this, restaurants)

        rvRestaurantsNearby.layoutManager = LinearLayoutManager(this)
        rvRestaurantsNearby.setHasFixedSize(true)
        rvRestaurantsNearby.adapter = mainAdapter

        mainAdapter?.setOnItemClickCallback(object : OnMainAdapterCallback {
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
//        AndroidNetworking.get("${ApiEndpoint.BASEURL}${ApiEndpoint.SearchEndpoint}$query&lat=$lat&lon=$lng&radius=20000&sort=cost&order=asc")
//            .addHeaders("user-key", "b47b1abf3c3436d473570116cd8a2621")
//            .setPriority(Priority.HIGH)
//            .build()
//            .getAsJSONObject(object : JSONObjectRequestListener {
//                override fun onResponse(response: JSONObject) {
//                    try {
//                        mProgressBar?.dismiss()
//                        if (restaurants.isNotEmpty()) restaurants.clear()
//                        val jsonArray = response.getJSONArray("restaurants")
//
//                        for (i in 0 until jsonArray.length()) {
//                            val jsonObject = jsonArray.getJSONObject(i)
//                            val dataApi = RestaurantResponseDto()
//                            val jsonObjectData = jsonObject.getJSONObject("restaurant")
//                            val jsonObjectDataTwo = jsonObjectData.getJSONObject("user_rating")
//                            val AggregateRating = jsonObjectDataTwo.getDouble("aggregate_rating")
//                            val jsonObjectDataThree = jsonObjectData.getJSONObject("location")
//
//                            dataApi.id = jsonObjectData.getString("id")
//                            dataApi.name = jsonObjectData.getString("name")
//                            dataApi.thumbRestaurant = jsonObjectData.getString("thumb")
//                            dataApi.ratingText = jsonObjectDataTwo.getString("rating_text")
//                            dataApi.addressRestaurant = jsonObjectDataThree.getString("locality_verbose")
//                            dataApi.aggregateRating = AggregateRating
//                            restaurants.add(dataApi)
//                        }
//                        showRecyclerRestaurant()
//                        mainAdapter?.notifyDataSetChanged()
//                    } catch (e: JSONException) {
//                        e.printStackTrace()
//                        Toast.makeText(this@MainActivity, "Failed to display data!", Toast.LENGTH_SHORT).show()
//                    }
//                }
//
//                override fun onError(anError: ANError) {
//                    mProgressBar?.dismiss()
//                    Toast.makeText(this@MainActivity, "No internet connection!", Toast.LENGTH_SHORT).show()
//                }
//            })
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
                        mainAdapter?.notifyDataSetChanged()
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
//        AndroidNetworking.get(ApiEndpoint.BASEURL + ApiEndpoint.Collection + "lat=" + lat + "&lon=" + lng)
//            .addHeaders("user-key", "b47b1abf3c3436d473570116cd8a2621")
//            .setPriority(Priority.HIGH)
//            .build()
//            .getAsJSONObject(object : JSONObjectRequestListener {
//                override fun onResponse(response: JSONObject) {
//                    try {
//                        mProgressBar?.dismiss()
//                        val jsonArray = response.getJSONArray("collections")
//
//                        for (i in 0 until jsonArray.length()) {
//                            val jsonObject = jsonArray.getJSONObject(i)
//                            val dataApi = HorizontalDto()
//                            val jsonObjectData = jsonObject.getJSONObject("collection")
//
//                            dataApi.imageUrl = jsonObjectData.getString("image_url")
//                            dataApi.urlRestaurant = jsonObjectData.getString("url")
//                            dataApi.title = jsonObjectData.getString("title")
//                            dataApi.description = jsonObjectData.getString("description")
//                            collections.add(dataApi)
//                        }
//                        mainAdapterHorizontal?.notifyDataSetChanged()
//                    } catch (e: JSONException) {
//                        e.printStackTrace()
//                        Toast.makeText(this@MainActivity, "Failed to display data!", Toast.LENGTH_SHORT).show()
//                    }
//                }
//
//                override fun onError(anError: ANError) {
//                    mProgressBar?.dismiss()
//                    Toast.makeText(this@MainActivity, "No internet connection!", Toast.LENGTH_SHORT).show()
//                }
//            })

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
                        mainAdapterHorizontal?.notifyDataSetChanged()
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
//        AndroidNetworking.get(ApiEndpoint.BASEURL + ApiEndpoint.Geocode + "lat=" + lat + "&lon=" + lng)
//            .addHeaders("user-key", "b47b1abf3c3436d473570116cd8a2621")
//            .setPriority(Priority.HIGH)
//            .build()
//            .getAsJSONObject(object : JSONObjectRequestListener {
//                override fun onResponse(response: JSONObject) {
//                    try {
//                        mProgressBar?.dismiss()
//                        restaurants = ArrayList()
//                        val jsonArray = response.getJSONArray("nearby_restaurants")
//
//                        for (i in 0 until jsonArray.length()) {
//                            val jsonObject = jsonArray.getJSONObject(i)
//                            val dataApi = RestaurantResponseDto()
//                            val jsonObjectData = jsonObject.getJSONObject("restaurant")
//                            val jsonObjectDataTwo = jsonObjectData.getJSONObject("user_rating")
//                            val AggregateRating = jsonObjectDataTwo.getDouble("aggregate_rating")
//                            val jsonObjectDataThree = jsonObjectData.getJSONObject("location")
//
//                            dataApi.id = jsonObjectData.getString("id")
//                            dataApi.name = jsonObjectData.getString("name")
//                            dataApi.thumbRestaurant = jsonObjectData.getString("thumb")
//                            dataApi.ratingText = jsonObjectDataTwo.getString("rating_text")
//                            dataApi.addressRestaurant =
//                                jsonObjectDataThree.getString("locality_verbose")
//                            dataApi.aggregateRating = AggregateRating
//                            restaurants.add(dataApi)
//                        }
//                        showRecyclerRestaurant()
//                        mainAdapter?.notifyDataSetChanged()
//                    } catch (e: JSONException) {
//                        e.printStackTrace()
//                        Toast.makeText(
//                            this@MainActivity,
//                            "Failed to display data!",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    }
//                }
//
//                override fun onError(anError: ANError) {
//                    mProgressBar?.dismiss()
//                    Toast.makeText(this@MainActivity, "No internet connection!", Toast.LENGTH_SHORT)
//                        .show()
//                }
//            })

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

            response.nearbyRestaurant.forEach { item ->

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
                // Log.d("MainActivity", restaurants.toString())
                showRecyclerRestaurant()
                mainAdapter?.notifyDataSetChanged()
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

    override fun onSupportNavigateUp(): Boolean {
        if (this.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            this.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            this.drawerLayout.openDrawer(GravityCompat.START)
        }
        return true
    }

    override fun onBackPressed() {
        if (this.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            this.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(actionBarToggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}