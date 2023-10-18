package com.lovisgod.easylinksdk

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.lovisgod.easylinksdk.MifarePlus.MifareEventListener
import com.lovisgod.easylinksdk.ui.BluetoothActivity
import com.lovisgod.easylinksdk.ui.MifareOneSetting
import com.lovisgod.easylinksdk.ui.SelectFileActivity
import com.pax.gl.commhelper.impl.GLCommDebug
import com.paxsz.easylink.api.EasyLinkSdkManager
import com.paxsz.easylink.listener.FileDownloadListener
import pax.ecr.protocol.api.Debug


class MainActivity : AppCompatActivity(), MifareEventListener {
    lateinit var testBtbtn: Button
    lateinit var testFileBtn: Button
    lateinit var testmifareBtn : Button
    lateinit var testMifareBalanceBtn: Button
    lateinit var testMifareLoadParamsBtn: Button

    lateinit var easyLinkSdkApplication: EasyLinkSdkApplication
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        easyLinkSdkApplication = EasyLinkSdkApplication.getInstance()
        easyLinkSdkApplication.setupMifarelistener(this)
        testBtbtn = findViewById(R.id.test_bt_btn)
        testFileBtn = findViewById(R.id.test_file_btn)
        testmifareBtn = findViewById(R.id.test_mifare_btn)
        testMifareBalanceBtn = findViewById(R.id.test_mifare_balance_btn)
        testMifareLoadParamsBtn = findViewById(R.id.test_mifare_param_btn)
        handleClick()
    }

    private fun handleClick() {
        testBtbtn.setOnClickListener {
           easyLinkSdkApplication.initiateConnection()
        }

        testFileBtn.setOnClickListener {
            val intent = Intent(this, SelectFileActivity::class.java)
            intent.putExtra("platform", "lite")
            startActivity(intent)
        }

        testmifareBtn.setOnClickListener {
           easyLinkSdkApplication.chargeCard(1000)
        }

        testMifareBalanceBtn.setOnClickListener {
            easyLinkSdkApplication.readCardBalance()
        }

        testMifareLoadParamsBtn.setOnClickListener {
            easyLinkSdkApplication.loadParamsToDevice(this, myFileDownloadListener())
        }
    }

    override fun onCardActivated(ret: Int) {
       runOnUiThread {
           Toast.makeText(this,
               "Card activation result ::: $ret",
               Toast.LENGTH_SHORT).show()
       }
    }

    override fun onCardChargeDone(ret: Int, value: String, usage: String, message: String?) {
       runOnUiThread {
           Toast.makeText(this,
               "charge done :: ret:: $ret::::value:::$value:::usage::$usage:::message:::$message",
               Toast.LENGTH_SHORT).show()
       }
    }

    override fun onCardBalanceRead(ret: Int, balance: String) {
       runOnUiThread {
           Toast.makeText(this,
               "balance read :::: ret:::$ret:::: balance:::${balance}",
               Toast.LENGTH_SHORT).show()
       }
    }

    override fun onCardTopped(ret: Int, value: String, message: String?) {
       runOnUiThread {
           Toast.makeText(this,
               "card topped :::: ret:::$ret::::value::::$value message:::${message}",
               Toast.LENGTH_SHORT).show()
       }
    }

}


 class myFileDownloadListener : FileDownloadListener {
    var percent: String? = null
    override fun onDownloadProgress(current: Int, total: Int) {
        percent = "$current/$total"
        println("${percent}")
//        runOnUiThread {
//            alertDialog1!!.setMessage(percent)
//            alertDialog1!!.show()
//        }
    }

    override fun cancelDownload(): Boolean {
        // TODO Auto-generated method stub
        return false
    }
}


class SampleApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        EasyLinkSdkApplication.getInstance().onCreate(this)

    }
}