package com.zjw.sdkdemo.manager

import android.content.Context
import android.os.PowerManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.zjw.sdkdemo.base.BaseApplication

class WakeLockManager private constructor() : LifecycleObserver {

    private var mPowerManager: PowerManager? = null

    private var mWakeLock: PowerManager.WakeLock? = null

    private var mLifecycle: Lifecycle? = null

    companion object {
        @JvmStatic
        val instance = SingletonHolder.INSTANCE
    }

    private object SingletonHolder {
        val INSTANCE = WakeLockManager()
    }

    init {
        mPowerManager = BaseApplication.Companion.mContext.getSystemService(Context.POWER_SERVICE) as PowerManager?
    }

    fun isScreenOn(): Boolean {
        return mPowerManager?.isInteractive ?: false
    }

    fun keepUnLock(lifecycle: Lifecycle? = null) {
        mLifecycle = lifecycle
        mLifecycle?.addObserver(this)
        if (isScreenOn()) {
            mWakeLock = mPowerManager?.newWakeLock(PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP, this::class.java.simpleName)
            mWakeLock?.acquire(7 * 24 * 60 * 60 * 1000L)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private fun onLifecycleDestroy() {
        mWakeLock?.release()
        mWakeLock = null
        mLifecycle?.removeObserver(this)
        mLifecycle = null
    }
}