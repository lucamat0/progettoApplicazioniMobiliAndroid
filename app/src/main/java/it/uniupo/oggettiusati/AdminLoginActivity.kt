package it.uniupo.oggettiusati


import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import it.uniupo.oggettiusati.adapter.AdminViewPagerAdapter
import kotlinx.coroutines.tasks.await


val pageTitlesArray = arrayOf("Home", "Chat", "Personal")

/**
 * Array che memorizza le icone che verranno mostrate nel menu principale
 *
 * @author Busto Matteo
 */
private val tabIcons :IntArray= intArrayOf(
    R.drawable.baseline_home_50,
    R.drawable.baseline_chat_bubble_50,
    R.drawable.baseline_person_50
)

/**
 * Activity che viene utilizzata nel momento in cui il login va a buon fine e l'utente è un Amministratore
 *
 * @author Amato Luca
 * @author Busto Matteo
 */
class AdminLoginActivity : UserLoginActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_logged)

        supportActionBar?.title = "[${supportActionBar?.title}] - admin"

        val viewPager = findViewById<ViewPager2>(R.id.viewPager2_admin)
        viewPager.adapter = AdminViewPagerAdapter(supportFragmentManager, lifecycle, tabIcons.size)

        val tabLayout = findViewById<TabLayout>(R.id.tabLayout_admin)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = pageTitlesArray[position]
            tab.icon = ContextCompat.getDrawable(this, tabIcons[position])
        }.attach()
    }

    companion object {

        /**
         * Controlla se l'utente specificato è un amministratore
         *
         * @author Amato Luca
         * @param userId Identificativo dell'utente
         * @return true se l'utente e' un amministratore altrimenti false
         */
        suspend fun isAmministratore(userId: String): Boolean {
            return Firebase.firestore.collection("utente").document(userId).get().await().getBoolean("amministratore") as Boolean
        }
    }
}

