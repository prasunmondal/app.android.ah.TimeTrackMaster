package com.prasunmondal.ah.master.timetrack.SheetUtils

import com.prasunmondal.lib.posttogsheets.PostToGSheet

class ToSheets private constructor() {
    companion object {


        const val googleScript_scriptURL =
            "https://script.google.com/macros/s/AKfycbyoYcCSDEbXuDuGf0AhQjEi61ECAkl8JUv4ffNofz1yBIKfcT4/exec"

        private const val devDB: String = "https://docs.google.com/spreadsheets/d/1CvGQnFZL9YpUm1Ws_PtKFW_K8NVmm3OpEUZTwmfT4DA/edit#gid=0"
        private const val userDB: String = "https://docs.google.com/spreadsheets/d/1gZA5tqllOArlLJb2nLcmLqfNR-cdgFzNqTl9ZKyzcOI/edit#gid=0"

        // user profile
        const val userLogs_sheet = devDB
        const val userLogs_tab = "userLogs"
        const val userErrors_sheet = devDB
        const val userErrors_tab = "userErrors"

        // dev profile
        const val devLogs_sheet = devDB
        const val devLogs_tab = "devLogs"
        const val devErrors_sheet = devDB
        const val devErrors_tab = "devErrors"

        lateinit var logs: PostToGSheet
        lateinit var errors: PostToGSheet
    }

    fun skipPost(): Boolean {
        return false
    }
}