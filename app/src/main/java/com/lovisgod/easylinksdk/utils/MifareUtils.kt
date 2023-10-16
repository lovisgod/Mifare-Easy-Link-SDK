package com.lovisgod.easylinksdk.utils

import android.util.Log

object MifareUtils {

    fun decimalToByteArray(value: Int, numBytes: Int = 4): ByteArray {
        require(numBytes >= 1 && numBytes <= 4) { "numBytes must be between 1 and 4" }

        val byteArray = ByteArray(numBytes)

        for (i in 0 until numBytes) {
            byteArray[i] = ((value shr (8 * i)) and 0xFF).toByte()
        }

        println("Value  byte array: ${byteArray.joinToString { "0x%02X".format(it) }}")

        return byteArray
    }


    fun hexadecimalStringToByteArray(hexString: String): ByteArray {
        val cleanHexString = hexString.replace("0x", "") // Remove "0x" prefix if present
        require(cleanHexString.length % 2 == 0) { "Hex string length must be even." }

        val byteArray = ByteArray(cleanHexString.length / 2)

        for (i in 0 until cleanHexString.length step 2) {
            val byteString = cleanHexString.substring(i, i + 2)
            val byteValue = byteString.toInt(16).toByte()
            byteArray[i / 2] = byteValue
        }

        println("Value  byte array: ${byteArray.joinToString { "0x%02X".format(it) }}")

        return byteArray
    }

    fun byteArrayHexStringToDecimal(byteArray: ByteArray): Int {
        var result = 0
        for (i in 0 until byteArray.size) {
         result = result or ((byteArray[i].toInt() and 0xFF) shl (8 * i))
        }
        println("Decimal value: $result")
        return result
    }


}