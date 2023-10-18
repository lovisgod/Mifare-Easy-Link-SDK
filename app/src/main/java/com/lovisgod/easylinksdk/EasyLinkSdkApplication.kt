package com.lovisgod.easylinksdk

import android.app.Application
import android.content.Context
import android.content.Intent
import com.lovisgod.easylinksdk.MifarePlus.MifareEventListener
import com.lovisgod.easylinksdk.ui.BluetoothActivity
import com.lovisgod.easylinksdk.ui.MifareOneHelper
import com.pax.gl.commhelper.impl.GLCommDebug
import com.paxsz.easylink.api.EasyLinkSdkManager
import com.paxsz.easylink.listener.FileDownloadListener
import pax.ecr.protocol.api.Debug

class EasyLinkSdkApplication private constructor(){

    companion object {
        private var instance: EasyLinkSdkApplication? = null
        private var app : Application? = null

        fun getInstance(): EasyLinkSdkApplication {
            if (instance == null) {
                synchronized(EasyLinkSdkApplication::class.java) {
                    if (instance == null) {
                        instance = EasyLinkSdkApplication()
                    }
                }
            }
            return instance!!
        }
    }

    private val mifareOneHelper = MifareOneHelper.getInstance()

    var manager: EasyLinkSdkManager? = null

    fun onCreate(application: Application){
      this.manager = EasyLinkSdkManager.getInstance(application)
      app = application
      EasyLinkSdkManager.getInstance(application).setDebugMode(true)
      Debug.setDebugLevel(Debug.EDebugLevel.DEBUG_LEVEL_NONE)
      GLCommDebug.setDebugLevel(GLCommDebug.EDebugLevel.DEBUG_LEVEL_NONE)

      mifareOneHelper.setUp(application.applicationContext)

    }

    fun setupMifarelistener(mifareEventListener: MifareEventListener) {
        mifareOneHelper.setListener(mifareEventListener)
    }

    private fun showScreen(clazz: Class<*>) {
        val intent = Intent(app, clazz).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        app?.startActivity(intent)
    }


    fun activateCard() = mifareOneHelper.activateCard()

    fun readCardBalance() = mifareOneHelper.readBalance()

    fun chargeCard(value: Int) = mifareOneHelper.charge(value)

    fun createValueBlock() = mifareOneHelper.createValueBlock()

    fun addValue(value: Int) = mifareOneHelper.addValue(value.toString())

    fun initiateConnection() = showScreen(BluetoothActivity::class.java)

    fun loadParamsToDevice(context: Context, fileDownloadListener: FileDownloadListener) =
        mifareOneHelper.loadParamsToDevice(context, fileDownloadListener)

}