package com.diva.restofinder.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.diva.restofinder.R
import com.diva.restofinder.model.RestaurantResponseDto
import com.diva.restofinder.utils.OnMainAdapterCallback
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.diva.restofinder.model.RestaurantDataDto
import kotlinx.android.synthetic.main.list_item_main.view.*

class MainAdapter(
    private val mContext: Context, private val items: List<Any>
) : RecyclerView.Adapter<MainAdapter.ViewHolder>() {


    private var onMainAdapterCallback: OnMainAdapterCallback? = null

    fun setOnItemClickCallback(onMainAdapterCallback: OnMainAdapterCallback?) {
        this.onMainAdapterCallback = onMainAdapterCallback
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.list_item_main, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = items[position]

        when(data) {
            is RestaurantResponseDto -> {
                Glide.with(mContext)
                    .asBitmap()
                    .load(data.restaurant.thumbRestaurant)
                    .transform(CenterCrop(), RoundedCorners(25))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(object : CustomTarget<Bitmap>() {
                        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                            holder.imgResto.setImageBitmap(resource)
                            data.restaurant = data.restaurant.copy(thumbRestaurantBitmap = resource)
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                            // TODO("Not yet implemented")
                        }
                    })

                if (data.restaurant.isFavorite) {
                    holder.tvFavorites.text = "Remove From Favorites"
                    holder.imgFavorite.setImageResource(R.drawable.ic_favorite_checked)
                } else {
                    holder.tvFavorites.text = "Add to Favorites"
                    holder.imgFavorite.setImageResource(R.drawable.ic_favorite_uncheck)
                }

                holder.ratingResto.numStars = 5
                holder.ratingResto.stepSize = 0.5.toFloat()
                holder.ratingResto.rating = data.restaurant.rating.ratingValue?.toFloat() ?: 0f

                holder.tvNameResto.text = data.restaurant.name
                holder.tvAddress.text = data.restaurant.restaurantLocation.address
                holder.tvRating.text =
                    " | ${holder.ratingResto.rating} ${data.restaurant.rating.ratingText}"
                holder.cvListMain.setOnClickListener {
                    onMainAdapterCallback?.onItemMainClicked(data)
                }

                holder.btnAddToFavorites.setOnClickListener {
                    data.restaurant = data.restaurant.copy(isFavorite = !data.restaurant.isFavorite)

                    if (data.restaurant.isFavorite) {
                        holder.tvFavorites.text = "Remove from Favorites"
                        holder.imgFavorite.setImageResource(R.drawable.ic_favorite_checked)
                    } else {
                        holder.tvFavorites.text = "Add to Favorites"
                        holder.imgFavorite.setImageResource(R.drawable.ic_favorite_uncheck)
                    }
                    onMainAdapterCallback?.onFavoriteClicked(data, data.restaurant.isFavorite)
                }
            }
            is RestaurantDataDto -> {
                Glide.with(mContext)
                    .asBitmap()
                    .load(data.thumbRestaurant)
                    .transform(CenterCrop(), RoundedCorners(25))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(object : CustomTarget<Bitmap>() {
                        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                            holder.imgResto.setImageBitmap(resource)
                            data.thumbRestaurantBitmap = resource
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                            // TODO("Not yet implemented")
                        }
                    })

                if (data.isFavorite) {
                    holder.tvFavorites.text = "Remove from Favorites"
                    holder.imgFavorite.setImageResource(R.drawable.ic_favorite_checked)
                } else {
                    holder.tvFavorites.text = "Add to Favorites"
                    holder.imgFavorite.setImageResource(R.drawable.ic_favorite_uncheck)
                }

                holder.ratingResto.numStars = 5
                holder.ratingResto.stepSize = 0.5.toFloat()
                holder.ratingResto.rating = data.rating.ratingValue?.toFloat() ?: 0f

                holder.tvNameResto.text = data.name
                holder.tvAddress.text = data.restaurantLocation.address
                holder.tvRating.text =
                    " | ${holder.ratingResto.rating} ${data.rating.ratingText}"
                holder.cvListMain.setOnClickListener {
                    onMainAdapterCallback?.onItemMainClicked(data as RestaurantDataDto)
                }

                holder.btnAddToFavorites.setOnClickListener {
                    data.isFavorite = !data.isFavorite

                    if (data.isFavorite) {
                        holder.tvFavorites.text = "Remove from Favorites"
                        holder.imgFavorite.setImageResource(R.drawable.ic_favorite_checked)
                    } else {
                        holder.tvFavorites.text = "Add to Favorites"
                        holder.imgFavorite.setImageResource(R.drawable.ic_favorite_uncheck)
                    }
                    onMainAdapterCallback?.onFavoriteClicked(data, data.isFavorite)
                }
            }
        }


    }

    override fun getItemCount(): Int {
        return items.size
    }

    //Class Holder
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var cvListMain: CardView
        var imgResto: ImageView
        var imgFavorite: ImageView
        var btnAddToFavorites: LinearLayout
        var tvFavorites: TextView
        var tvNameResto: TextView
        var tvAddress: TextView
        var tvRating: TextView
        var ratingResto: RatingBar

        init {
            cvListMain = itemView.cvListMain
            imgResto = itemView.imgResto
            imgFavorite = itemView.imgFavorite
            btnAddToFavorites = itemView.btnAddToFavorites
            tvFavorites = itemView.tvFavorites
            tvNameResto = itemView.tvNameResto
            tvRating = itemView.tvRating
            tvAddress = itemView.tvAddress
            ratingResto = itemView.ratingResto
        }
    }
}