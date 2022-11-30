package com.example.presence

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import com.example.presence.authentication.LoginActivity
import android.content.Context
import android.net.ConnectivityManager
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.getSystemService


class SplashScreenActivity : AppCompatActivity() {

    private var SPLASH_SCREEN_TIME : Long = 6500
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_splash_screen)
        Handler(Looper.myLooper()!!).postDelayed({
            if(!isInternetAvailable())
            {
                AlertDialog.Builder( this ).apply {
                    setTitle("No Internet Connection")
                    setMessage("Please check your internet connection and try again.")
                    setCancelable( false )
                    setPositiveButton( "OK" ) { dialog, which ->
                        dialog.dismiss()
                        finishAffinity()
                    }
                    create()
                }.show()
            }
            else
            {
                intent= Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }, SPLASH_SCREEN_TIME)
    }
    private fun isInternetAvailable(): Boolean {
        var activity=this
        val connectivityManager = activity?.applicationContext?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

}