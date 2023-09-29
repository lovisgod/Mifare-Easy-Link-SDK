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

package com.lovisgod.easylinksdk.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class MyLog {
	
	//TODO: adjust these settings for release version!
    public static boolean DEBUG_V = true;
    public static boolean DEBUG_D = true;
    public static boolean DEBUG_I = true;
    public static boolean DEBUG_W = true;
    public static boolean DEBUG_E = true;
     
    public static void toast(Context context, String content){
        Toast.makeText(context, content, Toast.LENGTH_SHORT).show();
    }
    
    public static void v(String tag, String msg){
        if(DEBUG_V){
            Log.v(tag, msg);
        }
    }
     
    public static void d(String tag, String msg){
        if(DEBUG_D){
            Log.d(tag, msg);
        }
    }
    
    public static void i(String tag, String msg){
        if(DEBUG_I){
        	int iLen = msg.length(), iLogLen=0, iSendLen=0;
        	
        	for(int i=0; i<(iLen/2048 + (iLen%2048==0?0:1)); i++){
        		
        		iLogLen = ((iLen-iSendLen) > 2048) ? 2048 : (iLen-iSendLen);
        		
        		byte[] temp4;
        		temp4=new byte[iLogLen];
//        		for(int j=0;j<iLogLen;j++)
//        		{
//        			temp4[j]=msg.getBytes()[iSendLen];
//        		}
        		
        		System.arraycopy(msg.getBytes(), iSendLen, temp4, 0, iLogLen);
        		
    			Log.i(tag, new String(temp4));
    			
    			iSendLen += iLogLen;
        	}
    		
        }
    }
    
    public static void w(String tag, String msg){
        if(DEBUG_W){
            Log.w(tag, msg);
        }
    }
    
    public static void e(String tag, String msg){
        if(DEBUG_E){
            Log.e(tag, msg);
        }
    }    
}