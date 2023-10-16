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
 * 20180712                  huangwp                Create
 *
 *
 * ============================================================================
 */
package com.lovisgod.easylinksdk.ui

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.core.content.PermissionChecker
import com.lovisgod.easylinksdk.R
import com.lovisgod.easylinksdk.utils.MyLog
import com.paxsz.easylink.api.EasyLinkSdkManager
import com.paxsz.easylink.listener.FileDownloadListener
import java.io.File
import java.io.FileOutputStream
import java.util.Collections

class SelectFileActivity : Activity(), OnItemClickListener {
    lateinit var fileListAdapter: ArrayAdapter<*>
    private var platForm: String? = null
    private val arrayListPath = mutableListOf<String>()
    private val arrayListName = mutableListOf<String>()
    private var fileList = listOf<String>()
    private val hashMap: HashMap<String, String> = HashMap()
    private var filePath: String? = null
    private var isFromRkiPage = false
    private var easyLinkSdkManager: EasyLinkSdkManager? = null
    private var alertDialog1: AlertDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_file)
        val intent = intent
        platForm = intent.getStringExtra("platform")
        isFromRkiPage = intent.getBooleanExtra("fromRkiPage", false)
        Log.d(TAG, "platForm =$platForm")
        startListFile()
        fileListAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, arrayListName)
//        fileListAdapter = ArrayAdapter<MutableList<String>>(
//            this, android.R.layout.simple_list_item_1, arrayListName
//        )

        val listView = findViewById<View>(R.id.list_view) as ListView
        listView.adapter = fileListAdapter
        listView.onItemClickListener = this
        easyLinkSdkManager = EasyLinkSdkManager.getInstance(applicationContext)
        alertDialog1 = AlertDialog.Builder(this@SelectFileActivity)
            .setTitle("download file") //标题
            .setMessage("") //内容
            .create()
    }

    private fun startListFile() {
//       if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
//           if (Environment.isExternalStorageManager()) {
//               getFile();
//           } else {
//               Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
//               intent.setData(Uri.parse("package:" + SelectFileActivity.this.getPackageName()));
//               startActivityForResult(intent, 1002);
//           }
//       }else
        //API LEVEL 18
        //If your application is targeting an API level before 23 (Android M) then both:ContextCompat.CheckSelfPermission and Context.checkSelfPermission doesn't work and always returns 0 (PERMISSION_GRANTED). Even if you run the application on Android 6.0 (API 23).
        //As I said in the 1st point, if you targeting an API level before 23 on Android 6.0 then ContextCompat.CheckSelfPermission and Context.checkSelfPermission doesn't work. Fortunately you can use PermissionChecker.checkSelfPermission to check run-time permissions.
        val context = this@SelectFileActivity.applicationContext
        val targetSdkVer = this@SelectFileActivity.applicationInfo.targetSdkVersion
        MyLog.i(TAG, "target sdk :$targetSdkVer")
        val isDenied: Boolean
        isDenied = if (targetSdkVer <= Build.VERSION_CODES.M) {
            PermissionChecker.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PermissionChecker.PERMISSION_GRANTED || PermissionChecker.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PermissionChecker.PERMISSION_GRANTED
        } else {
            checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || checkSelfPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        }
        MyLog.i(TAG, "check permission Denied? :$isDenied")
        MyLog.e(
            TAG,
            "should show request? " + shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)
        )
        if (isDenied) {
            if (targetSdkVer > Build.VERSION_CODES.M) {
                MyLog.e(TAG, "request permission")
                requestPermissions(
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ), 1001
                )
            } else {
                Toast.makeText(
                    this@SelectFileActivity,
                    "read or write storage permission was denied, please allow it at the app permission list.",
                    Toast.LENGTH_SHORT
                ).show()
            }
            return
        }
        file
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.i(TAG, "onRequestPermissionsResult: $requestCode")
        if (requestCode == 1001) {
            for (grantResult in grantResults) {
                if (grantResult == PackageManager.PERMISSION_GRANTED) {
                    continue
                } else {
                    Log.i(TAG, "onRequestPermissionsResult,grantResult $grantResult")
                    break
                }
            }
            startListFile()
        }
    }

    //    @Override
    //    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    //        super.onActivityResult(requestCode, resultCode, data);
    //        if (requestCode == 1002 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
    //            if (Environment.isExternalStorageManager()) {
    //                startListFile();
    //            } else {
    //                Toast.makeText(SelectFileActivity.this,"request manage external storage permission failed",Toast.LENGTH_SHORT).show();
    //            }
    //        }
    //    }
    private fun refreshFileList() {
        Collections.sort(arrayListName, Collections.reverseOrder())
//        fileListAdapter?.addAll(arrayListName)
        fileListAdapter!!.notifyDataSetChanged()
    }

    override fun onBackPressed() {
//        super.onBackPressed();
        if (isFromRkiPage) {
            val intent = Intent()
            intent.putExtra("filePath", "back press")
            setResult(1000, intent)
            finish()
        } else {
            super.onBackPressed()
        }
    }

    /**
     * 54      * 响应ListView中item的点击事件
     * 55
     */
    override fun onItemClick(arg0: AdapterView<*>, v: View, position: Int, id: Long) {
        Toast.makeText(
            this, "item clicked, pos -->$position",
            Toast.LENGTH_SHORT
        ).show()
        Log.d(TAG, "onItemClick:$position")
        Log.d(TAG, "onItemClick String:" + arg0.adapter.getItem(position))
        filePath = hashMap[arg0.adapter.getItem(position)] as String?
        Log.d(TAG, "onItemClick value:$filePath")
        if (isFromRkiPage) {
            val intent = Intent()
            intent.putExtra("filePath", filePath)
            setResult(1000, intent)
            finish()
        } else {
            Thread {
                val result: String
                val ret = easyLinkSdkManager!!.fileDownLoad(filePath, myFileDownloadListener())
                Log.d(TAG, "fileDownLoad ret =$ret")
                result = "ret = $ret"
                runOnUiThread {
                    alertDialog1!!.setMessage(result)
                    alertDialog1!!.show()
                }
            }.start()
        }
    }

    private val file: Unit
        private get() {
            Thread(Runnable {
                val state = Environment.getExternalStorageState()
                if (Environment.MEDIA_MOUNTED == state) {
                    Log.d(TAG, "Environment")
                    val root = Environment.getExternalStorageDirectory()
                    Log.d(TAG, "getExternalStorageDirectory:$root")
                    val path = root.path + "/EasyLinkSdkFile"
                    fileScan(path)
                    val file = File(path)
                    Log.d(TAG, "path:$path")
                    if (!file.exists()) {
                        CopyAssets(this@SelectFileActivity, "EasyLinkParam", path)
                    }
                    var files = file.listFiles()
                    if (files == null || files.size <= 0) {
                        return@Runnable
                    }
                    if (files.size == 1) {
                        CopyAssets(this@SelectFileActivity, "EasyLinkParam", path)
                        files = file.listFiles()
                    }
                    getDirFile(files)
                    Log.d(TAG, "getDirFile end arrayListPath.size():" + arrayListPath.size)
                    Log.d(TAG, "getDirFile platform:$platForm")
                    if (platForm == "server") {
                        fileList = getSeverParam(arrayListPath)
                    } else if (platForm == "full") {
                        fileList = getFullParam(arrayListPath)
                    } else if (platForm == "lite") {
                        fileList = getLiteParam(arrayListPath)
                    } else if (platForm == "rki") {
                        fileList = getRkiParam(arrayListPath)
                    }
                    runOnUiThread { refreshFileList() }
                }
            }).start()
        }

    private fun getDirFile(files: Array<File>?): MutableList<String> {
        for (i in files!!.indices) {
            //Log.d("huangwp","files:"+ files[i].getName());
            if (files[i].isDirectory) {
                //Log.d("huangwp","getDirFile dir files:"+ files[i].getName());
                getDirFile(files[i].listFiles())
            } else {
                //Log.d("huangwp","getDirFile normal files:"+ files[i].getName());
                Log.d(TAG, "getDirFile normal files:" + files[i].path)
                arrayListPath.add(files[i].path)
            }
        }
        //Log.d("huangwp","getDirFile end:");
        return arrayListPath
    }

    private fun getRkiParam(strings: List<String>): List<String> {
        val parmList = mutableListOf<String>()
        for (list in strings) {
            if (list.contains("rki")) {
                parmList.add(list)
                arrayListName.add(list.substring(list.lastIndexOf("/") + 1))
                hashMap[list.substring(list.lastIndexOf("/") + 1)] = list
                Log.d(TAG, "getRkiParam list :$list")
            }
        }
        return parmList
    }

    private fun getFullParam(strings: List<String>): List<String> {
        val parmList = mutableListOf<String>()
        for (list in strings) {
            if (list.contains("full")) {
                parmList.add(list)
                arrayListName.add(list.substring(list.lastIndexOf("/") + 1))
                hashMap[list.substring(list.lastIndexOf("/") + 1)] = list
                Log.d(TAG, "getFullParam list :$list")
            }
        }
        return parmList
    }

    private fun getLiteParam(strings: List<String>): List<String> {
        val parmList = mutableListOf<String>()
        for (list in strings) {
            if (list.contains("lite")) {
                parmList.add(list)
                arrayListName.add(list.substring(list.lastIndexOf("/") + 1))
                hashMap[list.substring(list.lastIndexOf("/") + 1)] = list
                Log.d(TAG, "getLiteParam list :$list")
            }
        }
        Log.d(TAG, "getLiteParam parmList.size() :" + parmList.size)
        return parmList
    }

    private fun getSeverParam(strings: List<String>): List<String> {
        val parmList = mutableListOf<String?>()
        Log.d(TAG, "getSeverParam strings.size() :" + strings.size)
        for (list in strings) {
            Log.d(TAG, "getSeverParam list :$list")
            if (list.contains("server")) {
                arrayListName.add(list.substring(list.lastIndexOf("/") + 1))
                parmList.add(list)
                hashMap[list.substring(list.lastIndexOf("/") + 1)] = list
                Log.d(TAG, "getSeverParam list :$list")
            }
        }
        Log.d(TAG, "getSeverParam parmList.size() :" + parmList.size)
        return parmList as List<String>
    }

    fun CopyAssets(context: Context, oldPath: String, newPath: String) {
        try {
            val fileNames = context.assets.list(oldPath) // 获取assets目录下的所有文件及目录名
            Log.w(TAG, "CopyAssets: " + fileNames!!.size)
            if (fileNames.size > 0) { // 如果是目录
                val file = File(newPath)
                file.mkdirs() // 如果文件夹不存在，则递归
                for (fileName in fileNames) {
                    CopyAssets(context, "$oldPath/$fileName", "$newPath/$fileName")
                    fileScan(newPath)
                }
            } else { // 如果是文件
                val `is` = context.assets.open(oldPath)
                val fos = FileOutputStream(File(newPath))
                val buffer = ByteArray(1024)
                var byteCount = 0
                while (`is`.read(buffer).also { byteCount = it } != -1) { // 循环从输入流读取
                    // buffer字节
                    fos.write(buffer, 0, byteCount) // 将读取的输入流写入到输出流
                }
                fos.flush() // 刷新缓冲区
                `is`.close()
                fos.close()
                fileScan(newPath)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun fileScan(fName: String) {
        val data = Uri.parse("file:///$fName")
        sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, data))
    }

    internal inner class myFileDownloadListener : FileDownloadListener {
        var percent: String? = null
        override fun onDownloadProgress(current: Int, total: Int) {
            percent = "$current/$total"
            runOnUiThread {
                alertDialog1!!.setMessage(percent)
                alertDialog1!!.show()
            }
        }

        override fun cancelDownload(): Boolean {
            // TODO Auto-generated method stub
            return false
        }
    }

    companion object {
        private const val TAG = "SelectFileActivity"
    }
}