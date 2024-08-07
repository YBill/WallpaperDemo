package com.bill.wallpaperdemo

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    fun onWallpaper(view: View) {
        startActivity(Intent(this, WallpaperActivity::class.java))
    }

    fun onVideoWallpaper(view: View) {
        startActivity(Intent(this, VideoLiveWallpaperActivity::class.java))
    }

    fun onCameraWallpaper(view: View) {
        startActivity(Intent(this, CameraLiveWallpaperActivity::class.java))
    }

}