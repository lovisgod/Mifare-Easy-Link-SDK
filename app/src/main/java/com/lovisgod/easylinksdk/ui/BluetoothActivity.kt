package com.lovisgod.easylinksdk.ui

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.BaseAdapter
import android.widget.CheckBox
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import androidx.core.content.PermissionChecker.checkSelfPermission
import com.lovisgod.easylinksdk.R
import com.lovisgod.easylinksdk.manage.ConfigManager
import com.lovisgod.easylinksdk.observer.DataWatcher
import com.lovisgod.easylinksdk.observer.IObservable
import com.lovisgod.easylinksdk.utils.MyLog
import com.paxsz.easylink.api.EasyLinkSdkManager
import com.paxsz.easylink.api.ResponseCode
import com.paxsz.easylink.device.DeviceInfo
import com.paxsz.easylink.listener.IDeviceStateChangeListener
import com.paxsz.easylink.listener.SearchDeviceListener
import java.security.KeyPairGenerator
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom

/**
 * Created by zhanzc on 2017/8/8.
 * 测试连接功能
 */
class BluetoothActivity : Activity(), View.OnClickListener, IObservable {
    private var tv_progress: TextView? = null
    private var encCheckBox: CheckBox? = null
    private var lv_bluetooth: ListView? = null
    private var deviceInfos: ArrayList<DeviceInfo> = arrayListOf()
    private var adapter: BluetoothDeviceAdapter? = null
    private var manager: EasyLinkSdkManager? = null
    private val mHandler = Handler { message ->
        val ret = message.arg1
        if (message.what == 0) {
            Toast.makeText(this@BluetoothActivity, "BT Disconnect, ret = $ret", Toast.LENGTH_LONG)
                .show()
        } else if (message.what == 1) {
            Toast.makeText(this@BluetoothActivity, "BT connect, ret = $ret", Toast.LENGTH_LONG)
                .show()
            manager?.registerDisconnectStateListener(IDeviceStateChangeListener {
                Toast.makeText(
                    this@BluetoothActivity,
                    "BT has disconnect",
                    Toast.LENGTH_SHORT
                ).show()
            })
        }
        false
    }
    private var viewHolder: ViewHolder? = null
    private fun connectWithEnc(@NonNull devInfo: DeviceInfo): Int {
        var ret = 0
        try {
            val keyPairGen = KeyPairGenerator.getInstance("RSA")
            // 初始化密钥对生成器，密钥大小为96-1024位
            keyPairGen.initialize(2048, SecureRandom())
            // 生成一个密钥对，保存在keyPair中
            val keyPair = keyPairGen.generateKeyPair()
            ret = manager?.connect(devInfo, keyPair)!!
        } catch (e: NoSuchAlgorithmException) {
            Log.d(TAG, "NoSuchAlgorithmException: $e")
        }
        return ret
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bluetooth)

        // 观察者观察被观察者
        DataWatcher.getInstance().register(this)
        manager = EasyLinkSdkManager.getInstance(this)
        lv_bluetooth = findViewById<View>(R.id.lv_bluetooth) as ListView
        adapter = BluetoothDeviceAdapter(this, deviceInfos)
        lv_bluetooth!!.adapter = adapter
        lv_bluetooth!!.onItemClickListener = OnItemClickListener { adapterView, view, i, l ->

            manager?.stopSearchingDevice()
            Thread {
                Log.d(TAG, "checkbox = " + encCheckBox!!.isChecked)
                val ret: Int
                ret = if (encCheckBox!!.isChecked == false) {
                    manager!!.connect((deviceInfos.get(i)))
                } else {
                    connectWithEnc(deviceInfos.get(i))
                }
                ConfigManager.getInstance(this@BluetoothActivity).bluetoothMac =
                    deviceInfos.get(i).identifier
                ConfigManager.getInstance(this@BluetoothActivity).deviceName =
                    deviceInfos.get(i).deviceName
                ConfigManager.getInstance(this@BluetoothActivity).commType =
                    deviceInfos.get(i).commType.name
                ConfigManager.getInstance(this@BluetoothActivity).save()
                val message = Message.obtain()
                message.arg1 = ret
                message.what = 1
                mHandler.sendMessage(message)
            }.start()
        }
        tv_progress = findViewById<View>(R.id.tv_progress) as TextView
        findViewById<View>(R.id.btn_stop_searching).setOnClickListener(this)
        findViewById<View>(R.id.btn_search).setOnClickListener(this)
        findViewById<View>(R.id.btn_reconnect).setOnClickListener(this)
        encCheckBox = findViewById<CheckBox>(R.id.checkBox)
    }

    override fun onDestroy() {
        // 观察者观察被观察者
        DataWatcher.getInstance().unregister(this)
        super.onDestroy()
    }

    private fun startSearch() {
        tv_progress!!.text = "Search......"
        deviceInfos!!.clear()
        adapter!!.notifyDataSetChanged()
        val ret: Int = manager!!.searchDevices(BluetoothListener(), 40000)
        if (ret == ResponseCode.EL_SDK_RET_BT_NOT_ENABLED) {
            Toast.makeText(this@BluetoothActivity, "Bluetooth  is not enable", Toast.LENGTH_SHORT)
                .show()
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH_CONNECT), 90)
                return
            }
            startActivityForResult(enableBtIntent, 1004)
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btn_search -> processBtScan()
            R.id.btn_stop_searching -> manager!!.stopSearchingDevice()
            R.id.btn_reconnect -> {
                val commType =
                    if (ConfigManager.getInstance(this@BluetoothActivity).commType == DeviceInfo.CommType.BLUETOOTH.name) DeviceInfo.CommType.BLUETOOTH else DeviceInfo.CommType.BLUETOOTH
                val deviceName = ConfigManager.getInstance(this@BluetoothActivity).deviceName
                val macAddr = ConfigManager.getInstance(this@BluetoothActivity).bluetoothMac
                MyLog.e(
                    TAG,
                    "reconnect: commType->" + commType.name + ", deviceName->" + deviceName + ", mac->" + macAddr
                )
                Thread {
                    var ret = 0
                    val devInfo = DeviceInfo(commType, deviceName, macAddr)
                    ret = if (encCheckBox!!.isChecked == false) {
                        manager!!.connect(devInfo)
                    } else {
                        connectWithEnc(devInfo)
                    }
                    val message = Message.obtain()
                    message.arg1 = ret
                    message.what = 1
                    mHandler.sendMessage(message)
                }.start()
            }
        }
    }

    private fun processBtScan() {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            Toast.makeText(
                this@BluetoothActivity,
                "Bluetooth feature is not available",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, 1004)
            return
        }
        //        if (!isLocationEnable(BluetoothActivity.this)) {//some smart android device need to open location switch
//            setLocationService();
//            return;
//        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { //API LEVEL 18
            //If your application is targeting an API level before 23 (Android M) then both:ContextCompat.CheckSelfPermission and Context.checkSelfPermission doesn't work and always returns 0 (PERMISSION_GRANTED). Even if you run the application on Android 6.0 (API 23).
            //As I said in the 1st point, if you targeting an API level before 23 on Android 6.0 then ContextCompat.CheckSelfPermission and Context.checkSelfPermission doesn't work. Fortunately you can use PermissionChecker.checkSelfPermission to check run-time permissions.
            val context = this@BluetoothActivity.applicationContext
            val targetSdkVer = this@BluetoothActivity.applicationInfo.targetSdkVersion
            MyLog.i(TAG, "target sdk :$targetSdkVer")
            val isDenied: Boolean
            isDenied = if (targetSdkVer <= Build.VERSION_CODES.M) {
                checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) !== PERMISSION_GRANTED || checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) !== PERMISSION_GRANTED
            } else {
                checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED || checkSelfPermission(
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            }
            MyLog.i(TAG, "check permission Denied? :$isDenied")
            MyLog.e(
                TAG,
                "should show request? " + shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)
            )
            if (isDenied) {
                if (targetSdkVer > Build.VERSION_CODES.M) {
                    MyLog.e(TAG, "request permission")
                    requestPermissions(
                        arrayOf(
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ), 1001
                    )
                } else {
                    Toast.makeText(
                        this@BluetoothActivity,
                        "Location permission was denied, please allow it at the app permission list.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return
            }
        }
        startSearch()
    }

    override fun update(paramDeviceInfo: DeviceInfo) {
        println("update device info::: ${paramDeviceInfo.deviceName}")
        deviceInfos!!.add(paramDeviceInfo)
        adapter!!.notifyDataSetChanged()
    }

    override fun onSearchFinish() {
        tv_progress!!.text = "Searching complete"
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
            processBtScan()
        }
    }

    private fun setLocationService() {
        val locationIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        this.startActivityForResult(locationIntent, 1002)
    }

    /**
     * Location service if enable
     *
     * @param context
     * @return location is enable if return true, otherwise disable.
     */
    private fun isLocationEnable(context: Context): Boolean {
        val locationManager = context.getSystemService(LOCATION_SERVICE) as LocationManager
        val networkProvider = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        val gpsProvider = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        Log.w(TAG, "is gps on?$gpsProvider,net ?$networkProvider")
        return if (networkProvider || gpsProvider) true else false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.i(TAG, "onActivityResult: $requestCode, resultCode:$resultCode")
        if (requestCode == 1002 || requestCode == 1004 && resultCode == RESULT_OK) {
            processBtScan()
        }
    }

    private class BluetoothListener : SearchDeviceListener {
        override fun discoverOneDevice(deviceInfo: DeviceInfo) {

            println("device info, :::: ${deviceInfo.deviceName}")
            // 通知被观察者更新数据
            DataWatcher.getInstance().notifyObservable(deviceInfo)
        }

        override fun discoverComplete() {
            // 通知被观察者更新数据
            DataWatcher.getInstance().notifyFinish()
        }
    }

    private class ViewHolder {
        var tv_bluetooth: TextView? = null
    }

    private inner class BluetoothDeviceAdapter(
        private val context: Context,
        private val deviceList: List<DeviceInfo>
    ) : BaseAdapter() {
        override fun getCount(): Int {
            return deviceList.size
        }

        override fun getItem(i: Int): Any {
            return deviceList[i]
        }

        override fun getItemId(i: Int): Long {
            return i.toLong()
        }

        override fun getView(i: Int, view: View?, viewGroup: ViewGroup?): View? {
            var convertView: View? = view
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.item_bluetooth, viewGroup, false)
            }
            val textViewItemName = convertView
                ?.findViewById(R.id.tv_bluetooth) as TextView

            textViewItemName.text = deviceList[i].deviceName + "  Addr: " + deviceList[i].identifier
            return convertView
        }

    }

    companion object {
        private const val TAG = "BluetoothActivity"
    }
}