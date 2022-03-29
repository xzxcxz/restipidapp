package com.diva.restofinder.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.diva.restofinder.R
import com.diva.restofinder.adapter.HighlightsAdapter
import com.diva.restofinder.db.FavoriteRestaurantDao
import com.diva.restofinder.model.RestaurantDataDto
import com.diva.restofinder.model.RestaurantDetailResponseDto
import com.diva.restofinder.networking.ZomatoAPI
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_detail_resto.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class DetailRestaurantActivity : AppCompatActivity() {

    private var mProgressBar: ProgressDialog? = null
    private var highlightsAdapter: HighlightsAdapter? = null
    private val modelHighlightResponses: MutableList<String> = ArrayList()

    private var ratingValue = 0.0
    private var restaurantId: String? = null
    private var imageCover: String? = null
    private var title: String? = null
    private var rating: String? = null
    private var restaurantName: String? = null
    private var restaurantObj: RestaurantDataDto? = null

    private var isSaved = false

    @Inject
    lateinit var zomatoAPI: ZomatoAPI

    @Inject
    lateinit var favoriteRestaurantDao: FavoriteRestaurantDao

    @SuppressLint("Assert", "SetTextI18n", "ObsoleteSdkInt")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_resto)

        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        if (Build.VERSION.SDK_INT >= 21) {
            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false)
            window.statusBarColor = resources.getColor(R.color.colorPrimary)
        }

        mProgressBar = ProgressDialog(this)
        mProgressBar?.setTitle("Please wait")
        mProgressBar?.setCancelable(false)
        mProgressBar?.setMessage("Showing data...")

        toolbar.title = ""
//        setSupportActionBar(toolbar)
//        assert(supportActionBar != null)
//        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        isSaved = intent.getBooleanExtra(IS_SAVED, false)

        restaurantObj = intent.getParcelableExtra(DETAIL) as RestaurantDataDto?
        if (restaurantObj != null) {
            Log.d("DetailActivity", restaurantObj.toString())

            restaurantId = restaurantObj?.id
            imageCover = restaurantObj?.thumbRestaurant
            ratingValue = restaurantObj?.rating?.ratingValue ?: 0.0
            title = restaurantObj?.name
            rating = restaurantObj?.rating?.ratingText
            restaurantName = restaurantObj?.name

            tvTitle.text = title
            tvRestoName.text = restaurantName
            tvRating.text = "$ratingValue | $rating"
            tvTitle.isSelected = true
            val newValue = ratingValue.toFloat()

            ratingResto.numStars = 5
            ratingResto.stepSize = 0.5.toFloat()
            ratingResto.rating = newValue

            if (isSaved) {
                Glide.with(this)
                    .load(restaurantObj?.thumbRestaurantBitmap)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(imgCover)
            } else {
                Glide.with(this)
                    .load(imageCover)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(imgCover)
            }

            //method get Highlight
            showRecyclerViewList()

            //method get Detail
            getDetailRestaurant()
        }
    }

    private fun showRecyclerViewList() {
        highlightsAdapter = HighlightsAdapter(modelHighlightResponses)

        rvHighlights.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        rvHighlights.setHasFixedSize(true)
        rvHighlights.adapter = highlightsAdapter
    }

    private fun getDetailRestaurant() {
        mProgressBar?.show()
//        AndroidNetworking.get(ApiEndpoint.BASEURL + ApiEndpoint.DetailRestaurant + IdResto)
//            .addHeaders("user-key", "b47b1abf3c3436d473570116cd8a2621")
//            .setPriority(Priority.HIGH)
//            .build()
//            .getAsJSONObject(object : JSONObjectRequestListener {
//                @SuppressLint("SetTextI18n")
//                override fun onResponse(response: JSONObject) {
//                    try {
//                        mProgressBar?.dismiss()
//                        val jsonArrayOne = response.getJSONArray("highlights")
//
//                        for (i in 0 until jsonArrayOne.length()) {
//                            val dataApi = HighlightResponseDto(listOf("One", "Two", "Three"))
//                            modelHighlightResponses.add(dataApi)
//                        }
//
//                        val jsonObjectData = response.getJSONObject("location")
//                        val jsonArrayTwo = response.getJSONArray("establishment")
//
//                        for (x in 0 until jsonArrayTwo.length()) {
//                            val Establishment = jsonArrayTwo[x].toString()
//                            tvEstablishment.text = Establishment
//                        }
//
//                        val AverageCost = response.getString("average_cost_for_two")
//                        val PriceRange = response.getString("price_range")
//                        val Currency = response.getString("currency")
//                        val Timings = response.getString("timings")
//                        val LocalityVerbose = jsonObjectData.getString("locality_verbose")
//                        val Address = jsonObjectData.getString("address")
//                        val Telepon = response.getString("phone_numbers")
//                        val Website = response.getString("url")
//                        val Latitude = jsonObjectData.getDouble("latitude")
//                        val Longitude = jsonObjectData.getDouble("longitude")
//
//                        tvLocalityVerbose.text = LocalityVerbose
//                        tvAverageCost.text = "$Currency $AverageCost / $PriceRange person"
//                        tvAddress.text = Address
//                        tvOpenTime.text = Timings
//
//                        llRoute.setOnClickListener {
//                            val intent = Intent(Intent.ACTION_VIEW,
//                                Uri.parse("http://maps.google.com/maps?daddr=$Latitude,$Longitude"))
//                            startActivity(intent)
//                        }
//
//                        llTelpon.setOnClickListener {
//                            val intent: Intent
//                            intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$Telepon"))
//                            startActivity(intent)
//                        }
//
//                        llWebsite.setOnClickListener {
//                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(Website))
//                            startActivity(intent)
//                        }
//
//                        highlightsAdapter?.notifyDataSetChanged()
//                    } catch (e: JSONException) {
//                        e.printStackTrace()
//                        Toast.makeText(this@DetailRestoActivity,
//                            "Failed to display data!", Toast.LENGTH_SHORT).show()
//                    }
//                }
//
//                override fun onError(anError: ANError) {
//                    mProgressBar?.dismiss()
//                    Toast.makeText(this@DetailRestoActivity,
//                        "No internet connection !", Toast.LENGTH_SHORT).show()
//                }
//            })

        if (isSaved) {

            CoroutineScope(Dispatchers.IO).launch {
                restaurantObj?.let { data ->
                    val restaurantDetail =
                        favoriteRestaurantDao.getRestaurantDetailById(data.id)

                    withContext(Dispatchers.Main) {
                        Log.d("DetailActivity", data.toString())
                        mProgressBar?.dismiss()
                        displayRestaurantDetail(restaurantDetail)
                    }
                }

            }
        } else {
            CoroutineScope(Dispatchers.IO).launch {

                val response = withContext(Dispatchers.IO) {
                    zomatoAPI.getRestaurantDetail(
                        restaurantId ?: ""
                    )
                }

                withContext(Dispatchers.Main) {
                    mProgressBar?.dismiss()
                }

                if (response.isSuccessful) {
                    response.body()?.let { res ->
                        withContext(Dispatchers.Main) {
                            Log.d("DetailActivity", res.toString())
                            displayRestaurantDetail(res)
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        mProgressBar?.dismiss()
                        Toast.makeText(
                            this@DetailRestaurantActivity,
                            "No internet connection!",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                }
            }
        }

    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    private fun displayRestaurantDetail(restaurantDetail: RestaurantDetailResponseDto) {
        restaurantDetail.highlights?.let {
            modelHighlightResponses.clear()
            modelHighlightResponses.addAll(restaurantDetail.highlights)
        }

        tvEstablishment.text = restaurantDetail.establishment.toString()
        tvLocalityVerbose.text = restaurantDetail.location?.locality
        tvAverageCost.text =
            "${restaurantDetail.currency} ${restaurantDetail.averageCostForTwo} / ${restaurantDetail.priceRange} person"
        tvAddress.text = restaurantDetail.location?.address
        tvAddress.text = restaurantDetail.timings

        llRoute.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("http://maps.google.com/maps?daddr=${restaurantDetail.location?.latitude},${restaurantDetail.location?.longitude}")
            )
            startActivity(intent)
        }

        llTelpon.setOnClickListener {
            val intent =
                Intent(Intent.ACTION_DIAL, Uri.parse("tel:${restaurantDetail.phoneNumbers}"))
            startActivity(intent)
        }

        llWebsite.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(restaurantDetail.url))
            startActivity(intent)
        }

        highlightsAdapter?.notifyDataSetChanged()
    }

//    private fun getReviewResto() {
//        mProgressBar?.show()
//        AndroidNetworking.get(ApiEndpoint.BASEURL + ApiEndpoint.ReviewRestaurant + IdResto)
//            .addHeaders("user-key", "b47b1abf3c3436d473570116cd8a2621")
//            .setPriority(Priority.HIGH)
//            .build()
//            .getAsJSONObject(object : JSONObjectRequestListener {
//                override fun onResponse(response: JSONObject) {
//                    try {
//                        mProgressBar?.dismiss()
//                        val jsonArray = response.getJSONArray("user_reviews")
//
//                        for (i in 0 until jsonArray.length()) {
//                            val jsonObject = jsonArray.getJSONObject(i)
//                            val dataApi = ReviewDto()
//                            val jsonObjectDataOne = jsonObject.getJSONObject("review")
//                            val jsonObjectDataTwo = jsonObjectDataOne.getJSONObject("user")
//                            dataApi.ratingReview = jsonObjectDataOne.getDouble("rating")
//                            dataApi.reviewText = jsonObjectDataOne.getString("review_text")
//                            dataApi.reviewTime = jsonObjectDataOne.getString("review_time_friendly")
//                            dataApi.nameUser = jsonObjectDataTwo.getString("name")
//                            dataApi.profileImage = jsonObjectDataTwo.getString("profile_image")
//                            reviewDto.add(dataApi)
//                        }
//
//                        reviewAdapter?.notifyDataSetChanged()
//                    } catch (e: JSONException) {
//                        e.printStackTrace()
//                        Toast.makeText(this@DetailRestoActivity, "Failed to display data!", Toast.LENGTH_SHORT).show()
//                    }
//                }
//
//                override fun onError(anError: ANError) {
//                    mProgressBar?.dismiss()
//                    Toast.makeText(this@DetailRestoActivity, "No internet connection !", Toast.LENGTH_SHORT).show()
//                }
//            })
//    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        const val DETAIL = "restaurant_detail"
        const val IS_SAVED = "is_saved"
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
}