package com.zjw.sdkdemo.utils

import android.content.Context
import android.content.Context.AUDIO_SERVICE
import android.media.AudioManager
import android.os.SystemClock
import android.view.KeyEvent

object MusicUtils {

    /**
     * 音乐控制指令
     *
     * @param context
     * @param keyCode
     */
    @JvmStatic
    fun controlMusic(context: Context, keyCode: Int) {
        val eventTime = SystemClock.uptimeMillis()
        val key = KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN, keyCode, 0)
        dispatchMediaKeyToAudioService(context, key)
        dispatchMediaKeyToAudioService(context, KeyEvent.changeAction(key, KeyEvent.ACTION_UP))
    }

    @JvmStatic
    fun dispatchMediaKeyToAudioService(context: Context, event: KeyEvent?) {
        val audioManager = context.getSystemService(AUDIO_SERVICE) as AudioManager?
        if (audioManager != null) {
            try {
                audioManager.dispatchMediaKeyEvent(event)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}