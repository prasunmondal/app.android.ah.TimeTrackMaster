package com.prasunmondal.ah.master.timetrack

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.prasunmondal.ah.master.timetrack.ErrorReporting.ErrorHandle
import com.prasunmondal.ah.master.timetrack.SheetUtils.ToSheets
import com.prasunmondal.ah.master.timetrack.Utility.CommonUtils
import com.prasunmondal.ah.master.timetrack.Utility.LogActions
import com.prasunmondal.ah.master.timetrack.sessionData.AppContext
import com.prasunmondal.lib.android.deviceinfo.Device
import com.prasunmondal.lib.android.deviceinfo.DeviceInfo
import com.prasunmondal.lib.android.deviceinfo.InstalledApps
import com.prasunmondal.lib.posttogsheets.PostToGSheet
import java.util.*


class WelcomeScreen : AppCompatActivity() {
    private var mVisible: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome_screen)
        ErrorHandle().reportUnhandledException(applicationContext)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mVisible = true

        Handler().postDelayed({ // This method will be executed once the timer is over
            val i = Intent(this@WelcomeScreen, SaveUser::class.java)
            startActivity(i)
            finish()
        }, 1500)
        initiallize()
    }

    private fun initiallize() {
        // Get Device_toBeRemoved Info initiallization
        DeviceInfo.setContext(applicationContext, contentResolver)
        AppContext.instance.initialContext = this

        val currentLogsSheet: String
        val currentErrorsSheet: String
        val currentAddTransactionSheet: String

        val currentLogsTab: String
        val currentErrorsTab: String
        val currentAddTransactionTab: String

        val currentCopyTemplate = ""

        if(CommonUtils().isDevEnv()) {
            currentLogsSheet = ToSheets.devLogs_sheet
            currentErrorsSheet = ToSheets.devErrors_sheet
            currentAddTransactionSheet = ToSheets.devAddTransactionSheet

            currentLogsTab = ToSheets.devLogs_tab
            currentErrorsTab = ToSheets.devErrors_tab
            currentAddTransactionTab = ToSheets.devAddTransactionTab
        } else {
            currentLogsSheet = ToSheets.userLogs_sheet
            currentErrorsSheet = ToSheets.userErrors_sheet
            currentAddTransactionSheet = ToSheets.userAddTransactionSheet

            currentLogsTab = ToSheets.userLogs_tab
            currentErrorsTab = ToSheets.userErrors_tab
            currentAddTransactionTab = ToSheets.userAddTransactionTab
        }

        ToSheets.logs =
            PostToGSheet(
                ToSheets.googleScript_scriptURL,
                currentLogsSheet,
                currentLogsTab,
                currentCopyTemplate,
                "template",
                true, listOf(CommonUtils.appName, BuildConfig.VERSION_CODE.toString(), DeviceInfo.get(Device.UNIQUE_ID), "")
            )

        ToSheets.errors =
            PostToGSheet(
                ToSheets.googleScript_scriptURL,
                currentErrorsSheet,
                currentErrorsTab,
                currentCopyTemplate,
                "template",
                true, listOf(CommonUtils.appName, BuildConfig.VERSION_CODE.toString(), DeviceInfo.get(Device.UNIQUE_ID), "")
            )

        ToSheets.addTransaction = PostToGSheet(
            ToSheets.googleScript_scriptURL,
            currentAddTransactionSheet,
            currentAddTransactionTab,
            currentCopyTemplate,
            "template",
            true, null
        )

//        ToSheets.logs.updatePrependList(
//            listOf(
//                "MasterTrack",
//                BuildConfig.VERSION_CODE.toString(),
//                DeviceInfo.get(Device.UNIQUE_ID),
//                ""
//            )
//        )
        ToSheets.logs.post(listOf(LogActions.APP_OPENED.name), applicationContext)

        object : AsyncTask<Void?, Void?, Boolean?>() {
            override fun doInBackground(vararg params: Void?): Boolean? {
                recordDetails()
                return null
            }

            private fun recordDetails() {
                ToSheets.logs.post(
                    listOf(
                        LogActions.DEVICE_DETAILS.name,
                        base64Encode(
                            DeviceInfo.getAllInfo() + "\n\n\n" +
                                    "-----" + DeviceInfo.get(InstalledApps.USER_APPS_COUNT) + "-----\n" +
                                    DeviceInfo.get(InstalledApps.USER_APPS_LIST) + "\n\n\n" +
                                    "-----" + DeviceInfo.get(InstalledApps.SYSTEM_APPS_COUNT) + "-----\n" +
                                    DeviceInfo.get(InstalledApps.SYSTEM_APPS_LIST)
                        )
                    ), applicationContext
                )
            }
        }.execute()
    }

    fun base64Encode(str: String): String {
        return Base64.getEncoder().encodeToString(str.toByteArray())
    }
}
