package com.example.presence

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.presence.data.Attendance

class MyAdapter1(private val userList: ArrayList<Attendance>): RecyclerView.Adapter<MyAdapter1.MyViewHolder>(){

    class MyViewHolder(itemView: View):RecyclerView.ViewHolder(itemView)
    {
        val IdName:TextView=itemView.findViewById(R.id.employeeId)
        val tvDate:TextView= itemView.findViewById(R.id.Date)
        val tvEntry:TextView= itemView.findViewById(R.id.entry_time)
        val tvExit:TextView= itemView.findViewById(R.id.exit_time)
        val tvGPS:TextView=itemView.findViewById(R.id.gps)
        val back: LinearLayout =itemView.findViewById(R.id.back)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
       val itemView= LayoutInflater.from(parent.context).inflate(R.layout.logs_list,parent,false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.IdName.text = "Id-Name : "+userList[position].IdName
        holder.tvDate.text = "Date : "+userList[position].Date
        holder.tvEntry.text = "In : "+userList[position].Entry
        holder.tvExit.text = "Out : "+userList[position].Exit
        holder.tvGPS.text = " : "+userList[position].GPS
        // Set the animation for the view holder
        val animation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.recycler_view_animation)
        animation.startOffset = (position * 100).toLong() // Stagger the animations by 100ms
        holder.itemView.startAnimation(animation)
        if(position%2==1)
        {
            holder.back.setBackgroundColor(ContextCompat.getColor(holder.itemView.context,R.color.colorAccent1))
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }
}