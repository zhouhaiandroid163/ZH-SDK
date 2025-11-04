package com.zjw.sdkdemo.utils

import android.util.Log
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.FileIOUtils
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.PathUtils
import com.blankj.utilcode.util.ThreadUtils
import com.blankj.utilcode.util.TimeUtils
import com.blankj.utilcode.util.UriUtils
import com.blankj.utilcode.util.ZipUtils
import com.sifli.siflidfu.DFUImagePath
import com.sifli.siflidfu.Protocol
import java.io.File
import java.util.Locale

object MyFileUtils {
    private val tag: String = FileUtils::class.java.simpleName

    private val sifliPhotoCacheFilePath = PathUtils.getExternalAppFilesPath() + "/sifliDial/sifliPhoto.zip"
    private val sifliOtaCacheFolderPath = PathUtils.getExternalAppFilesPath() + "/sifliOta/"

    @JvmStatic
    fun saveSifliPhotoCacheFile(byteArray: ByteArray?): File {
        FileUtils.delete(sifliPhotoCacheFilePath)
        FileIOUtils.writeFileFromIS(sifliPhotoCacheFilePath, ConvertUtils.bytes2InputStream(byteArray))
        return File(sifliPhotoCacheFilePath)
    }

    //region 处理文件
    @JvmStatic
    fun saveSifliOtaCacheFile(zipFile: File): ArrayList<DFUImagePath> {
        val fileList = ArrayList<DFUImagePath>()

        try {
            //创建目录
            FileUtils.createOrExistsDir(sifliOtaCacheFolderPath)
            //清除已使用的文件
            FileUtils.deleteAllInDir(sifliOtaCacheFolderPath)

            //解压文件
            ZipUtils.unzipFile(zipFile, FileUtils.getFileByPath(sifliOtaCacheFolderPath))
            //遍历文件
            val result = FileUtils.listFilesInDir(sifliOtaCacheFolderPath, true)

            fileList.clear()
            for (file in result) {
                //填充文件
                val name: String = FileUtils.getFileNameNoExtension(file)
                var dfuFile: DFUImagePath? = null
                if (name.uppercase(Locale.ENGLISH).contains("ctrl_packet".uppercase(Locale.ENGLISH))) {
                    dfuFile = DFUImagePath(null, UriUtils.file2Uri(file), Protocol.IMAGE_ID_CTRL)
                } else if (name.uppercase(Locale.ENGLISH).contains("outapp".uppercase(Locale.ENGLISH)) || name.uppercase(Locale.ENGLISH).contains("outcom_app".uppercase(Locale.ENGLISH))) {
                    dfuFile = DFUImagePath(null, UriUtils.file2Uri(file), Protocol.IMAGE_ID_HCPU)
                } else if (name.uppercase(Locale.ENGLISH).contains("outex".uppercase(Locale.ENGLISH)) || name.uppercase(Locale.ENGLISH).contains("outcom_ex".uppercase(Locale.ENGLISH))) {
                    dfuFile = DFUImagePath(null, UriUtils.file2Uri(file), Protocol.IMAGE_ID_EX)
                } else if (name.uppercase(Locale.ENGLISH).contains("outfont".uppercase(Locale.ENGLISH)) || name.uppercase(Locale.ENGLISH).contains("outcom_font".uppercase(Locale.ENGLISH))) {
                    dfuFile = DFUImagePath(null, UriUtils.file2Uri(file), Protocol.IMAGE_ID_FONT)
                } else if (name.uppercase(Locale.ENGLISH).contains("outres".uppercase(Locale.ENGLISH)) || name.uppercase(Locale.ENGLISH).contains("outcom_res".uppercase(Locale.ENGLISH))) {
                    dfuFile = DFUImagePath(null, UriUtils.file2Uri(file), Protocol.IMAGE_ID_RES)
                }
                if (dfuFile != null) {
                    fileList.add(dfuFile)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(tag, "saveFileAndUnZip Exception e=${e.message}")

        }
        return fileList
    }

    @JvmStatic
    fun saveLog(logPath: String, content: String) {
        ThreadUtils.executeBySingle(object : ThreadUtils.Task<String>() {
            override fun doInBackground(): String {
                val buffer = StringBuffer()
                buffer.append(TimeUtils.getNowString(TimeUtils.getSafeDateFormat("MM-dd HH:mm:ss.SSS")))
                buffer.append("  ------>  ")
                buffer.append(content)
                buffer.append("\n")
                FileIOUtils.writeFileFromString(logPath, buffer.toString(), true)
                return content
            }

            override fun onSuccess(result: String?) {
                LogUtils.d("日志保存成功 --> $result")
            }

            override fun onCancel() {
            }

            override fun onFail(t: Throwable?) {
                LogUtils.e("日志保存失败 ---> ${t?.localizedMessage}")
            }
        })
    }
}