package ru.oldowl.core.ui

import androidx.recyclerview.widget.RecyclerView
import android.view.View

class EmptyDataObserver(
        private val recyclerView: androidx.recyclerview.widget.RecyclerView,
        private val emptyView: View
) : androidx.recyclerview.widget.RecyclerView.AdapterDataObserver() {

    override fun onChanged() {
        checkIfEmpty()
    }

    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
        checkIfEmpty()
    }

    override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
        checkIfEmpty()
    }

    private fun checkIfEmpty() {
        val empty = recyclerView.adapter?.itemCount == 0

        emptyView.visibility = if (empty) View.VISIBLE else View.GONE
        recyclerView.visibility = if (empty) View.GONE else View.VISIBLE
    }
}