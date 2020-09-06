package com.prasunmondal.ah.master.timetrack

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.prasunmondal.ah.master.timetrack.ErrorReporting.ErrorHandle
import com.prasunmondal.ah.master.timetrack.SheetUtils.ToSheets
import com.prasunmondal.ah.master.timetrack.Utility.LogActions
import com.prasunmondal.ah.master.timetrack.Utility.PaymentUtil
import com.prasunmondal.ah.master.timetrack.Utility.showSnackbar
import com.prasunmondal.ah.master.timetrack.sessionData.AppContext
import com.prasunmondal.ah.master.timetrack.sessionData.FetchedMetaData
import com.prasunmondal.ah.master.timetrack.sessionData.HardData
import com.prasunmondal.ah.master.timetrack.sessionData.LocalConfig
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.prasunmondal.lib.android.deviceinfo.Device
import com.prasunmondal.lib.android.deviceinfo.DeviceInfo
import kotlinx.android.synthetic.main.activity_app_browser.*
import java.io.PrintWriter
import java.io.StringWriter
import java.util.*

class AppBrowser : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_browser)
        setSupportActionBar(toolbar)

        setActionbarTextColor()

        ToSheets.logs.post(listOf(LogActions.DOWNLOAD_START.name, "Metadata"), applicationContext)
        FileManagerUtil.Singleton.instance.metadata.download(::enableViewBreakdownButton)

        ErrorHandle().reportUnhandledException(applicationContext)

        val webView: WebView = findViewById(R.id.appBrowserView)
        webView.webViewClient = MyWebViewClient()
        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webView.settings.useWideViewPort = true
        webView.settings.loadWithOverviewMode = true
        webView.webViewClient = WebViewClient()
        webView.webChromeClient = WebChromeClient()
        loadPage(HardData.Singleton.instance.client_submitFormURL)

        AppContext.instance.initialContext = this

        supportActionBar!!.setDisplayShowTitleEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        disableViewBreakdownButton()
    }

    private fun disableViewBreakdownButton() {
        val button = findViewById<FloatingActionButton>(R.id.showBreakdowns)
        button.hide()
    }

    private fun showToast() {
        ToSheets.logs.post(
            listOf(
                LogActions.DISPLAY.name, "Breakview:FAILED:: ::Login to access this feature"
            ),
            applicationContext
        )
        Toast.makeText(this, "Login to access this feature.", Toast.LENGTH_LONG).show()
    }

    private fun enableViewBreakdownButton() {
        ToSheets.logs.post(
            listOf(LogActions.DOWNLOAD_COMPLETE.name, "Metadata"),
            applicationContext
        )
        promptAndInitiateUpdate(findViewById(R.id.appBrowserView))
        updateButtonData()
        val button = findViewById<FloatingActionButton>(R.id.showBreakdowns)
        button.show()
        if (!LocalConfig.Singleton.instance.doesUsernameExists()) {
            button.setOnClickListener {
                showToast()
            }
        }
    }

    private fun loadPage(url: String) {
        ToSheets.logs.post(listOf(LogActions.LOADING_URL.name, url), applicationContext)
        val webView: WebView = findViewById(R.id.appBrowserView)
        webView.webViewClient = object : WebViewClient() {

            override fun onReceivedError(
                view: WebView,
                errorCode: Int,
                description: String,
                failingUrl: String
            ) {
                Toast.makeText(this@AppBrowser, "Error:$description", Toast.LENGTH_SHORT).show()
            }
        }
        webView.loadUrl(url)
    }

    fun loadAddForm(view: View) {
        ToSheets.logs.post(listOf(LogActions.CLICKED.name, "Add Expense"), applicationContext)
        loadPage(HardData.Singleton.instance.client_submitFormURL)
    }

    fun loadDetails(view: View) {
        ToSheets.logs.post(listOf(LogActions.CLICKED.name, "Summary Page"), applicationContext)
        loadPage(HardData.Singleton.instance.detailsFormViewPage)
        Toast.makeText(this, "Fetching Data. Please Wait...", Toast.LENGTH_SHORT).show()
    }

    fun loadEditPage(view: View) {
        ToSheets.logs.post(listOf(LogActions.CLICKED.name, "Edit Page"), applicationContext)
        loadPage(HardData.Singleton.instance.client_editPage)
        Toast.makeText(this, "Fetching Data. Please Wait...", Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("DefaultLocale")
    fun onClickPayButton(view: View) {
        ToSheets.logs.post(listOf(LogActions.CLICKED.name, "Pay Button"), applicationContext)
        try {

            if (PaymentUtil.Singleton.instance.isPayOptionEnabled()) {
                goToPaymentOptionsPage()
                val currentUser =
                    LocalConfig.Singleton.instance.getValue(LocalConfig.Singleton.instance.USERNAME)!!
                        .toLowerCase()
                val amount =
                    FetchedMetaData.Singleton.instance.getValue(FetchedMetaData.Singleton.instance.TAG_PENDING_BILL + currentUser)!!
                val note =
                    FetchedMetaData.Singleton.instance.getValue(FetchedMetaData.Singleton.instance.PAYMENT_UPI_PAY_DESCRIPTION)
                val name =
                    FetchedMetaData.Singleton.instance.getValue(FetchedMetaData.Singleton.instance.PAYMENT_UPI_PAY_NAME)
                val upiId =
                    FetchedMetaData.Singleton.instance.getValue(FetchedMetaData.Singleton.instance.PAYMENT_UPI_PAY_UPIID)
                payUsingUpi(amount, upiId!!, name!!, note!!)
                ToSheets.logs.post(
                    listOf(LogActions.PAYMENT.name, "Initiated for Rs $amount"), applicationContext
                )
            } else if (PaymentUtil.Singleton.instance.isDisplayButtonEnabled()) {
                ToSheets.logs.post(
                    listOf(LogActions.PAYMENT.name, "No Due"), applicationContext
                )
                Toast.makeText(this, "No Payment Due", Toast.LENGTH_SHORT).show()
            }
        } catch (e: java.lang.Exception) {
            val sw = StringWriter()
            e.printStackTrace(PrintWriter(sw))
            val sStackTrace: String = sw.toString()
            ToSheets.logs.post(
                "Error after initiating payment:\n$sStackTrace",
                applicationContext
            )
        }
    }

    private fun promptAndInitiateUpdate(view: View) {
        var availableVers =
            FetchedMetaData.Singleton.instance.getValue(FetchedMetaData.Singleton.instance.APP_DOWNLOAD_VERSION)
        val apkUrl =
            FetchedMetaData.Singleton.instance.getValue(FetchedMetaData.Singleton.instance.APP_DOWNLOAD_LINK)
        val currentVers = BuildConfig.VERSION_CODE
        if (availableVers == null) {
            availableVers = currentVers.toString()
        }
        if (availableVers.toInt() > currentVers && apkUrl!!.isNotEmpty()) {
            ToSheets.logs.post(
                listOf(
                    LogActions.APP_UPDATE.name,
                    "Update Available - VERSION_CODE:$availableVers - URL:$apkUrl"
                ), applicationContext
            )
            view.showSnackbar(
                R.string.updateAvailable,
                Snackbar.LENGTH_INDEFINITE, R.string.update
            ) {
                downloadAndUpdate()
            }
        } else {
            ToSheets.logs.post(
                listOf(LogActions.APP_UPDATE.name, "No Update Available"),
                applicationContext
            )
        }
    }

    private fun downloadAndUpdate() {
        val i = Intent(this@AppBrowser, updateAppView::class.java)
        startActivity(i)
        finish()
    }

    @SuppressLint("DefaultLocale")
    fun updateButtonData() {
        val payBillBtn = findViewById<Button>(R.id.pay_bill_btn)
        var showString: String
        if (PaymentUtil.Singleton.instance.isAmountButtonVisible()) {
            val currentUser =
                LocalConfig.Singleton.instance.getValue(LocalConfig.Singleton.instance.USERNAME)!!
                    .toLowerCase()
            val payBill = PaymentUtil.Singleton.instance.getPendingBill(currentUser)
            val outstandingBal = PaymentUtil.Singleton.instance.getOutstandingAmount(currentUser)

            if (payBill != null) {
                if (payBill.toInt() > 0) {
                    showString = "Due: ₹ $payBill"
                    showString += "\n(click to pay)"
                    payBillBtn.backgroundTintList =
                        ColorStateList.valueOf(resources.getColor(R.color.infoBtn_paymentDue_bkg))
                    payBillBtn.setTextColor(resources.getColor(R.color.infoBtn_paymentDue_txt))
                } else {
                    showString = "You Get\n₹ " + (-1 * payBill.toInt()).toString()
                    payBillBtn.backgroundTintList =
                        ColorStateList.valueOf(resources.getColor(R.color.infoBtn_YouGet_bkg))
                    payBillBtn.setTextColor(resources.getColor(R.color.infoBtn_YouGet_txt))
                }
            } else if (outstandingBal != null) {
                showString = "Outstanding Bal\n₹ $outstandingBal"
                if (outstandingBal.toInt() > 0) {
                    payBillBtn.backgroundTintList =
                        ColorStateList.valueOf(resources.getColor(R.color.infoBtn_OutstandingPositive_bkg))
                    payBillBtn.setTextColor(resources.getColor(R.color.infoBtn_OutstandingPositive_txt))
                } else {
                    payBillBtn.backgroundTintList =
                        ColorStateList.valueOf(resources.getColor(R.color.infoBtn_OutstandingNegative_bkg))
                    payBillBtn.setTextColor(resources.getColor(R.color.infoBtn_OutstandingNegative_txt))
                }
            } else {
                showString = "Couldn't fetch data..."
            }
        } else {
            showString = "No User Configured..."
        }
        ToSheets.logs.post(
            listOf(LogActions.DISPLAY.name, "Dashboard::\n$showString"),
            applicationContext
        )
        payBillBtn.text = showString
    }

    private val UPI_PAYMENT = 0
    private fun payUsingUpi(amount: String, upiId: String, name: String, note: String) {

        val uri = Uri.parse("upi://pay").buildUpon()
            .appendQueryParameter("pa", upiId)
            .appendQueryParameter("pn", name)
            .appendQueryParameter("tn", note)
            .appendQueryParameter("am", amount)
            .appendQueryParameter("cu", "INR")
            .build()


        val upiPayIntent = Intent(Intent.ACTION_VIEW)
        upiPayIntent.data = uri

        // will always show a dialog to user to choose an app
        val chooser = Intent.createChooser(upiPayIntent, "Pay with")

        // check if intent resolves
        if (null != chooser.resolveActivity(packageManager)) {
            startActivityForResult(chooser, UPI_PAYMENT)
        } else {
            Toast.makeText(
                this@AppBrowser,
                "No UPI app found, please install one to continue",
                Toast.LENGTH_SHORT
            ).show()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            UPI_PAYMENT -> if (Activity.RESULT_OK == resultCode || resultCode == 11) {
                if (data != null) {
                    val trxt = data.getStringExtra("response")
                    Log.d("UPI", "onActivityResult: $trxt")
                    val dataList = ArrayList<String>()
                    if (trxt != null) {
                        dataList.add(trxt)
                    }
                    upiPaymentDataOperation(dataList)
                } else {
                    Log.d("UPI", "onActivityResult: " + "Return data is null")
                    val dataList = ArrayList<String>()
                    dataList.add("nothing")
                    upiPaymentDataOperation(dataList)
                }
            } else {
                Log.d(
                    "UPI",
                    "onActivityResult: " + "Return data is null"
                ) //when user simply back without payment
                val dataList = ArrayList<String>()
                dataList.add("nothing")
                upiPaymentDataOperation(dataList)
            }
        }
    }

    @SuppressLint("DefaultLocale")
    private fun upiPaymentDataOperation(data: ArrayList<String>) {
        if (isConnectionAvailable(this@AppBrowser)) {
            var str: String? = data[0]
            Log.d("UPIPAY", "upiPaymentDataOperation: " + str!!)
            var paymentCancel = ""
            if (str == null) str = "discard"
            var status = ""
            var approvalRefNo = ""
            val response = str.split("&".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            for (i in response.indices) {
                val equalStr =
                    response[i].split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                if (equalStr.size >= 2) {
                    if (equalStr[0].toLowerCase() == "Status".toLowerCase()) {
                        status = equalStr[1].toLowerCase()
                    } else if (equalStr[0].toLowerCase() == "ApprovalRefNo".toLowerCase() || equalStr[0].toLowerCase() == "txnRef".toLowerCase()) {
                        approvalRefNo = equalStr[1]
                    }
                } else {
                    paymentCancel = "Payment cancelled by user."
                }
            }

            if (status == "success") {
                //Code to handle successful transaction here.
//                Toast.makeText(this@MainActivity, "Transaction successful.", Toast.LENGTH_SHORT).show()
                Log.d("UPI", "responseStr: $approvalRefNo")
            } else if ("Payment cancelled by user." == paymentCancel) {
//                Toast.makeText(this@MainActivity, "Payment cancelled by user.", Toast.LENGTH_SHORT).show()
            } else {
//                Toast.makeText(this@MainActivity, "Transaction failed.Please try again", Toast.LENGTH_SHORT).show()
            }
        } else {
//            Toast.makeText(this@MainActivity, "Internet connection is not available. Please check and try again", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {

        @Suppress("DEPRECATION")
        fun isConnectionAvailable(context: Context): Boolean {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (connectivityManager != null) {
                val netInfo = connectivityManager.activeNetworkInfo
                if (netInfo != null && netInfo.isConnected
                    && netInfo.isConnectedOrConnecting
                    && netInfo.isAvailable
                ) {
                    return true
                }
            }
            return false
        }
    }

    fun goToPaymentOptionsPage() {
        val i = Intent(this@AppBrowser, ShowPaymentOptions::class.java)
        startActivity(i)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the main_menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id: Int = item.itemId
        if (id == R.id.action_favorite) {
            ToSheets.logs.post(LogActions.LOGOUT.name, applicationContext)
            ToSheets.logs.updatePrependList(
                listOf(
                    "MasterTrack", BuildConfig.VERSION_CODE.toString(), DeviceInfo.get(
                        Device.UNIQUE_ID
                    ), ""
                )
            )
            goToSaveUserPage()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    fun goToSaveUserPage() {
        LocalConfig.Singleton.instance.deleteData()
        val i = Intent(this@AppBrowser, SaveUser::class.java)
        startActivity(i)
        finish()
    }

    fun showBreakdowns(view: View) {
        val i = Intent(this@AppBrowser, TransactionsListing::class.java)
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

        findViewById<TextView>(R.id.toolbar_Text1).text = "MasterTrack"
        try {
            var user =
                LocalConfig.Singleton.instance.getValue(LocalConfig.Singleton.instance.USERNAME)
            if (user!!.isNotEmpty())
                findViewById<TextView>(R.id.toolbar_Text2).text = "- " + user
        } catch (e: Exception) {
            findViewById<TextView>(R.id.toolbar_Text2).text = "Anonymous"
        }
    }
}

class MyWebViewClient : WebViewClient() {
    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
        return false
    }
}