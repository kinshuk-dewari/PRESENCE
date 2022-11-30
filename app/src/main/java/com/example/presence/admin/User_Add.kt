package com.example.presence.admin

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.cardview.widget.CardView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.presence.R
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
import kotlinx.android.synthetic.main.activity_user_add.*

class User_Add : AppCompatActivity() {
    private lateinit var toggle: ActionBarDrawerToggle
    @SuppressLint("CutPasteId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_add)

        var check=""
        val db: DatabaseReference = FirebaseDatabase.getInstance("https://presence-23cbb-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Check")
        db.get().addOnSuccessListener {
            check=it.child("admin").value.toString()
            if (check == "${FirebaseAuth.getInstance().currentUser?.uid}")
            {
                findViewById<FloatingActionButton>(R.id.floatingActionButton7).visibility= View.VISIBLE
                val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout7)
                val navView: NavigationView = findViewById(R.id.nav_view7)
                val bottomNavigationView : BottomNavigationView =findViewById(R.id.navigation7)

                //handling floating action menu
                findViewById<FloatingActionButton>(R.id.floatingActionButton7).setOnClickListener{
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
                val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout7)
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to load the data", Toast.LENGTH_SHORT).show()
        }

        val dropDown : AutoCompleteTextView = findViewById(R.id.employeeId)

        val listed  = ArrayList<String>()
        val HashMap=HashMap<String,String> ()
        val databaseReference: DatabaseReference = FirebaseDatabase.getInstance("https://presence-23cbb-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Employer")
        databaseReference.get().addOnSuccessListener {
            if(it.exists())
            {
                for(data in it.children)
                {
                    if(data.child("status").value=="No")
                    {
                        HashMap["${data.child("employeeId").value}-${data.child("userName").value}"]=data.child("uid").value.toString()
                        listed.add("${data.child("employeeId").value}-${data.child("userName").value}")
                    }
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

        navigation7.setOnNavigationItemSelectedListener {
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
        findViewById<Button>(R.id.loadData).setOnClickListener {
            id=findViewById<TextView>(R.id.employeeId).text.toString()
            val db: DatabaseReference=FirebaseDatabase.getInstance("https://presence-23cbb-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Users")
            db.child(HashMap[id].toString()).get().addOnSuccessListener {
                if(it.exists())
                {
                    val card=findViewById<CardView>(R.id.info)
                    findViewById<TextView>(R.id.userName).text = "${it.child("fullName").value}"
                    findViewById<TextView>(R.id.userGender).text = "${it.child("gender").value}"
                    findViewById<TextView>(R.id.userEmployeeId).text = "${it.child("employeeNo").value}"
                    findViewById<TextView>(R.id.userPhone).text = "${it.child("phoneNo").value}"
                    findViewById<TextView>(R.id.userAddress).text = "${it.child("officeAddress").value}"
                    findViewById<TextView>(R.id.userDesignation).text = "${it.child("designation").value}"
                    card.visibility=View.VISIBLE
                }
                else
                {
                    Toast.makeText(this, "User Doesn't exists", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to load the data", Toast.LENGTH_SHORT).show()
            }
        }
        findViewById<Button>(R.id.addEmployee).setOnClickListener {
            id=findViewById<TextView>(R.id.employeeId).text.toString()
            FirebaseDatabase.getInstance("https://presence-23cbb-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Employer").child(HashMap[id].toString()).child("status").setValue("Yes").addOnSuccessListener {
                Toast.makeText(this, "Account has been Verified", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(this, "Error occurred while verifying the user", Toast.LENGTH_SHORT).show()
            }
        }
        nav_view7.setNavigationItemSelectedListener {
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
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout7)
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}