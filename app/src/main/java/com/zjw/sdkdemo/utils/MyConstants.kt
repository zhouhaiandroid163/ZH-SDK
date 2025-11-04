package com.zjw.sdkdemo.utils

object MyConstants {
    const val SCAN_DEVICE_TIME = 10 * 1000
    const val EXTRA_DEVICE_PROTOCOL: String = "ble_device_protocol"
    const val EXTRA_DEVICE_ADDRESS: String = "ble_device_address"
    const val EXTRA_DEVICE_NAME: String = "ble_device_name"
    const val EXTRA_DEVICE_IS_BIND: String = "ble_device_is_bind"

    var deviceProtocol: String? = null
    var deviceAddress: String? = null
    var deviceName: String? = null
    var isBind: Boolean? = null
}
