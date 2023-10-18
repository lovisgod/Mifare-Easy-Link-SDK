package com.lovisgod.easylinksdk.utils

import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import com.paxsz.easylink.api.EasyLinkSdkManager
import com.paxsz.easylink.listener.FileDownloadListener
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream


class ParameterUtils {


    fun loadParameterFiles(context: Context,
                           easyLinkSdkManager: EasyLinkSdkManager,
                           fileDownloadListener: FileDownloadListener): Int{
        val assetManager: AssetManager = context.assets
        try {
            val files = assetManager.list("data")
            for (i in files!!.indices) {
                Log.d("TestActivity", files[i])
                val temFilePath = createTempFileFromAsset(context, files[i])
                var ret =easyLinkSdkManager.fileDownLoad(temFilePath, fileDownloadListener)
                println("file download ret:::::: $ret")
            }

        } catch (e1: IOException) {
            e1.printStackTrace()
        }
        return 0
    }

    fun createTempFileFromAsset(context: Context, assetFileName: String): String? {
        val inputStream: InputStream = context.assets.open(assetFileName)

        // Define a directory for your temporary files. You can use the cache directory or any other suitable location.
        val tempDir = context.cacheDir

        // Generate a unique file name for your temporary file.
        val tempFileName = "temp_file_${System.currentTimeMillis()}$assetFileName"

        println(tempFileName)


        val tempFile = File(tempDir, tempFileName)

        try {
            val outputStream = FileOutputStream(tempFile)
            val buffer = ByteArray(1024)
            var length: Int

            while (inputStream.read(buffer).also { length = it } > 0) {
                outputStream.write(buffer, 0, length)
            }

            outputStream.flush()
            outputStream.close()
            inputStream.close()

            println("temp file path :::: ${tempFile.absolutePath}")

            // Return the path to the temporary file.
            return tempFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

}