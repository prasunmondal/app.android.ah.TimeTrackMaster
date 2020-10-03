package com.prasunmondal.ah.master.timetrack.Utility

import com.prasunmondal.lib.android.deviceinfo.Device
import com.prasunmondal.lib.android.deviceinfo.DeviceInfo

class CommonUtils {

    companion object {
        val appName = "AH.TimeTrack.Master"
    }

    fun isDevEnv(): Boolean {
        if(DeviceInfo.get(Device.UNIQUE_ID) == "ffffffff-87c0-30bd-0000-000075b319f8") {
            return true
        }
        return false
    }
}