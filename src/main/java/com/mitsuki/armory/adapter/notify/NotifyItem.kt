package com.mitsuki.armory.adapter.notify

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

sealed class NotifyItem {
    abstract fun dispatch(adapter: RecyclerView.Adapter<*>)

    class NewData(private val count: Int) : NotifyItem() {
        override fun dispatch(adapter: RecyclerView.Adapter<*>) {
            adapter.notifyItemRangeInserted(0, count)
        }
    }

    class LoadData(private val index: Int, private val count: Int) : NotifyItem() {
        override fun dispatch(adapter: RecyclerView.Adapter<*>) {
            adapter.notifyItemRangeInserted(index, count)
        }
    }

    class ClearData(private val count: Int) : NotifyItem() {
        override fun dispatch(adapter: RecyclerView.Adapter<*>) {
            adapter.notifyItemRangeRemoved(0, count)
        }
    }

    class RemoveData(private val index: Int, private val count: Int = 1) : NotifyItem() {
        override fun dispatch(adapter: RecyclerView.Adapter<*>) {
            adapter.notifyItemRangeRemoved(index, count)
        }
    }

    class UpdateData(private val position: Int, private val count: Int = 1) : NotifyItem() {
        override fun dispatch(adapter: RecyclerView.Adapter<*>) {
            adapter.notifyItemRangeChanged(position, count)
        }
    }

    class RefreshData(private val diffResult: DiffUtil.DiffResult) : NotifyItem() {
        override fun dispatch(adapter: RecyclerView.Adapter<*>) {
            diffResult.dispatchUpdatesTo(adapter)
        }
    }
}