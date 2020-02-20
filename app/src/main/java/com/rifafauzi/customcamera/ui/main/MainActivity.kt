package com.rifafauzi.customcamera.ui.main

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.setupActionBarWithNavController
import com.rifafauzi.customcamera.R

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initMain()
        setupToolbar()
        setupNavController()

    }

    private fun initMain() {
        navController = Navigation.findNavController(this,
            R.id.mainContent
        )
        toolbar = findViewById(R.id.mainToolbar)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        setupActionBarWithNavController(navController)
    }

    private fun setupNavController() {
        navController.addOnDestinationChangedListener(navigationListener)
    }

    private fun hideToolbarSubtitle() {
        supportActionBar?.subtitle = null
    }

    private fun showToolbar(shouldShow: Boolean) {
        if (shouldShow) toolbar.visibility = View.VISIBLE else toolbar.visibility = View.GONE
    }

    private fun showToolbarBackArrow(shouldShow: Boolean) {
        supportActionBar?.setDisplayHomeAsUpEnabled(shouldShow)
    }

    private val navigationListener =
        NavController.OnDestinationChangedListener { _, destination, _ ->
            invalidateOptionsMenu()
            hideToolbarSubtitle()
            when (destination.id) {
                R.id.cameraFragment -> {
                    showToolbar(false)
                    showToolbarBackArrow(false)
                }
                R.id.galleryFragment -> {
                    showToolbar(true)
                    showToolbarBackArrow(true)
                }
                else -> {
                    showToolbar(false)
                    showToolbarBackArrow(false)
                }
            }
        }

    override fun onBackPressed() {
        when (navController.currentDestination?.id) {
            R.id.cameraFragment -> {
                finish()
            }
            R.id.galleryFragment -> {
                navController.navigate(R.id.cameraFragment)
            }

            else -> {
                navController.navigateUp()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        navController.navigateUp()
        return true
    }

}