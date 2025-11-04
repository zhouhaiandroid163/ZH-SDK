package com.zjw.sdkdemo.utils

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.widget.AppCompatTextView
import com.blankj.utilcode.util.FileIOUtils
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.PathUtils
import com.blankj.utilcode.util.TimeUtils
import com.zjw.sdkdemo.function.MainActivity
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

object LogSendUtil {
    private val tag: String = LogSendUtil::class.java.simpleName

    private val logPath = PathUtils.getAppDataPathExternalFirst() + "/log/"

    fun sendText(title: String, logTextView: AppCompatTextView) {

        val requestJson = getSendMsgData(title, logTextView)
        if (requestJson.isEmpty()) {
            Log.i(tag, "sendLog requestJson is null")
            return
        }

        getToken(object : Callback {
            override fun onSuccess(response: String) {
                Log.i(tag, "getToken onSuccess")

                val token = JSONObject(response).getString("app_access_token")
                if (token.isEmpty()) {
                    Log.i(tag, "getToken token is null")
                    return
                }

                sendMsg(requestJson, token, object : Callback {
                    override fun onSuccess(response: String) {
                        Log.i(tag, "sendMsg onSuccess")
                    }

                    override fun onFailure(e: Exception) {
                        Log.i(tag, "getToken onFailure e=${e.toString()}")
                        e.printStackTrace()
                    }
                })

            }

            override fun onFailure(e: Exception) {
                Log.i(tag, "getToken onFailure e=${e.toString()}")
                e.printStackTrace()
            }
        })
    }

    fun sendLogFile(title: String, logTextView: AppCompatTextView) {
        val inputData = logTextView.text.toString()
        if (inputData.isEmpty()) {
            Log.i(tag, "sendFile inputData is null")
            return
        }

        getToken(object : Callback {
            override fun onSuccess(response: String) {
                Log.i(tag, "getToken onSuccess")

                val token = JSONObject(response).getString("app_access_token")
                if (token.isEmpty()) {
                    Log.i(tag, "getToken token is null")
                    return
                }

                val time = TimeUtils.getNowString(TimeUtils.getSafeDateFormat("yyyy_MM_dd_HH_mm_ss"))
                val fileName = "$title$time.java"
                val filePath = logPath + fileName

                uploadFile(fileName, filePath, token, object : Callback {
                    override fun onSuccess(response: String) {
                        val fileKey = JSONObject(JSONObject(response).getString("data").toString()).getString("file_key")
                        Log.i(tag, "fileKey=$fileKey")
                        val requestJson = getSendFileData(fileKey)
                        Log.i(tag, "requestJson=$requestJson")

                        sendMsg(requestJson, token, object : Callback {
                            override fun onSuccess(response: String) {
                                println("发送成功，响应: $response")
                            }

                            override fun onFailure(e: Exception) {
                                Log.i(tag, "getToken onFailure e=${e.toString()}")
                                e.printStackTrace()
                            }
                        })

                    }

                    override fun onFailure(e: Exception) {
                        Log.i(tag, "uploadFile onFailure e=${e.toString()}")
                    }
                })


            }

            override fun onFailure(e: Exception) {
                Log.i(tag, "getToken onFailure e=${e.toString()}")
                e.printStackTrace()
            }
        })
    }

    fun sendFile(title: String, logStr: StringBuffer) {
        if (logStr.isEmpty()) {
            Log.i(tag, "sendFile logStr is null")
            return
        }

        getToken(object : Callback {
            override fun onSuccess(response: String) {
                Log.i(tag, "getToken onSuccess")

                val token = JSONObject(response).getString("app_access_token")
                if (token.isEmpty()) {
                    Log.i(tag, "getToken token is null")
                    return
                }

                val time = TimeUtils.getNowString(TimeUtils.getSafeDateFormat("yyyy_MM_dd_HH_mm_ss"))
                val fileName = "$title$time.java"
                val filePath = logPath + fileName
                FileIOUtils.writeFileFromString(filePath, logStr.toString())

                uploadFile(fileName, filePath, token, object : Callback {
                    override fun onSuccess(response: String) {
                        val fileKey = JSONObject(JSONObject(response).getString("data").toString()).getString("file_key")
                        Log.i(tag, "fileKey=$fileKey")
                        val requestJson = getSendFileData(fileKey)
                        Log.i(tag, "requestJson=$requestJson")

                        sendMsg(requestJson, token, object : Callback {
                            override fun onSuccess(response: String) {
                                println("发送成功，响应: $response")
                            }

                            override fun onFailure(e: Exception) {
                                Log.i(tag, "getToken onFailure e=${e.toString()}")
                                e.printStackTrace()
                            }
                        })

                    }

                    override fun onFailure(e: Exception) {
                        Log.i(tag, "uploadFile onFailure e=${e.toString()}")
                    }
                })


            }

            override fun onFailure(e: Exception) {
                Log.i(tag, "getToken onFailure e=${e.toString()}")
                e.printStackTrace()
            }
        })


    }

    fun sendLogDataFile(title: String, logArr: JSONArray) {
        if (logArr.length() <= 0) {
            Log.i(tag, "sendHtmlFile logArr is null")
            return
        }
        val deviceContent = "deviceInfoCallBack onDeviceInfo data=${GsonUtils.toJson(MainActivity.GlobalData.deviceInfo)}"
        logArr.put(0, JSONObject().put("time", MyFormatUtils.getTime()).put("content", deviceContent))

        getToken(object : Callback {
            override fun onSuccess(response: String) {
                Log.i(tag, "getToken onSuccess")

                val token = JSONObject(response).getString("app_access_token")
                if (token.isEmpty()) {
                    Log.i(tag, "getToken token is null")
                    return
                }

                getDocText(token, object : Callback {
                    override fun onSuccess(response: String) {
                        Log.i(tag, "getDocText onSuccess response=$response")

                        val dataStr = JSONObject(response).getString("data")
                        if (dataStr.isEmpty()) {
                            Log.i(tag, "getDocText dataStr is null")
                            return
                        }
                        val docArr = getDocJson(dataStr)


                        val logData = JSONObject()
                        logData.put("logArr", logArr)
                        logData.put("docArr", docArr)

                        val time = TimeUtils.getNowString(TimeUtils.getSafeDateFormat("yyyy_MM_dd_HH_mm_ss"))
                        val fileName = "$title$time.data"
                        val filePath = logPath + fileName
                        FileIOUtils.writeFileFromString(filePath, logData.toString())

                        uploadFile(fileName, filePath, token, object : Callback {
                            override fun onSuccess(response: String) {
                                val fileKey = JSONObject(JSONObject(response).getString("data").toString()).getString("file_key")
                                Log.i(tag, "fileKey=$fileKey")
                                val requestJson = getSendFileData(fileKey)
                                Log.i(tag, "requestJson=$requestJson")

                                sendMsg(requestJson, token, object : Callback {
                                    override fun onSuccess(response: String) {
                                        println("发送成功，响应: $response")
                                    }

                                    override fun onFailure(e: Exception) {
                                        Log.i(tag, "getToken onFailure e=${e.toString()}")
                                        e.printStackTrace()
                                    }
                                })

                            }

                            override fun onFailure(e: Exception) {
                                Log.i(tag, "uploadFile onFailure e=${e.toString()}")
                            }
                        })

                    }

                    override fun onFailure(e: Exception) {
                        Log.i(tag, "getDocText onFailure e=${e.toString()}")
                    }
                })
            }

            override fun onFailure(e: Exception) {
                Log.i(tag, "getToken onFailure e=${e.toString()}")
                e.printStackTrace()
            }
        })
    }

    fun getSendFileData(fileKey: String): String {
        return """{"receive_id": "${SpUtils.getLogUserID()}","content": "{\"file_key\":\"$fileKey\"}","msg_type": "file"}""".trimIndent()
    }

    fun getSendMsgData(title: String, logTextView: AppCompatTextView): String {
        val userID = SpUtils.getLogUserID()
        val inputData = logTextView.text.toString()
        if (inputData.isEmpty()) {
            return ""
        }
        val dataList = inputData.split("\n")
        val zhObject = JSONObject()
        val titleObject = JSONObject()
        titleObject.put("title", title)
        val contentArray = JSONArray()
        for (data in dataList) {
            val newData = data.replace("\"", "")
            contentArray.put(JSONArray().put(JSONObject().put("tag", "text").put("text", newData)))
        }
        titleObject.put("content", contentArray)
        zhObject.put("zh_cn", titleObject)
        val zhJson = zhObject.toString()

        val myData = zhJson.replace("\"", "\\\"")
        val jsonData = """
        {
	    "receive_id": "$userID",
	    "content": "$myData",
	    "msg_type": "post"
        }
        """.trimIndent()
        return jsonData
    }

    interface Callback {
        fun onSuccess(response: String)
        fun onFailure(e: Exception)
    }

    fun getToken(callback: Callback) {
        Thread {
            var connection: HttpURLConnection? = null
            try {
                val jsonBody = JSONObject().apply {
                    put("app_id", "cli_a865d0def03bd00b")
                    put("app_secret", "2ZkQKeOfcqCpKIY2VmTbUh0AsF8fOzIg")
                }.toString()

                val url = URL("https://open.feishu.cn/open-apis/auth/v3/app_access_token/internal")
                connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8")
                connection.setRequestProperty("Accept", "application/json")
                connection.doOutput = true
                connection.doInput = true

                DataOutputStream(connection.outputStream).use { outputStream ->
                    outputStream.write(jsonBody.toByteArray(StandardCharsets.UTF_8))
                    outputStream.flush()
                }

                val responseCode = connection.responseCode
                val inputStream = if (responseCode == HttpURLConnection.HTTP_OK) {
                    connection.inputStream
                } else {
                    connection.errorStream
                }

                BufferedReader(InputStreamReader(inputStream, StandardCharsets.UTF_8)).use { reader ->
                    val response = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        response.append(line)
                    }
                    Handler(Looper.getMainLooper()).post {
                        if (responseCode == HttpURLConnection.HTTP_OK) {
                            callback.onSuccess(response.toString())
                        } else {
                            callback.onFailure(IOException("HTTP error code: $responseCode, response: $response"))
                        }
                    }
                }
            } catch (e: Exception) {
                Handler(Looper.getMainLooper()).post {
                    callback.onFailure(e)
                }
            } finally {
                connection?.disconnect()
            }
        }.start()
    }

    fun uploadFile(fileName: String, filePath: String, token: String, callback: Callback) {
        Thread {
            val client = OkHttpClient()
            try {
                val file = File(filePath)
                if (!file.exists() || !file.isFile) {
                    callback.onFailure(IOException("file is null path=$filePath"))
                    return@Thread
                }
                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file_type", "stream")
                    .addFormDataPart("file_name", fileName)
                    .addFormDataPart("file", fileName, RequestBody.create(getMimeType(filePath).toMediaTypeOrNull(), file))
                    .build()

                val request = Request.Builder()
                    .url("https://open.feishu.cn/open-apis/im/v1/files")
                    .addHeader("Authorization", "Bearer $token")
                    .addHeader("Content-Type", "multipart/form-data")
                    .post(requestBody)
                    .build()

                client.newCall(request).enqueue(object : okhttp3.Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        callback.onFailure(IOException("HTTP error message: ${e.message}"))
                    }

                    override fun onResponse(call: Call, response: Response) {
                        val responseBody = response.body?.string() ?: ""
                        if (response.isSuccessful) {
                            callback.onSuccess(responseBody)
                        } else {
                            callback.onFailure(IOException("HTTP error code: $${response.code}, response: $responseBody"))
                        }
                    }
                })

            } catch (e: Exception) {
                Handler(Looper.getMainLooper()).post {
                    callback.onFailure(e)
                }
            }
        }.start()
    }

    fun sendMsg(jsonData: String, token: String, callback: Callback) {
        Thread {
            var connection: HttpURLConnection? = null
            try {
                val url = URL("https://open.feishu.cn/open-apis/im/v1/messages?receive_id_type=user_id")
                connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8")
                connection.setRequestProperty("Accept", "application/json")
                connection.setRequestProperty("Authorization", "Bearer $token")
                connection.doOutput = true
                connection.doInput = true
                DataOutputStream(connection.outputStream).use { outputStream ->
                    outputStream.write(jsonData.toByteArray(StandardCharsets.UTF_8))
                    outputStream.flush()
                }
                val responseCode = connection.responseCode
                val inputStream = if (responseCode == HttpURLConnection.HTTP_OK) {
                    connection.inputStream
                } else {
                    connection.errorStream
                }

                BufferedReader(InputStreamReader(inputStream, StandardCharsets.UTF_8)).use { reader ->
                    val response = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        response.append(line)
                    }
                    Handler(Looper.getMainLooper()).post {
                        if (responseCode == HttpURLConnection.HTTP_OK) {
                            callback.onSuccess(response.toString())
                        } else {
                            callback.onFailure(IOException("HTTP error code: $responseCode, response: $response"))
                        }
                    }
                }
            } catch (e: Exception) {
                Handler(Looper.getMainLooper()).post {
                    callback.onFailure(e)
                }
            } finally {
                connection?.disconnect()
            }
        }.start()
    }

    fun getDocText(token: String, callback: Callback) {
        Log.i(tag, "token=$token")
        Thread {
            val docId = "DuVEdRBAuoLvCPxv9njcV0B4nyf"
            //
            val url = "https://open.feishu.cn/open-apis/docx/v1/documents/$docId/raw_content?lang=0"
            val client = OkHttpClient()
            val request = Request.Builder().url(url).addHeader("Authorization", "Bearer $token")
                .addHeader("Content-Type", "multipart/form-data")
                .get().build()
            try {
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        callback.onSuccess(responseBody.toString())
                    } else {
                        throw IOException("request fail: ${response.code} ${response.message}")
                    }
                }
            } catch (e: Exception) {
                Handler(Looper.getMainLooper()).post {
                    callback.onFailure(e)
                }
            }
        }.start()
    }

    private fun getDocJson(docStr: String): JSONArray {
        val listData = docStr.split("\\n")

        val dataArr = JSONArray()

        val dataLen = listData.size
//        val dataLen = 200
        for (pos in 0..dataLen - 1) {
            val data = listData[pos]
            if (data.startsWith("public interface") || data.startsWith("public class")) {

                var strData = ""
                for (i in pos..dataLen - 1) {
                    strData += listData[i] + "\n"
                    if (listData[i] == "}") {
                        break
                    }
                }
                val title = listData[pos - 2]
                val type = listData[pos - 1]
                val dataObject = JSONObject()
                dataObject.put("title", title)
                dataObject.put("type", type)
                dataObject.put("content", strData)
                dataArr.put(dataObject)
//                Log.i(tag, title)
//                Log.i(tag, type)
//                Log.i(tag, strData)

            } else if (data.startsWith("public")) {
                var strData = ""
                var dataCount = 0
                for (i in pos downTo 0) {
                    if (listData[i] == "method") {
                        break
                    }
                    dataCount++
                    strData = listData[i] + "\n" + strData
                }
                val title = listData[pos - dataCount - 1]
                val type = listData[pos - dataCount]
                val dataObject = JSONObject()
                dataObject.put("title", title)
                dataObject.put("type", type)
                dataObject.put("content", strData)
                dataArr.put(dataObject)
//                Log.i(tag, title)
//                Log.i(tag, type)
//                Log.i(tag, strData)
            }
        }

        return dataArr
    }

    private fun getMimeType(filePath: String): String {
        val extension = filePath.substringAfterLast('.', "").lowercase()
        return when (extension) {
            "txt" -> "text/plain"
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "pdf" -> "application/pdf"
            "doc", "docx" -> "application/msword"
            "xls", "xlsx" -> "application/vnd.ms-excel"
            else -> "application/octet-stream"
        }
    }
}
