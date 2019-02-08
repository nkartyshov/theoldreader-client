package ru.oldowl.ui.adapter

interface UpdatableAdapter<T> {
    fun update(items: List<T>)
}