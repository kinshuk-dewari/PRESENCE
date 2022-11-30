package com.example.presence.attendance

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.presence.*
import com.example.presence.admin.AddLocation
import com.example.presence.admin.TeamAttendance
import com.example.presence.admin.User_Add
import com.example.presence.authentication.LoginActivity
import com.example.presence.bottom_navigation.Home
import com.example.presence.bottom_navigation.Profile
import com.example.presence.bottom_navigation.Settings
import com.example.presence.face_recognization.Face
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
import com.google.maps.android.SphericalUtil
import kotlinx.android.synthetic.main.activity_window.*
import kotlinx.android.synthetic.main.add_task_dialog.*
import kotlinx.android.synthetic.main.alert.*
import java.time.LocalTime

private var mContext: Context? = null
class Window : AppCompatActivity(), OnMapReadyCallback {
    private var currentLocation : Location?=null
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
        setContentView(R.layout.activity_window)
        mContext=this@Window
        var check=""
        val db: DatabaseReference = FirebaseDatabase.getInstance("https://presence-23cbb-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Check")
        db.get().addOnSuccessListener {
            check = it.child("admin").value.toString()
            if (check == "${FirebaseAuth.getInstance().currentUser?.uid}")
            {
                findViewById<FloatingActionButton>(R.id.floatingActionButton3).visibility= View.VISIBLE
                val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout3)
                val navView: NavigationView = findViewById(R.id.nav_view3)
                val bottomNavigationView : BottomNavigationView =findViewById(R.id.navigation3)

                //handling floating action menu
                findViewById<FloatingActionButton>(R.id.floatingActionButton3).setOnClickListener{
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
                val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout3)
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to load the data", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.markAttendance).setOnClickListener {
            val databaseReference: DatabaseReference = FirebaseDatabase.getInstance("https://presence-23cbb-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Location")
            databaseReference.get().addOnSuccessListener {
                if(it.exists()) {

                    val latitude = it.child("Latitude").value.toString().toDouble()
                    val longitude = it.child("Longitude").value.toString().toDouble()
                    val radius = it.child("Radius").value.toString().toDouble()
                    val b= checkForGeoFenceEntry(latitude,longitude,radius)

                    Toast.makeText(this, b.toString(), Toast.LENGTH_SHORT).show()
                    if(b)
                    {
                        startActivity(Intent(this, Face::class.java))
                        finish()
                    }
                    else
                    {


                        val dialog = Dialog(mContext!!)
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                        dialog.setContentView(R.layout.alert)
                        dialog.setCanceledOnTouchOutside(false)
                        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                        dialog.show()

                        dialog.dialog_ok.setOnClickListener{
                            dialog.dismiss()
                            startActivity(Intent(this@Window, LoginActivity::class.java))
                            finish()
                        }
                    }
                }
            }
        }
        val bottomNavigationView : BottomNavigationView =findViewById(R.id.navigation3)
        val layoutParams: CoordinatorLayout.LayoutParams =
            bottomNavigationView.layoutParams as CoordinatorLayout.LayoutParams
        layoutParams.behavior = BottomNavigationBehaviour()


        navigation3.setOnNavigationItemSelectedListener {
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

        nav_view3.setNavigationItemSelectedListener {
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
        fusedLocationClient =  LocationServices.getFusedLocationProviderClient(this)
        fetchLocation()
    }
    fun checkForGeoFenceEntry(geofenceLat: Double, geofenceLong: Double, radius: Double): Boolean{
        val startLatLng = LatLng(currentLocation!!.latitude, currentLocation!!.longitude) // User Location
        val geofenceLatLng = LatLng(geofenceLat, geofenceLong) // Center of geofence

        val distanceInMeters = SphericalUtil.computeDistanceBetween(startLatLng, geofenceLatLng)

        if (distanceInMeters < radius) {
            // User is inside the Geo-fence
            //showNotificationEvent.call()
            Toast.makeText(this, "true", Toast.LENGTH_SHORT).show()
            return true
        }
        else
        {
            Toast.makeText(this, "false", Toast.LENGTH_SHORT).show()
            return false
        }
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
                Toast.makeText(applicationContext, currentLocation?.latitude.toString() + "" +
                        currentLocation?.longitude, Toast.LENGTH_SHORT).show()
                val supportMapFragment = (supportFragmentManager.findFragmentById(R.id.map) as
                        SupportMapFragment?)!!
                supportMapFragment.getMapAsync(this)
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
        }.addOnFailureListener {
            Toast.makeText(this, "No data found in database", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onBackPressed() {
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout3)
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
