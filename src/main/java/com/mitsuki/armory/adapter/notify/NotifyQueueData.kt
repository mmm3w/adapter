package com.mitsuki.armory.adapter.notify

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import androidx.annotation.MainThread
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import java.lang.ref.WeakReference
import java.util.ArrayDeque

class NotifyQueueData<T>(private val diffCallback: DiffUtil.ItemCallback<T>) {

    private val mData = arrayListOf<T>()
    val count get() = mData.size
    fun item(index: Int) = mData[index]

    private val mDelivery: Handler by lazy { Handler(Looper.getMainLooper()) }
    private val mWorkThread: HandlerThread by lazy { HandlerThread("NotifyQueueData").apply { start() } }
    private val mHanlder = Handler(mWorkThread.looper)

    private val pendingUpdates: ArrayDeque<NotifyData<T>> = ArrayDeque()
    private var targetAdapter: WeakReference<RecyclerView.Adapter<*>>? = null

    @MainThread
    fun postUpdate(data: NotifyData<T>) {
        pendingUpdates.add(data)
        if (pendingUpdates.size > 1) return
        updateData(data)
    }

    @MainThread
    private fun updateData(data: NotifyData<T>) {
        when (data) {
            is NotifyData.Insert,
            is NotifyData.RangeInsert,
            is NotifyData.RemoveAt,
            is NotifyData.Remove,
            is NotifyData.Change,
            is NotifyData.ChangeAt -> applyNotify(data)
            is NotifyData.RangeRemove,
            is NotifyData.ChangeIf,
            is NotifyData.Refresh -> {
                mHanlder.post {
                    data.calculateDiff(mData, diffCallback)
                    mDelivery.post { applyNotify(data) }
                }
            }
        }
    }

    @MainThread
    private fun applyNotify(notifyData: NotifyData<T>) {
        pendingUpdates.remove()
        targetAdapter?.get()?.apply { notifyData.dispatchUpdates(mData, this) }
        if (pendingUpdates.isNotEmpty()) {
            pendingUpdates.peek()?.apply { updateData(this) }
        }
    }

    fun attachAdapter(adapter: RecyclerView.Adapter<*>) {
        targetAdapter = WeakReference(adapter)
    }
}