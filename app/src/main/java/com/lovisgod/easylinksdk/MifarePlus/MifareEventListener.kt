package com.lovisgod.easylinksdk.MifarePlus

interface MifareEventListener {
    fun onCardActivated(ret: Int, serialInfo: String)
    fun onCardChargeDone(ret: Int, value: String, usage: String, serialInfo: String, message: String? = "")

    fun onCardBalanceRead(ret: Int, balance: String, serialInfo: String)

    fun onCardTopped(ret: Int, value: String, serialInfo: String, message: String? = "")
}


interface MifareInternalEventListener {
    fun onCardResetDone(ret: Int, serialInfo: String)
    fun onCardReadDone(ret: Int, value: String, usage: String, serialInfo: String)
    fun onCardChargeDone(ret: Int, value: String, usage: String, serialInfo: String, message: String? = "")

    fun onCardBalanceRead(ret: Int, balance: String, serialInfo: String)

    fun onCardTopped(ret: Int, value: String, serialInfo: String, message: String? = "")

}