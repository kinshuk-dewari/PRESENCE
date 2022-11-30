package com.example.presence.admin

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.presence.BottomNavigationBehaviour
import com.example.presence.R
import com.example.presence.bottom_navigation.Home
import com.example.presence.bottom_navigation.Profile
import com.example.presence.bottom_navigation.Settings
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_add_location.*

class AddLocation : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var currentLocation : Location
    private lateinit var fusedLocationClient : FusedLocationProviderClient
    private lateinit var map : GoogleMap
    private val LOCATION_PERMISSION_REQUEST = 1
    private val permissionCode = 101

    private fun getLocationAccess(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            map.isMyLocationEnabled = true
        }
        else{
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),LOCATION_PERMISSION_REQUEST)
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == LOCATION_PERMISSION_REQUEST){
            if(grantResults.contains(PackageManager.PERMISSION_GRANTED)){
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                map.isMyLocationEnabled = true
            }
            else{
                Toast.makeText(this, "User has not granted Permission", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var toolbar : androidx.appcompat.widget.Toolbar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_location)

        val databaseReference: DatabaseReference = FirebaseDatabase.getInstance("https://presence-23cbb-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Location")
        findViewById<Button>(R.id.addLocation).setOnClickListener {
            val latitude=findViewById<TextView>(R.id.latitude).text.toString()
            val longitude=findViewById<TextView>(R.id.longitude).text.toString()
            val radius=findViewById<TextView>(R.id.radius).text.toString()
            val location = HashMap<String,String>()
            location["Latitude"]= latitude
            location["Longitude"]=longitude
            location["Radius"]=radius
            databaseReference.setValue(location).addOnSuccessListener {
                startActivity(Intent(this, AddLocation::class.java))
                overridePendingTransition(0,0)
                finish()
                Toast.makeText(this, "Geolocation for attendance is Updated", Toast.LENGTH_LONG).show()
            }.addOnFailureListener {
                    exception:Exception->Toast.makeText(this,exception.toString(),Toast.LENGTH_LONG).show()
            }
        }
        var check=""
        val db: DatabaseReference = FirebaseDatabase.getInstance("https://presence-23cbb-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Check")
        db.get().addOnSuccessListener {
            check=it.child("admin").value.toString()
            if (check == "${FirebaseAuth.getInstance().currentUser?.uid}")
            {
                findViewById<FloatingActionButton>(R.id.floatingActionButton5).visibility= View.VISIBLE
                val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout5)
                val navView: NavigationView = findViewById(R.id.nav_view5)
                val bottomNavigationView : BottomNavigationView =findViewById(R.id.navigation5)

                //handling floating action menu
                findViewById<FloatingActionButton>(R.id.floatingActionButton5).setOnClickListener{
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
                val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout5)
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to load the data", Toast.LENGTH_SHORT).show()
        }
        val bottomNavigationView : BottomNavigationView =findViewById(R.id.navigation5)
        val layoutParams: CoordinatorLayout.LayoutParams =
            bottomNavigationView.layoutParams as CoordinatorLayout.LayoutParams
        layoutParams.behavior = BottomNavigationBehaviour()
        navigation5.setOnNavigationItemSelectedListener {
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

        nav_view5.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.teamsAttendance ->{
                    startActivity(Intent(this, TeamAttendance::class.java))
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

        fusedLocationClient =  LocationServices.getFusedLocationProviderClient(this@AddLocation)
        fetchLocation()
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(toggle.onOptionsItemSelected(item))
        {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun fetchLocation() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), permissionCode)
            return
        }
        val task = fusedLocationClient.lastLocation
        task.addOnSuccessListener{location ->
            if (location != null){
                currentLocation = location
                Toast.makeText(applicationContext, currentLocation.latitude.toString() + "" +
                        currentLocation.longitude, Toast.LENGTH_SHORT).show()
                val supportMapFragment = (supportFragmentManager.findFragmentById(R.id.map) as
                        SupportMapFragment?)!!
                supportMapFragment.getMapAsync(this@AddLocation)
            }
        }
    }



    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        var markerOptions:MarkerOptions
        getLocationAccess()
        val databaseReference: DatabaseReference = FirebaseDatabase.getInstance("https://presence-23cbb-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Location")
        databaseReference.get().addOnSuccessListener {
            if(it.exists())
            {
                val latitude=it.child("Latitude").value.toString().toDouble()
                val longitude=it.child("Longitude").value.toString().toDouble()
                val latLng= LatLng(latitude,longitude)
                val radius=it.child("Radius").value.toString().toDouble()
                findViewById<TextView>(R.id.latitude).text= latitude.toString()
                findViewById<TextView>(R.id.longitude).text= longitude.toString()
                findViewById<TextView>(R.id.radius).text=radius.toString()
                Toast.makeText(this, "Getting the Location from database", Toast.LENGTH_SHORT).show()

                markerOptions=MarkerOptions().position(latLng).title("Attendance location")

                map.mapType = GoogleMap.MAP_TYPE_SATELLITE
                map.animateCamera(CameraUpdateFactory.newLatLng(latLng))
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
                map.addMarker(markerOptions)
                map.addCircle(
                    CircleOptions()
                        .center(latLng)
                        .radius(radius)
                        .strokeColor(ContextCompat.getColor(this, R.color.black))
                )
            }
            else
            {
                val latLng = LatLng(currentLocation.latitude, currentLocation.longitude)
                val radius=100.00
                findViewById<TextView>(R.id.latitude).text= currentLocation.latitude.toString()
                findViewById<TextView>(R.id.longitude).text= currentLocation.longitude.toString()
                findViewById<TextView>(R.id.radius).text=radius.toString()
                Toast.makeText(this, "Getting the Current Location", Toast.LENGTH_SHORT).show()

                markerOptions=MarkerOptions().position(latLng).title("Attendance location")

                map.mapType = GoogleMap.MAP_TYPE_NORMAL
                map.animateCamera(CameraUpdateFactory.newLatLng(latLng))
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
                map.addMarker(markerOptions)
                map.addCircle(
                    CircleOptions()
                        .center(latLng)
                        .radius(radius)
                        .strokeColor(ContextCompat.getColor(this, R.color.black))
                        .fillColor(ContextCompat.getColor(this, R.color.grey))
                )
            }
        }.addOnFailureListener {
            Toast.makeText(this, "No data found in database", Toast.LENGTH_SHORT).show()
        }

    }

    override fun onBackPressed() {
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout5)
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
