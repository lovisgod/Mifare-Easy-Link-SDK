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

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.lovisgod.easylinksdk.MifarePlus.MifareEventListener
import com.lovisgod.easylinksdk.MifarePlus.MifareInternalEventListener
import com.lovisgod.easylinksdk.R
import com.lovisgod.easylinksdk.manage.ConfigManager
import com.lovisgod.easylinksdk.utils.ConstantsMifare
import com.lovisgod.easylinksdk.utils.MifareUtils
import com.lovisgod.easylinksdk.utils.Utils
import com.paxsz.easylink.api.EasyLinkSdkManager
import com.paxsz.easylink.model.picc.EDetectMode
import com.paxsz.easylink.model.picc.ELedStatus
import com.paxsz.easylink.model.picc.EM1KeyType
import com.paxsz.easylink.model.picc.EM1OperateType
import com.paxsz.easylink.model.picc.EPiccRemoveMode
import com.paxsz.easylink.model.picc.PiccCardInfo

class MifareOneSetting() : Activity(), View.OnClickListener, MifareInternalEventListener {
    private var configManager: ConfigManager? = null
    private var func = 0
    private var etPasswordType: EditText? = null
    private var etPassword: EditText? = null
    private var etBlockNo: EditText? = null
    private var etBlockValue: EditText? = null
    private var btnAll: Button? = null
    private var btnRead: Button? = null
    private var btnWrite: Button? = null
    private var btnInc: Button? = null
    private var btnDec: Button? = null
    private var btnActivate: Button? = null
    private var btnCharge: Button? = null
    private var btnBalance: Button? = null
    private var btnvalue: Button? = null
    private var textInfoView: TextView? = null
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mifare_one_setting)
        etPasswordType = findViewById(R.id.et_password_type)
        etPassword = findViewById(R.id.et_password)
        etBlockNo = findViewById(R.id.et_block_no)
        etBlockValue = findViewById(R.id.et_block_value)
        textInfoView = findViewById(R.id.textViewBase)
        btnAll = findViewById<Button>(R.id.btn_all)
        btnRead = findViewById(R.id.btn_read)
        btnWrite = findViewById(R.id.btn_write)
        btnInc = findViewById(R.id.btn_increament)
        btnDec = findViewById(R.id.btn_decreament)
        btnActivate = findViewById(R.id.btn_activate)
        btnCharge = findViewById(R.id.btn_charge)
        btnBalance = findViewById(R.id.btn_balnce)
        btnvalue = findViewById(R.id.btn_createValue)
        btnAll!!.setOnClickListener(this)
        btnRead!!.setOnClickListener(this)
        btnWrite!!.setOnClickListener(this)
        btnInc!!.setOnClickListener(this)
        btnDec!!.setOnClickListener(this)
        btnActivate!!.setOnClickListener(this)
        btnCharge!!.setOnClickListener(this)
        btnBalance!!.setOnClickListener(this)
        btnvalue!!.setOnClickListener(this)
        val intent = intent
        val tv = findViewById<View>(R.id.list_title) as TextView
        tv.text = "Mifare One"
        configManager = ConfigManager.getInstance(applicationContext)
        func = intent.getIntExtra("FUNC", 0)
        setEditText()
        easyLink = EasyLinkSdkManager.getInstance(this)
    }

    fun setUp(mifareEventListener: MifareEventListener) {
        this.mifareEventListener = mifareEventListener
    }

    private fun setEditText() {
        passwordType = configManager!!.getValueByTag(m1keyPasswordType, "A")
        password = configManager!!.getValueByTag(m1keyPassword, "FFFFFFFFFFFF")
        blkNo = configManager!!.getValueByTag(m1keyblkNo, "12")
        blkValue = configManager!!.getValueByTag(m1keyblkValue, "00000000ffffffff000000000AF50AF5")
        etPasswordType!!.setText(passwordType)
        etPassword!!.setText(password)
        etBlockNo!!.setText(blkNo)
        etBlockValue!!.setText(blkValue)
    }

    private fun saveKeyData() {
        configManager!!.saveTagValue(m1keyPasswordType, etPasswordType!!.text.toString())
        configManager!!.saveTagValue(m1keyPassword, etPassword!!.text.toString())
        configManager!!.saveTagValue(m1keyblkNo, etBlockNo!!.text.toString())
        configManager!!.saveTagValue(m1keyblkValue, etBlockValue!!.text.toString())
    }

    override fun onClick(view: View) {
        timeRecord = ""
        flowResult = ""
        textInfoView!!.text = "Please Tap Card"
        when (view.id) {
            R.id.btn_all -> {
                Log.d(TAG, "password type:" + etPasswordType!!.text.toString())
                Log.d(TAG, "password :" + etPassword!!.text.toString())
                Log.d(TAG, "block no :" + etBlockNo!!.text.toString())
                Log.d(TAG, "block value :" + etBlockValue!!.text.toString())
                saveKeyData()
                //                MifarePlusApdu mifarePlusApdu = new MifarePlusApdu();
                //mifarePlusApdu.testAesCbcEncrypt();
//                mifarePlusApdu.macTest();
                testPiccAll()
            }

            R.id.btn_write -> testPiccWrite()
            R.id.btn_read -> testPiccRead()
            R.id.btn_increament -> testPiccIncreament()
            R.id.btn_decreament -> testPiccDecreament()
            R.id.btn_activate -> activateCard()
            R.id.btn_charge -> charge(10000)
            R.id.btn_balnce -> readBalance()
            R.id.btn_createValue -> createValueBlock()
            else -> {}
        }
    }

    fun activateCard() {
       resetDefaultKey()
    }

    private fun readBalance() {
        Thread(object : Runnable {
            override fun run() {
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
                val password = configManager!!.getValueByTag("${m1keyPassword}${ConstantsMifare.KEY_BLOCK_NUMBER}", "FFFFFFFFFFFF")
//                val password = "FAFFFAFFFFFA"
                ret = easyLink!!.piccM1Authority(
                    keyType,
                   ConstantsMifare.VALUE_BLOCK_NUMBER.toByte(),
                    Utils.str2Bcd(password.toString()),
                    outPiccCardInfo.serialInfo
                )
                Log.d(TAG, "piccM1Authority ret:$ret")
                val readBlkValue = ByteArray(16)
                Log.d("readxxx","readblockvalue:: ${Utils.bcd2Str(readBlkValue)}")
                ret = easyLink!!.piccM1ReadBlock(
                   ConstantsMifare.VALUE_BLOCK_NUMBER.toByte(),
                    readBlkValue
                )
                Log.d("readxxxxx","readblockvalue:: ${Utils.bcd2Str(readBlkValue)}")
                if (ret == 0) {
                   val balanceString = Utils.bcd2Str(readBlkValue).take(8)
                   Log.d("readxxxxx","balance string:: ${balanceString}")
                   val balanceByte = MifareUtils.hexadecimalStringToByteArray(balanceString)
                   val balanceDecimal = MifareUtils.byteArrayHexStringToDecimal(balanceByte)
                   this@MifareOneSetting.onCardBalanceRead(ret, balanceDecimal.toString())
                   Log.d("readxxxxx","balance decimal string:: ${balanceDecimal.toString()}")
                }
                Log.d(TAG, "piccM1ReadBlock ret:$ret")
                ret = easyLink!!.piccRemove(EPiccRemoveMode.REMOVE, 0.toByte())
                Log.d(TAG, "piccRemove ret:$ret")
                easyLink!!.piccClose()
                easyLink!!.piccLight(0x02.toByte(), ELedStatus.OFF)
            }
        }).start()
    }

    fun charge(value: Int) {
        Thread(object : Runnable {
            override fun run() {
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
                val password = configManager!!.getValueByTag("${m1keyPassword}${ConstantsMifare.KEY_BLOCK_NUMBER}", "FFFFFFFFFFFF")
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
                    Log.d("readxxx","readblockvalue:: ${Utils.bcd2Str(readBlkValue)}")
                    ret = easyLink!!.piccM1ReadBlock(
                        ConstantsMifare.VALUE_BLOCK_NUMBER.toByte(),
                        readBlkValue
                    )
                    Log.d("readxxxxx","readblockvalue:: ${Utils.bcd2Str(readBlkValue)}")
                    if (ret == 0) {
                        val balanceString = Utils.bcd2Str(readBlkValue).take(8)
                        Log.d("readxxxxx","balance string:: ${balanceString}")
                        val balanceByte = MifareUtils.hexadecimalStringToByteArray(balanceString)
                        val balanceDecimal = MifareUtils.byteArrayHexStringToDecimal(balanceByte)
                        Log.d("readxxxxx","balance decimal string:: ${balanceDecimal.toString()}")

                        if (balanceDecimal >= value) {
                            val readBlkValuex = ByteArray(16)
                            ret = easyLink!!.piccM1Operate(
                                EM1OperateType.DECREMENT,
                                ConstantsMifare.VALUE_BLOCK_NUMBER.toByte(),
                                MifareUtils.decimalToByteArray(value),
                                ConstantsMifare.VALUE_BLOCK_NUMBER.toByte()
                            )
                            Log.d(TAG, "piccM1Operate decreament ret:$ret")
                            this@MifareOneSetting.onCardChargeDone(ret, value.toString(), "charge")
                            if (ret == 0) {
                                ret = easyLink!!.piccM1ReadBlock(
                                    ConstantsMifare.VALUE_BLOCK_NUMBER.toByte(),
                                    readBlkValuex
                                )

                                Log.d(TAG, "piccM1ReadBlock ret:$ret")
                                Log.d("readxxx","readblockvalue:: ${Utils.bcd2Str(readBlkValuex)}")
                            }
                        } else {
                            this@MifareOneSetting.onCardChargeDone(ConstantsMifare.INSUFFICIENT_BALANCE_CODE, value.toString(), "charge", "Insufficient Balance")
                        }
                    }
                }

                easyLink!!.piccClose()
                easyLink!!.piccLight(0x02.toByte(), ELedStatus.OFF)
            }
        }).start()
    }

    // our value tag is created in block two
    fun resetDefaultKey() {
         val value: String = "FAFFFAFFFFFA"
        Thread(object : Runnable {
            override fun run() {
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
                    configManager!!.saveTagValue("${m1keyPassword}${ConstantsMifare.KEY_BLOCK_NUMBER}", value)
                }
                this@MifareOneSetting.onCardResetDone(ret)

                Log.d(TAG, "change key ret:$ret")
                flowResult += "change key ret ="
                flowResult += ret
                easyLink!!.piccClose()
                easyLink!!.piccLight(0x02.toByte(), ELedStatus.OFF)
            }
        }).start()
        return
    }

    // our value tag is created in block two
    fun createValueBlock(value: String = "00000000ffffffff000000000AF50AF5") {
        Thread(object : Runnable {
            override fun run() {
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
                val password = configManager!!.getValueByTag("${m1keyPassword}${ConstantsMifare.KEY_BLOCK_NUMBER}", "FFFFFFFFFFFF")
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
            }
        }).start()
        return
    }

    private fun testPiccAll() {
        Thread(object : Runnable {
            override fun run() {
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
                        Log.d(
                            TAG,
                            "piccDetect getSerialInfo:" + Utils.bcd2Str(outPiccCardInfo.serialInfo)
                        )
                        Log.d(TAG, "piccDetect getCardType:" + outPiccCardInfo.cardType)
                        Log.d(TAG, "piccDetect getOther:" + Utils.bcd2Str(outPiccCardInfo.other))
                        beginTime = System.currentTimeMillis()
                        timeRecord += "begin time:"
                        timeRecord += beginTime.toString()
                        timeRecord += "\n"
                        break
                    }
                }
                val keyType: EM1KeyType
                if (("A" == etPasswordType!!.text.toString())) {
                    keyType = EM1KeyType.TYPE_A
                } else {
                    keyType = EM1KeyType.TYPE_B
                }
                ret = easyLink!!.piccM1Authority(
                    keyType,
                    etBlockNo!!.text.toString().toInt()
                        .toByte(),
                    Utils.str2Bcd(etPassword!!.text.toString()),
                    outPiccCardInfo.serialInfo
                )
                Log.d(TAG, "piccM1Authority ret:$ret")
                flowResult += "piccM1Authority ret ="
                flowResult += ret
                flowResult += "\n"
                val readBlkValue = ByteArray(16)
                //                ret = easyLink.piccM1ReadBlock((byte)Integer.parseInt(etBlockNo.getText().toString()),readBlkValue);
//                Log.d(TAG,"piccM1ReadBlock ret:" + ret);
//                flowResult += "piccM1ReadBlock ret =";
//                flowResult += ret +"\nread blkdata:";
//                flowResult += Utils.bcd2Str(readBlkValue);
//                flowResult += "\n";
                ret = easyLink!!.piccM1WriteBlock(
                    etBlockNo!!.text.toString().toInt().toByte(), Utils.str2Bcd(
                        etBlockValue!!.text.toString()
                    )
                )
                Log.d(TAG, "piccM1WriteBlock ret:$ret")
                flowResult += "piccM1WriteBlock ret ="
                flowResult += ret
                flowResult += "\n"

//                readBlkValue = new byte[16];
                ret = easyLink!!.piccM1ReadBlock(
                    etBlockNo!!.text.toString().toInt().toByte(),
                    readBlkValue
                )
                Log.d(TAG, "piccM1ReadBlock ret:$ret")
                flowResult += "piccM1ReadBlock ret ="
                flowResult += "$ret\nread blkdata:"
                flowResult += Utils.bcd2Str(readBlkValue)
                flowResult += "\n"
                ret = easyLink!!.piccM1Operate(
                    EM1OperateType.INCREMENT,
                    etBlockNo!!.text.toString().toInt()
                        .toByte(),
                    byteArrayOf(0x09, 0x00, 0x00, 0x00),
                    etBlockNo!!.text.toString().toInt()
                        .toByte()
                )
                Log.d(TAG, "piccM1Operate increament 09 00 00 00 ret:$ret")
                flowResult += "piccM1Operate ret = $ret,increament 09 00 00 00\n"
                ret = easyLink!!.piccM1ReadBlock(
                    etBlockNo!!.text.toString().toInt().toByte(),
                    readBlkValue
                )
                Log.d(TAG, "piccM1ReadBlock ret:$ret")
                flowResult += "piccM1ReadBlock ret ="
                flowResult += "$ret\nread after increament blkdata:"
                flowResult += Utils.bcd2Str(readBlkValue)
                flowResult += "\n"
                ret = easyLink!!.piccM1Operate(
                    EM1OperateType.DECREMENT,
                    etBlockNo!!.text.toString().toInt()
                        .toByte(),
                    byteArrayOf(0x01, 0x00, 0x00, 0x00),
                    etBlockNo!!.text.toString().toInt()
                        .toByte()
                )
                Log.d(TAG, "piccM1Operate decreament 01 00 00 00 ret:$ret")
                flowResult += "piccM1Operate ret = $ret,decreament 01 00 00 00\n"
                ret = easyLink!!.piccM1ReadBlock(
                    etBlockNo!!.text.toString().toInt().toByte(),
                    readBlkValue
                )
                Log.d(TAG, "piccM1ReadBlock ret:$ret")
                flowResult += "piccM1ReadBlock ret ="
                flowResult += "$ret\nread after decreament blkdata:"
                flowResult += Utils.bcd2Str(readBlkValue)
                flowResult += "\n"
                easyLink!!.piccClose()
                easyLink!!.piccLight(0x02.toByte(), ELedStatus.OFF)
                finishTime = System.currentTimeMillis()
                timeRecord += "finish time:"
                timeRecord += finishTime.toString()
                timeRecord += "\ntime:" + (finishTime - beginTime) + "ms"
                timeRecord += "\n"
                runOnUiThread(Runnable { //Ui线程中执行
                    textInfoView!!.text = flowResult + timeRecord
                })
            }
        }).start()
    }

    private fun testPiccDecreament() {
        Thread(object : Runnable {
            override fun run() {
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
                        Log.d(
                            TAG,
                            "piccDetect getSerialInfo:" + Utils.bcd2Str(outPiccCardInfo.serialInfo)
                        )
                        Log.d(TAG, "piccDetect getCardType:" + outPiccCardInfo.cardType)
                        Log.d(TAG, "piccDetect getOther:" + Utils.bcd2Str(outPiccCardInfo.other))
                        beginTime = System.currentTimeMillis()
                        timeRecord += "begin time:"
                        timeRecord += beginTime.toString()
                        timeRecord += "\n"
                        break
                    }
                }
                val keyType: EM1KeyType
                if (("A" == etPasswordType!!.text.toString())) {
                    keyType = EM1KeyType.TYPE_A
                } else {
                    keyType = EM1KeyType.TYPE_B
                }
                ret = easyLink!!.piccM1Authority(
                    keyType,
                    etBlockNo!!.text.toString().toInt()
                        .toByte(),
                    Utils.str2Bcd(etPassword!!.text.toString()),
                    outPiccCardInfo.serialInfo
                )
                Log.d(TAG, "piccM1Authority ret:$ret")
                flowResult += "piccM1Authority ret ="
                flowResult += ret
                flowResult += "\n"
                val readBlkValue = ByteArray(16)
                ret = easyLink!!.piccM1Operate(
                    EM1OperateType.DECREMENT,
                    etBlockNo!!.text.toString().toInt()
                        .toByte(),
                    byteArrayOf(0x01, 0x00, 0x00, 0x00),
                    etBlockNo!!.text.toString().toInt()
                        .toByte()
                )
                Log.d(TAG, "piccM1Operate decreament 01 00 00 00 ret:$ret")
                flowResult += "piccM1Operate ret = $ret,decreament 01 00 00 00\n"
                ret = easyLink!!.piccM1ReadBlock(
                    etBlockNo!!.text.toString().toInt().toByte(),
                    readBlkValue
                )
                Log.d(TAG, "piccM1ReadBlock ret:$ret")
                flowResult += "piccM1ReadBlock ret ="
                flowResult += "$ret\nread after decreament blkdata:"
                flowResult += Utils.bcd2Str(readBlkValue)
                flowResult += "\n"
                easyLink!!.piccClose()
                easyLink!!.piccLight(0x02.toByte(), ELedStatus.OFF)
                finishTime = System.currentTimeMillis()
                timeRecord += "finish time:"
                timeRecord += finishTime.toString()
                timeRecord += "\ntime:" + (finishTime - beginTime) + "ms"
                timeRecord += "\n"
                runOnUiThread(object : Runnable {
                    override fun run() {
                        //Ui线程中执行
                        textInfoView!!.text = flowResult + timeRecord
                    }
                })
            }
        }).start()
    }

    private fun testPiccIncreament() {
        Thread(object : Runnable {
            override fun run() {
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
                        Log.d(
                            TAG,
                            "piccDetect getSerialInfo:" + Utils.bcd2Str(outPiccCardInfo.serialInfo)
                        )
                        Log.d(TAG, "piccDetect getCardType:" + outPiccCardInfo.cardType)
                        Log.d(TAG, "piccDetect getOther:" + Utils.bcd2Str(outPiccCardInfo.other))
                        beginTime = System.currentTimeMillis()
                        timeRecord += "begin time:"
                        timeRecord += beginTime.toString()
                        timeRecord += "\n"
                        break
                    }
                }
                val keyType: EM1KeyType
                if (("A" == etPasswordType!!.text.toString())) {
                    keyType = EM1KeyType.TYPE_A
                } else {
                    keyType = EM1KeyType.TYPE_B
                }
                ret = easyLink!!.piccM1Authority(
                    keyType,
                    etBlockNo!!.text.toString().toInt()
                        .toByte(),
                    Utils.str2Bcd(etPassword!!.text.toString()),
                    outPiccCardInfo.serialInfo
                )
                Log.d(TAG, "piccM1Authority ret:$ret")
                flowResult += "piccM1Authority ret ="
                flowResult += ret
                flowResult += "\n"
                val readBlkValue = ByteArray(16)
                ret = easyLink!!.piccM1Operate(
                    EM1OperateType.INCREMENT,
                    etBlockNo!!.text.toString().toInt()
                        .toByte(),
                    byteArrayOf(0x10, 0x27, 0x00, 0x00),
                    etBlockNo!!.text.toString().toInt()
                        .toByte()
                )
                Log.d(TAG, "piccM1Operate increament ret:$ret")
                flowResult += "piccM1Operate ret = $ret,increament\n"
                ret = easyLink!!.piccM1ReadBlock(
                    etBlockNo!!.text.toString().toInt().toByte(),
                    readBlkValue
                )
                Log.d(TAG, "piccM1ReadBlock ret:$ret")
                flowResult += "piccM1ReadBlock ret ="
                flowResult += "$ret\nread after increament blkdata:"
                flowResult += Utils.bcd2Str(readBlkValue)
                flowResult += "\n"
                easyLink!!.piccClose()
                easyLink!!.piccLight(0x02.toByte(), ELedStatus.OFF)
                finishTime = System.currentTimeMillis()
                timeRecord += "finish time:"
                timeRecord += finishTime.toString()
                timeRecord += "\ntime:" + (finishTime - beginTime) + "ms"
                timeRecord += "\n"
                runOnUiThread(object : Runnable {
                    override fun run() {
                        //Ui线程中执行
                        textInfoView!!.text = flowResult + timeRecord
                    }
                })
            }
        }).start()
    }

    private fun testPiccWrite() {
        Thread(object : Runnable {
            override fun run() {
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
                        Log.d(
                            TAG,
                            "piccDetect getSerialInfo:" + Utils.bcd2Str(outPiccCardInfo.serialInfo)
                        )
                        Log.d(TAG, "piccDetect getCardType:" + outPiccCardInfo.cardType)
                        Log.d(TAG, "piccDetect getOther:" + Utils.bcd2Str(outPiccCardInfo.other))
                        beginTime = System.currentTimeMillis()
                        timeRecord += "begin time:"
                        timeRecord += beginTime.toString()
                        timeRecord += "\n"
                        break
                    }
                }
                val keyType: EM1KeyType
                if (("A" == etPasswordType!!.text.toString())) {
                    keyType = EM1KeyType.TYPE_A
                } else {
                    keyType = EM1KeyType.TYPE_B
                }
                ret = easyLink!!.piccM1Authority(
                    keyType,
                    etBlockNo!!.text.toString().toInt()
                        .toByte(),
                    Utils.str2Bcd(etPassword!!.text.toString()),
                    outPiccCardInfo.serialInfo
                )
                Log.d(TAG, "piccM1Authority ret:$ret")
                flowResult += "piccM1Authority ret ="
                flowResult += ret
                flowResult += "\n"
                val readBlkValue = ByteArray(16)
                ret = easyLink!!.piccM1WriteBlock(
                    etBlockNo!!.text.toString().toInt().toByte(), Utils.str2Bcd(
                        etBlockValue!!.text.toString()
                    )
                )
                Log.d(TAG, "piccM1WriteBlock ret:$ret")
                flowResult += "piccM1WriteBlock ret ="
                flowResult += ret
                flowResult += "\n"
                easyLink!!.piccClose()
                easyLink!!.piccLight(0x02.toByte(), ELedStatus.OFF)
                finishTime = System.currentTimeMillis()
                timeRecord += "finish time:"
                timeRecord += finishTime.toString()
                timeRecord += "\ntime:" + (finishTime - beginTime) + "ms"
                timeRecord += "\n"
                runOnUiThread(object : Runnable {
                    override fun run() {
                        //Ui线程中执行
                        textInfoView!!.text = flowResult + timeRecord
                    }
                })
            }
        }).start()
    }

    private fun testPiccRead() {
        Thread(object : Runnable {
            override fun run() {
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
                        Log.d(
                            TAG,
                            "piccDetect getSerialInfo:" + Utils.bcd2Str(outPiccCardInfo.serialInfo)
                        )
                        Log.d(TAG, "piccDetect getCardType:" + outPiccCardInfo.cardType)
                        Log.d(TAG, "piccDetect getOther:" + Utils.bcd2Str(outPiccCardInfo.other))
                        beginTime = System.currentTimeMillis()
                        timeRecord += "begin time:"
                        timeRecord += beginTime.toString()
                        timeRecord += "\n"
                        break
                    }
                }
                val keyType: EM1KeyType
                if (("A" == etPasswordType!!.text.toString())) {
                    keyType = EM1KeyType.TYPE_A
                } else {
                    keyType = EM1KeyType.TYPE_B
                }
                ret = easyLink!!.piccM1Authority(
                    keyType,
                    etBlockNo!!.text.toString().toInt()
                        .toByte(),
                    Utils.str2Bcd(etPassword!!.text.toString()),
                    outPiccCardInfo.serialInfo
                )
                Log.d(TAG, "piccM1Authority ret:$ret")
                flowResult += "piccM1Authority ret ="
                flowResult += ret
                flowResult += "\n"
                val readBlkValue = ByteArray(16)
                Log.d("readxxx","readblockvalue:: ${Utils.bcd2Str(readBlkValue)}")
                ret = easyLink!!.piccM1ReadBlock(
                    etBlockNo!!.text.toString().toInt().toByte(),
                    readBlkValue
                )
                Log.d("readxxxxx","readblockvalue:: ${Utils.bcd2Str(readBlkValue)}")
                Log.d(TAG, "piccM1ReadBlock ret:$ret")
                flowResult += "piccM1ReadBlock ret ="
                flowResult += "$ret\nread blkdata:"
                flowResult += Utils.bcd2Str(readBlkValue)
                flowResult += "\n"
                ret = easyLink!!.piccRemove(EPiccRemoveMode.REMOVE, 0.toByte())
                Log.d(TAG, "piccRemove ret:$ret")
                easyLink!!.piccClose()
                easyLink!!.piccLight(0x02.toByte(), ELedStatus.OFF)
                finishTime = System.currentTimeMillis()
                timeRecord += "finish time:"
                timeRecord += finishTime.toString()
                timeRecord += "\ntime:" + (finishTime - beginTime) + "ms"
                timeRecord += "\n"
                runOnUiThread(object : Runnable {
                    override fun run() {
                        //Ui线程中执行
                        textInfoView!!.text = flowResult + timeRecord
                    }
                })
            }
        }).start()
    }

    companion object {
        private val TAG = "MifareOneSetting"
        private val m1keyPasswordType = "m1passwordType"
        private val m1keyPassword = "m1password"
        private val m1keyblkNo = "m1blkNo"
        private val m1keyblkValue = "blkValue"
    }

    override fun onCardResetDone(ret: Int) {
        println("on card key reset done ::::: ret == $ret")
//        this.mifareEventListener?.onCardActivated(ret)
    }

    override fun onCardReadDone(ret: Int, value: String, usage: String) {
       println("on card read done ::::: ret == $ret ::::: usage == $usage ::::: value == $value")

    }

    override fun onCardChargeDone(ret: Int, value: String, usage: String, message: String?) {
        println("on card charge done ::::: ret == $ret ::::: usage == $usage ::::: value == $value ::::: message == $message")
//        this.mifareEventListener?.onCardChargeDone(ret, value, usage, message)
    }

    override fun onCardBalanceRead(ret: Int, balance: String) {
        println("on card balance done ::::: ret == $ret :::::balance == $balance")
//        this.mifareEventListener?.onCardBalanceRead(ret, balance)
    }

    override fun onCardTopped(ret: Int, value: String, message: String?) {
        println("on card topped done ::::: ret == $ret :::::value == $value ::::: message == $message")
    }
}