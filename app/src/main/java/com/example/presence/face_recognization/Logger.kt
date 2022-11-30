package com.example.presence.face_recognization

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.presence.face_recognization.Face

// Logs message using log_textview present in face
class Logger {

    companion object {

        @RequiresApi(Build.VERSION_CODES.O)
        fun log(message : String ) {
            if(message=="Identified")
            {
                Face.check()
            }
            else if(message=="Proxy")
            {
                Face.proxy()
            }
            else
            {
                Face.setMessage(  Face.logTextView.text.toString() + "\n" + ">> $message" )
                // To scroll to the last message
                // See this SO answer -> https://stackoverflow.com/a/37806544/10878733
                while ( Face.logTextView.canScrollVertically(1) ) {
                    Face.logTextView.scrollBy(0, 10);
                }
            }

        }

    }
}