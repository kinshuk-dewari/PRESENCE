package com.example.presence

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout

class FAQ : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_faq)

        findViewById<WebView>(R.id.webView).loadUrl("file:///android_asset/FAQs.html")
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}