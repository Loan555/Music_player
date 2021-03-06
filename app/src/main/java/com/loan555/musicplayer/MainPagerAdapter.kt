package com.loan555.musicplayer

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.loan555.musicplayer.model.PlayList
import com.loan555.musicplayer.ui.dashboard.DashboardFragment
import com.loan555.musicplayer.ui.home.HomeFragment
import com.loan555.musicplayer.ui.notifications.NotificationsFragment
import com.loan555.musicplayer.ui.orther.LikeFragment
import com.loan555.musicplayer.ui.search.DiscoverFragment

class MainPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
    override fun getCount(): Int = NUM_PAGES

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> HomeFragment()
            1 -> DashboardFragment()
            3 -> NotificationsFragment()
            2 -> DiscoverFragment()
            else -> LikeFragment()
        }
    }
}
