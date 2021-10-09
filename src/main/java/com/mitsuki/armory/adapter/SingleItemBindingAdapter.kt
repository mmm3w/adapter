package com.mitsuki.armory.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

open class SingleItemBindingAdapter<VB : ViewBinding>(
    private val layoutRes: Int,
    private val bind: (View) -> VB,
    enable: Boolean = true
) :
    RecyclerView.Adapter<SingleItemBindingAdapter.ViewHolder<VB>>() {

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

    open val onViewHolderCreate: ViewHolder<VB>.() -> Unit = {}
    open val onViewHolderBind: ViewHolder<VB>.() -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<VB> {
        return ViewHolder(parent, layoutRes, bind).apply(onViewHolderCreate)
    }

    override fun getItemCount(): Int = if (isEnable) 1 else 0

    override fun onBindViewHolder(holder: ViewHolder<VB>, position: Int) {
        holder.apply(onViewHolderBind)
    }

    class ViewHolder<VB : ViewBinding>(
        parent: ViewGroup,
        @LayoutRes layoutRes: Int,
        bind: (View) -> VB
    ) :
        RecyclerView.ViewHolder(
            LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
        ) {
        val binding by lazy { bind(itemView) }
    }
}