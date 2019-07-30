package ru.oldowl.core.ui

import androidx.recyclerview.widget.DiffUtil

class SimpleDiff<T> (
    private val compareItems: (old: T, new: T) -> Boolean,
    private val compareContents: (old: T, new: T) -> Boolean = { old, new -> old == new }
) : DiffUtil.ItemCallback<T>() {

    override fun areItemsTheSame(old: T, new: T): Boolean = compareItems(old, new)

    override fun areContentsTheSame(old: T, new: T): Boolean = compareContents(old, new)
}