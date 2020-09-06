package com.prasunmondal.ah.master.timetrack

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.Gravity.END
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.prasunmondal.ah.master.timetrack.ErrorReporting.ErrorHandle
import com.prasunmondal.ah.master.timetrack.SheetUtils.ToSheets
import com.prasunmondal.ah.master.timetrack.Utility.FileReadUtil
import com.prasunmondal.ah.master.timetrack.Utility.LogActions
import com.prasunmondal.ah.master.timetrack.sessionData.AppContext
import com.prasunmondal.ah.master.timetrack.sessionData.FetchedMetaData
import com.prasunmondal.ah.master.timetrack.sessionData.LocalConfig
import com.prasunmondal.lib.android.downloadfile.DownloadableFiles
import kotlinx.android.synthetic.main.activity_transactions_listing.*
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.math.abs
import com.prasunmondal.ah.master.timetrack.FileManagerUtil.Singleton.instance as fm

class TransactionRecord {

    lateinit var addedBy: String
    lateinit var customerName: String
    lateinit var contactNo: String
    lateinit var qty: String
    lateinit var totalPrice: String
    lateinit var recordGenerationTime: String
    lateinit var data: String
    lateinit var transactionType: String
    lateinit var totalCost: String
    lateinit var rate: String
}

class Tabs {
    val Tab_MyTransaction = "Tab_MyTransaction"
    val Tab_MyExpenses = "Tab_MyExpenses"
    val Tab_MySpent = "Tab_MySpent"
    val Tab_showAll = "Tab_showAll"

    val tabViewID_MyTransaction = R.id.tabMyTransactions
    val tabViewID_MyExpenses = R.id.tabExpenses
    val tabViewID_MySpent = R.id.tabSpent
    val tabViewID_showAll = R.id.tabAll

    var activeTab = Tab_MyTransaction

    object Singleton {
        var instance = Tabs()
    }
}

@Suppress("DEPRECATION")
class TransactionsListing : AppCompatActivity() {

    private val priceType_RATE = "priceType_CREDIT"
    private val priceType_DEBIT = "priceType_DEBIT"
    private val priceType_CREDIT = "priceType_TOTAL"
    private val priceType_NONE = "priceType_NONE"

    private val label_DownloadingData = "Downloading Data..."

    private val sortTag_itemName_Asc = "▲Item name"
    private val sortTag_price_Asc = "▲Amount"
    private val sortTag_date_Asc = "▲Date"
    private val sortTag_itemName_Desc = "▼Item name"
    private val sortTag_price_Desc = "▼Amount"
    private val sortTag_date_Desc = "▼Date"

    private val cardType_all = "\uD83D\uDC40 All"
    private val cardType_minimal = "\uD83D\uDC40 Minimal"
    private val cardType_relevant = "\uD83D\uDC40 Relevant"

    private var current_cardType = cardType_all

    private var currentSortOrder = sortTag_date_Desc

    private var current_showDecimal = false

    val breakdownSheet = DownloadableFiles(
        FetchedMetaData.Singleton.instance.getValue(FetchedMetaData.Singleton.instance.TAG_BREAKDOWN_URL)!!,
        "",
        "calculatingSheet.csv",
        "MasterTrack",
        "fetching transaction details", {},
        AppContext.instance.initialContext
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transactions_listing)
        ErrorHandle().reportUnhandledException(applicationContext)

        ToSheets.logs.post(listOf(LogActions.CLICKED.name, "Open Breakview"), applicationContext)
        Tabs.Singleton.instance.activeTab = Tabs.Singleton.instance.Tab_MyTransaction


        if (breakdownSheet.doesExist()) {
            initDisplay()
            ToSheets.logs.post(
                listOf(
                    LogActions.DISPLAY.name,
                    "Breakview:show:: ::With cached data"
                ), applicationContext
            )
        }

        setSupportActionBar(toolbar)
        setActionbarTextColor()
        AppContext.instance.initialContext = this


        val linearLayout = findViewById<LinearLayout>(R.id.cardContainers)
        val sharedBy = TextView(this)
        if (!breakdownSheet.doesExist())
            sharedBy.text = label_DownloadingData

        sharedBy.setTextColor(resources.getColor(R.color.tabs_text_inactive))
        sharedBy.gravity = Gravity.CENTER
        sharedBy.setPadding(0, 150, 0, 10)
        linearLayout.addView(sharedBy)
        Tabs.Singleton.instance.activeTab = Tabs.Singleton.instance.Tab_MyTransaction

        breakdownSheet.download(::startDisplay)
        ToSheets.logs.post(
            listOf(LogActions.DOWNLOAD_START.name, "Breakview data"),
            applicationContext
        )
    }

    private var displayStarted = false
    private fun startDisplay() {
        if (!displayStarted)
            ToSheets.logs.post(
                listOf(LogActions.DOWNLOAD_COMPLETE.name, "Breakview data"),
                applicationContext
            )
        displayStarted = true
        fetchDatafromFile()
        TransactionsManager.Singleton.instance.transactions.reverse()
        enableSorting()
        applyCardView()
        ToSheets.logs.post(
            listOf(
                LogActions.DISPLAY.name,
                "Breakview:show:: ::With fetched data"
            ), applicationContext
        )
    }

    private fun initDisplay() {
        fetchDatafromFile()
        TransactionsManager.Singleton.instance.transactions.reverse()
        enableSorting()
        applyCardView()
    }

    @SuppressLint("SetTextI18n")
    private fun displayCards() {
        var i = 1
        val tabType = Tabs.Singleton.instance.activeTab
        transactionSort(tabType)

        val linearLayout = findViewById<LinearLayout>(R.id.cardContainers)
        linearLayout.removeAllViews()

        setTabFormatting(Tabs.Singleton.instance.activeTab)

        var showMethod = ::showAll

        when (tabType) {
            Tabs.Singleton.instance.Tab_showAll -> {
                showMethod = ::showAll
            }
            Tabs.Singleton.instance.Tab_MyExpenses -> {
                showMethod = ::isDebitTransaction
            }
            Tabs.Singleton.instance.Tab_MySpent -> {
                showMethod = ::isCreditTransaction
            }
            Tabs.Singleton.instance.Tab_MyTransaction -> {
                showMethod = ::isMyTransactions
            }
        }

        var sum = 0.0
        for (z: Int in 0 until TransactionsManager.Singleton.instance.transactions.size) {
            val result = addMyCreditTextBox(
                i,
                TransactionsManager.Singleton.instance.transactions[z],
                showMethod,
                tabType
            )
            if (result != null) {
                i++
                sum = result.plus(sum)
            }
        }

        if (i == 1) {
            val lLayout = findViewById<LinearLayout>(R.id.cardContainers)
            val sharedBy = TextView(this)
            if (displayStarted)
                sharedBy.text = "No Transactions Found!"
            else if (!breakdownSheet.doesExist())
                sharedBy.text = label_DownloadingData
            sharedBy.setTextColor(resources.getColor(R.color.tabs_text_inactive))
            sharedBy.gravity = Gravity.CENTER
            sharedBy.setPadding(0, 150, 0, 10)
            lLayout.addView(sharedBy)
        }
        if (displayStarted) {
            var totalString = ""
            val totalField2 = findViewById<TextView>(R.id.totalView)
            when (tabType) {
                Tabs.Singleton.instance.Tab_showAll -> {
                    totalString = "All Transaction:  "
                }
                Tabs.Singleton.instance.Tab_MyExpenses -> {
                    totalString = "Total Debit:  "
                }
                Tabs.Singleton.instance.Tab_MySpent -> {
                    totalString = "Total Credit:  "
                }
                Tabs.Singleton.instance.Tab_MyTransaction -> {
                    totalString = "Total Balance.  "
                }
            }
            totalField2.text =
                totalString + "₹ ${stringAbsolute(round2Decimal(sum.toString()))}    |    ${i - 1} items"
            if (tabType == Tabs.Singleton.instance.Tab_MyTransaction && !current_showDecimal)
                totalField2.text =
                    totalString + "₹ ${stringAbsolute(roundInt(sum.toString()))}    |    ${i - 1} items"
        }


        val backgroundColor = resources.getColor(R.color.breakdown_tabsBackground)
        var textColor = resources.getColor(R.color.cardsColor_credit)
        when (tabType) {
            Tabs.Singleton.instance.Tab_showAll -> {
                textColor = resources.getColor(R.color.tabs_text_active)
            }
            Tabs.Singleton.instance.Tab_MyExpenses -> {
                textColor = resources.getColor(R.color.cardsColor_debit)
            }
            Tabs.Singleton.instance.Tab_MySpent -> {
                textColor = resources.getColor(R.color.cardsColor_credit)
            }
            Tabs.Singleton.instance.Tab_MyTransaction -> {
                textColor = if (sum > 0)
                    resources.getColor(R.color.cardsColor_debit)
                else
                    resources.getColor(R.color.cardsColor_credit)
            }
        }
        val totalField = findViewById<TextView>(R.id.totalView)
        val spanString = SpannableString(totalField.text)
        spanString.setSpan(StyleSpan(Typeface.BOLD), 0, spanString.length, 0)
        totalField.text = spanString
        totalField.setTextColor(textColor)
        totalField.setBackgroundColor(backgroundColor)
    }

    @SuppressLint("SetTextI18n")
    private fun addMyCreditTextBox(
        serialNo: Int,
        transaction: TransactionRecord,
        showConstraint: (TransactionRecord) -> Boolean,
        tabType: String
    ): Double? {
        if (!showConstraint.invoke(transaction))
            return null

        var textColor = R.color.cardsColor_debit

        if (tabType == Tabs.Singleton.instance.Tab_MySpent)
            textColor = R.color.cardsColor_credit

        if (!isDebitTransaction(transaction))
            textColor = R.color.notInvolvedTextColorRow1

        val linearLayout = findViewById<LinearLayout>(R.id.cardContainers)

        val llv0 = LinearLayout(applicationContext)
        llv0.orientation = LinearLayout.VERTICAL
        llv0.setPadding(20, 10, 20, 10)

        val llv1 = LinearLayout(applicationContext)
        llv1.orientation = LinearLayout.VERTICAL
        llv1.setBackgroundResource(R.drawable.rounded_layout_red)

        llv0.addView(llv1)


        val cardView = CardView(this)
        linearLayout.addView(cardView)

        val serialNoField = TextView(this)
        serialNoField.textSize = 15F
        serialNoField.setTextColor(resources.getColor(textColor))
        serialNoField.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        serialNoField.setPadding(20, 0, 0, 0)

        val price1 = TextView(this)
        price1.width = 407
        price1.textSize = 15F
        price1.gravity = END
        price1.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        price1.setPadding(20, 0, 20, 0)

        val itemNameField = TextView(this)
        itemNameField.width = (getScreenWidth(AppContext.instance.initialContext) - (15 + 307 + 50))
        itemNameField.textSize = 15F
        itemNameField.setTextColor(resources.getColor(textColor))
        itemNameField.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        itemNameField.setPadding(20, 0, 20, 0)

        val price2 = TextView(this)
        price2.textSize = 12F
        price2.width = 300
        price2.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        price2.gravity = END
        price2.alpha = 0.85F
        price2.setPadding(0, 0, 0, 0)

        val sharedBy = TextView(this)
        sharedBy.textSize = 12F
        sharedBy.width = (getScreenWidth(AppContext.instance.initialContext) - (200 + 50))
        sharedBy.alpha = 0.6F
        sharedBy.setTextColor(resources.getColor(R.color.textColorCreator))
        sharedBy.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        sharedBy.setPadding(20, 10, 50, 0)

        val recordOriginDetailsField = TextView(this)
        recordOriginDetailsField.textSize = 12F
        recordOriginDetailsField.setTextColor(resources.getColor(R.color.notInvolvedTextColorRow1))
        recordOriginDetailsField.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        recordOriginDetailsField.setPadding(20, 10, 0, 20)



        serialNoField.text = "$serialNo."
        itemNameField.text = transaction.customerName
        showPrices_textNColor(price1, transaction, price1_getText(tabType, transaction))
        showPrices_textNColor(price2, transaction, price2_getText(tabType, transaction))
        recordOriginDetailsField.text =
            "+ " + get1word(transaction.addedBy) + " . " + transaction.recordGenerationTime.split(" ")[0]
        if (current_cardType == cardType_minimal)
            sharedBy.text = ""
        else if (current_cardType == cardType_relevant)
            sharedBy.text = get1word(transaction.contactNo)
        else
            sharedBy.text = transaction.data + " . " + get1word(transaction.contactNo)


        val llh1 = LinearLayout(applicationContext)
        val llh2 = LinearLayout(applicationContext)
        val llh3 = LinearLayout(applicationContext)
        llh1.orientation = LinearLayout.HORIZONTAL
        llh2.orientation = LinearLayout.HORIZONTAL
        llh3.orientation = LinearLayout.HORIZONTAL

        llh1.addView(serialNoField)
        llh1.addView(itemNameField)
        llh1.addView(price1)

        llh2.addView(sharedBy)
        llh2.addView(price2)

        llh3.addView(recordOriginDetailsField)

        llh3.gravity = END
        llh2.setPadding(20, 0, 20, 0)
        llh3.setPadding(20, 0, 20, 0)

        llv1.addView(llh1)
        llv1.addView(llh2)
        if (current_cardType == cardType_all)
            llv1.addView(llh3)

        llv0.setOnClickListener {
            goToViewTransaction(transaction)
        }
        linearLayout.addView(llv0)

        serialNoField.setTextColor(getColor_text1(tabType, transaction))
        itemNameField.setTextColor(getColor_text1(tabType, transaction))

        if (tabType == Tabs.Singleton.instance.Tab_MyTransaction)
            return transaction.totalCost.toDouble()
        if (tabType == Tabs.Singleton.instance.Tab_MySpent)
            return transaction.totalCost.toDouble()
        if (tabType == Tabs.Singleton.instance.Tab_MyExpenses)
            return transaction.totalCost.toDouble()
        if (tabType == Tabs.Singleton.instance.Tab_showAll)
            return transaction.totalPrice.toDouble()
        return 0.0
    }

    private fun get1word(str: String): String {
        val names: MutableList<String> = str.split(", ") as MutableList<String>
        var result = ""
        for (i: Int in 0 until names.size) {
            if (i != 0)
                result += ", "
            result += names[i].split(" ")[0]
        }
        return result
    }

    private val username = LocalConfig.Singleton.instance.getValue("username")!!

    private fun isCreditTransaction(transaction: TransactionRecord): Boolean {
        return transaction.transactionType == "CREDIT"
    }

    private fun isDebitTransaction(transaction: TransactionRecord): Boolean {
        return transaction.transactionType == "DEBIT"
    }

    private fun showAll(transaction: TransactionRecord): Boolean {
        return true
    }

    private fun isMyTransactions(transaction: TransactionRecord): Boolean {
        return isCreditTransaction(transaction) || isDebitTransaction(transaction)
    }

    fun tabShowall(view: View) {
        Tabs.Singleton.instance.activeTab = Tabs.Singleton.instance.Tab_showAll
        displayCards()
        ToSheets.logs.post(
            listOf(
                LogActions.CLICKED.name,
                "Breakview:Tab:: ::" + Tabs.Singleton.instance.activeTab
            ),
            applicationContext
        )
    }

    private fun setTabFormatting(activeTab: String) {
        val label1 = findViewById<TextView>(Tabs.Singleton.instance.tabViewID_showAll)
        val label2 = findViewById<TextView>(Tabs.Singleton.instance.tabViewID_MyTransaction)
        val label3 = findViewById<TextView>(Tabs.Singleton.instance.tabViewID_MyExpenses)
        val label4 = findViewById<TextView>(Tabs.Singleton.instance.tabViewID_MySpent)

        label1.setTextColor(resources.getColor(R.color.tabs_text_inactive))
        label2.setTextColor(resources.getColor(R.color.tabs_text_inactive))
        label3.setTextColor(resources.getColor(R.color.tabs_text_inactive))
        label4.setTextColor(resources.getColor(R.color.tabs_text_inactive))

        var activateLabel = label1
        when (activeTab) {
            Tabs.Singleton.instance.Tab_showAll -> {
                activateLabel = label1
            }
            Tabs.Singleton.instance.Tab_MyExpenses -> {
                activateLabel = label3
            }
            Tabs.Singleton.instance.Tab_MySpent -> {
                activateLabel = label4
            }
            Tabs.Singleton.instance.Tab_MyTransaction -> {
                activateLabel = label2
            }
        }

        activateLabel.setTextColor(resources.getColor(R.color.tabs_text_active))
    }

    fun tabMyexpenses(view: View) {
        Tabs.Singleton.instance.activeTab = Tabs.Singleton.instance.Tab_MyExpenses
        displayCards()
        ToSheets.logs.post(
            listOf(
                LogActions.CLICKED.name,
                "Breakview:Tab:: ::" + Tabs.Singleton.instance.activeTab
            ),
            applicationContext
        )
    }

    fun tabMyspent(view: View) {
        Tabs.Singleton.instance.activeTab = Tabs.Singleton.instance.Tab_MySpent
        displayCards()
        ToSheets.logs.post(
            listOf(
                LogActions.CLICKED.name,
                "Breakview:Tab:: ::" + Tabs.Singleton.instance.activeTab
            ),
            applicationContext
        )
    }

    fun tabMytransaction(view: View) {
        Tabs.Singleton.instance.activeTab = Tabs.Singleton.instance.Tab_MyTransaction
        displayCards()
        ToSheets.logs.post(
            listOf(
                LogActions.CLICKED.name,
                "Breakview:Tab:: ::" + Tabs.Singleton.instance.activeTab
            ),
            applicationContext
        )
    }

    private fun transactionSort(activeTab: String) {
        if (!displayStarted)
            return

        findViewById<TextView>(R.id.labelSort).text = currentSortOrder
        when (currentSortOrder) {
            sortTag_date_Asc -> {
                fetchDatafromFile()
            }
            sortTag_date_Desc -> {
                fetchDatafromFile()
                TransactionsManager.Singleton.instance.transactions.reverse()
            }

            sortTag_price_Asc -> {
                fetchDatafromFile()
                when (activeTab) {
                    Tabs.Singleton.instance.Tab_showAll -> {
                        TransactionsManager.Singleton.instance.transactions.sortBy { t -> t.totalPrice.toFloat() }
                    }
                    Tabs.Singleton.instance.Tab_MyTransaction -> {
                        TransactionsManager.Singleton.instance.transactions.sortBy { t -> t.totalCost.toFloat() }
                    }
                    Tabs.Singleton.instance.Tab_MyExpenses -> {
                        TransactionsManager.Singleton.instance.transactions.sortBy { t -> t.totalCost.toFloat() }
                    }
                    Tabs.Singleton.instance.Tab_MySpent -> {
                        TransactionsManager.Singleton.instance.transactions.sortBy { t -> t.rate.toFloat() }
                    }
                }
            }
            sortTag_price_Desc -> {
                fetchDatafromFile()
                TransactionsManager.Singleton.instance.transactions.reverse()
                when (activeTab) {
                    Tabs.Singleton.instance.Tab_showAll -> {
                        TransactionsManager.Singleton.instance.transactions.sortBy { t -> t.totalPrice.toFloat() }
                    }
                    Tabs.Singleton.instance.Tab_MyTransaction -> {
                        TransactionsManager.Singleton.instance.transactions.sortBy { t -> t.totalCost.toFloat() }
                    }
                    Tabs.Singleton.instance.Tab_MyExpenses -> {
                        TransactionsManager.Singleton.instance.transactions.sortBy { t -> t.totalCost.toFloat() }
                    }
                    Tabs.Singleton.instance.Tab_MySpent -> {
                        TransactionsManager.Singleton.instance.transactions.sortBy { t -> t.rate.toFloat() }
                    }
                }
                TransactionsManager.Singleton.instance.transactions.reverse()
            }

            sortTag_itemName_Asc -> {
                fetchDatafromFile()
                TransactionsManager.Singleton.instance.transactions.sortBy { t -> t.customerName.toLowerCase() }
            }
            sortTag_itemName_Desc -> {
                fetchDatafromFile()
                TransactionsManager.Singleton.instance.transactions.reverse()
                TransactionsManager.Singleton.instance.transactions.sortBy { t -> t.customerName.toLowerCase() }
                TransactionsManager.Singleton.instance.transactions.reverse()
            }
        }
    }

    private fun round2Decimal(st: String): String {
        if (!current_showDecimal) {
            val df = DecimalFormat("#")
            df.roundingMode = RoundingMode.FLOOR
            return df.format(st.toDouble())
        }
        val df = DecimalFormat("#.##")
        df.roundingMode = RoundingMode.CEILING
        return df.format(st.toDouble())
    }

    private fun roundInt(st: String): String {
        val df = DecimalFormat("#")
        df.roundingMode = RoundingMode.CEILING
        return df.format(st.toDouble())
    }

    private fun getColor_text1(tabType: String, transaction: TransactionRecord): Int {
        if (!isCreditTransaction(transaction) && !isDebitTransaction(transaction)) {
            return resources.getColor(R.color.notInvolvedTextColorRow1)
        }
        when (tabType) {
            Tabs.Singleton.instance.Tab_showAll -> {
                return resources.getColor(R.color.cardsColor_white)
            }
            Tabs.Singleton.instance.Tab_MyExpenses -> {
                return resources.getColor(R.color.cardsColor_debit)
            }
            Tabs.Singleton.instance.Tab_MySpent -> {
                return resources.getColor(R.color.cardsColor_credit)
            }
            Tabs.Singleton.instance.Tab_MyTransaction -> {
                return resources.getColor(R.color.cardsColor_white)
            }
        }
        return resources.getColor(R.color.cardsColor_white)
    }

    private fun price1_getText(tabType: String, transaction: TransactionRecord): String {
        when (tabType) {
            Tabs.Singleton.instance.Tab_showAll -> {
                if (isCreditTransaction(transaction))
                    return priceType_CREDIT
                return priceType_DEBIT
            }
            Tabs.Singleton.instance.Tab_MyExpenses -> {
                return priceType_DEBIT
            }
            Tabs.Singleton.instance.Tab_MySpent -> {
                return priceType_CREDIT
            }
            Tabs.Singleton.instance.Tab_MyTransaction -> {
                if (isCreditTransaction(transaction))
                    return priceType_CREDIT
                return priceType_DEBIT
            }
        }
        return priceType_CREDIT
    }

    private fun price2_getText(tabType: String, transaction: TransactionRecord): String {
        when (tabType) {
            Tabs.Singleton.instance.Tab_showAll -> {
                return priceType_RATE
            }
            Tabs.Singleton.instance.Tab_MyExpenses -> {
                return priceType_RATE
            }
            Tabs.Singleton.instance.Tab_MySpent -> {
                return priceType_RATE
            }
            Tabs.Singleton.instance.Tab_MyTransaction -> {
                return priceType_RATE
            }
        }
        return priceType_RATE
    }

    @SuppressLint("SetTextI18n")
    private fun showPrices_textNColor(
        textView: TextView,
        transaction: TransactionRecord,
        priceType: String
    ) {
        val pre = "₹ "
        when (priceType) {
            priceType_RATE -> {
                textView.text = pre + round2Decimal(transaction.rate)
                textView.setTextColor(resources.getColor(R.color.notInvolvedTextColorRow1))
            }
            priceType_DEBIT -> {
                textView.text = pre + round2Decimal(transaction.totalCost)
                textView.setTextColor(resources.getColor(R.color.cardsColor_debit))
            }
            priceType_CREDIT -> {
                textView.text = pre + stringAbsolute((round2Decimal(transaction.totalPrice)))
                textView.setTextColor(resources.getColor(R.color.cardsColor_credit))
            }
            priceType_NONE -> {
                textView.text = ""
            }
        }
    }

    private fun stringAbsolute(round2Decimal: String): Any? {
        return round2Decimal.replace("-","")
    }

    private fun goToViewTransaction(transaction: TransactionRecord) {
        LocalConfig.Singleton.instance.viewTransaction = transaction
        val i = Intent(this@TransactionsListing, ViewTransaction::class.java)
        startActivity(i)
    }

    @Suppress("DEPRECATION")
    private fun setActionbarTextColor() {
        val title = ""
        val spannableTitle: Spannable = SpannableString("")
        spannableTitle.setSpan(
            ForegroundColorSpan(Color.BLACK),
            0,
            spannableTitle.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        supportActionBar!!.title = title
        window.statusBarColor = resources.getColor(R.color.colorPrimaryDark)
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.colorPrimary)))

        findViewById<TextView>(R.id.toolbar_Text1).text = "Transactions"
        try {
            val user =
                LocalConfig.Singleton.instance.getValue(LocalConfig.Singleton.instance.USERNAME)
            if (user!!.isNotEmpty())
                findViewById<TextView>(R.id.toolbar_Text2).text = "- " + user
        } catch (e: Exception) {
            findViewById<TextView>(R.id.toolbar_Text2).text = "Anonymous"
        }
    }

    private fun getScreenWidth(context: Context): Int {
        val displayMetrics = DisplayMetrics()
        (context as Activity).windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.widthPixels
    }

    private fun changeSortOrder() {
        when (currentSortOrder) {
            sortTag_date_Desc -> {
                currentSortOrder = sortTag_date_Asc
            }
            sortTag_date_Asc -> {
                currentSortOrder = sortTag_price_Desc
            }
            sortTag_price_Desc -> {
                currentSortOrder = sortTag_price_Asc
            }
            sortTag_price_Asc -> {
                currentSortOrder = sortTag_itemName_Asc
            }
            sortTag_itemName_Asc -> {
                currentSortOrder = sortTag_itemName_Desc
            }
            sortTag_itemName_Desc -> {
                currentSortOrder = sortTag_date_Desc
            }
        }
        ToSheets.logs.post(
            listOf(LogActions.CLICKED.name, "Breakview:Sort:: ::$currentSortOrder"),
            this.applicationContext
        )
        displayCards()
    }

    private fun showOptions() {
        val ll2 = findViewById<LinearLayout>(R.id.linearLayout2)
        val params: ViewGroup.LayoutParams = ll2.layoutParams

        val tview = findViewById<TextView>(R.id.tabMore)
        if (params.height == 0) {
            params.height = 90
            tview.setTextColor(resources.getColor(R.color.tabs_text_active))
        } else {
            params.height = 0
            tview.setTextColor(resources.getColor(R.color.tabs_text_inactive))
        }
        ll2.layoutParams = params
    }

    private fun changeView() {
        when (current_cardType) {
            cardType_all -> {
                current_cardType = cardType_relevant
            }
            cardType_relevant -> {
                current_cardType = cardType_minimal
            }
            cardType_minimal -> {
                current_cardType = cardType_all
            }
        }
        ToSheets.logs.post(
            listOf(LogActions.CLICKED.name, "Breakview:cardtype:: ::$current_cardType"),
            this.applicationContext
        )
        applyCardView()
    }

    private fun applyCardView() {
        findViewById<TextView>(R.id.labelChangeView).text = current_cardType
        displayCards()
    }

    private fun enableSorting() {
        findViewById<TextView>(R.id.labelSort).setOnClickListener {
            changeSortOrder()
        }

        findViewById<TextView>(R.id.tabMore).setOnClickListener {
            showOptions()
        }

        findViewById<TextView>(R.id.labelChangeView).setOnClickListener {
            changeView()
        }

        findViewById<TextView>(R.id.labelDecimalView).setOnClickListener {
            changeDecimalValue()
        }
    }

    private fun changeDecimalValue() {
        current_showDecimal = !current_showDecimal
        val tview = findViewById<TextView>(R.id.labelDecimalView)
        if (current_showDecimal) {
            tview.setTextColor(resources.getColor(R.color.tabs_text_active))
        } else {
            tview.setTextColor(resources.getColor(R.color.tabs_text_inactive))
        }
        ToSheets.logs.post(
            listOf(LogActions.CLICKED.name, "Breakview:decimal:: ::$current_showDecimal"),
            this.applicationContext
        )
        displayCards()
    }

    private fun fetchDatafromFile() {
        try {
            println("fetching data")
            TransactionsManager.Singleton.instance.transactions = mutableListOf()
            FileReadUtil.Singleton.instance.printCSVfile(fm.downloadLink_CalculatingSheet)
        } catch (e: java.lang.Exception) {
            ToSheets.logs.post(listOf("", "File not found handled!"), applicationContext)
        }
    }
}
