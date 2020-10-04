package com.prasunmondal.ah.master.timetrack.Files

import com.opencsv.CSVReader
import com.prasunmondal.ah.master.timetrack.Models.Customer
import com.prasunmondal.lib.android.downloadfile.DownloadableFiles
import java.io.File
import java.io.FileReader
import java.io.IOException

class ReadCustomerDetails {

    fun getListOfValues(file: DownloadableFiles, valueColumnIndex: Int): MutableList<String> {
        var list : MutableList<String> = mutableListOf<String>()
        try {
            val reader = CSVReader(FileReader(File(file.getLocalURL())))
            var nextLine: Array<String>
            while (reader.peek() != null) {
                nextLine = reader.readNext()
                if(nextLine[valueColumnIndex].length > 0)
                    list.add(nextLine[valueColumnIndex])
            }
        } catch (e: IOException) {
            println(e)
            throw (e)
        }
//        "when asked value list is empty.. need to update the values"
        return list
    }

    fun populateCustomerList(file: DownloadableFiles): MutableList<Customer> {
        var list : MutableList<Customer> = mutableListOf<Customer>()
        try {
            var count = 0
            val reader = CSVReader(FileReader(File(file.getLocalURL())))
            var nextLine: Array<String>
            while (reader.peek() != null) {
                nextLine = reader.readNext()
                count++
                if(nextLine[Customer.CSV_INDEX_NAME].length > 0 && count > 1) {
                    var c = Customer(nextLine[Customer.CSV_INDEX_NAME],
                        nextLine[Customer.CSV_INDEX_CONTACTNO],
                        nextLine[Customer.CSV_INDEX_ADDRESS],
                        nextLine[Customer.CSV_INDEX_PRICE].toFloat())
                    list.add(c)
                }
            }
        } catch (e: IOException) {
            println(e)
            throw (e)
        }
//        "when asked value list is empty.. need to update the values"
        return list
    }
}