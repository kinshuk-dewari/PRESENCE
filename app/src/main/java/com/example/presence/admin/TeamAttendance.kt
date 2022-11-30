package com.example.presence.admin

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.presence.attendance.Logs
import com.example.presence.R
import com.example.presence.attendance.Graph
import com.example.presence.attendance.RetrieveData
import com.example.presence.bottom_navigation.Home
import com.example.presence.bottom_navigation.Profile
import com.example.presence.bottom_navigation.Settings
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_add_location.*
import kotlinx.android.synthetic.main.activity_team_attendance.*

class TeamAttendance : AppCompatActivity() {
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var toolbar : androidx.appcompat.widget.Toolbar
    @SuppressLint("CutPasteId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_team_attendance)

        var check=""
        val db: DatabaseReference = FirebaseDatabase.getInstance("https://presence-23cbb-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Check")
        db.get().addOnSuccessListener {
            check=it.child("admin").value.toString()
            if (check == "${FirebaseAuth.getInstance().currentUser?.uid}")
            {
                findViewById<FloatingActionButton>(R.id.floatingActionButton6).visibility= View.VISIBLE
                val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout6)
                val navView: NavigationView = findViewById(R.id.nav_view6)
                val bottomNavigationView : BottomNavigationView =findViewById(R.id.navigation6)

                //handling floating action menu
                findViewById<FloatingActionButton>(R.id.floatingActionButton6).setOnClickListener{
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
                val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout6)
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to load the data", Toast.LENGTH_SHORT).show()
        }

        val dropDown : AutoCompleteTextView = findViewById(R.id.employeeId)

        val listed  = ArrayList<String>()
        val HashMap=HashMap<String,String> ()
        val databaseReference: DatabaseReference =FirebaseDatabase.getInstance("https://presence-23cbb-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Employer")
        databaseReference.get().addOnSuccessListener {
            if(it.exists())
            {
                for(data in it.children)
                {
                    HashMap["${data.child("employeeId").value}-${data.child("userName").value}"]=data.child("uid").value.toString()
                    listed.add("${data.child("employeeId").value}-${data.child("userName").value}")
                }
            }
            else
            {
                Toast.makeText(this, "User Doesn't exists", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to load the data", Toast.LENGTH_SHORT).show()
        }


        val listedAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item,listed)

        dropDown.setAdapter(listedAdapter)

        navigation6.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.settings -> {
                    startActivity(Intent(this , Settings :: class.java))
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
                    finish()
                }
                R.id.home -> {
                    startActivity(Intent(this , Home :: class.java))
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

        var id:String=""

        findViewById<Button>(R.id.todayAttendance).setOnClickListener {
            id=findViewById<TextView>(R.id.employeeId).text.toString()
            if(id!="")
            {
                val intent=Intent(this, RetrieveData::class.java)
                intent.putExtra("Attendance","Today")
                intent.putExtra("Uid",HashMap[id])
                overridePendingTransition(0,0)
                startActivity(intent)
            }

        }
        findViewById<Button>(R.id.monthAttendance).setOnClickListener {
            id=findViewById<TextView>(R.id.employeeId).text.toString()
            if(id!="")
            {
                val intent=Intent(this, RetrieveData::class.java)
                intent.putExtra("Attendance","Month")
                intent.putExtra("Uid",HashMap[id])
                overridePendingTransition(0,0)
                startActivity(intent)
            }
        }
        findViewById<Button>(R.id.yearAttendance).setOnClickListener {
            id=findViewById<TextView>(R.id.employeeId).text.toString()
            if(id!="")
            {
                val intent=Intent(this, RetrieveData::class.java)
                intent.putExtra("Attendance","Year")
                intent.putExtra("Uid",HashMap[id])
                overridePendingTransition(0,0)
                startActivity(intent)
            }
        }
        findViewById<Button>(R.id.logs).setOnClickListener {
            startActivity(Intent(this, Logs::class.java))
            overridePendingTransition(0,0)
        }
        findViewById<Button>(R.id.visual).setOnClickListener {
            id=findViewById<TextView>(R.id.employeeId).text.toString()
            if(id!="")
            {
                val intent=Intent(this, Graph::class.java)
                intent.putExtra("Uid",HashMap[id])
                overridePendingTransition(0,0)
                startActivity(intent)
            }
        }
        nav_view6.setNavigationItemSelectedListener {
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
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout6)
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }

    }
}