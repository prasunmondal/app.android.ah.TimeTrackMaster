package com.prasunmondal.ah.master.timetrack.Utility

import com.prasunmondal.lib.android.deviceinfo.Device
import com.prasunmondal.lib.android.deviceinfo.DeviceInfo

class CommonUtils {

    companion object {
        val appName = "AH.TimeTrack.Master"
    }

    fun isDevEnv(): Boolean {
        var devID = DeviceInfo.get(Device.UNIQUE_ID)
        return (devID == "ffffffff-87c0-30bd-0000-000075b319f8" || devID == "00000000-1c29-463a-0000-000075b319f8")
    }
}