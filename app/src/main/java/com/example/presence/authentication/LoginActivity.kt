package com.example.presence.authentication

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.presence.bottom_navigation.Home
import com.example.presence.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.layout_login.*


class LoginActivity : AppCompatActivity() {
    private var MAGIC_CLOAK : Long = 1500
    private lateinit var bounce: Animation
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        bounce = AnimationUtils.loadAnimation(
            this, R.anim.bounce)

        val scroll=findViewById<ScrollView>(R.id.scrollView)
        ObjectAnimator.ofInt(scroll,"scrollY",0,1700).apply {
            duration=1000
            startDelay=500
        }.start()

        Handler(Looper.myLooper()!!).postDelayed({
            findViewById<TextView>(R.id.magic_cloak0).visibility= View.GONE
            findViewById<TextView>(R.id.magic_cloak1).visibility= View.GONE
            findViewById<TextView>(R.id.magic_cloak2).visibility= View.GONE
            findViewById<TextView>(R.id.magic_cloak3).visibility= View.GONE
            findViewById<TextView>(R.id.magic_cloak4).visibility= View.GONE
        },MAGIC_CLOAK)

        val email=intent.getStringExtra("em")
        val password=intent.getStringExtra("ps")


        findViewById<EditText>(R.id.email).setText(email)
        findViewById<EditText>(R.id.password).setText(password)


        val textView = findViewById<TextView>(R.id.doNotHaveAccount)
        textView.setOnClickListener{
            val intent = Intent(this , Register :: class.java)
            startActivity(intent)
            overridePendingTransition(
                androidx.appcompat.R.anim.abc_slide_in_top,
                androidx.appcompat.R.anim.abc_slide_out_bottom)
            finish()
        }

        forgotPassword.setOnClickListener{
            startActivity(Intent(this@LoginActivity, ForgotPasswordActivity::class.java))

        }

        val button=findViewById<Button>(R.id.signIn)
        button.setOnClickListener{
            button.startAnimation(bounce)
            var count=0
            if(TextUtils.isEmpty(findViewById<EditText>(R.id.email).text.toString())){
                count+=1
                Toast.makeText(this@LoginActivity, "Email Address required",Toast.LENGTH_SHORT).show()
            }
            if(TextUtils.isEmpty(findViewById<EditText>(R.id.password).text.toString())){
                count+=1
                Toast.makeText(this@LoginActivity,"Password required",Toast.LENGTH_SHORT).show()
            }
            if(count==0)
            {
                val em=findViewById<EditText>(R.id.email).text.toString()
                val pass=findViewById<EditText>(R.id.password).text.toString()

                FirebaseAuth.getInstance().signInWithEmailAndPassword(em,pass).addOnCompleteListener { task ->
                    if(task.isSuccessful) {
                        val firebaseUser: FirebaseUser = task.result!!.user!!
                        val databaseReference: DatabaseReference = FirebaseDatabase.getInstance("https://presence-23cbb-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Employer")
                        databaseReference.child(firebaseUser.uid).get().addOnSuccessListener {
                            if(it.child("status").value=="No")
                            {
                                Toast.makeText(this, "Your Account is not verified!", Toast.LENGTH_SHORT).show()
                                FirebaseAuth.getInstance().signOut()
                            }
                            else
                            {
                                Toast.makeText(this@LoginActivity,"Login Successful.",Toast.LENGTH_SHORT).show()
                                val intent= Intent(this, Home::class.java)
                                intent.flags= Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                overridePendingTransition(R.anim.fade_in,R.anim.fade_out)
                                finish()
                            }
                        }
                    }
                    else
                    {
                        Toast.makeText(this@LoginActivity,task.exception!!.message.toString(),Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if(FirebaseAuth.getInstance().currentUser != null){
            startActivity(Intent(this@LoginActivity, Home::class.java))
            finish()
        }
    }

}