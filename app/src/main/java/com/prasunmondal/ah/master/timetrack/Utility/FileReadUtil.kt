package com.prasunmondal.ah.master.timetrack.Utility

import com.prasunmondal.ah.master.timetrack.TransactionRecord
import com.prasunmondal.ah.master.timetrack.TransactionsManager
import com.prasunmondal.ah.master.timetrack.FilePaths
import com.prasunmondal.ah.master.timetrack.sessionData.LocalConfig
import com.opencsv.CSVReader
import java.io.File
import java.io.FileReader
import java.io.IOException

class FileReadUtil {

    object Singleton {
        var instance = FileReadUtil()
    }

    fun readPairCSVnPopulateMap(map: MutableMap<String, String>, fileName: FilePaths) {
        try {
            val reader = CSVReader(FileReader(File(fileName.destination)))
            var nextLine: Array<String>
            while (reader.peek() != null) {
                nextLine = reader.readNext()
                map[nextLine[0]] = nextLine[1]
            }
        } catch (e: IOException) {
            throw (e)
        }
    }

    fun readPairCSVnPopulateMap(map: MutableMap<String, String>, fileName: String) {
        try {
            val reader = CSVReader(FileReader(File(fileName)))
            var nextLine: Array<String>
            while (reader.peek() != null) {
                nextLine = reader.readNext()
                map[nextLine[0]] = nextLine[1]
            }
        } catch (e: IOException) {
            throw (e)
        }
    }

    fun printCSVfile(
//        map: MutableMap<String, String>,
        fileName: FilePaths
    ) {

        var user = ""
//        user = "Prasun Mondal"
        user = LocalConfig.Singleton.instance.getValue("username")!!
        var nameIndex = 0
        var itemIndex = 0
        var sharedByIndex = 0
        var qtyIndex = 0
        var priceIndex = 0
        var createTimeIndex = 0
        var timeIndex = 0
        var editLinkIndex = 0
        var userDebitIndex = 0
        var userCreditIndex = 0

        TransactionsManager.Singleton.instance.transactions.clear()
        try {
            val reader = CSVReader(FileReader(File(fileName.destination)))
            var nextLine: Array<String>
            var lineToRead = 1
            val maxLines = 215
            var startLine = maxLines
            while (reader.peek() != null && lineToRead < maxLines) {
                lineToRead++
                nextLine = reader.readNext()
//
//                if (nextLine[0] == "start") {
//                    startLine = lineToRead + 2
//                    for (i in 0..nextLine.size - 1) {
//                        if (nextLine[i] == "app_name")
//                            nameIndex = i
//                        if (nextLine[i] == "app_item")
//                            itemIndex = i
//                        if (nextLine[i] == "app_sharedBy")
//                            sharedByIndex = i
//                        if (nextLine[i] == "app_qty")
//                            qtyIndex = i
//                        if (nextLine[i] == "app_price")
//                            priceIndex = i
//                        if (nextLine[i] == "app_createTime")
//                            createTimeIndex = i
//                        if (nextLine[i] == "app_time")
//                            timeIndex = i
//                        if (nextLine[i] == "app_editLink")
//                            editLinkIndex = i
//                        if (nextLine[i] == user + "_debit")
//                            userDebitIndex = i
//                        if (nextLine[i] == user + "_credit")
//                            userCreditIndex = i
//                    }
//                }


                if (lineToRead > 1) {
                    print(
                        nextLine[nameIndex] + " - " + nextLine[itemIndex] + " - " + nextLine[sharedByIndex] + " - " + nextLine[qtyIndex] + " - " +
                                nextLine[priceIndex] + " - " + nextLine[createTimeIndex] + " - " + nextLine[timeIndex] + " - " + nextLine[editLinkIndex]
                                + " - " + nextLine[userDebitIndex] + " - " + nextLine[userCreditIndex]
                    )

                    val newRecord = TransactionRecord()
                    newRecord.name = nextLine[2]
                    newRecord.customerName = nextLine[3]
                    newRecord.contactNo = nextLine[4]
                    newRecord.qty = nextLine[5]
                    newRecord.price = nextLine[9]
                    newRecord.createTime = nextLine[1]
                    newRecord.time = nextLine[0]
                    newRecord.transactionType = nextLine[10]
                    newRecord.userDebit = "0"
                    newRecord.userCredit = "0"

                    if (newRecord.transactionType == "DEBIT")
                        newRecord.userDebit = nextLine[11]
                    else
                        newRecord.userCredit = nextLine[11]

                    if (newRecord.createTime.isNotEmpty())
                        TransactionsManager.Singleton.instance.transactions.add(newRecord)
                }
            }
        } catch (e: IOException) {
            throw (e)
        }
    }
}