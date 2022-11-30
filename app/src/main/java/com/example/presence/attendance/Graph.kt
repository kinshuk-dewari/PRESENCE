package com.example.presence.attendance

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import com.example.presence.MyAdapter
import com.example.presence.R
import com.example.presence.data.Attendance
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.charts.RadarChart
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.checkerframework.checker.units.qual.min
import java.lang.Integer.max
import java.lang.Integer.min
import java.time.LocalDateTime
import java.time.Month
import java.time.YearMonth
import java.time.temporal.ChronoField
import java.util.Calendar

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

class Graph : AppCompatActivity() {
    lateinit var barChart:BarChart
    lateinit var pieChart:PieChart
    lateinit var date:TextView
    private var db = Firebase.firestore
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_graph)
        var current= LocalDateTime.now()
        var year=current.get(ChronoField.YEAR)
        val userId=intent.getStringExtra("Uid")
        db = FirebaseFirestore.getInstance()
        val d=db.collection("User").document("Attendance").collection("$userId")
        val dropDown : Spinner = findViewById(R.id.month)
        val listed = ArrayList<String>()
        listed.add("January")
        listed.add("February")
        listed.add("March")
        listed.add("April")
        listed.add("May")
        listed.add("June")
        listed.add("July")
        listed.add("August")
        listed.add("September")
        listed.add("October")
        listed.add("November")
        listed.add("December")
        val listedAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item,listed)
        dropDown.setAdapter(listedAdapter)

        var mp = HashMap<String,String>()
        mp["January"]="1"
        mp["February"]="2"
        mp["March"]="3"
        mp["April"]="4"
        mp["May"]="5"
        mp["June"]="6"
        mp["July"]="7"
        mp["August"]="8"
        mp["September"]="9"
        mp["October"]="10"
        mp["November"]="11"
        mp["December"]="12"

        var bb=leap(year)
        var mp1 = HashMap<String,Int>()
        mp1["January"]=Month.JANUARY.length(bb)
        mp1["February"]=Month.FEBRUARY.length(bb)
        mp1["March"]=Month.MARCH.length(bb)
        mp1["April"]=Month.APRIL.length(bb)
        mp1["May"]=Month.MAY.length(bb)
        mp1["June"]=Month.JUNE.length(bb)
        mp1["July"]=Month.JULY.length(bb)
        mp1["August"]=Month.AUGUST.length(bb)
        mp1["September"]=Month.SEPTEMBER.length(bb)
        mp1["October"]=Month.OCTOBER.length(bb)
        mp1["November"]=Month.NOVEMBER.length(bb)
        mp1["December"]=Month.DECEMBER.length(bb)

        val goBarChart=findViewById<Button>(R.id.go_bar_chart)
        val goPiChart=findViewById<Button>(R.id.go_pi_chart)

        barChart=findViewById(R.id.bar_chart)
        pieChart=findViewById(R.id.pie_chart)
        date=findViewById(R.id.date)
        pieChart.visibility= View.GONE
        barChart.visibility= View.GONE

        goBarChart.setOnClickListener {
            val month=findViewById<Spinner>(R.id.month).selectedItem.toString()
            barChart.visibility= View.VISIBLE
            date.visibility=View.VISIBLE
            pieChart.visibility= View.GONE
            goBarChart.visibility= View.GONE
            goPiChart.visibility= View.VISIBLE

            val list:ArrayList<BarEntry> =ArrayList()
            d.document(year.toString()).collection(mp[month].toString()).get().addOnSuccessListener{
                if(!it.isEmpty){
                    for(data in it.documents){
                        val attendance: Attendance? = data.toObject(Attendance::class.java)
                        var a=attendance?.Date.toString().split("/")
                        list.add(BarEntry(a[0].toFloat(),(1).toFloat()))
                    }
                    val barDataSet=BarDataSet(list,"Present")
                    barDataSet.setColors(ColorTemplate.MATERIAL_COLORS,255)
                    barDataSet.valueTextColor=Color.BLACK
                    val barData=BarData(barDataSet)
                    barChart.setFitBars(true)
                    barChart.data=barData
                    barChart.description.text="Present on these Days"
                    barChart.animateY(500)
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
        goPiChart.setOnClickListener {
            var count=0
            val month=findViewById<Spinner>(R.id.month).selectedItem.toString()
            date.visibility=View.GONE
            barChart.visibility= View.GONE
            pieChart.visibility= View.VISIBLE
            goPiChart.visibility= View.GONE
            goBarChart.visibility= View.VISIBLE
            val list1:ArrayList<PieEntry> =ArrayList()
            var low=31
            var high=1
            d.document(year.toString()).collection(mp[month].toString()).get().addOnSuccessListener{
                if(!it.isEmpty){
                    for(data in it.documents){
                        val attendance: Attendance? = data.toObject(Attendance::class.java)
                        var a=attendance?.Date.toString().split("/")
                        low= min(low,a[0].toInt())
                        high=max(high,a[0].toInt())
                        count++
                    }
                    list1.add(PieEntry((count.toFloat()/mp1[month]!!.toFloat())*100,"Present"))
                    list1.add(PieEntry(((low+(high.toFloat()-(low+count)))/(mp1[month]!!.toFloat()))*100,"Absent"))
                    list1.add(PieEntry(((mp1[month]!!.toFloat()-high.toFloat())/mp1[month]!!.toFloat())*100,"Not Marked"))
                    val pieDataSet=PieDataSet(list1,"%")
                    pieDataSet.setColors(ColorTemplate.MATERIAL_COLORS,255)
                    pieDataSet.valueTextSize=12f
                    pieDataSet.valueTextColor= Color.BLACK
                    val pieData=PieData(pieDataSet)
                    pieChart.data=pieData
                    pieChart.description.text="Present/Absent"
                    pieChart.centerText="%"
                    pieChart.animateY(500)
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
    }
}