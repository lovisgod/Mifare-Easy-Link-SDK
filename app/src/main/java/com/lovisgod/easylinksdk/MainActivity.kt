package com.lovisgod.easylinksdk

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.lovisgod.easylinksdk.ui.BluetoothActivity
import com.pax.gl.commhelper.impl.GLCommDebug
import com.paxsz.easylink.api.EasyLinkSdkManager
import pax.ecr.protocol.api.Debug


class MainActivity : AppCompatActivity() {
    lateinit var testBtbtn: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        testBtbtn = findViewById(R.id.test_bt_btn)

        handleClick()
    }

    private fun handleClick() {
        testBtbtn.setOnClickListener {
            val intent = Intent(this, BluetoothActivity::class.java)
            startActivity(intent)
        }
    }
}


class SampleApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        EasyLinkSdkApplication().onCreate(this)
        
        EasyLinkSdkManager.getInstance(this).setDebugMode(true)
        Debug.setDebugLevel(Debug.EDebugLevel.DEBUG_LEVEL_NONE)
        GLCommDebug.setDebugLevel(GLCommDebug.EDebugLevel.DEBUG_LEVEL_NONE)
    }
}