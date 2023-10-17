package com.lovisgod.easylinksdk

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.lovisgod.easylinksdk.ui.BluetoothActivity
import com.lovisgod.easylinksdk.ui.MifareOneSetting
import com.lovisgod.easylinksdk.ui.SelectFileActivity
import com.pax.gl.commhelper.impl.GLCommDebug
import com.paxsz.easylink.api.EasyLinkSdkManager
import pax.ecr.protocol.api.Debug


class MainActivity : AppCompatActivity() {
    lateinit var testBtbtn: Button
    lateinit var testFileBtn: Button
    lateinit var testmifareBtn : Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        testBtbtn = findViewById(R.id.test_bt_btn)
        testFileBtn = findViewById(R.id.test_file_btn)
        testmifareBtn = findViewById(R.id.test_mifare_btn)
        handleClick()
    }

    private fun handleClick() {
        testBtbtn.setOnClickListener {
            val intent = Intent(this, BluetoothActivity::class.java)
            startActivity(intent)
        }

        testFileBtn.setOnClickListener {
            val intent = Intent(this, SelectFileActivity::class.java)
            intent.putExtra("platform", "lite")
            startActivity(intent)
        }

        testmifareBtn.setOnClickListener {
            val intent = Intent(this, MifareOneSetting::class.java)
//            intent.putExtra("platform", "lite")
            startActivity(intent)
        }
    }
}


class SampleApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        EasyLinkSdkApplication().onCreate(this)

    }
}