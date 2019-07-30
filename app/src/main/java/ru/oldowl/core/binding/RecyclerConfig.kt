package ru.oldowl.core.binding

import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import android.view.View

class RecyclerConfig(
        var adapter: RecyclerView.Adapter<*>,
        var layoutManager: RecyclerView.LayoutManager,
        var dividerItemDecoration: DividerItemDecoration? = null,
        var emptyView: View? = null
)