package com.zjw.sdkdemo.livedata

import android.os.Handler
import android.os.Looper
import androidx.annotation.MainThread
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import java.util.concurrent.ConcurrentHashMap

class UnFlawedLiveData<T> {
    private val mMainHandler: Handler = Handler(Looper.getMainLooper())
    private var mValue: T? = null
    private val mObserverMap: ConcurrentHashMap<Observer<in T?>?, MutableLiveData<T?>> = ConcurrentHashMap<Observer<in T?>?, MutableLiveData<T?>>()


    @MainThread
    fun observe(owner: LifecycleOwner, observer: Observer<in T?>) {
        checkMainThread("observe")
        val lifecycle = owner.lifecycle
        if (lifecycle.currentState == Lifecycle.State.DESTROYED) {
            return
        }

        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                mObserverMap.remove(observer)
            }
        })

        val liveData = MutableLiveData<T?>()
        liveData.observe(owner, observer)
        mObserverMap.put(observer, liveData)
    }


    @set:MainThread
    var value: T?
        get() = mValue
        set(value) {
            checkMainThread("setValue")
            mValue = value
            for (liveData in mObserverMap.values) {
                liveData.value = value
            }
        }


    fun postValue(value: T?) {
        mMainHandler.post { this.value = value }
    }

    private fun checkMainThread(methodName: String?) {
        check(Looper.myLooper() == Looper.getMainLooper()) {
            ("UnFlowLiveData, Cannot invoke " + methodName
                    + " on a background thread")
        }
    }
}