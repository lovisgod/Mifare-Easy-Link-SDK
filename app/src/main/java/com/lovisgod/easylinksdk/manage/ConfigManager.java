/*
 *
 *  * ============================================================================
 *  * = COPYRIGHT
 *  *        PAX Computer Technology(Shenzhen) CO., LTD PROPRIETARY INFORMATION
 *  *   This software is supplied under the terms of a license agreement or
 *  *   nondisclosure agreement with PAX Computer Technology(Shenzhen) CO., LTD and may not be
 *  *   copied or disclosed except in accordance with the terms in that agreement.
 *  *      Copyright (C) ${YEAR}-? PAX Computer Technology(Shenzhen) CO., LTD All rights reserved.
 *  * Description: // Detail description about the function of this module,
 *  *             // interfaces with the other modules, and dependencies.
 *  * Revision History:
 *  * Date	                 Author	                Action
 *  * ${DATE}  	         ${USER}           	Create
 *  * ============================================================================
 *
 */

package com.lovisgod.easylinksdk.manage;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.lovisgod.easylinksdk.utils.MyLog;


/**
 * <div class="zh">
 * <b><font color=red>ע��: ��ģ��� D800 ������</font></b><br>
 * ConfigManager ���ڶ�ȡ,����������Ϣ, ��(ͨ�Ų���ȵ�
 * </div>
 * <div class="en">
 * <b><font color=red>NOTE: this module is not applicable for D800</font></b><br>
 * ConfigManager manages the configuration information, including communication parameters etc.
 * </div>
 */
public class ConfigManager {
	public static final int CONN_TIMEOUT_DEFAULT = 37000; //ms
	public static final int READ_TIMEOUT_DEFAULT = 60000;	//ms   //jason
	private static String TAG = "ConfigManager";
	
	//FIXME! below default values are for test purpose
	//set to resonable default values when release.
	
 

	public String commType = "BLUETOOTH";		//ip/bluetooth
	
	public void saveTagValue(String tag, String value){
		if(null == tag || tag.trim().equals("")){
			MyLog.i(TAG, "invalid tag");
			return;
		}
		
		if(null != settings){
			settings = context.getSharedPreferences(CONFIG_FILE_NAME, context.MODE_PRIVATE);
		}
		
		Editor editor = settings.edit();
		editor.putString(tag, value);
		MyLog.i(TAG, "save...");
		MyLog.i(TAG, tag);
		MyLog.i(TAG, value);
		boolean result = editor.commit();
		MyLog.i(TAG, "save result " + result);
	}
	
	
	public String getValueByTag(String tag, String defVal){
		if(null == tag || tag.trim().equals("")){
			MyLog.i(TAG, "invalid tag");
			return null;
		}
		
		if(null != settings){
			settings = context.getSharedPreferences(CONFIG_FILE_NAME, context.MODE_PRIVATE);
		}
		
		String val = settings.getString(tag, defVal);
		MyLog.i(TAG, "get value...");
		MyLog.i(TAG, tag);
		MyLog.i(TAG, val);
		return val;
	}
	
	
	/**
	 * <div class="zh">
	 *		server��ַ: ipͨ�ŷ�ʽ�µ� server ��ַ, ������IP��ַ, Ҳ�����������, Ĭ�� 192.168.100.101
     * </div>
     * <div class="en">
     * 		server address: in ip communication type, either an IP address or a host name, default to 192.168.100.101
     * </div>
	 */
	public String serverAddr = "211.162.210.217";

	public String fileDescriptor = "/dev/ttyPos1";
	public String attr = "115200,8,n,1";

	/**
	 * <div class="zh">
	 *		server�˿�: ip ͨ�ŷ�ʽ�µ� server�˿�, Ĭ��10297, һ������²����޸Ĵ�����
     * </div>
     * <div class="en">
     * 		server port: server port in ip communication type, default to 10297, usually need not change this setting.
     * </div>
	 */
	public int serverPort = 33449;
	/**
	 * <div class="zh">
	 *		6�� MAC ��ַ, Ĭ��Ϊ"00:00:00:00:00:00", �������óɺϷ���ַ������ͨ��
     * </div>
     * <div class="en">
     * 		bluetooth MAC address, default to "00:00:00:00:00:00", you MUST set this to a valid address to use bluetooth
     * </div>
	 */
	public String bluetoothMac = "";
			//"2C:26:C5:9A:5B:A1";			//my zte
			//"C4:6A:B7:13:2C:D0";		//my xiaomi
			//"00:07:80:57:8F:C7";		//D210	
			//"B8:F9:34:22:5E:E1"		//xperia 
			//"00:08:CA:B8:B1:59";	//D900
	public String deviceName = "";
	/**
	 * <div class="zh">
	 *		ͨ�Ž��ճ�ʱ, Ĭ��2000ms
     * </div>
     * <div class="en">
     * 		receive timeout, default to 2000 ms
     * </div>
	 */	
	public int receiveTimeout = READ_TIMEOUT_DEFAULT;

	//make connect timeout unconfigurable by user app
	private int connectTimeout = CONN_TIMEOUT_DEFAULT;

	private static final String CONFIG_FILE_NAME = "mposSettings";
	private static ConfigManager configManager;
	private SharedPreferences settings;
	private Context context;

	private ConfigManager(Context context) {
		this.context = context;
		load();
	}
	
    /**
     * <div class="zh">
     * ��� ConfigManager ����
     * </div>
     * <div class="en">
     * Get a ConfigManager Instance
     * </div>
     * 
     * @return
     * <div class="zh">
     * 			ConfigManager����
     * </div>
     * <div class="en">
     *          a ConfigManager object
     * </div>
     * @param context 
     * <div class="zh">Ӧ�õ�ǰ��context</div>
     * <div class="en">application context currently</div>
     */	
	public static ConfigManager getInstance(Context context) {
		if (configManager == null) {
			configManager = new ConfigManager(context);
		}
		return configManager;
	}

    /**
     * <div class="zh">
     * �������ļ���װ��������Ϣ
     * </div>
     * <div class="en">
     * Load configurations from the specified config file 
     * </div>
     */		
	public void load() {
		settings = context.getSharedPreferences(CONFIG_FILE_NAME, context.MODE_PRIVATE);
		commType = settings.getString("commType", commType);
		serverAddr = settings.getString("serverAddr", serverAddr);
		serverPort = settings.getInt("serverPort", serverPort);
		//connectTimeout = settings.getInt("connectTimeout", connectTimeout);
		receiveTimeout = settings.getInt("receiveTimeout", receiveTimeout);
		bluetoothMac = settings.getString("bluetoothMac", bluetoothMac);
		deviceName = settings.getString("deviceName", deviceName);

		fileDescriptor = settings.getString("fileDescriptor", fileDescriptor);
		attr = settings.getString("attr", attr);
	}

    /**
     * <div class="zh">
     * ����������Ϣ
     * </div>
     * <div class="en">
     * Save configuration
     * </div>
     */	
	public void save() {
		Editor editor = settings.edit();
		editor.putString("commType", commType);
		editor.putString("serverAddr", serverAddr);
		editor.putInt("serverPort", serverPort);
		//editor.putInt("connectTimeout", connectTimeout);
		editor.putInt("receiveTimeout", receiveTimeout);
		editor.putString("bluetoothMac", bluetoothMac);
		editor.putString("deviceName", deviceName);
		editor.putString("fileDescriptor", fileDescriptor);
		editor.putString("attr", attr);
		boolean result = editor.commit();
		MyLog.i(TAG, "save result " + result);
	}
}
