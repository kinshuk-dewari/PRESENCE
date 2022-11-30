package com.example.presence.bottom_navigation

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.DatePicker
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.presence.*
import com.example.presence.admin.AddLocation
import com.example.presence.admin.TeamAttendance
import com.example.presence.admin.User_Add
import com.example.presence.attendance.ShowAttendance
import com.example.presence.attendance.Window
import com.example.presence.events.Events
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.apply_leave.*
import kotlinx.android.synthetic.main.apply_leave.dialog_add
import kotlinx.android.synthetic.main.reason.*
import java.util.Date


private var mContext: Context? = null
class Home : AppCompatActivity() {

    private lateinit var toggle: ActionBarDrawerToggle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        mContext=this
        var mail=""
        var check=""
        val databaseReference: DatabaseReference = FirebaseDatabase.getInstance("https://presence-23cbb-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Check")
        databaseReference.get().addOnSuccessListener {
            check=it.child("admin").value.toString()
            if (check == "${FirebaseAuth.getInstance().currentUser?.uid}")
            {
                findViewById<FloatingActionButton>(R.id.floatingActionButton).visibility=View.VISIBLE
                val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
                val navView: NavigationView = findViewById(R.id.nav_view)
                val bottomNavigationView : BottomNavigationView =findViewById(R.id.navigation)


                //handling floating action menu
                findViewById<FloatingActionButton>(R.id.floatingActionButton).setOnClickListener{
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
                //findViewById<FloatingActionButton>(R.id.floatingActionButton).visibility=View.GONE
                val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            }
            mail=it.child("email").value.toString()
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to load the data", Toast.LENGTH_SHORT).show()
        }

        //
        val bottomNavigationView : BottomNavigationView =findViewById(R.id.navigation)
        bottomNavigationView.selectedItemId=R.id.home
        val layoutParams: CoordinatorLayout.LayoutParams =
            bottomNavigationView.layoutParams as CoordinatorLayout.LayoutParams
        layoutParams.behavior = BottomNavigationBehaviour()

        findViewById<CardView>(R.id.markAttendance).setOnClickListener {
            startActivity(Intent(this, Window::class.java))
            overridePendingTransition(R.anim.fade_in,R.anim.fade_out)
            finish()
        }
        findViewById<CardView>(R.id.showAttendance).setOnClickListener {
            startActivity(Intent(this, ShowAttendance::class.java))
            overridePendingTransition(R.anim.fade_in,R.anim.fade_out)
            finish()
        }
        findViewById<CardView>(R.id.applyForLeave).setOnClickListener {

            var datePicker:Date?=null
            val dialog = Dialog(mContext!!)
            dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.apply_leave)
            dialog.setCanceledOnTouchOutside(false)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.show()

            dialog.dialog_add.setOnClickListener{
                datePicker=Date(dialog.datePicker.year,dialog.datePicker.month,dialog.datePicker.dayOfMonth)
                val dialog1=Dialog(mContext!!)
                dialog1.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE)
                dialog1.setContentView(R.layout.reason)
                dialog1.setCanceledOnTouchOutside(false)
                dialog1.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dialog1.show()
                dialog1.dialog_add.setOnClickListener {
                    dialog1.dismiss()
                    dialog.dismiss()
                    val mIntent = Intent(Intent.ACTION_SENDTO)
                    mIntent.data = Uri.parse("mailto:${mail}?subject=Leave Application&body=I want to apply for leave on ${datePicker.toString()} because ${dialog1.reason.text}")
                    mIntent.setPackage("com.google.android.gm")
                    try {
                        startActivity(mIntent)
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText(this, "Gmail is not installed on this device.", Toast.LENGTH_LONG).show()
                    }

                }
                dialog1.dialog_cancel.setOnClickListener {
                    dialog1.dismiss()
                }
            }
        }
        findViewById<CardView>(R.id.events).visibility=View.GONE
        findViewById<CardView>(R.id.todo).setOnClickListener {
            startActivity(Intent(this, Todo::class.java))
            overridePendingTransition(R.anim.fade_in,R.anim.fade_out)
        }
        findViewById<CardView>(R.id.events).setOnClickListener {
            startActivity(Intent(this, Events::class.java))
            overridePendingTransition(R.anim.fade_in,R.anim.fade_out)
        }
        navigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.settings -> {

                    startActivity(Intent(this , Settings :: class.java))
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
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

        nav_view.setNavigationItemSelectedListener {
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
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}