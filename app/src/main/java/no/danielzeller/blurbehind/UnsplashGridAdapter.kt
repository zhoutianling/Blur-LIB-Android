package no.danielzeller.blurbehind

import android.animation.ObjectAnimator
import android.graphics.Bitmap
import android.graphics.drawable.Animatable
import android.support.v4.app.FragmentManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Callback
import kotlinx.android.synthetic.main.card2.view.*
import kotlinx.android.synthetic.main.loader_view.view.*
import no.danielzeller.blurbehind.animation.ScaleInImageView
import no.danielzeller.blurbehind.extensions.delay
import no.danielzeller.blurbehind.extensions.onEnd
import no.danielzeller.blurbehind.model.UnsplashItem
import java.lang.Exception
import java.lang.ref.WeakReference


class UnsplashGridAdapter(val items: List<UnsplashItem>, private val supportFragmentManager: FragmentManager, private val viewModel: UnsplashViewModel) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(viewType, parent, false)

        if (viewType == R.layout.card1) {
            return TextOnlyViewHolder(v)
        }
        return CardViewHolder(v)
    }

    override fun getItemCount(): Int {
        return items.count()
    }

    override fun getItemViewType(position: Int): Int {
        return items[position].layoutID
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        var viewHolder = holder as TextOnlyViewHolder

        viewHolder.subHeading.text = item.subHeading
        viewHolder.heading.text = item.heading

        if (holder is CardViewHolder) {
            setupImageView(holder, item)
            setupCardOnClickListener(holder, item)
        } else {
            setupTextOnClickListener(holder, item)
        }
    }

    private fun setupCardOnClickListener(viewHolder: CardViewHolder, item: UnsplashItem) {
        viewHolder.itemView.setOnClickListener {
            if (viewHolder.progressBar.visibility != View.VISIBLE) {
                (item.action as ((itemView: View, item: UnsplashItem) -> Unit)).invoke(viewHolder.itemView, item)
            }
        }
    }

    private fun setupTextOnClickListener(viewHolder: TextOnlyViewHolder, item: UnsplashItem) {
        viewHolder.itemView.setOnClickListener {
            (item.action as (() -> Unit)).invoke()
        }
    }

    private fun setupImageView(viewHolder: CardViewHolder, item: UnsplashItem) {

        (viewHolder.progressBarImage.drawable as Animatable).start()

        val bitmap = viewModel.picassoCache.get(item.imageUrl + "\n")
        if (bitmap == null) {
            loadImage(viewHolder, item)
        } else {
            viewHolder.image.setImageBitmap(bitmap)
        }
    }

    private fun loadImage(viewHolder: CardViewHolder, item: UnsplashItem) {
        val loaderRef = WeakReference<View>(viewHolder.progressBar)
        val imageRef = WeakReference<ScaleInImageView>(viewHolder.image)
        viewHolder.progressBar.visibility = View.VISIBLE
        viewHolder.progressBar.alpha = 1f
        viewModel.picasso.load(item.imageUrl).config(Bitmap.Config.HARDWARE).into(viewHolder.image, object : Callback {
            override fun onSuccess() {
                if (loaderRef.get() != null) {
                    ObjectAnimator.ofFloat(loaderRef.get(), View.ALPHA, 1f, 0f).delay(50).setDuration(450).onEnd { loaderRef.get()?.visibility = View.GONE }.start()
                    imageRef.get()?.introAnimate()
                }
            }

            override fun onError(e: Exception?) {
            }
        })
    }

    open inner class TextOnlyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val heading: TextView
        val subHeading: TextView

        init {
            heading = view.heading
            subHeading = view.subHeading
        }
    }

    inner class CardViewHolder(view: View) : TextOnlyViewHolder(view) {
        val image: ScaleInImageView
        val progressBar: View
        val progressBarImage: ImageView

        init {
            image = view.image
            progressBar = view.findViewById(R.id.progressView)
            progressBarImage = view.loader
        }
    }
}