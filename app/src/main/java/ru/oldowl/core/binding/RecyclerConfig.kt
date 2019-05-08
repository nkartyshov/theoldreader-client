package ru.oldowl.core.binding

import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.RecyclerView

class RecyclerConfig(
        var adapter: RecyclerView.Adapter<*>,
        var layoutManager: RecyclerView.LayoutManager,
        var dividerItemDecoration: DividerItemDecoration? = null
)