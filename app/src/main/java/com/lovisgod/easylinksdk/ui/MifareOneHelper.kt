/*
 * ===========================================================================================
 * = COPYRIGHT
 *          PAX Computer Technology(Shenzhen) CO., LTD PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or nondisclosure
 *   agreement with PAX Computer Technology(Shenzhen) CO., LTD and may not be copied or
 *   disclosed except in accordance with the terms in that agreement.
 *     Copyright (C) YYYY-? PAX Computer Technology(Shenzhen) CO., LTD All rights reserved.
 * Description: // Detail description about the function of this module,
 *             // interfaces with the other modules, and dependencies.
 * Revision History:
 * Date                      Author                 Action
 * 20191127                  huangwp                Create
 *
 *
 * ============================================================================
 */
package com.lovisgod.easylinksdk.ui

import android.content.Context
import android.util.Log
import com.lovisgod.easylinksdk.MifarePlus.MifareEventListener
import com.lovisgod.easylinksdk.MifarePlus.MifareInternalEventListener
import com.lovisgod.easylinksdk.manage.ConfigManager
import com.lovisgod.easylinksdk.utils.ConstantsMifare
import com.lovisgod.easylinksdk.utils.MifareUtils
import com.lovisgod.easylinksdk.utils.ParameterUtils
import com.lovisgod.easylinksdk.utils.Utils
import com.paxsz.easylink.api.EasyLinkSdkManager
import com.paxsz.easylink.listener.FileDownloadListener
import com.paxsz.easylink.model.picc.EDetectMode
import com.paxsz.easylink.model.picc.ELedStatus
import com.paxsz.easylink.model.picc.EM1KeyType
import com.paxsz.easylink.model.picc.EM1OperateType
import com.paxsz.easylink.model.picc.EPiccRemoveMode
import com.paxsz.easylink.model.picc.PiccCardInfo

class MifareOneHelper private constructor():  MifareInternalEventListener {

    companion object {
        private var instance: MifareOneHelper? = null

        fun getInstance(): MifareOneHelper {
            if (instance == null) {
                synchronized(MifareOneHelper::class.java) {
                    if (instance == null) {
                        instance = MifareOneHelper()
                    }
                }
            }
            return instance!!
        }
    }

    private val TAG = "MifareOneSetting"
    private val m1keyPasswordType = "m1passwordType"
    private val m1keyPassword = "m1password"
    private val m1keyblkNo = "m1blkNo"
    private val m1keyblkValue = "blkValue"



    private var configManager: ConfigManager? = null
    private var func = 0
    private var passwordType: String? = null
    private var password: String? = null
    private var blkNo: String? = null
    private var blkValue: String? = null
    private var flowResult = ""
    private var timeRecord = ""
    private var beginTime: Long = 0
    private var finishTime: Long = 0
    private var mifareEventListener: MifareEventListener? = null
    var easyLink: EasyLinkSdkManager? = null




    fun setUp(applicationContext: Context) {
        configManager = ConfigManager.getInstance(applicationContext)
        easyLink = EasyLinkSdkManager.getInstance(applicationContext)
        password = configManager!!.getValueByTag("${m1keyPassword}${ConstantsMifare.KEY_BLOCK_NUMBER}", "FFFFFFFFFFFF")
        blkValue = configManager!!.getValueByTag(m1keyblkValue, "00000000ffffffff000000000AF50AF5")
    }

    fun setListener(mifareEventListener: MifareEventListener ) {
        this.mifareEventListener = mifareEventListener
    }


    fun loadParamsToDevice(context: Context, fileDownloadListener: FileDownloadListener) {
        ParameterUtils().loadParameterFiles(context, easyLink!!, fileDownloadListener)
    }

    fun activateCard() {
       resetDefaultKey()
    }

    fun readBalance() {
        Thread {
            var ret = easyLink!!.piccOpen()
            val flag = true
            val outPiccCardInfo = PiccCardInfo()
            Log.d(TAG, "piccOpen ret:$ret")
            easyLink!!.piccLight(0x02.toByte(), ELedStatus.ON)
            while (flag) {
                ret = easyLink!!.piccDetect(EDetectMode.ONLY_M, outPiccCardInfo)
                Log.d(TAG, "piccDetect ret:$ret")
                if (ret == 0) {
                    Log.d(
                        TAG,
                        "piccDetect getSerialInfo:" + Utils.bcd2Str(outPiccCardInfo.serialInfo)
                    )
                    Log.d(TAG, "piccDetect getCardType:" + outPiccCardInfo.cardType)
                    Log.d(TAG, "piccDetect getOther:" + Utils.bcd2Str(outPiccCardInfo.other))
                    break
                }
            }
            val keyType: EM1KeyType = EM1KeyType.TYPE_A
            val password = configManager!!.getValueByTag(
                "${m1keyPassword}${ConstantsMifare.KEY_BLOCK_NUMBER}",
                "FFFFFFFFFFFF"
            )
            //                val password = "FAFFFAFFFFFA"
            ret = easyLink!!.piccM1Authority(
                keyType,
                ConstantsMifare.VALUE_BLOCK_NUMBER.toByte(),
                Utils.str2Bcd(password.toString()),
                outPiccCardInfo.serialInfo
            )
            Log.d(TAG, "piccM1Authority ret:$ret")
            val readBlkValue = ByteArray(16)
            Log.d("readxxx", "readblockvalue:: ${Utils.bcd2Str(readBlkValue)}")
            ret = easyLink!!.piccM1ReadBlock(
                ConstantsMifare.VALUE_BLOCK_NUMBER.toByte(),
                readBlkValue
            )
            Log.d("readxxxxx", "readblockvalue:: ${Utils.bcd2Str(readBlkValue)}")
            if (ret == 0) {
                val balanceString = Utils.bcd2Str(readBlkValue).take(8)
                Log.d("readxxxxx", "balance string:: ${balanceString}")
                val balanceByte = MifareUtils.hexadecimalStringToByteArray(balanceString)
                val balanceDecimal = MifareUtils.byteArrayHexStringToDecimal(balanceByte)
                this@MifareOneHelper.onCardBalanceRead(ret, balanceDecimal.toString())
                Log.d("readxxxxx", "balance decimal string:: ${balanceDecimal.toString()}")
            }
            Log.d(TAG, "piccM1ReadBlock ret:$ret")
            ret = easyLink!!.piccRemove(EPiccRemoveMode.REMOVE, 0.toByte())
            Log.d(TAG, "piccRemove ret:$ret")
            easyLink!!.piccClose()
            easyLink!!.piccLight(0x02.toByte(), ELedStatus.OFF)
        }.start()
    }

    fun charge(value: Int) {
        Thread {
            var ret = easyLink!!.piccOpen()
            val flag = true
            val outPiccCardInfo = PiccCardInfo()
            Log.d(TAG, "piccOpen ret:$ret")
            easyLink!!.piccLight(0x02.toByte(), ELedStatus.ON)
            while (flag) {
                ret = easyLink!!.piccDetect(EDetectMode.ONLY_M, outPiccCardInfo)
                Log.d(TAG, "piccDetect ret:$ret")
                if (ret == 0) {
                    Log.d(
                        TAG,
                        "piccDetect getSerialInfo:" + Utils.bcd2Str(outPiccCardInfo.serialInfo)
                    )
                    Log.d(TAG, "piccDetect getCardType:" + outPiccCardInfo.cardType)
                    Log.d(TAG, "piccDetect getOther:" + Utils.bcd2Str(outPiccCardInfo.other))
                    break
                }
            }
            val keyType: EM1KeyType = EM1KeyType.TYPE_A
            val password = configManager!!.getValueByTag(
                "${m1keyPassword}${ConstantsMifare.KEY_BLOCK_NUMBER}",
                "FFFFFFFFFFFF"
            )
            //                val password = "FFFFFFFFFFFF"
            println("password is :::: ${password}")
            ret = easyLink!!.piccM1Authority(
                keyType,
                ConstantsMifare.VALUE_BLOCK_NUMBER.toByte(),
                Utils.str2Bcd(password.toString()),
                outPiccCardInfo.serialInfo
            )
            Log.d(TAG, "piccM1Authority ret:$ret")
            if (ret == 0) {
                val readBlkValue = ByteArray(16)
                Log.d("readxxx", "readblockvalue:: ${Utils.bcd2Str(readBlkValue)}")
                ret = easyLink!!.piccM1ReadBlock(
                    ConstantsMifare.VALUE_BLOCK_NUMBER.toByte(),
                    readBlkValue
                )
                Log.d("readxxxxx", "readblockvalue:: ${Utils.bcd2Str(readBlkValue)}")
                if (ret == 0) {
                    val balanceString = Utils.bcd2Str(readBlkValue).take(8)
                    Log.d("readxxxxx", "balance string:: ${balanceString}")
                    val balanceByte = MifareUtils.hexadecimalStringToByteArray(balanceString)
                    val balanceDecimal = MifareUtils.byteArrayHexStringToDecimal(balanceByte)
                    Log.d("readxxxxx", "balance decimal string:: ${balanceDecimal.toString()}")

                    if (balanceDecimal >= value) {
                        val readBlkValuex = ByteArray(16)
                        ret = easyLink!!.piccM1Operate(
                            EM1OperateType.DECREMENT,
                            ConstantsMifare.VALUE_BLOCK_NUMBER.toByte(),
                            MifareUtils.decimalToByteArray(value),
                            ConstantsMifare.VALUE_BLOCK_NUMBER.toByte()
                        )
                        Log.d(TAG, "piccM1Operate decreament ret:$ret")
                        this@MifareOneHelper.onCardChargeDone(ret, value.toString(), "charge")
                        if (ret == 0) {
                            ret = easyLink!!.piccM1ReadBlock(
                                ConstantsMifare.VALUE_BLOCK_NUMBER.toByte(),
                                readBlkValuex
                            )

                            Log.d(TAG, "piccM1ReadBlock ret:$ret")
                            Log.d("readxxx", "readblockvalue:: ${Utils.bcd2Str(readBlkValuex)}")
                        }
                    } else {
                        this@MifareOneHelper.onCardChargeDone(
                            ConstantsMifare.INSUFFICIENT_BALANCE_CODE,
                            value.toString(),
                            "charge",
                            "Insufficient Balance"
                        )
                    }
                }
            }

            easyLink!!.piccClose()
            easyLink!!.piccLight(0x02.toByte(), ELedStatus.OFF)
        }.start()
    }

    // our value tag is created in block two
    private fun resetDefaultKey() {
         val value: String = "FAFFFAFFFFFA"
        Thread {
            var ret = easyLink!!.piccOpen()
            val flag = true
            val outPiccCardInfo = PiccCardInfo()
            Log.d(TAG, "piccOpen ret:$ret")
            flowResult += "piccOpen ret ="
            flowResult += ret
            flowResult += "\n"
            easyLink!!.piccLight(0x02.toByte(), ELedStatus.ON)
            while (flag) {
                ret = easyLink!!.piccDetect(EDetectMode.ONLY_M, outPiccCardInfo)
                Log.d(TAG, "piccDetect ret:$ret")
                if (ret == 0) {
                    Log.d(TAG, "piccDetect serial info:" + outPiccCardInfo.serialInfo)
                    Log.d(TAG, "piccDetect getCardType:" + outPiccCardInfo.cardType)
                    break
                }
            }
            val password = "FFFFFFFFFFFF"
            println("Password is ::: $password")
            val keyType: EM1KeyType = EM1KeyType.TYPE_A
            ret = easyLink!!.piccM1Authority(
                keyType,
                ConstantsMifare.KEY_BLOCK_NUMBER.toByte(),
                Utils.str2Bcd(password.toString()),
                outPiccCardInfo.serialInfo
            )
            Log.d(TAG, "authority ret:$ret")
            ret = easyLink!!.piccM1WriteBlock(
                ConstantsMifare.KEY_BLOCK_NUMBER.toByte(),
                Utils.str2Bcd(value + "FF078069FFFFFFFFFFFF")
            )
            if (ret == 0) {
                configManager!!.saveTagValue(
                    "${m1keyPassword}${ConstantsMifare.KEY_BLOCK_NUMBER}",
                    value
                )
            }
            this@MifareOneHelper.onCardResetDone(ret)

            Log.d(TAG, "change key ret:$ret")
            flowResult += "change key ret ="
            flowResult += ret
            easyLink!!.piccClose()
            easyLink!!.piccLight(0x02.toByte(), ELedStatus.OFF)
        }.start()
        return
    }

    // our value tag is created in block two
    fun createValueBlock(value: String = "00000000ffffffff000000000AF50AF5") {
        Thread {
            var ret = easyLink!!.piccOpen()
            val flag = true
            val outPiccCardInfo = PiccCardInfo()
            Log.d(TAG, "piccOpen ret:$ret")
            flowResult += "piccOpen ret ="
            flowResult += ret
            flowResult += "\n"
            easyLink!!.piccLight(0x02.toByte(), ELedStatus.ON)
            while (flag) {
                ret = easyLink!!.piccDetect(EDetectMode.ONLY_M, outPiccCardInfo)
                Log.d(TAG, "piccDetect ret:$ret")
                if (ret == 0) {
                    Log.d(TAG, "piccDetect serial info:" + outPiccCardInfo.serialInfo)
                    Log.d(TAG, "piccDetect getCardType:" + outPiccCardInfo.cardType)
                    break
                }
            }
            val password = configManager!!.getValueByTag(
                "${m1keyPassword}${ConstantsMifare.KEY_BLOCK_NUMBER}",
                "FFFFFFFFFFFF"
            )
            //                val password = "FFFFFFFFFFFF"
            val keyType: EM1KeyType = EM1KeyType.TYPE_A
            ret = easyLink!!.piccM1Authority(
                keyType,
                ConstantsMifare.VALUE_BLOCK_NUMBER.toByte(),
                Utils.str2Bcd(password.toString()),
                outPiccCardInfo.serialInfo
            )
            Log.d(TAG, "piccM1Authority ret:$ret")
            flowResult += "piccM1Authority ret ="
            flowResult += ret
            flowResult += "\n"
            val readBlkValue = ByteArray(16)
            ret = easyLink!!.piccM1WriteBlock(
                ConstantsMifare.VALUE_BLOCK_NUMBER.toByte(),
                Utils.str2Bcd(value)
            )
            Log.d(TAG, "piccM1writeBlock write value ret:$ret")
            flowResult += "piccM1writeBlock ret = $ret\n"
            ret = easyLink!!.piccM1ReadBlock(
                ConstantsMifare.VALUE_BLOCK_NUMBER.toByte(),
                readBlkValue
            )
            Log.d(TAG, "piccM1ReadBlock ret:$ret")
            easyLink!!.piccClose()
            easyLink!!.piccLight(0x02.toByte(), ELedStatus.OFF)
        }.start()
        return
    }



     fun addValue(value: String) {
        Thread {
            var ret = easyLink!!.piccOpen()
            val flag = true
            val outPiccCardInfo = PiccCardInfo()
            Log.d(TAG, "piccOpen ret:$ret")
            easyLink!!.piccLight(0x02.toByte(), ELedStatus.ON)
            while (flag) {
                ret = easyLink!!.piccDetect(EDetectMode.ONLY_M, outPiccCardInfo)
                Log.d(TAG, "piccDetect ret:$ret")
                if (ret == 0) {
                    Log.d(
                        TAG,
                        "piccDetect getSerialInfo:" + Utils.bcd2Str(outPiccCardInfo.serialInfo)
                    )
                    Log.d(TAG, "piccDetect getCardType:" + outPiccCardInfo.cardType)
                    Log.d(TAG, "piccDetect getOther:" + Utils.bcd2Str(outPiccCardInfo.other))
                    break
                }
            }
            val keyType = EM1KeyType.TYPE_A

            val password = configManager!!.getValueByTag(
                "${m1keyPassword}${ConstantsMifare.KEY_BLOCK_NUMBER}",
                "FFFFFFFFFFFF"
            )

            ret = easyLink!!.piccM1Authority(
                keyType,
                ConstantsMifare.VALUE_BLOCK_NUMBER.toByte(),
                Utils.str2Bcd(password.toString()),
                outPiccCardInfo.serialInfo
            )
            if (ret == 0) {
                Log.d(TAG, "piccM1Authority ret:$ret")
                val readBlkValue = ByteArray(16)
                ret = easyLink!!.piccM1Operate(
                    EM1OperateType.INCREMENT,
                    ConstantsMifare.VALUE_BLOCK_NUMBER.toByte(),
                    byteArrayOf(0x10, 0x27, 0x00, 0x00),
                    ConstantsMifare.VALUE_BLOCK_NUMBER.toByte(),
                )

                if (ret == 0) {
                    Log.d(TAG, "piccM1Operate increament ret:$ret")
                    ret = easyLink!!.piccM1ReadBlock(
                        ConstantsMifare.VALUE_BLOCK_NUMBER.toByte(),
                        readBlkValue
                    )
                    Log.d(TAG, "piccM1ReadBlock ret:$ret")
                }
            }

            easyLink!!.piccClose()
            easyLink!!.piccLight(0x02.toByte(), ELedStatus.OFF)
        }.start()
    }


    override fun onCardResetDone(ret: Int) {
        println("on card key reset done ::::: ret == $ret")
        this.mifareEventListener?.onCardActivated(ret)
    }

    override fun onCardReadDone(ret: Int, value: String, usage: String) {
       println("on card read done ::::: ret == $ret ::::: usage == $usage ::::: value == $value")

    }

    override fun onCardChargeDone(ret: Int, value: String, usage: String, message: String?) {
        println("on card charge done ::::: ret == $ret ::::: usage == $usage ::::: value == $value ::::: message == $message")
        this.mifareEventListener?.onCardChargeDone(ret, value, usage, message)
    }

    override fun onCardBalanceRead(ret: Int, balance: String) {
        println("on card balance done ::::: ret == $ret :::::balance == $balance")
        this.mifareEventListener?.onCardBalanceRead(ret, balance)
    }

    override fun onCardTopped(ret: Int, value: String, message: String?) {
        println("on card topped done ::::: ret == $ret :::::value == $value ::::: message == $message")
        this.mifareEventListener?.onCardTopped(ret, value, message)
    }

}