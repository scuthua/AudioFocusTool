package com.flower.audiofocustool

import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btn_request.setOnClickListener { startService("request") }
        btn_abandon.setOnClickListener { startService("abandon") }
    }


    private fun startService(type: String) {
        val intent = Intent(this, AudioFocusService::class.java)
        intent.putExtra("type", type)
        startService(intent)
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            startForegroundService(intent)
//        } else {
//            startService(intent)
//        }
    }
}
