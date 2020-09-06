package com.prasunmondal.ah.master.timetrack.sessionData

import android.content.Context

class AppContext {

    private object InstanceHolder {
        val INSTANCE = AppContext()
    }

    companion object {
        val instance: AppContext by lazy { InstanceHolder.INSTANCE }
    }

    lateinit var initialContext: Context

    var systemInfo = ""
    var uniqueDeviceID = ""
}