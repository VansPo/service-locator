package example.di.ui

import android.graphics.drawable.Drawable
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import example.di.R
import example.di.data.model.Image

class ImageAdapter : RecyclerView.Adapter<ImageAdapter.ViewHolder>() {

    private val items: MutableList<Image> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.list_item,
                parent,
                false
            )
        )
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    fun replaceAll(data: List<Image>) {
        items.clear()
        items += data
        notifyDataSetChanged()
    }

    class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {

        private val imageView: ImageView by lazy { view.findViewById<ImageView>(R.id.image) }

        fun bind(image: Image) {
            Glide.with(view)
                .load(image.url)
////                .addListener(object : RequestListener<Drawable> {
////                    override fun onLoadFailed(
////                        e: GlideException?,
////                        model: Any?,
////                        target: Target<Drawable>?,
////                        isFirstResource: Boolean
////                    ): Boolean {
////                        Log.e("image", "fail", e)
////                        return true
////                    }
////
////                    override fun onResourceReady(
////                        resource: Drawable?,
////                        model: Any?,
////                        target: Target<Drawable>?,
////                        dataSource: DataSource?,
////                        isFirstResource: Boolean
////                    ): Boolean {
////                        return true
////                    }
////
////                })
                .into(imageView)
        }
    }
}
