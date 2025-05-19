package com.example.stockdash.ui.explore.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.stockdash.R
import com.example.stockdash.data.model.ExploreStockInfo
import com.example.stockdash.util.resolveThemeColor
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerFrameLayout
import java.util.Locale

class ExploreStockViewAdapter(
    private val onItemClicked: (ExploreStockInfo) -> Unit // Lambda for click events
) : ListAdapter<ExploreStockInfo?, ExploreStockViewAdapter.StockViewHolder>(StockDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.explore_stock_item_layout, parent, false)
        return StockViewHolder(view)
    }

    override fun onBindViewHolder(holder: StockViewHolder, position: Int) {
        val stock = getItem(position) // Get item from ListAdapter
        holder.bind(stock, onItemClicked)
    }

    fun showUnloadedItems(count: Int) {
        submitList(List(count) { null })
    }

    class StockViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val itemImage: ImageView = itemView.findViewById(R.id.imageViewStock)
        private val stockNameTextView: TextView = itemView.findViewById(R.id.stockName)
        private val stockPriceTextView: TextView = itemView.findViewById(R.id.stockPrice)
        private val stockPriceDifferenceTextView: TextView = itemView.findViewById(R.id.sockPriceDifference)
        private val exploreItemShimmer: ShimmerFrameLayout = itemView.findViewById(R.id.exploreItemShimmer)

        fun bind(stock: ExploreStockInfo?, onItemClicked: (ExploreStockInfo) -> Unit) {
            if (stock == null){
                exploreItemShimmer.startShimmer()
                return
            }
            exploreItemShimmer.hideShimmer()

            stockNameTextView.text = stock.ticker
            stockPriceTextView.text = itemView.context.getString(R.string.price_before_dollar, stock.price.toString())

            // Formatting color for change percentage
            val colorAtr = if (stock.priceChangePercentage != null && stock.priceChangePercentage < 0)
                R.attr.stockPriceDownColor
            else
                R.attr.stockPriceUpColor

            stockPriceDifferenceTextView.setTextColor(itemView.context.resolveThemeColor(colorAtr))

            val changeAmountInString = String.Companion.format(Locale.getDefault(), "%+f", stock.priceChange).trimEnd('0').trimEnd('.')
            stockPriceDifferenceTextView.text = String.Companion.format(Locale.getDefault(), "%s (%+.2f%%)", changeAmountInString , stock.priceChangePercentage)

            val logoUrl = itemView.context.getString(R.string.logo_url, stock.ticker)

            itemImage.load(logoUrl) {
                 crossfade(true) // For a smooth transition
            }
            itemView.setOnClickListener {
                onItemClicked(stock)
            }
        }
    }



    // DiffUtil helps RecyclerView efficiently update items
    class StockDiffCallback : DiffUtil.ItemCallback<ExploreStockInfo>() {
        override fun areItemsTheSame(oldItem: ExploreStockInfo, newItem: ExploreStockInfo): Boolean {
            return oldItem.ticker == newItem.ticker // Assuming ticker is a unique identifier
        }

        override fun areContentsTheSame(oldItem: ExploreStockInfo, newItem: ExploreStockInfo): Boolean {
            return oldItem == newItem // Relies on StockInfo being a data class
        }
    }
}