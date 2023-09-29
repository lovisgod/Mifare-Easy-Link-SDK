package com.lovisgod.easylinksdk

import android.app.Application
import com.paxsz.easylink.api.EasyLinkSdkManager

class EasyLinkSdkApplication {

    var manager: EasyLinkSdkManager? = null
    var instancex : EasyLinkSdkApplication? = null

    fun onCreate(application: Application){
      this.instancex = getInstance()
      this.manager = EasyLinkSdkManager.getInstance(application)
    }

    fun getInstance() : EasyLinkSdkApplication {
        return  this
    }
}