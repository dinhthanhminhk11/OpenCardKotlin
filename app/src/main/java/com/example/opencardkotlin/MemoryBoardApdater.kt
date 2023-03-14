package com.example.opencardkotlin

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.opencardkotlin.models.BoardSize
import com.example.opencardkotlin.models.MemoryCard
import kotlin.math.min

class MemoryBoardApdater(
    private val context: Context,
    private val boardSize: BoardSize,
    private val cardList: List<MemoryCard>,
    private val callBack: CardClickListener
) : RecyclerView.Adapter<MemoryBoardApdater.ViewHolder>() {

    companion object {
        private const val MARGIN_SIZE = 10;
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val cardWidth: Int = parent.width / boardSize.getWith() - (2 * MARGIN_SIZE)
        val cardHeight: Int = parent.height / boardSize.getHeight() - (2 * MARGIN_SIZE)
        val cardSideLength: Int = min(cardWidth, cardHeight)
        val view: View = LayoutInflater.from(context).inflate(R.layout.item_memory, parent, false)
        val layoutParams: ViewGroup.MarginLayoutParams =
            view.findViewById<CardView>(R.id.cardView).layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.width = cardSideLength
        layoutParams.height = cardSideLength

        layoutParams.setMargins(MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = boardSize.numCard;

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val memoryCard: MemoryCard = cardList[position]

        holder.image.alpha = if (memoryCard.isMatched) .4f else 1.0f
        val colorStateList = if (memoryCard.isMatched) ContextCompat.getColorStateList(
            context, R.color.gray
        ) else null

        ViewCompat.setBackgroundTintList(holder.image, colorStateList)

        holder.image.setImageResource(if (memoryCard.isFaceUp) memoryCard.identifier else R.drawable.ic_launcher_background) // check trạng thái đổi ảnh
        holder.image.setOnClickListener {
            callBack.onCardClicked(position)
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: CardView by lazy { itemView.findViewById<CardView>(R.id.cardView) }
        val image: ImageView by lazy { itemView.findViewById<ImageView>(R.id.image) }
    }

    interface CardClickListener {
        fun onCardClicked(position: Int)
    }

}
