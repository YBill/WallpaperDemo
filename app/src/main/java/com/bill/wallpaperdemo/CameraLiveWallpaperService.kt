package com.bill.wallpaperdemo

import android.Manifest
import android.app.Activity
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Camera
import android.service.wallpaper.WallpaperService
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.io.IOException

/**
 * author ywb
 * date 2024/8/7
 * desc
 */
class CameraLiveWallpaperService : WallpaperService() {

    companion object {
        private const val TAG = "CameraLiveWallpaperService"

        fun closeWallpaper(context: Context?) {
            try {
                WallpaperManager.getInstance(context).clear()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun previewWallPaper(activity: AppCompatActivity?) {
            Log.d(TAG, "previewWallPaper")
            if (activity == null) {
                return
            }

            val activityResultLauncher = activity.registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    setWallPaper(activity)
                } else if (result.resultCode == Activity.RESULT_CANCELED) {
                    activity.finish()
                }
            }

            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestCameraPermission(activity, activityResultLauncher)
            } else {
                startCameraPreview(activity, activityResultLauncher)
            }

        }

        private fun requestCameraPermission(activity: AppCompatActivity, activityResultLauncher: ActivityResultLauncher<Intent>) {
            val requestCameraPermissionLauncher =
                activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                    if (isGranted) {
                        startCameraPreview(activity, activityResultLauncher)
                    } else {
                        activity.finish()
                    }
                }
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        private fun startCameraPreview(activity: AppCompatActivity, activityResultLauncher: ActivityResultLauncher<Intent>) {
            val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
            intent.putExtra(
                WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, ComponentName(activity.applicationContext, CameraLiveWallpaperService::class.java)
            )
            activityResultLauncher.launch(intent)
        }

        private fun setWallPaper(context: Context?) {
            Log.d(TAG, "setWallPaper")
            if (context is Activity) {
                Toast.makeText(context, "Wallpaper set successfully", Toast.LENGTH_SHORT).show()
                context.finish()
            }
        }

    }

    override fun onCreateEngine(): Engine {
        return CameraEngine()
    }

    private inner class CameraEngine : Engine() {
        private lateinit var camera: Camera

        override fun onVisibilityChanged(visible: Boolean) {
            if (visible) {
                startPreview()
            } else {
                stopPreview()
            }
        }

        override fun onDestroy() {
            super.onDestroy()
            stopPreview()
        }

        private fun startPreview() {
            camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT)
            camera.setDisplayOrientation(90)
            try {
                camera.setPreviewDisplay(surfaceHolder)
                camera.startPreview()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        private fun stopPreview() {
            try {
                camera.stopPreview()
                camera.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }
}