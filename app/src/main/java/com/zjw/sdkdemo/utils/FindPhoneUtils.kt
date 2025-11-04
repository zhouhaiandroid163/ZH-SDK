package com.zjw.sdkdemo.utils

import android.app.AlertDialog
import android.app.Service
import android.content.Context
import android.content.DialogInterface
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Vibrator
import android.util.Log
import com.blankj.utilcode.util.ActivityUtils
import com.zhapp.ble.ControlBleTools
import com.zhapp.ble.callback.MicroCallBack
import com.zhapp.ble.parsing.ParsingStateManager.SendCmdStateListener
import com.zhapp.ble.parsing.SendCmdState
import com.zjw.sdkdemo.R
import com.zjw.sdkdemo.base.BaseApplication

object FindPhoneUtils {
    private val tag: String = FindPhoneUtils::class.java.simpleName

    @JvmStatic
    fun findPhone(mode: Int) {
        if (mode == MicroCallBack.FindPhoneMode.FIND_START.mode) {
            startFindPhone()
        } else if (mode == MicroCallBack.FindPhoneMode.FIND_STOP.mode) {
            stopFindPhone()
        }
    }

    var mMediaPlayer: MediaPlayer? = null
    private var findPhoneDialog: AlertDialog? = null
    private var am: AudioManager? = null
    private var vibrator: Vibrator? = null
    private var volume = 0

    private fun startFindPhone() {
        val mediaUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        if (mMediaPlayer == null) {
            mMediaPlayer = MediaPlayer.create(BaseApplication.mContext, mediaUri)
            mMediaPlayer!!.isLooping = true
            mMediaPlayer!!.start()
        }
        if (am == null) {
            am = BaseApplication.mContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager?
        }
        try {
            volume = am!!.getStreamVolume(AudioManager.STREAM_MUSIC)
            am!!.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                am!!.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
                AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE
            )
        } catch (e: SecurityException) {
            Log.e(tag, "startFindPhone: " + e.message, e)
        }
        if (vibrator == null) {
            vibrator = BaseApplication.mContext.getSystemService(Service.VIBRATOR_SERVICE) as Vibrator?
        }
        vibrator!!.vibrate(longArrayOf(2000, 1000, 2000, 1000), 0)

        findPhoneDialog = AlertDialog.Builder(ActivityUtils.getTopActivity())
            .setTitle(BaseApplication.mContext.getString(R.string.device_find_phone))
            .setMessage(BaseApplication.mContext.getString(R.string.device_find_phone_tip))
            .setPositiveButton(BaseApplication.mContext.getString(R.string.btn_known)) { dialog: DialogInterface?, which: Int ->
                if (findPhoneDialog != null && findPhoneDialog!!.isShowing) {
                    findPhoneDialog!!.dismiss()
                    stopFindPhone()

                    //发送给设备结束找手机 Send to device to end phone search
                    Log.i(tag, "sendCloseFindPhone")
                    ControlBleTools.getInstance().sendCloseFindPhone(object : SendCmdStateListener() {
                        override fun onState(state: SendCmdState?) {
                            Log.i(tag, "sendCloseFindPhone state=$state")
                        }
                    })
                }
            }.create()
        findPhoneDialog!!.show()
    }

    private fun stopFindPhone() {
        if (mMediaPlayer != null) {
            mMediaPlayer!!.stop()
            mMediaPlayer!!.release()
            mMediaPlayer = null
        }
        if (vibrator != null) {
            vibrator!!.cancel()
            vibrator = null
        }
        if (volume != 0) {
            try {
                if (am == null) {
                    am = BaseApplication.mContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager?
                }
                am!!.setStreamVolume(AudioManager.STREAM_MUSIC, volume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE)
            } catch (e: SecurityException) {
                Log.e(tag, "stopFindPhone: " + e.message, e)
            }
        }
        if (findPhoneDialog != null && findPhoneDialog!!.isShowing) {
            findPhoneDialog!!.cancel()
        }
    }
}