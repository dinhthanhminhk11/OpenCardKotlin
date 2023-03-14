package com.example.opencardkotlin

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.opencardkotlin.models.BoardSize
import java.lang.Integer.min

class ImagePickerAdapter(
    private val context: Context,
    private val imageUriList: List<Uri>,
    private val boardSize: BoardSize,
    private val callBack: ImageClickListener
) : RecyclerView.Adapter<ImagePickerAdapter.ViewHolder>() {

    interface ImageClickListener {
        fun onPlaceholderClick()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): ImagePickerAdapter.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.card_image, parent, false)
        val cardWidth = parent.width / boardSize.getWith()
        val cardHeght = parent.height / boardSize.getHeight()
        val cardSideLength = min(cardWidth, cardHeght)
        val layoutParams = view.findViewById<ImageView>(R.id.image).layoutParams
        layoutParams.width = cardSideLength
        layoutParams.height = cardSideLength
        return ViewHolder(view)
    }

    override fun getItemCount() = boardSize.getNumPairs()

    override fun onBindViewHolder(holder: ImagePickerAdapter.ViewHolder, position: Int) {
        if (position < imageUriList.size) {
            holder.bind(imageUriList[position])
        } else {
            holder.bind()
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val image: ImageView by lazy { itemView.findViewById<ImageView>(R.id.image) }

        fun bind(uri: Uri) {
            image.setImageURI(uri)
            image.setOnClickListener(null)
        }

        fun bind() {
            image.setOnClickListener {
                callBack.onPlaceholderClick()
            }
        }

    }
}
