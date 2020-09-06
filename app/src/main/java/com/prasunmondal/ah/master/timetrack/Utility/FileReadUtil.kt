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
        fileName: FilePaths
    ) {

        TransactionsManager.Singleton.instance.transactions.clear()
        try {
            val reader = CSVReader(FileReader(File(fileName.destination)))
            var nextLine: Array<String>
            var lineToRead = 0
            val maxLines = 215
            var startLine = maxLines
            while (reader.peek() != null && lineToRead < maxLines) {
                lineToRead++
                nextLine = reader.readNext()

                if (lineToRead > 1) {
                    print(
                        nextLine[0] + " - " + nextLine[1] + " - " + nextLine[2] + " - " + nextLine[3] + " - " +
                                nextLine[4] + " - " + nextLine[5] + " - " + nextLine[6] + " - " + nextLine[7]
                                + " - " + nextLine[8] + " - " + nextLine[9]
                                + " - " + nextLine[10] + " - " + nextLine[11]
                                + " - " + nextLine[12] + " - " + nextLine[13]
                    )

                    val newRecord = TransactionRecord()

                    newRecord.addedBy = nextLine[13]
                    newRecord.customerName = nextLine[2]
                    newRecord.contactNo = nextLine[8]
                    newRecord.qty = "qty"
                    newRecord.totalPrice = nextLine[10]
                    newRecord.recordGenerationTime = nextLine[1]
                    newRecord.data = nextLine[7]
                    newRecord.transactionType = nextLine[11]
                    newRecord.totalCost = nextLine[10]
                    newRecord.rate = nextLine[9]



//                    if (newRecord.transactionType == "DEBIT")
//                        newRecord.totalCost = nextLine[11]
//                    else
//                        newRecord.rate = nextLine[11]

                    if (newRecord.recordGenerationTime.isNotEmpty())
                        TransactionsManager.Singleton.instance.transactions.add(newRecord)
                }
            }
        } catch (e: IOException) {
            throw (e)
        }
    }
}