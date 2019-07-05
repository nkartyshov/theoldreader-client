package ru.oldowl.core.binding

import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.RecyclerView
import android.view.View

class RecyclerConfig(
        var adapter: RecyclerView.Adapter<*>,
        var layoutManager: RecyclerView.LayoutManager,
        var dividerItemDecoration: DividerItemDecoration? = null,
        var emptyView: View? = null
)