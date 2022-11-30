package com.example.presence.bottom_navigation

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.presence.*
import com.example.presence.R
import com.example.presence.admin.AddLocation
import com.example.presence.admin.TeamAttendance
import com.example.presence.admin.User_Add
import com.example.presence.data.Attendance
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_profile.*
import java.io.File
import java.time.LocalDateTime
import java.time.Month
import java.time.temporal.ChronoField

class Profile : AppCompatActivity() {
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var toolbar : androidx.appcompat.widget.Toolbar
    private var db = Firebase.firestore
    private var month:Int=0
    private var year:Int=0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        var current= LocalDateTime.now()
        var y=current.get(ChronoField.YEAR)
        var m=current.get(ChronoField.MONTH_OF_YEAR)
        var d=current.get(ChronoField.DAY_OF_MONTH)
        var check=""
        db = FirebaseFirestore.getInstance()
        val dd=db.collection("User").document("Attendance").collection("${FirebaseAuth.getInstance().currentUser?.uid}").document(y.toString())
        val db: DatabaseReference = FirebaseDatabase.getInstance("https://presence-23cbb-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Check")
        db.get().addOnSuccessListener {
            check=it.child("admin").value.toString()
            if (check == "${FirebaseAuth.getInstance().currentUser?.uid}")
            {
                findViewById<FloatingActionButton>(R.id.floatingActionButton1).visibility= View.VISIBLE
                val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout1)
                findViewById<NavigationView>(R.id.nav_view1)
                findViewById<BottomNavigationView>(R.id.navigation1)

                findViewById<FloatingActionButton>(R.id.floatingActionButton1).setOnClickListener {
                    drawerLayout.openDrawer(GravityCompat.START)
                }


                toggle = ActionBarDrawerToggle(this, drawerLayout,R.string.nav_open,R.string.nav_close)
                drawerLayout.addDrawerListener(toggle)
                toggle.syncState()
            }
            else
            {

                val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout1)
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to load the data", Toast.LENGTH_SHORT).show()
        }

        for(i in 1..m)
        {
          dd.collection(i.toString()).get().addOnSuccessListener{
                if(!it.isEmpty){
                    for(data in it.documents){
                        year++
                        findViewById<TextView>(R.id.yeartext).setText(year.toString())
                        if(i==m)
                        {
                            month++
                            findViewById<TextView>(R.id.monthtext).setText((month).toString())
                        }
                    }
                }
            }.addOnFailureListener{
                Toast.makeText(this,it.toString(), Toast.LENGTH_LONG).show()
            }
        }

        dd.collection(m.toString()).document(d.toString()).get().addOnSuccessListener{
            if(it.exists())
            {
                findViewById<TextView>(R.id.daytext).setText((1).toString())
            }
        }
        val bottomNavigationView : BottomNavigationView =findViewById(R.id.navigation1)
        bottomNavigationView.selectedItemId=R.id.profile

        navigation1.setOnNavigationItemSelectedListener() {
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
            }
            true
        }

        getUserProfilePicture()
        getUserData()

        nav_view1.setNavigationItemSelectedListener {
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
    private fun getUserProfilePicture() {
        val storageReference: StorageReference = FirebaseStorage.getInstance().getReference("Profile/${FirebaseAuth.getInstance().currentUser?.uid}.jpg")
        val localFile= File.createTempFile("tempImage","jpg")
        storageReference.getFile(localFile).addOnSuccessListener{
            val bitmap= BitmapFactory.decodeFile(localFile.absolutePath)
            findViewById<ImageView>(R.id.userImageView).setImageBitmap(bitmap)
        }.addOnFailureListener{
            Toast.makeText(this@Profile,"Failed to retrieve image",Toast.LENGTH_SHORT).show()
        }
    }
    private fun getUserData() {
        val databaseReference: DatabaseReference=FirebaseDatabase.getInstance("https://presence-23cbb-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Users")
        databaseReference.child("${FirebaseAuth.getInstance().currentUser?.uid}").get().addOnSuccessListener {
            if(it.exists())
            {
                findViewById<TextView>(R.id.name).setText("${it.child("fullName").value}")
                findViewById<TextView>(R.id.gender).setText("${it.child("gender").value}")
                findViewById<TextView>(R.id.employeeId).setText("${it.child("employeeNo").value}")
                findViewById<TextView>(R.id.phoneNo).setText("${it.child("phoneNo").value}")
                findViewById<TextView>(R.id.officeAddress).setText("${it.child("officeAddress").value}")
                findViewById<TextView>(R.id.designation).setText("${it.child("designation").value}")
                findViewById<TextView>(R.id.email).setText("${FirebaseAuth.getInstance().currentUser?.email}")
            }
            else
            {
                Toast.makeText(this@Profile, "User Doesn't exists", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this@Profile, "Failed to load the data", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onBackPressed() {
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout1)
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
    fun leap(year:Int): Boolean {
        if(year%400==0)
        {
            return true;
        }
        if(year%100==0)
        {
            return false;
        }
        if(year%4==0)
        {
            return true;
        }
        return false;
    }
}