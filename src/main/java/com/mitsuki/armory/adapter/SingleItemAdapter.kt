package com.mitsuki.armory.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView

abstract class SingleItemAdapter(enable: Boolean) :
    RecyclerView.Adapter<SingleItemAdapter.ViewHolder>() {

    open var isEnable: Boolean = enable
        set(value) {
            if (value != field) {
                if (value && !field) {
                    notifyItemInserted(0)
                } else if (!value && field) {
                    notifyItemRemoved(0)
                }
                field = value
            }
        }

    abstract val layoutRes: Int
    open val onViewHolderCreate: ViewHolder.() -> Unit = {}
    open val onViewHolderBind: ViewHolder.() -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(parent, layoutRes).apply(onViewHolderCreate)
    }

    override fun getItemCount(): Int = if (isEnable) 1 else 0

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.apply(onViewHolderBind)
    }

    open class ViewHolder(parent: ViewGroup, @LayoutRes layoutRes: Int) :
        RecyclerView.ViewHolder(
            LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
        )
}