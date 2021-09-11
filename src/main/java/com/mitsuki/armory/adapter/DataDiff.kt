package com.mitsuki.armory.adapter

import androidx.recyclerview.widget.DiffUtil

class DataDiff<T>(
    private val diff: DiffUtil.ItemCallback<T>,
    private val oldList: List<T>,
    private val newList: List<T>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldList[oldItemPosition] ?: return newList[newItemPosition] == null
        val newItem = newList[newItemPosition] ?: return false
        return diff.areItemsTheSame(oldItem, newItem)
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldList[oldItemPosition] ?: return newList[newItemPosition] == null
        val newItem = newList[newItemPosition] ?: return false
        return diff.areContentsTheSame(oldItem, newItem)

    }
}

fun <T> calculateDiff(
    diff: DiffUtil.ItemCallback<T>,
    oldList: List<T>,
    newList: List<T>
): DiffUtil.DiffResult {
    return DiffUtil.calculateDiff(DataDiff(diff, oldList, newList))
}

fun <T> calculateDiff(
    diff: DiffUtil.ItemCallback<T>,
    oldList: Array<T>,
    newList: Array<T>
): DiffUtil.DiffResult {
    return calculateDiff(diff, oldList.asList(), newList.asList())
}
