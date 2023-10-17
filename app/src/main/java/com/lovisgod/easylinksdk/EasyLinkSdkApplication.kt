package com.lovisgod.easylinksdk

import android.app.Application
import com.pax.gl.commhelper.impl.GLCommDebug
import com.paxsz.easylink.api.EasyLinkSdkManager
import pax.ecr.protocol.api.Debug

class EasyLinkSdkApplication {

    var manager: EasyLinkSdkManager? = null
    var instancex : EasyLinkSdkApplication? = null

    fun onCreate(application: Application){
      this.manager = EasyLinkSdkManager.getInstance(application)

      EasyLinkSdkManager.getInstance(application).setDebugMode(true)
      Debug.setDebugLevel(Debug.EDebugLevel.DEBUG_LEVEL_NONE)
      GLCommDebug.setDebugLevel(GLCommDebug.EDebugLevel.DEBUG_LEVEL_NONE)


        this.instancex = getInstance()

    }

    fun getInstance() : EasyLinkSdkApplication {
        return  EasyLinkSdkApplication()
    }


}