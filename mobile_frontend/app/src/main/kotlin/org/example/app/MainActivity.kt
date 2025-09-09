package org.example.app

import android.Manifest
import android.content.res.Configuration
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import android.widget.Toast
import android.view.View
import android.widget.TextView

import org.example.app.ui.ChatFragment
import org.example.app.ui.HomeFragment
import org.example.app.ui.ScanFragment
import org.example.app.ui.ShareFragment
import org.example.app.ui.SettingsFragment
import org.example.app.ui.HistoryFragment
import org.example.app.utils.PermissionHelper

/**
 * MainActivity hosts the primary UI: AppBar with a Drawer (left) and a BottomNavigation bar (bottom).
 * It switches between fragments for Home (AI suggestions), Share, Scan, and Chat using the bottom bar,
 * and opens History and Settings from the navigation drawer.
 */
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var navView: NavigationView
    private lateinit var drawerToggle: ActionBarDrawerToggle

    // PUBLIC_INTERFACE
    override fun onCreate(savedInstanceState: Bundle?) {
        /**
         * Initialize the activity, inflate the main layout, set up the toolbar,
         * drawer, bottom navigation, and request runtime permissions on first launch.
         *
         * Parameters:
         * - savedInstanceState: The saved instance state.
         *
         * Returns: None
         */
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.app_name)

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        navView.setNavigationItemSelectedListener(this)

        drawerToggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.drawer_open, R.string.drawer_close
        )
        drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        bottomNav = findViewById(R.id.bottom_nav)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    switchTo(HomeFragment())
                    true
                }
                R.id.nav_share -> {
                    switchTo(ShareFragment())
                    true
                }
                R.id.nav_scan -> {
                    switchTo(ScanFragment())
                    true
                }
                R.id.nav_chat -> {
                    switchTo(ChatFragment())
                    true
                }
                else -> false
            }
        }

        if (savedInstanceState == null) {
            bottomNav.selectedItemId = R.id.nav_home
            switchTo(HomeFragment())
        }

        // Request runtime permissions proactively
        if (!PermissionHelper.hasAllPermissions(this)) {
            PermissionHelper.requestAllPermissions(this, REQUEST_PERMISSIONS)
        }
    }

    // PUBLIC_INTERFACE
    override fun onPostCreate(savedInstanceState: Bundle?) {
        /**
         * Sync drawer toggle state after onRestoreInstanceState has occurred.
         *
         * Parameters:
         * - savedInstanceState: The saved instance state.
         *
         * Returns: None
         */
        super.onPostCreate(savedInstanceState)
        drawerToggle.syncState()
    }

    // PUBLIC_INTERFACE
    override fun onConfigurationChanged(newConfig: Configuration) {
        /**
         * Propagate configuration changes to the drawer toggle.
         *
         * Parameters:
         * - newConfig: The new configuration.
         *
         * Returns: None
         */
        super.onConfigurationChanged(newConfig)
        drawerToggle.onConfigurationChanged(newConfig)
    }

    // PUBLIC_INTERFACE
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        /**
         * Handle side-drawer navigation item clicks to show History or Settings.
         *
         * Parameters:
         * - item: The selected menu item.
         *
         * Returns: true if handled, false otherwise.
         */
        when (item.itemId) {
            R.id.drawer_history -> switchTo(HistoryFragment())
            R.id.drawer_settings -> switchTo(SettingsFragment())
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    // PUBLIC_INTERFACE
    override fun onBackPressed() {
        /**
         * Handle back press by closing the drawer if it's open, otherwise defer to default.
         *
         * Returns: None
         */
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    // PUBLIC_INTERFACE
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        /**
         * Handle the results for the runtime permission request, providing feedback to the user.
         *
         * Parameters:
         * - requestCode: The request code originally supplied to requestPermissions.
         * - permissions: The requested permissions.
         * - grantResults: The corresponding grant results.
         *
         * Returns: None
         */
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS) {
            if (PermissionHelper.hasAllPermissions(this)) {
                Toast.makeText(this, getString(R.string.permissions_granted), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, getString(R.string.permissions_denied), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun switchTo(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment, fragment::class.java.simpleName)
            .commitAllowingStateLoss()
    }

    companion object {
        private const val REQUEST_PERMISSIONS = 2001
    }
}
