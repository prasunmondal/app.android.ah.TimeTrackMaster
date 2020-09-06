package com.prasunmondal.ah.master.timetrack

class TransactionsManager {

    object Singleton {
        var instance = TransactionsManager()
    }

    var transactions = mutableListOf<TransactionRecord>()
    var customers = mutableListOf<String>()
}