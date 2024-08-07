package com.bill.wallpaperdemo

/**
 * author ywb
 * date 2024/8/7
 * desc
 */
object WallpaperData {

    private var currentData: WallpaperEntity? = null
    private var previewData: WallpaperEntity? = null

    fun getCurrentData(): WallpaperEntity? {
        return currentData
    }

    fun setCurrentData(data: WallpaperEntity?) {
        currentData = data
    }

    fun getPreviewData(): WallpaperEntity? {
        return previewData
    }

    fun setPreviewData(data: WallpaperEntity?) {
        previewData = data
    }

}