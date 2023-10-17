package com.lovisgod.easylinksdk.MifarePlus

interface MifareEventListener {
    fun onCardActivated(ret: Int)
    fun onCardChargeDone(ret: Int, value: String, usage: String, message: String? = "")

    fun onCardBalanceRead(ret: Int, balance: String)

    fun onCardTopped(ret: Int, value: String, message: String? = "")
}


interface MifareInternalEventListener {
    fun onCardResetDone(ret: Int)
    fun onCardReadDone(ret: Int, value: String, usage: String)
    fun onCardChargeDone(ret: Int, value: String, usage: String, message: String? = "")

    fun onCardBalanceRead(ret: Int, balance: String)

    fun onCardTopped(ret: Int, value: String, message: String? = "")

}