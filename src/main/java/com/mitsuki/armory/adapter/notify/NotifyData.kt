package com.mitsuki.armory.adapter.notify

import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import java.util.*

sealed class NotifyData<T> {
    @WorkerThread
    open fun calculateDiff(source: MutableList<T>, diffCallback: DiffUtil.ItemCallback<T>) {
    }

    @MainThread
    abstract fun dispatchUpdates(source: MutableList<T>, adapter: RecyclerView.Adapter<*>)

    abstract fun directUpdate(source: MutableList<T>)

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

        override fun directUpdate(source: MutableList<T>) {
            if (index < 0) source.add(data) else source.add(index, data)
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

        override fun directUpdate(source: MutableList<T>) {
            if (index < 0) source.addAll(data) else source.addAll(index, data)
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

        override fun directUpdate(source: MutableList<T>) {
            if (count == 1) source.removeAt(index)
            else if (count > 1) source.subList(index, index + count - 1).clear()
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

        override fun directUpdate(source: MutableList<T>) {
            val position = source.indexOf(data)
            if (position >= 0) {
                source.removeAt(position)
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

        override fun directUpdate(source: MutableList<T>) {
            source.removeAll(data)
        }
    }

    class Clear<T> : NotifyData<T>() {
        @MainThread
        override fun dispatchUpdates(source: MutableList<T>, adapter: RecyclerView.Adapter<*>) {
            val count = source.size
            source.clear()
            adapter.notifyItemRangeRemoved(0, count)
        }

        override fun directUpdate(source: MutableList<T>) {
            source.clear()
        }
    }

    class Change<T>(private val index: Int, private val data: T, private val payload: Any? = null) :
        NotifyData<T>() {
        @WorkerThread
        override fun dispatchUpdates(source: MutableList<T>, adapter: RecyclerView.Adapter<*>) {
            source.removeAt(index)
            source.add(index, data)
            adapter.notifyItemChanged(index, payload)
        }

        override fun directUpdate(source: MutableList<T>) {
            source.removeAt(index)
            source.add(index, data)
        }
    }

    class ChangeAt<T>(
        private val index: Int,
        private val action: (T) -> T,
        private val payload: Any? = null
    ) : NotifyData<T>() {
        @WorkerThread
        override fun dispatchUpdates(source: MutableList<T>, adapter: RecyclerView.Adapter<*>) {
            val temp = source[index]
            source[index] = action(temp)
            adapter.notifyItemChanged(index, payload)
        }

        override fun directUpdate(source: MutableList<T>) {
            val temp = source[index]
            source[index] = action(temp)
        }
    }

    class ChangeIf<T>(private val filter: (T) -> Boolean, private val action: (T) -> T) :
        NotifyData<T>() {
        private lateinit var diffResult: DiffUtil.DiffResult
        private lateinit var newData: List<T>

        @WorkerThread
        override fun calculateDiff(source: MutableList<T>, diffCallback: DiffUtil.ItemCallback<T>) {
            newData = ArrayList(source).map { if (filter(it)) action(it) else it }
            diffResult = calculateDiff(diffCallback, source, newData)
        }

        @MainThread
        override fun dispatchUpdates(source: MutableList<T>, adapter: RecyclerView.Adapter<*>) {
            if (!this::diffResult.isInitialized) throw IllegalStateException("[ChangeIf $this]:You must be call calculateDiff before dispatchUpdates")
            source.clear()
            source.addAll(newData)
            diffResult.dispatchUpdatesTo(adapter)
        }

        override fun directUpdate(source: MutableList<T>) {
            val new = ArrayList(source).map { if (filter(it)) action(it) else it }
            source.clear()
            source.addAll(new)
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

        override fun directUpdate(source: MutableList<T>) {
            source.clear()
            source.addAll(newData)
        }
    }

    class Move<T>(private val fromPosition: Int, private val toPosition: Int) : NotifyData<T>() {
        @MainThread
        override fun dispatchUpdates(source: MutableList<T>, adapter: RecyclerView.Adapter<*>) {
            if (fromPosition < toPosition) {
                for (i in fromPosition until toPosition) {
                    Collections.swap(source, i, i + 1)
                }
            } else {
                for (i in fromPosition downTo toPosition + 1) {
                    Collections.swap(source, i, i - 1)
                }
            }
            adapter.notifyItemMoved(fromPosition, toPosition)
        }

        override fun directUpdate(source: MutableList<T>) {
            if (fromPosition < toPosition) {
                for (i in fromPosition until toPosition) {
                    Collections.swap(source, i, i + 1)
                }
            } else {
                for (i in fromPosition downTo toPosition + 1) {
                    Collections.swap(source, i, i - 1)
                }
            }
        }
    }
}