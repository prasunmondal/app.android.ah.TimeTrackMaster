package com.prasunmondal.ah.master.timetrack.SheetUtils

import android.content.Context
import com.prasunmondal.ah.master.timetrack.Models.Customer
import com.prasunmondal.lib.android.deviceinfo.Device
import com.prasunmondal.lib.android.deviceinfo.DeviceInfo
import com.prasunmondal.lib.posttogsheets.PostToGSheet
import java.text.SimpleDateFormat
import java.util.*

class ToSheets private constructor() {
    companion object {


        const val googleScript_scriptURL =
            "https://script.google.com/macros/s/AKfycbyoYcCSDEbXuDuGf0AhQjEi61ECAkl8JUv4ffNofz1yBIKfcT4/exec"

        private const val devDB: String = "https://docs.google.com/spreadsheets/d/1CvGQnFZL9YpUm1Ws_PtKFW_K8NVmm3OpEUZTwmfT4DA/edit#gid=0"
        private const val userDB: String = "https://docs.google.com/spreadsheets/d/1gZA5tqllOArlLJb2nLcmLqfNR-cdgFzNqTl9ZKyzcOI/edit#gid=0"

        const val userDataCSV = "https://docs.google.com/spreadsheets/d/e/2PACX-1vQoaGXygHiidBr9sPv9E9r_kQBP-5On2Dk6_Jp1Y6QzGX9rR6ZjZAxzSVwOK1kjGI_jwbhwRuNCXd45/pub?gid=23552406&single=true&output=csv"

        // user profile
        const val userLogs_sheet = devDB
        const val userLogs_tab = "userLogs"
        const val userErrors_sheet = devDB
        const val userErrors_tab = "userErrors"
        const val userAddTransactionSheet = userDB
        const val userAddTransactionTab = "Transactions"

        // dev profile
        const val devLogs_sheet = devDB
        const val devLogs_tab = "devLogs"
        const val devErrors_sheet = devDB
        const val devErrors_tab = "devErrors"
        const val devAddTransactionSheet = devDB
        const val devAddTransactionTab = "Transactions"

        lateinit var logs: PostToGSheet
        lateinit var errors: PostToGSheet
        lateinit var addTransaction: PostToGSheet
        lateinit var breakdownCSVURL: String

        fun creditAmount(c: Customer, amount: Long, context: Context) {
            addTransaction.post(
                listOf(
                    c.name,
                    c.phoneNumber,
                    c.address,
                    "",
                    "",
                    "",
                    "",
                    "",
                    amount.toString(),
                    "CREDIT",
                    "ENTERED",
                    DeviceInfo.get(Device.UNIQUE_ID)
                ), context
            )
        }
    }

    fun skipPost(): Boolean {
        return false
    }


}