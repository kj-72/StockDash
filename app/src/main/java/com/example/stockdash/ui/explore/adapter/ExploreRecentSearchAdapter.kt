package com.example.stockdash.ui.explore.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.stockdash.R
import com.example.stockdash.data.model.RecentSearch

class ExploreRecentSearchAdapter (
    private val onItemClicked: (RecentSearch) -> Unit
) : ListAdapter<RecentSearch, ExploreRecentSearchAdapter.RecentSearchExploreViewHolder>(RecentSearchStockExploreInfoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentSearchExploreViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recent_search_item_layout, parent, false) // Layout for search results
        return RecentSearchExploreViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecentSearchExploreViewHolder, position: Int) {
        val stockInfo = getItem(position)
        holder.bind(stockInfo, onItemClicked)

        currentList.lastOrNull()?.let { lastItem -> // No divider for last item
            if (position == currentList.indexOf(lastItem)) {
                holder.itemView.findViewById<View>(R.id.divider).visibility = View.GONE
            }
        }
    }


    class RecentSearchExploreViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val stockTickerTextView: TextView = itemView.findViewById(R.id.recentSearchText)

        fun bind(stock: RecentSearch, onItemClicked: (RecentSearch) -> Unit) {
            stockTickerTextView.text = stock.symbol

            itemView.setOnClickListener {
                onItemClicked(stock)
            }
        }
    }

    class RecentSearchStockExploreInfoDiffCallback : DiffUtil.ItemCallback<RecentSearch>() {
        override fun areItemsTheSame(oldItem: RecentSearch, newItem: RecentSearch): Boolean {
            return oldItem == newItem
        }
        override fun areContentsTheSame(oldItem: RecentSearch, newItem: RecentSearch): Boolean {
            return oldItem.symbol == newItem.symbol
        }
    }
}