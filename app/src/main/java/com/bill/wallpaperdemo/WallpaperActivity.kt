package com.bill.wallpaperdemo

import android.Manifest
import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.io.IOException

class WallpaperActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "WallpaperActivity_TAG"
    }

    private var infoTv: AppCompatTextView? = null
    private var wallpaperIv: AppCompatImageView? = null
    private var mBitmap: Bitmap? = null

    private lateinit var storagePermissionLauncher: ActivityResultLauncher<String>
    private lateinit var manageExternalStoragePermissionLauncher: ActivityResultLauncher<Intent>

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_wallpaper)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        infoTv = findViewById(R.id.tv_info)
        wallpaperIv = findViewById(R.id.iv_wallpaper)

        infoTv?.text = "Android ${Build.VERSION.RELEASE}"
        infoTv?.append("\t\tAPI Version ${Build.VERSION.SDK_INT}")

        Log.i(TAG, "Current phone version is ${Build.VERSION.SDK_INT}")

        // 根据wallpaperManager.drawable方法说明，获取wallpaperManager.drawable在
        // Android S 之前需要READ_EXTERNAL_STORAGE权限，从T开始不能获取，
        // 如果想要获取，需要MANAGE_EXTERNAL_STORAGE权限
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S) {
            manageExternalStoragePermissionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                // it.resultCode 不表示用户是否授权，只表示用户是否关闭了权限请求界面
                if (Environment.isExternalStorageManager()) {
                    Log.d(TAG, "User authorization manager external storage permission successful, load wallpaper now")
                    infoTv?.append("\n有所有文件管理权限")
                    loadCurrentWallpaper()
                } else {
                    Log.d(TAG, "User authorization manager external storage permission failed")
                    infoTv?.append("\n没有所有文件管理权限，无法加载系统默认壁纸")
                    Toast.makeText(applicationContext, "No manager external storage permission to read system wallpaper", Toast.LENGTH_LONG).show()
                }
            }
            if (Environment.isExternalStorageManager()) {
                Log.i(TAG, "Check manager external storage permission, already granted, load wallpaper now")
                infoTv?.append("\n有所有文件管理权限")
                loadCurrentWallpaper()
            } else {
                Log.i(TAG, "Check manager external storage permission, and no permission, request it now")
                manageExternalStoragePermissionLauncher.launch(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION))
            }
        } else {
            storagePermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    Log.d(TAG, "User authorization storage permission successful, load wallpaper now")
                    infoTv?.append("\n有文件读取权限")
                    loadCurrentWallpaper()
                } else {
                    Log.d(TAG, "User authorization storage permission failed")
                    infoTv?.append("\n没有文件读取权限，无法加载系统默认壁纸")
                    Toast.makeText(applicationContext, "No storage permission to read system wallpaper", Toast.LENGTH_LONG).show()
                }
            }
            Log.i(TAG, "Check storage permission")
            storagePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    private fun loadCurrentWallpaper() {
        val wallpaperManager = WallpaperManager.getInstance(this)
        try {
            val wallpaperDrawable: Drawable? = wallpaperManager.drawable
            Log.d(TAG, "loadCurrentWallpaper, wallpaperDrawable = $wallpaperDrawable")
            wallpaperIv?.setImageDrawable(wallpaperDrawable)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun onBuiltIn(view: View) {
        mBitmap = BitmapFactory.decodeResource(resources, R.drawable.wallpaper)
        wallpaperIv?.setImageBitmap(mBitmap)
    }

    fun onSetWallpaper(view: View) {
        if (mBitmap != null) {
            setWallpaperBitmap(mBitmap!!)
        } else {
            Toast.makeText(applicationContext, "Please load the image first", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setWallpaperBitmap(bitmap: Bitmap) {
        val wallpaperManager = WallpaperManager.getInstance(this)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            try {
                wallpaperManager.setBitmap(bitmap)
                Toast.makeText(applicationContext, "Wallpaper set successfully", Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else {
            showBottomSheetDialog(bitmap)
        }
    }

    private fun setWallpaperBitmap(bitmap: Bitmap, which: Int) {
        val wallpaperManager = WallpaperManager.getInstance(this)
        try {
            wallpaperManager.setBitmap(bitmap, null, true, which)
            Toast.makeText(applicationContext, "Wallpaper set successfully", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun showBottomSheetDialog(bitmap: Bitmap) {
        val bottomSheetDialog = BottomSheetDialog(this)
        val bottomSheetView: View = layoutInflater.inflate(R.layout.wallpaper_bottom_sheet_dialog, null)
        bottomSheetDialog.setContentView(bottomSheetView)

        bottomSheetView.findViewById<View>(R.id.set_home_screen_button).setOnClickListener {
            setWallpaperBitmap(bitmap, WallpaperManager.FLAG_SYSTEM)
            bottomSheetDialog.dismiss()
        }

        bottomSheetView.findViewById<View>(R.id.set_lock_screen_button).setOnClickListener {
            setWallpaperBitmap(bitmap, WallpaperManager.FLAG_LOCK)
            bottomSheetDialog.dismiss()
        }

        bottomSheetView.findViewById<View>(R.id.set_home_lock_screen_button).setOnClickListener {
            setWallpaperBitmap(bitmap, WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK)
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }
}