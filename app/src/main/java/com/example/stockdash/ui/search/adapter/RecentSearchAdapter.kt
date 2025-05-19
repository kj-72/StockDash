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
import com.example.stockdash.data.model.RecentSearch
import com.facebook.shimmer.ShimmerFrameLayout

class RecentSearchAdapter(
    private val onItemClicked: (RecentSearch) -> Unit,
    private val onItemRemoved: (RecentSearch) -> Unit
) : ListAdapter<RecentSearch, RecentSearchAdapter.RecentSearchViewHolder>(RecentSearchStockInfoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentSearchViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.search_item, parent, false) // Layout for search results
        return RecentSearchViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecentSearchViewHolder, position: Int) {
        val stockInfo = getItem(position)
        holder.bind(stockInfo, onItemClicked, onItemRemoved)
    }

    class RecentSearchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val stockLogoImageView: ImageView = itemView.findViewById(R.id.imageViewSearchStock)
        private val stockTickerTextView: TextView = itemView.findViewById(R.id.stockNameSearchStock)
        private val stockNameTextView: TextView = itemView.findViewById(R.id.companyNameSearchStock)
        private val stockTypeTextView: TextView = itemView.findViewById(R.id.typeSearchStock)
        private val removeButton: ImageView = itemView.findViewById(R.id.removeRecentSearch)
        private val shimmerLayout: ShimmerFrameLayout = itemView.findViewById(R.id.searchItemShimmer)

        init {
            shimmerLayout.hideShimmer()
        }

        fun bind(stock: RecentSearch, onItemClicked: (RecentSearch) -> Unit, onItemRemoved: (RecentSearch) -> Unit) {
            stockTickerTextView.text = stock.symbol
            stockNameTextView.text = stock.name
            stockTypeTextView.text = stock.type

            val logoUrl = itemView.context.getString(R.string.logo_url, stock.symbol)
            stockLogoImageView.load(logoUrl) {
                crossfade(true)
            }
            itemView.setOnClickListener {
                onItemClicked(stock)
            }
            removeButton.visibility = View.VISIBLE
            removeButton.setOnClickListener {
                onItemRemoved(stock)
            }
        }
    }

    class RecentSearchStockInfoDiffCallback : DiffUtil.ItemCallback<RecentSearch>() {
        override fun areItemsTheSame(oldItem: RecentSearch, newItem: RecentSearch): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: RecentSearch, newItem: RecentSearch): Boolean {
            return oldItem.symbol == newItem.symbol
        }
    }
}