@file:Suppress("DEPRECATION")

package com.prasunmondal.ah.master.timetrack

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.prasunmondal.ah.master.timetrack.ErrorReporting.ErrorHandle
import com.prasunmondal.ah.master.timetrack.SheetUtils.ToSheets
import com.prasunmondal.ah.master.timetrack.Utility.LogActions
import com.prasunmondal.ah.master.timetrack.sessionData.AppContext
import kotlinx.android.synthetic.main.activity_view_transaction.*
import java.math.RoundingMode
import java.text.DecimalFormat
import com.prasunmondal.ah.master.timetrack.sessionData.LocalConfig.Singleton.instance as lc

class ViewTransaction : AppCompatActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_transaction)
        ErrorHandle().reportUnhandledException(applicationContext)
        setSupportActionBar(toolbar)
        setActionbarTextColor()

        ToSheets.logs.post(
            listOf(
                LogActions.CLICKED.name, "Viewing Details:\n" + lc.viewTransaction.customerName
                        + " qty: " + lc.viewTransaction.qty
                        + " price: " + lc.viewTransaction.totalPrice
                        + " editURL: " + lc.viewTransaction.transactionType
            ),
            this.applicationContext
        )

        findViewById<TextView>(R.id.details_itemname).text = "Customer Name: " + lc.viewTransaction.customerName





        findViewById<TextView>(R.id.details_sharedBy).text =
            ""

        findViewById<TextView>(R.id.details_addedBy).text =
            "added by: " + lc.viewTransaction.addedBy + "  (" + lc.viewTransaction.recordGenerationTime + ")"



        if(lc.viewTransaction.transactionType == "CREDIT") {
            findViewById<TextView>(R.id.details_debit).text =
                "Credit: ₹ " + round2Decimal(lc.viewTransaction.totalCost)
            findViewById<TextView>(R.id.details_debit).setTextColor(resources.getColor(R.color.cardsColor_credit))

            findViewById<TextView>(R.id.details_credit).text = ""

            findViewById<TextView>(R.id.details_qty).text = ""

            findViewById<TextView>(R.id.details_totalPrice).text =
                "Credit Amount: ₹ " + lc.viewTransaction.totalPrice
        } else {
            findViewById<TextView>(R.id.details_debit).text =
                "Debit: ₹ " + round2Decimal(lc.viewTransaction.totalCost)
            findViewById<TextView>(R.id.details_debit).setTextColor(resources.getColor(R.color.cardsColor_debit))

            findViewById<TextView>(R.id.details_credit).text =
                "Rate: ₹ " + lc.viewTransaction.rate
            findViewById<TextView>(R.id.details_credit).setTextColor(resources.getColor(R.color.cardsColor_white))

            findViewById<TextView>(R.id.details_qty).text = "Total Time: " + lc.viewTransaction.contactNo

            findViewById<TextView>(R.id.details_totalPrice).text =
                "Total Price: ₹ " + lc.viewTransaction.totalPrice
        }


        val webView: WebView = findViewById(R.id.editBrowser)
        webView.visibility = View.GONE
        showEdit()
    }

    private fun showEdit() {
        val webView: WebView = findViewById(R.id.editBrowser)
        webView.webViewClient = MyWebViewClient()
        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webView.settings.useWideViewPort = true
        webView.settings.loadWithOverviewMode = true
        webView.webViewClient = WebViewClient()
        webView.webChromeClient = WebChromeClient()

        AppContext.instance.initialContext = this

        supportActionBar!!.setDisplayShowTitleEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        loadPage(lc.viewTransaction.transactionType)
    }

    @SuppressLint("SetTextI18n")
    fun onClickEdit(view: View) {
        val webView: WebView = findViewById(R.id.editBrowser)
        val editTransactionButton = findViewById<TextView>(R.id.editTransactionButton)
        if (webView.visibility == View.GONE) {
            ToSheets.logs.post(
                listOf(LogActions.CLICKED.name, "Open Edit Window"),
                applicationContext
            )
            webView.visibility = View.VISIBLE
            editTransactionButton.text = "Close Edit Window"
        } else {
            ToSheets.logs.post(
                listOf(LogActions.CLICKED.name, "Closed Edit Window"),
                applicationContext
            )
            webView.visibility = View.GONE
            editTransactionButton.text = "Edit this Transaction"
        }
    }

    private fun round2Decimal(st: String): String {
        val df = DecimalFormat("#.##")
        df.roundingMode = RoundingMode.CEILING
        return df.format(st.toDouble())
    }

    private fun loadPage(url: String) {
        ToSheets.logs.post(listOf(LogActions.LOADING_URL.name, url), applicationContext)
        val webView: WebView = findViewById(R.id.editBrowser)
        webView.webViewClient = object : WebViewClient() {

            override fun onReceivedError(
                view: WebView,
                errorCode: Int,
                description: String,
                failingUrl: String
            ) {
                Toast.makeText(this@ViewTransaction, "Error:$description", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        webView.loadUrl(url)
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

    @SuppressLint("SetTextI18n")
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

        findViewById<TextView>(R.id.toolbar_Text1).text = "MasterTrack"
        findViewById<TextView>(R.id.toolbar_Text2).text = "View Details"
    }
}