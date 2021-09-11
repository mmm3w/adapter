package com.mitsuki.armory.adapter.empty

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

abstract class EmptyAdapter<VH : RecyclerView.ViewHolder>(isEmpty: Boolean = false) : RecyclerView.Adapter<VH>() {
    var emptyState: EmptyState = EmptyState.Normal(isEmpty)
        set(emptyState) {
            if (field != emptyState) {
                if (field.isEmpty && !emptyState.isEmpty) {
                    notifyItemRemoved(0)
                } else if (!field.isEmpty && emptyState.isEmpty) {
                    notifyItemInserted(0)
                } else if (field.isEmpty && emptyState.isEmpty) {
                    notifyItemChanged(0)
                }
                field = emptyState
            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return onCreateViewHolder(parent, emptyState)
    }

    override fun getItemCount(): Int {
        return if (emptyState.isEmpty) 1 else 0
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        onBindViewHolder(holder, emptyState)
    }

    abstract fun onCreateViewHolder(parent: ViewGroup, emptyState: EmptyState): VH
    abstract fun onBindViewHolder(holder: VH, emptyState: EmptyState)
}