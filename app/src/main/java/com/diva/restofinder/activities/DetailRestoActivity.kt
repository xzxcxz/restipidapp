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
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.diva.restofinder.R
import com.diva.restofinder.adapter.HighlightsAdapter
import com.diva.restofinder.adapter.ReviewAdapter
import com.diva.restofinder.model.HighlightResponseDto
import com.diva.restofinder.model.RestaurantResponseDto
import com.diva.restofinder.networking.ApiEndpoint
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.synthetic.main.activity_detail_resto.*
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class   DetailRestoActivity : AppCompatActivity() {

    private var mProgressBar: ProgressDialog? = null
    private var highlightsAdapter: HighlightsAdapter? = null
    private val modelHighlightResponses: MutableList<HighlightResponseDto> = ArrayList()

    var RatingResto = 0.0
    var IdResto: String? = null
    var ImageCover: String? = null
    var Title: String? = null
    var Rating: String? = null
    var RestoName: String? = null
    var restaurantResponseDto: RestaurantResponseDto? = null

    @SuppressLint("Assert", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_resto)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        if (Build.VERSION.SDK_INT >= 21) {
            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false)
            window.statusBarColor = resources.getColor(R.color.colorPrimary)
        }

        mProgressBar = ProgressDialog(this)
        mProgressBar?.setTitle("Please wait")
        mProgressBar?.setCancelable(false)
        mProgressBar?.setMessage("Showing data...")

        toolbar.setTitle("")
        setSupportActionBar(toolbar)
        assert(supportActionBar != null)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        restaurantResponseDto = intent.getSerializableExtra(DETAIL_RESTO) as RestaurantResponseDto
        if (restaurantResponseDto != null) {
            IdResto = restaurantResponseDto?.id
            ImageCover = restaurantResponseDto?.thumbRestaurant
            RatingResto = restaurantResponseDto!!.rating.ratingValue
            Title = restaurantResponseDto?.name
            Rating = restaurantResponseDto?.rating?.ratingText
            RestoName = restaurantResponseDto?.name

            tvTitle.setText(Title)
            tvRestoName.setText(RestoName)
            tvRating.setText("$RatingResto | $Rating")
            tvTitle.setSelected(true)
            val newValue = RatingResto.toFloat()

            ratingResto.setNumStars(5)
            ratingResto.setStepSize(0.5.toFloat())
            ratingResto.setRating(newValue)

            Glide.with(this)
                .load(ImageCover)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imgCover)

            //method get Highlight
            showRecyclerViewList()

            //method get Detail
            getDetailResto()
        }
    }

    private fun showRecyclerViewList() {
        highlightsAdapter = HighlightsAdapter(modelHighlightResponses)

        rvHighlights.setLayoutManager(LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false))
        rvHighlights.setHasFixedSize(true)
        rvHighlights.setAdapter(highlightsAdapter)
    }

    private fun getDetailResto() {
        mProgressBar?.show()
        AndroidNetworking.get(ApiEndpoint.BASEURL + ApiEndpoint.DetailRestaurant + IdResto)
            .addHeaders("user-key", "b47b1abf3c3436d473570116cd8a2621")
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                @SuppressLint("SetTextI18n")
                override fun onResponse(response: JSONObject) {
                    try {
                        mProgressBar?.dismiss()
                        val jsonArrayOne = response.getJSONArray("highlights")

                        for (i in 0 until jsonArrayOne.length()) {
                            val dataApi = HighlightResponseDto(listOf("One", "Two", "Three"))
                            modelHighlightResponses.add(dataApi)
                        }

                        val jsonObjectData = response.getJSONObject("location")
                        val jsonArrayTwo = response.getJSONArray("establishment")

                        for (x in 0 until jsonArrayTwo.length()) {
                            val Establishment = jsonArrayTwo[x].toString()
                            tvEstablishment.text = Establishment
                        }

                        val AverageCost = response.getString("average_cost_for_two")
                        val PriceRange = response.getString("price_range")
                        val Currency = response.getString("currency")
                        val Timings = response.getString("timings")
                        val LocalityVerbose = jsonObjectData.getString("locality_verbose")
                        val Address = jsonObjectData.getString("address")
                        val Telepon = response.getString("phone_numbers")
                        val Website = response.getString("url")
                        val Latitude = jsonObjectData.getDouble("latitude")
                        val Longitude = jsonObjectData.getDouble("longitude")

                        tvLocalityVerbose.text = LocalityVerbose
                        tvAverageCost.text = "$Currency $AverageCost / $PriceRange person"
                        tvAddress.text = Address
                        tvOpenTime.text = Timings

                        llRoute.setOnClickListener {
                            val intent = Intent(Intent.ACTION_VIEW,
                                Uri.parse("http://maps.google.com/maps?daddr=$Latitude,$Longitude"))
                            startActivity(intent)
                        }

                        llTelpon.setOnClickListener {
                            val intent: Intent
                            intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$Telepon"))
                            startActivity(intent)
                        }

                        llWebsite.setOnClickListener {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(Website))
                            startActivity(intent)
                        }

                        highlightsAdapter?.notifyDataSetChanged()
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        Toast.makeText(this@DetailRestoActivity,
                            "Failed to display data!", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onError(anError: ANError) {
                    mProgressBar?.dismiss()
                    Toast.makeText(this@DetailRestoActivity,
                        "No internet connection !", Toast.LENGTH_SHORT).show()
                }
            })
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