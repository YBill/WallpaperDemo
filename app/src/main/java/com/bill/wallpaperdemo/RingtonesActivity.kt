package com.bill.wallpaperdemo

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class RingtonesActivity : AppCompatActivity() {

    private lateinit var permissionLauncher: ActivityResultLauncher<Intent>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_ringtones)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        permissionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            // it.resultCode 不表示用户是否授权，只表示用户是否关闭了权限请求界面
            Log.d("YBill", "registerForActivityResult ${it.resultCode}")
        }
    }

    fun handleSetRingtone(view: View) {
        permissionLauncher.launch(Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS))
    }
    fun handleSetNotification(view: View) {}
    fun handleSetAlarmSound(view: View) {}
    fun handleSetContactRingtone(view: View) {}
}