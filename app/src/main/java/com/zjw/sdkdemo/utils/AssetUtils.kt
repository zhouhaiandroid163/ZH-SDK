package com.zjw.sdkdemo.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.IOException
import java.io.InputStream

object AssetUtils {
    const val ASS_APRICOT_DIAL_PHOTO_FOLDER = "apricot/dial/photo/"
    const val ASS_APRICOT_DIAL_DIY_V1_FOLDER = "apricot/dial/diy_v1/"
    const val ASS_APRICOT_DIAL_DIY_V2_FOLDER = "apricot/dial/diy_v2/"
    const val ASS_APRICOT_ESIM_FOLDER = "apricot/esim/"

    const val ASS_APRICOT_SIFLI_PHOTO_FOLDER = "apricot/sifli/photo/"

    const val ASS_BERRY_FC_RESOURCE = "berry/fc_resource/"
    const val ASS_BERRY_OFFLINE_MAP_RESOURCE = "berry/offline_map/"

    /**
     * 获取Asset-bin文件
     * Get the Asset-bin file
     */
    @JvmStatic
    fun getAssetBytes(context: Context, fileName: String): ByteArray? {
        var buffer: ByteArray? = null
        try {
            val `is` = context.assets.open(fileName)
            val size = `is`.available()
            buffer = ByteArray(size)
            `is`.read(buffer)
            `is`.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return buffer
    }

    /**
     * 获取Asset文件-图片 Bitmap
     * Obtain Asset file - Image Bitmap
     */
    @JvmStatic
    fun getAssetBitmap(context: Context, fileName: String): Bitmap? {
        var bitmap: Bitmap? = null
        val assetManager = context.assets
        try {
            val inputStream = assetManager.open(fileName)
            bitmap = BitmapFactory.decodeStream(inputStream)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return bitmap
    }

    /**
     * 获取Asset-文件字符串数据
     * Obtain Asset-file string data
     */
    @JvmStatic
    fun getAssetFileContent(context: Context, filePath: String): String? {
        return try {
            val inputStream: InputStream = context.assets.open(filePath)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()

            String(buffer, Charsets.UTF_8)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

}