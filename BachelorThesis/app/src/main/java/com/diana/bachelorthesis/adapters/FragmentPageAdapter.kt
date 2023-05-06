package com.diana.bachelorthesis.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.diana.bachelorthesis.view.FavoriteDonationsFragment
import com.diana.bachelorthesis.view.FavoriteExchangesFragment

class FragmentPageAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle
) : FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int {
        return 2 // The number of fragments used in the tab layout
    }

    override fun createFragment(position: Int): Fragment {
        return if (position == 0)
            FavoriteExchangesFragment()
        else
            FavoriteDonationsFragment()
    }
}