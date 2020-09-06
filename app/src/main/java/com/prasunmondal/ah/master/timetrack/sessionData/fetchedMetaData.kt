package com.prasunmondal.ah.master.timetrack.sessionData

import com.prasunmondal.ah.master.timetrack.Utility.FileReadUtil.Singleton.instance as FileReadUtils
import com.prasunmondal.ah.master.timetrack.FileManagerUtil.Singleton.instance as FileManagers

class FetchedMetaData {

    val APP_DOWNLOAD_LINK = "app_download_link"
    val APP_DOWNLOAD_VERSION = "app_versCode"
    val PAYMENT_UPI_PAY_UPIID = "upi_paymentID"
    val PAYMENT_UPI_PAY_NAME = "upi_paymentReceivePersonName"
    val PAYMENT_UPI_PAY_DESCRIPTION = "upi_paymentDescription"

    val TAG_CURRENT_OUTSTANDING = "currentOutstanding_"
    val TAG_PENDING_BILL = "pendingBill_"

    var TAG_BREAKDOWN_URL = "breakdownCurrentTest"

    private var fetchedDataMap: MutableMap<String, String> = mutableMapOf()

    object Singleton {
        var instance = FetchedMetaData()
    }

    fun getValue(key: String): String? {
        FileReadUtils.readPairCSVnPopulateMap(
            fetchedDataMap,
            FileManagers.metadata.getLocalURL()
        )
        return fetchedDataMap[key]
    }

    fun getValueByLabel(pre: String, post: String): String {
        return getValue((pre + post.toLowerCase()))!!
    }
}