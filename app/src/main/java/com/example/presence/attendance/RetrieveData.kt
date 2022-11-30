package com.example.presence.attendance

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.presence.MyAdapter
import com.example.presence.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.time.LocalDateTime
import java.time.temporal.ChronoField
import com.example.presence.data.Attendance
class RetrieveData : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var userList: ArrayList<Attendance>
    private var db = Firebase.firestore
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_retrieve_data)
        var current= LocalDateTime.now()
        var year=current.get(ChronoField.YEAR)
        var month=current.get(ChronoField.MONTH_OF_YEAR)
        var day=current.get(ChronoField.DAY_OF_MONTH)
        var attendanceValue=intent.getStringExtra("Attendance")
        db = FirebaseFirestore.getInstance()

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val userId=intent.getStringExtra("Uid")
        userList = arrayListOf()
        val d=db.collection("User").document("Attendance").collection("$userId")
        if(attendanceValue=="Today")
        {
            findViewById<TextView>(R.id.header_title).setText("Today")
            findViewById<TextView>(R.id.date).setText("$day/$month/$year")
            d.document(year.toString()).collection(month.toString()).document(day.toString()).get().addOnSuccessListener{
                if(it.exists())
                {
                    val attendance: Attendance? = it.toObject(Attendance::class.java)
                    userList.add(attendance!!)
                    recyclerView.adapter = MyAdapter(userList)
                }
                else
                {
                    Toast.makeText(this, "No record found", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }.addOnFailureListener{
                Toast.makeText(this,it.toString(), Toast.LENGTH_LONG).show()
            }
        }
        else if(attendanceValue=="Month")
        {
            findViewById<TextView>(R.id.header_title).setText("Month")
            findViewById<TextView>(R.id.date).setText("$month/$year")
            d.document(year.toString()).collection(month.toString()).get().addOnSuccessListener{
                if(!it.isEmpty){
                    for(data in it.documents){
                        val attendance: Attendance? = data.toObject(Attendance::class.java)
                        userList.add(attendance!!)
                    }
                    recyclerView.adapter = MyAdapter(userList)
                }
                else
                {
                    Toast.makeText(this, "No record found", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }.addOnFailureListener{
                Toast.makeText(this,it.toString(), Toast.LENGTH_LONG).show()
            }
        }
        else if(attendanceValue=="Year")
        {
            findViewById<TextView>(R.id.header_title).setText("Month/Year")
            findViewById<TextView>(R.id.date).setText("$month/$year")
            var m=""
            val month= arrayOf("January","February","March","April","May","June","July","August","September","October","November","December")
            val alertDialog1=AlertDialog.Builder(this).apply{
                setTitle("Select a month to view the attendance")
                setCancelable(true)
                create()
            }
            alertDialog1.setItems(month){dialog,which->
                when(which)
                {
                    0->{m="1"}
                    1->{m="2"}
                    2->{m="3"}
                    3->{m="4"}
                    4->{m="5"}
                    5->{m="6"}
                    6->{m="7"}
                    7->{m="8"}
                    8->{m="9"}
                    9->{m="10"}
                    10->{m="11"}
                    11->{m="12"}
                }
                d.document(year.toString()).collection(m).get().addOnSuccessListener{
                    if(!it.isEmpty){
                        for(data in it.documents){
                            val attendance: Attendance? = data.toObject(Attendance::class.java)
                            userList.add(attendance!!)
                        }
                        recyclerView.adapter = MyAdapter(userList)
                    }
                    else
                    {
                        Toast.makeText(this, "No record found", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }.addOnFailureListener{
                    Toast.makeText(this,it.toString(), Toast.LENGTH_LONG).show()
                }
            }
            alertDialog1.show()
        }
    }
}
