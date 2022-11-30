package com.example.presence.bottom_navigation

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.presence.BottomNavigationBehaviour
import com.example.presence.FAQ
import com.example.presence.R
import com.example.presence.admin.AddLocation
import com.example.presence.admin.TeamAttendance
import com.example.presence.admin.User_Add
import com.example.presence.authentication.LoginActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_settings.*


class Settings : AppCompatActivity() {
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var toolbar : androidx.appcompat.widget.Toolbar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        var check=""
        val db: DatabaseReference = FirebaseDatabase.getInstance("https://presence-23cbb-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Check")
        db.get().addOnSuccessListener {
            check=it.child("admin").value.toString()
            if (check == "${FirebaseAuth.getInstance().currentUser?.uid}")
            {
                findViewById<FloatingActionButton>(R.id.floatingActionButton2).visibility= View.VISIBLE
                val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout2)
                findViewById<NavigationView>(R.id.nav_view2)
                findViewById<BottomNavigationView>(R.id.navigation2)

                findViewById<FloatingActionButton>(R.id.floatingActionButton2).setOnClickListener {
                    drawerLayout.openDrawer(GravityCompat.START)
                }


                toggle = ActionBarDrawerToggle(this, drawerLayout,
                    R.string.nav_open,
                    R.string.nav_close
                )
                drawerLayout.addDrawerListener(toggle)

                toggle.syncState()
            }
            else
            {
                val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout2)
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to load the data", Toast.LENGTH_SHORT).show()
        }

        val bottomNavigationView : BottomNavigationView =findViewById(R.id.navigation2)
        bottomNavigationView.selectedItemId=R.id.settings
        val layoutParams: CoordinatorLayout.LayoutParams =
            bottomNavigationView.layoutParams as CoordinatorLayout.LayoutParams
        layoutParams.behavior = BottomNavigationBehaviour()

        findViewById<Button>(R.id.logout).setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent=Intent(this, LoginActivity::class.java)
            intent.flags= Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
        findViewById<Button>(R.id.darkMode).setOnClickListener {

        }
        findViewById<Button>(R.id.customerCare).setOnClickListener {
            val mIntent = Intent(Intent.ACTION_SEND)
            mIntent.data = Uri.parse("mailto:")
            mIntent.type = "text/plain"

            mIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("presence.elitebarbs@gmail.com"))


            try {
                startActivity(Intent.createChooser(mIntent, "Choose Email Client..."))
            }
            catch (e: Exception){
                Toast.makeText(this,e.message,Toast.LENGTH_LONG).show()
            }
        }
        findViewById<Button>(R.id.faq).setOnClickListener {
            startActivity(Intent(this, FAQ::class.java))
        }
        navigation2.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.home -> {
                    startActivity(Intent(this , Home :: class.java))
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    finish()
                }
                R.id.profile -> {
                    startActivity(Intent(this , Profile:: class.java))
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    finish()
                }
            }
            true
        }

        nav_view2.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.teamsAttendance ->{
                    startActivity(Intent(this , TeamAttendance :: class.java))
                    overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right)
                    finish()
                }
                R.id.addLocation -> {
                    startActivity(Intent(this , AddLocation :: class.java))
                    overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right)
                    finish()
                }
                R.id.addUser ->{
                    startActivity(Intent(this , User_Add :: class.java))
                    overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right)
                    finish()
                }
            }
            true
        }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(toggle.onOptionsItemSelected(item))
        {
            return true
        }
        return super.onOptionsItemSelected(item)
    }
    override fun onBackPressed() {
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout2)
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

}