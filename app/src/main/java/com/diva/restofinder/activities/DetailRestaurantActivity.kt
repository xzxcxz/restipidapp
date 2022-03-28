package com.diva.restofinder.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
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
import com.diva.restofinder.model.RestaurantResponseDto
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
    private var restaurantResponseDto: RestaurantResponseDto? = null

    @Inject
    lateinit var zomatoAPI: ZomatoAPI

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
        setSupportActionBar(toolbar)
        assert(supportActionBar != null)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        restaurantResponseDto = intent.getSerializableExtra(DETAIL_RESTO) as RestaurantResponseDto
        if (restaurantResponseDto != null) {
            restaurantId = restaurantResponseDto?.id
            imageCover = restaurantResponseDto?.thumbRestaurant
            ratingValue = restaurantResponseDto!!.rating.ratingValue
            title = restaurantResponseDto?.name
            rating = restaurantResponseDto?.rating?.ratingText
            restaurantName = restaurantResponseDto?.name

            tvTitle.text = title
            tvRestoName.text = restaurantName
            tvRating.text = "$ratingValue | $rating"
            tvTitle.isSelected = true
            val newValue = ratingValue.toFloat()

            ratingResto.numStars = 5
            ratingResto.stepSize = 0.5.toFloat()
            ratingResto.rating = newValue

            Glide.with(this)
                .load(imageCover)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imgCover)

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

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
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
                    if (res.highlights.isNotEmpty()) {
                        modelHighlightResponses.clear()
                        modelHighlightResponses.addAll(res.highlights)
                    }

                    withContext(Dispatchers.Main) {
                        tvEstablishment.text = res.establishment.toString()
                        tvLocalityVerbose.text = res.location.locality
                        tvAverageCost.text =
                            "${res.currency} ${res.averageCostForTwo} / ${res.priceRange} person"
                        tvAddress.text = res.location.address
                        tvAddress.text = res.timings

                        llRoute.setOnClickListener {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("http://maps.google.com/maps?daddr=${res.location.latitude},${res.location.longitude}")
                            )
                            startActivity(intent)
                        }

                        llTelpon.setOnClickListener {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${res.phoneNumbers}"))
                            startActivity(intent)
                        }

                        llWebsite.setOnClickListener {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(res.url))
                            startActivity(intent)
                        }

                        highlightsAdapter?.notifyDataSetChanged()
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    mProgressBar?.dismiss()
                    Toast.makeText(this@DetailRestaurantActivity, "No internet connection!", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
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
        const val DETAIL_RESTO = "detailResto"
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