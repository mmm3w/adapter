package com.mitsuki.armory.adapter.notify

import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.mitsuki.armory.adapter.calculateDiff

sealed class NotifyData<T> {
    @WorkerThread
    open fun calculateDiff(source: MutableList<T>, diffCallback: DiffUtil.ItemCallback<T>) {
    }

    @MainThread
    abstract fun dispatchUpdates(source: MutableList<T>, adapter: RecyclerView.Adapter<*>)

    class Insert<T>(private val data: T, private val index: Int = -1) : NotifyData<T>() {
        @MainThread
        override fun dispatchUpdates(source: MutableList<T>, adapter: RecyclerView.Adapter<*>) {
            val position: Int
            if (index < 0) {
                position = source.size
                source.add(data)
            } else {
                position = index
                source.add(index, data)
            }
            adapter.notifyItemInserted(position)
        }
    }

    class RangeInsert<T>(private val data: List<T>, private val index: Int = -1) : NotifyData<T>() {
        @MainThread
        override fun dispatchUpdates(source: MutableList<T>, adapter: RecyclerView.Adapter<*>) {
            val position: Int
            if (index < 0) {
                position = source.size
                source.addAll(data)
            } else {
                position = index
                source.addAll(index, data)
            }
            adapter.notifyItemRangeInserted(position, data.size)
        }
    }

    class RemoveAt<T>(private val index: Int, private val count: Int = 1) : NotifyData<T>() {
        @MainThread
        override fun dispatchUpdates(source: MutableList<T>, adapter: RecyclerView.Adapter<*>) {
            if (count == 1) {
                source.removeAt(index)
                adapter.notifyItemRemoved(index)
            } else if (count > 1) {
                source.subList(index, index + count - 1).clear()
                adapter.notifyItemRangeRemoved(index, count)
            }
        }
    }

    class Remove<T>(private val data: T) : NotifyData<T>() {
        @MainThread
        override fun dispatchUpdates(source: MutableList<T>, adapter: RecyclerView.Adapter<*>) {
            val position = source.indexOf(data)
            if (position >= 0) {
                source.removeAt(position)
                adapter.notifyItemRemoved(position)
            }
        }
    }

    class RangeRemove<T>(private val data: List<T>) : NotifyData<T>() {
        private lateinit var diffResult: DiffUtil.DiffResult
        private lateinit var newData: List<T>

        @WorkerThread
        override fun calculateDiff(source: MutableList<T>, diffCallback: DiffUtil.ItemCallback<T>) {
            newData = ArrayList(source).apply { removeAll(data) }
            diffResult = calculateDiff(diffCallback, source, newData)
        }

        @MainThread
        override fun dispatchUpdates(source: MutableList<T>, adapter: RecyclerView.Adapter<*>) {
            if (!this::diffResult.isInitialized) throw IllegalStateException("[RangeRemove $this]:You must be call calculateDiff before dispatchUpdates")
            source.clear()
            source.addAll(newData)
            diffResult.dispatchUpdatesTo(adapter)
        }
    }

    class Change<T>(private val index: Int, private val data: T) : NotifyData<T>() {
        @WorkerThread
        override fun dispatchUpdates(source: MutableList<T>, adapter: RecyclerView.Adapter<*>) {
            source.removeAt(index)
            source.add(index, data)
            adapter.notifyItemChanged(index)
        }
    }

    class ChangeAt<T>(private val index: Int, private val action: T.() -> Unit) : NotifyData<T>() {
        @WorkerThread
        override fun dispatchUpdates(source: MutableList<T>, adapter: RecyclerView.Adapter<*>) {
            source[index]?.apply(action)
            adapter.notifyItemChanged(index)
        }
    }

    class ChangeIf<T>(private val filter: (T) -> Boolean, private val action: T.() -> Unit) :
        NotifyData<T>() {
        private lateinit var diffResult: DiffUtil.DiffResult
        private lateinit var newData: List<T>

        @WorkerThread
        override fun calculateDiff(source: MutableList<T>, diffCallback: DiffUtil.ItemCallback<T>) {
            newData = ArrayList(source).onEach { if (filter(it)) it.apply(action) }
            diffResult = calculateDiff(diffCallback, source, newData)
        }

        @MainThread
        override fun dispatchUpdates(source: MutableList<T>, adapter: RecyclerView.Adapter<*>) {
            if (!this::diffResult.isInitialized) throw IllegalStateException("[ChangeIf $this]:You must be call calculateDiff before dispatchUpdates")
            source.clear()
            source.addAll(newData)
            diffResult.dispatchUpdatesTo(adapter)
        }
    }

    class Refresh<T>(private val newData: List<T>) : NotifyData<T>() {
        private lateinit var diffResult: DiffUtil.DiffResult

        @WorkerThread
        override fun calculateDiff(source: MutableList<T>, diffCallback: DiffUtil.ItemCallback<T>) {
            diffResult = calculateDiff(diffCallback, source, newData)
        }

        @MainThread
        override fun dispatchUpdates(source: MutableList<T>, adapter: RecyclerView.Adapter<*>) {
            if (!this::diffResult.isInitialized) throw IllegalStateException("[Refresh $this]:You must be call calculateDiff before dispatchUpdates")
            source.clear()
            source.addAll(newData)
            diffResult.dispatchUpdatesTo(adapter)
        }
    }
}