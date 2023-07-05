package com.example.scanit

import ScanItSharedPreferences
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.scanit.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferences: ScanItSharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = ScanItSharedPreferences.getInstance(this)

        if (!sharedPreferences.getLoginStatus()) {
            // User is not logged in, go to the Login activity.
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish()
        } else {
            // User is logged in, proceed with the MainActivity logic.
            replaceFragment(DashboardFragment())
            binding.bottomNavigationView.selectedItemId = R.id.dashboard

            binding.bottomNavigationView.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.dashboard -> replaceFragment(DashboardFragment())
                    R.id.home -> {
                        val intent = Intent(this@MainActivity, ProductListActivity::class.java)
                        startActivity(intent)
                    }
                    R.id.scanner -> {
                        val intent = Intent(this@MainActivity, BarcodeScannerActivity::class.java)
                        startActivity(intent)
                    }
                    R.id.calculate -> {
                        val intent = Intent(this@MainActivity, PosActivity::class.java)
                        startActivity(intent)
                    }
                    R.id.account -> replaceFragment(AccountFragment())

                    else -> {
                    }
                }
                true
            }
        }
    }

    override fun onBackPressed() {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.frame_layout)
        if (currentFragment !is DashboardFragment) {
            binding.bottomNavigationView.selectedItemId = R.id.dashboard
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()
    }
}