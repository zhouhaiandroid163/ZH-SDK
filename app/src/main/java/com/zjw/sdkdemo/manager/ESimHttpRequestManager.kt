package com.zjw.sdkdemo.manager

import android.content.Context
import android.util.Log
import com.zjw.sdkdemo.utils.AssetUtils
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import java.security.KeyStore
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager
import kotlin.concurrent.Volatile

class ESimHttpRequestManager(httpResponseListener: HttpResponseListener, context: Context) {
    val codeOk: Int = 200
    val codeNoContent: Int = 204
    val contentTypeJson: String = "application/json"

    private val mHttpResponseListener: HttpResponseListener

    private val httpClient: OkHttpClient?

    private val retryInterceptor: RetryInterceptor

    companion object {
        private val tag: String = ESimHttpRequestManager::class.java.simpleName

        @Volatile
        private var instance: ESimHttpRequestManager? = null
        fun getInstance(httpResponseListener: HttpResponseListener, context: Context): ESimHttpRequestManager? {
            if (instance == null) {
                synchronized(ESimHttpRequestManager::class.java) {
                    if (instance == null) {
                        instance = ESimHttpRequestManager(httpResponseListener, context)
                    }
                }
            }
            return instance
        }


        fun stringToHeaders(headersString: String): Headers {
            val builder = Headers.Builder()
            val headers = headersString.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            for (header in headers) {
                val parts = header.split(": ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                if (parts.size > 1) {
                    builder.add(parts[0], parts[1])
                }
            }
            return builder.build()
        }
    }

    init {
        var context = context
        mHttpResponseListener = httpResponseListener
        context = context.applicationContext

        retryInterceptor = RetryInterceptor()
        httpClient = getHttpClientWithCertificate(context)
    }

    interface HttpResponseListener {
        fun onSuccess(responseBodyInfo: String, code: Int)
        fun onFailed(errorInfo: String, code: Int)
    }

    @Throws(MalformedURLException::class)
    fun callHttpRequest(uri: String, headers: String, body: String, control: Byte) {
        Log.i(tag, "callHttpRequest")
        val completeUrl = URL(uri)

        val mediaType = contentTypeJson.toMediaTypeOrNull()
        val requestBody = mediaType?.let { body.toRequestBody(it) }

        val requestBuilder = Request.Builder().post(requestBody!!).url(completeUrl.toString()).headers(stringToHeaders(headers))

        val request = requestBuilder.build()

        if (control.toInt() == 0x08) {
            httpClient!!.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    mHttpResponseListener.onFailed(e.toString(), 404)
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    Log.e(tag, "code: " + response.code)
                    Log.e(tag, "onResponse: $response")

                    //{"header":{"functionExecutionStatus":{"status":"Failed","statusCodeData":{"subjectCode":"8.1.1","reasonCode":"3.8","subjectIdentifier":"89033023426300000000019675655040","message":"The EID is not the same between reservation and request."}}}}
                    when (response.code) {
                        codeOk -> if (response.body != null) {
                            Log.i(tag, "Https onResponse")
                            mHttpResponseListener.onSuccess(response.body!!.string(), response.code)
                        }

                        codeNoContent -> {
                            mHttpResponseListener.onSuccess("", response.code)
                            Log.i(tag, "response code:" + response.code)
                        }

                        else -> {
                            mHttpResponseListener.onFailed("call::Unexpected status code" + response.code, response.code)
                            Log.e(tag, "call::Unexpected status code" + response.code)
                        }
                    }
                }
            })
        }
    }


    private fun getHttpClientWithCertificate(context: Context): OkHttpClient? {
        Log.i(tag, "getHttpClientWithCertificate")
        val mOkHttpClient = OkHttpClient.Builder()
        try {
            val mCertificateFactory = CertificateFactory.getInstance("X.509")
            val certInputStream = context.assets.open(AssetUtils.ASS_APRICOT_ESIM_FOLDER + "Symantec_GSMA_RSPv2-Root-CI1.crt")
            var mCertificate: Certificate
            try {
                mCertificate = mCertificateFactory.generateCertificate(certInputStream)
                Log.i(tag, "Certificate:$mCertificate")
            } finally {
                certInputStream.close()
            }

            // Create the Keystore and import the certificate
            val keyStoreType = KeyStore.getDefaultType()
            val keyStore = KeyStore.getInstance(keyStoreType)
            keyStore.load(null, null)
            keyStore.setCertificateEntry("ca", mCertificate)

            // Create the TrustManager, which checks whether you trust the server's certificate
            val tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm()
            val tmf = TrustManagerFactory.getInstance(tmfAlgorithm)
            tmf.init(keyStore)

            // Create the SocketFactory required for the TLS connection
            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, tmf.trustManagers, null)

            mOkHttpClient.addInterceptor(retryInterceptor)
            mOkHttpClient.sslSocketFactory(sslContext.socketFactory, (tmf.trustManagers[0] as X509TrustManager?)!!)
            return mOkHttpClient.build()
        } catch (e: Exception) {
            Log.e(tag, "getHttpClientWithCertificate: " + e.message, e)
        }

        return null
    }

    private class RetryInterceptor : Interceptor {
        private var retryCount = 0

        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            Log.i(tag, "intercept")
            val request = chain.request()
            var response: Response? = null
            var exception: IOException? = null

            // Tries to execute the request and tries to retry if it returns an empty value or an exception
            while (response == null && retryCount < MAX_RETRY_COUNT) {
                try {
                    response = chain.proceed(request)
                } catch (e: IOException) {
                    exception = e
                    retryCount++
                    Log.e(tag, "Request error, retryCount=$retryCount")
                    Log.e(tag, "Request error, retryCount=$retryCount")

                    try {
                        Thread.sleep(1000) // Sleep for 1 second and then try the request again
                    } catch (_: InterruptedException) {
                        break
                    }
                }
            }

            if (response == null && exception != null) {
                throw exception
            }
            return response!!
        }

        companion object {
            private const val MAX_RETRY_COUNT = 3 // Maximum diy_resource of retries
        }

    }

}