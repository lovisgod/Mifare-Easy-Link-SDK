package com.lovisgod.easylinksdk.MifarePlus

interface MifareEventListener {
    fun onCardActivated(ret: Int)
}


interface MifareInternalEventListener {
    fun onCardResetDone(ret: Int)
    fun onCardReadDone(ret: Int, value: String, usage: String)
    fun onCardChargeDone(ret: Int, value: String, usage: String)
}