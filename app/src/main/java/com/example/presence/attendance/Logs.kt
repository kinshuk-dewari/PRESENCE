package com.example.presence.attendance

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.presence.MyAdapter1
import com.example.presence.R
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.time.LocalDateTime
import java.time.temporal.ChronoField
import com.example.presence.data.Attendance
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import java.io.FileWriter
import java.io.IOException

class Logs : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var userList: ArrayList<Attendance>
    private var db = Firebase.firestore
    override fun onCreate(savedInstanceState: Bundle?) {
        var current = LocalDateTime.now()
        var year = current.get(ChronoField.YEAR)
        var month = current.get(ChronoField.MONTH_OF_YEAR)
        var day = current.get(ChronoField.DAY_OF_MONTH)
        var use:ArrayList<Attendance>?=null
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logs)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        userList = arrayListOf()
        findViewById<TextView>(R.id.header_title).setText("Today")
        findViewById<TextView>(R.id.date).setText("$day/$month/$year")
        val d = db.collection("User").document("Attendance")
        val databaseReference: DatabaseReference =
            FirebaseDatabase.getInstance("https://presence-23cbb-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("Employer")
        databaseReference.get().addOnSuccessListener {
            for (data in it.children) {
                d.collection(data.child("uid").value.toString()).document(year.toString())
                    .collection(month.toString()).document(day.toString()).get()
                    .addOnSuccessListener {
                        if (it.exists()) {
                            val attendance: Attendance? = it.toObject(Attendance::class.java)
                            userList.add(attendance!!)
                        }
                        use=userList
                        recyclerView.adapter = MyAdapter1(userList)
                    }.addOnFailureListener {
                    Toast.makeText(this, it.toString(), Toast.LENGTH_LONG).show()
                }
            }
        }
        findViewById<FloatingActionButton>(R.id.export).visibility= View.GONE
        findViewById<FloatingActionButton>(R.id.export).setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                    val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    requestPermissions(permissions, 1000)
                } else {
                    val folder = File(
                        "/storage/Presence"
                    )
                    Toast.makeText(this, folder.toString(), Toast.LENGTH_SHORT).show()
                    var success = true
                    if (!folder.exists()) {
                        success = folder.mkdir()
                    }
                    if (success) {
                        val file = File(folder, "Attendance.csv")
                        try {
                            val fileWriter = FileWriter(file)
                            fileWriter.append("Name")
                            fileWriter.append(',')
                            fileWriter.append("Date")
                            fileWriter.append(',')
                            fileWriter.append("Entry")
                            fileWriter.append(',')
                            fileWriter.append("Exit")
                            fileWriter.append(',')
                            fileWriter.append("GPS")
                            fileWriter.append('\n')
                            for (data in use!!) {
                                fileWriter.append(data.IdName)
                                fileWriter.append(',')
                                fileWriter.append(data.Date)
                                fileWriter.append(',')
                                fileWriter.append(data.Entry)
                                fileWriter.append(',')
                                fileWriter.append(data.Exit)
                                fileWriter.append(',')
                                fileWriter.append(data.GPS)
                                fileWriter.append('\n')
                            }
                            fileWriter.flush()
                            fileWriter.close()
                            Toast.makeText(this, "Log exported.", Toast.LENGTH_SHORT).show()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                }
            } else {
                val folder = File(
                    "/storage/Presence"
                )
                Toast.makeText(this, folder.toString(), Toast.LENGTH_SHORT).show()
                var success = true
                if (!folder.exists()) {
                    success = folder.mkdir()
                }
                if (success) {
                    val file = File(folder, "Attendance.csv")
                    try {
                        val fileWriter = FileWriter(file)
                        fileWriter.append("Name")
                        fileWriter.append(',')
                        fileWriter.append("Date")
                        fileWriter.append(',')
                        fileWriter.append("Entry")
                        fileWriter.append(',')
                        fileWriter.append("Exit")
                        fileWriter.append(',')
                        fileWriter.append("GPS")
                        fileWriter.append('\n')
                        for (data in use!!) {
                            fileWriter.append(data.IdName)
                            fileWriter.append(',')
                            fileWriter.append(data.Date)
                            fileWriter.append(',')
                            fileWriter.append(data.Entry)
                            fileWriter.append(',')
                            fileWriter.append(data.Exit)
                            fileWriter.append(',')
                            fileWriter.append(data.GPS)
                            fileWriter.append('\n')
                        }
                        fileWriter.flush()
                        fileWriter.close()
                        Toast.makeText(this, "Log exported.", Toast.LENGTH_SHORT).show()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
}