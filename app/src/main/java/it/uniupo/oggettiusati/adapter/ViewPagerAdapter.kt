package it.uniupo.oggettiusati.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import it.uniupo.oggettiusati.fragment.CartFragment
import it.uniupo.oggettiusati.fragment.ChatFragment
import it.uniupo.oggettiusati.fragment.FavoritesFragment
import it.uniupo.oggettiusati.fragment.HomeFragment
import it.uniupo.oggettiusati.fragment.OwnerObjecsFragment

class ViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle, private var numItem: Int) :
    FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int {
        return numItem
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> HomeFragment()
            1 -> CartFragment()
            2 -> ChatFragment()
            3 -> FavoritesFragment()
            else -> OwnerObjecsFragment()
        }
    }
}
