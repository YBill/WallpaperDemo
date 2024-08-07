package com.bill.wallpaperdemo

import android.app.Activity
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.service.wallpaper.WallpaperService
import android.util.Log
import android.view.SurfaceHolder
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

/**
 * author ywb
 * date 2024/8/7
 * desc
 */
class VideoLiveWallpaperService : WallpaperService() {

    companion object {
        private const val TAG = "VideoLiveWallpaperService"

        fun closeWallpaper(context: Context?) {
            try {
                WallpaperManager.getInstance(context).clear()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun previewWallPaper(activity: AppCompatActivity?, rawId: Int) {
            Log.d(TAG, "previewWallPaper")
            val entity = WallpaperEntity()
            entity.rawRes = rawId
            WallpaperData.setPreviewData(entity)
            startNewWallpaper(activity)
        }

        private fun startNewWallpaper(activity: AppCompatActivity?) {
            activity?.let {
                val activityResultLauncher = it.registerForActivityResult(
                    ActivityResultContracts.StartActivityForResult()
                ) { result ->
                    if (result.resultCode == Activity.RESULT_OK) {
                        setWallPaper(activity)
                    } else if (result.resultCode == Activity.RESULT_CANCELED) {
                        activity.finish()
                    }
                }
                val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
                intent.putExtra(
                    WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, ComponentName(it, VideoLiveWallpaperService::class.java)
                )
                activityResultLauncher.launch(intent)
            }
        }

        private fun setWallPaper(context: Context?) {
            Log.d(TAG, "setWallPaper")
            WallpaperData.getPreviewData()?.let {
                WallpaperData.setCurrentData(it)
            }
            WallpaperData.setPreviewData(null)
            if (context is Activity) {
                Toast.makeText(context, "Wallpaper set successfully", Toast.LENGTH_SHORT).show()
                context.finish()
            }
        }

    }

    override fun onCreateEngine(): Engine {
        return VideoEngine()
    }

    private inner class VideoEngine : Engine(), MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

        private var mPlayer: MediaPlayer? = null
        private var mVolume: Boolean // 是否有声音 false:无声
        private var isPapered = false

        private var wallpaperEntity: WallpaperEntity? = null

        init {
            mVolume = false
        }

        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)
        }

        override fun onDestroy() {
            super.onDestroy()
        }

        override fun onVisibilityChanged(visible: Boolean) {
            Log.d(TAG, "onVisibilityChanged: isPapered = $isPapered, visible = $visible")

            if (!isPapered) {
                return
            }
            try {
                if (visible) {
                    mPlayer?.start()
                } else {
                    mPlayer?.pause()
                }
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }
        }

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            super.onSurfaceCreated(holder)
            Log.d(TAG, "onSurfaceCreated")
            setVideo()
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            Log.d(TAG, "onSurfaceDestroyed")
            mPlayer?.apply {
                try {
                    stop()
                    release()
                } catch (e: IllegalStateException) {
                    e.printStackTrace()
                }
            }
            mPlayer = null
        }

        override fun onPrepared(mp: MediaPlayer?) {
            Log.d(TAG, "onPrepared")
            isPapered = true
            try {
                mp?.start()
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }
        }

        override fun onCompletion(mp: MediaPlayer?) {
            Log.d(TAG, "onCompletion")
            closeWallpaper(applicationContext)
        }

        override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
            Log.e(TAG, "onError: what = $what, extra = $extra")
            closeWallpaper(applicationContext)
            return true
        }

        private fun setVideo() {
            Log.d(TAG, "videoPath")
            loadWallpaperData()
            if (mPlayer == null) mPlayer = MediaPlayer()
            mPlayer?.let {
                try {
                    it.reset()
                    isPapered = false
                    it.setOnPreparedListener(this)
                    it.setOnCompletionListener(this)
                    it.setOnErrorListener(this)
                    it.isLooping = true
                    it.setSurface(surfaceHolder.surface)
                    setVolume()
                    val rawUri: Uri = buildRawUri(
                        applicationContext.packageName, R.raw.flower
                    )
                    it.setDataSource(applicationContext, rawUri)
                    it.prepareAsync()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        private fun loadWallpaperData() {
            if (isPreview) {
                wallpaperEntity = WallpaperData.getPreviewData()
            } else {
                wallpaperEntity = WallpaperData.getCurrentData()
            }
        }

        private fun setVolume() {
            mPlayer?.apply {
                try {
                    if (mVolume) {
                        setVolume(1.0f, 1.0f)
                    } else {
                        setVolume(0f, 0f)
                    }
                } catch (e: IllegalStateException) {
                    e.printStackTrace()
                }
            }
        }

        private fun buildRawUri(packageName: String, rawId: Int): Uri {
            return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + packageName + "/" + rawId)
        }

    }
}