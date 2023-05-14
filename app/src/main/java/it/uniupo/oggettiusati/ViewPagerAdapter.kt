package it.uniupo.oggettiusati

//autogenerato, potrebbe essere utile il tipo (classe) da cui eredita
//class ViewPagerAdapter(supportFragmentManager: FragmentManager, lifecycle: Lifecycle) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
//
//}

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> HomeFragment()
            1 -> CartFragment()
            else -> ChatFragment()
        }
        //return ChatFragment()
    }
}
