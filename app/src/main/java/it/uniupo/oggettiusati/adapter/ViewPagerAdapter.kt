package it.uniupo.oggettiusati.adapter

//autogenerato, potrebbe essere utile il tipo (classe) da cui eredita
//class ViewPagerAdapter(supportFragmentManager: FragmentManager, lifecycle: Lifecycle) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
//
//}

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
            0 -> HomeFragment(false)
            1 -> CartFragment(false)
            2 -> ChatFragment(false)
            3 -> FavoritesFragment(false)
            else -> OwnerObjecsFragment(false)
        }
    }
}
