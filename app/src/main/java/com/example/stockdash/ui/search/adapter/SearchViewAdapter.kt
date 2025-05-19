package com.example.stockdash.ui.search.adapter

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
import com.example.stockdash.data.model.SearchStockInfo
import com.facebook.shimmer.ShimmerFrameLayout

class SearchViewAdapter(
    private val onItemClicked: (SearchStockInfo) -> Unit // Lambda to handle item clicks
) : ListAdapter<SearchStockInfo?, SearchViewAdapter.SearchViewHolder>(SearchStockInfoDiffCallback()) { // ViewHolder and DiffCallback also renamed for consistency

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.search_item, parent, false) // Layout for search results
        return SearchViewHolder(view)
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        val stockInfo = getItem(position)
        holder.bind(stockInfo, onItemClicked)
    }

    fun showLoading() {
        submitList(List(8) { null })
    }

    class SearchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val stockLogoImageView: ImageView = itemView.findViewById(R.id.imageViewSearchStock)
        private val stockTickerTextView: TextView = itemView.findViewById(R.id.stockNameSearchStock)
        private val stockNameTextView: TextView = itemView.findViewById(R.id.companyNameSearchStock)
        private val stockTypeTextView: TextView = itemView.findViewById(R.id.typeSearchStock)
        private val shimmerLayout: ShimmerFrameLayout = itemView.findViewById(R.id.searchItemShimmer)

        fun bind(stock: SearchStockInfo?, onItemClicked: (SearchStockInfo) -> Unit) {
            if (stock == null) {
                shimmerLayout.startShimmer()
                return
            }
            shimmerLayout.hideShimmer()

            stockTickerTextView.text = stock.symbol
            stockNameTextView.text = stock.name ?: "N/A"
            stockTypeTextView.text = stock.type

            val logoUrl = itemView.context.getString(R.string.logo_url, stock.symbol)
            stockLogoImageView.load(logoUrl) {
                crossfade(true)
            }

            itemView.setOnClickListener {
                onItemClicked(stock)
            }
        }
    }

    // Renamed DiffCallback for consistency with adapter name
    class SearchStockInfoDiffCallback : DiffUtil.ItemCallback<SearchStockInfo>() {
        override fun areItemsTheSame(oldItem: SearchStockInfo, newItem: SearchStockInfo): Boolean {
            return oldItem.symbol == newItem.symbol
        }

        override fun areContentsTheSame(oldItem: SearchStockInfo, newItem: SearchStockInfo): Boolean {
            return oldItem == newItem
        }
    }
}