package com.mitsuki.armory.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2

class UnlimitedFragmentStateAdapter(
    private val mAdapter: FragmentStateAdapter,
    fragmentActivity: FragmentActivity
) :
    FragmentStateAdapter(fragmentActivity) {

    var isEnable = true
        set(value) {
            if (field != value) {
                field = value
                notifyDataSetChanged()
            }
        }

    val realItemCount: Int
        get() = mAdapter.itemCount

    private val infinite = Int.MAX_VALUE

    val startPosition: Int
        get() {
            if (!isEnable) return 0
            var current: Int = infinite / 2 - 1
            val remainder: Int = current % realItemCount
            if (remainder != 0) {
                current = current + realItemCount - remainder
            }
            return current
        }


    override fun getItemCount(): Int {
        return if (isEnable) {
            if (mAdapter.itemCount == 0) 0 else infinite
        } else {
            mAdapter.itemCount
        }
    }

    override fun createFragment(position: Int): Fragment {
        val realPosition = if (isEnable) {
            val remainder = (position - startPosition) % realItemCount
            if (remainder < 0)
                remainder + realItemCount
            else
                remainder
        } else {
            position
        }
        return mAdapter.createFragment(realPosition)
    }

    fun ensureCurrent(viewPager: ViewPager2, offset: Int = 0) {
        viewPager.setCurrentItem(startPosition + offset, false)
    }
}