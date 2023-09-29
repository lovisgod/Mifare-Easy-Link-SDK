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
 * 20180814                  huangwp                Create
 *
 *
 * ============================================================================
 */

package com.lovisgod.easylinksdk.EasyLinkParam;

import android.content.Context;
import android.util.Log;

import com.lovisgod.easylinksdk.manage.ConfigManager;
import com.lovisgod.easylinksdk.utils.Tools;


public class EasyLinkConfigParam {
    private final static String TAG = "EasyLinkConfigParam";

    public static String getSleepModeTimeout(Context appContext) {
        String val = ConfigManager.getInstance(appContext).getValueByTag("SleepModeTimeout", "");

        if (val == null || val.equals("")) {
            ConfigManager.getInstance(appContext).saveTagValue("SleepModeTimeout", "0000");
            return "0201020000";
        }
        String tlv = "020102";

        byte[] timeout = new byte[2];
        System.arraycopy(Tools.str2Bcd(val), 0, timeout, 2 - Tools.str2Bcd(val).length, Tools.str2Bcd(val).length);
        tlv += Tools.bcd2Str(timeout);
        Log.d(TAG, "tlv = " + tlv);
        return tlv;
    }

    public static String getReportTimeout(Context appContext) {
        String val = ConfigManager.getInstance(appContext).getValueByTag("ReportTimeout", "");
        if (val == null || val.equals("")) {
            ConfigManager.getInstance(appContext).saveTagValue("ReportTimeout", "0030");
            return "021B020030";
        }
        String tlv = "021B02";

        byte[] timeout = new byte[2];
        System.arraycopy(Tools.str2Bcd(val), 0, timeout, 2 - Tools.str2Bcd(val).length, Tools.str2Bcd(val).length);
        tlv += Tools.bcd2Str(timeout);
        Log.d(TAG, "tlv = " + tlv);
        return tlv;
    }

    public static String getEncryptType(Context appContext) {
        String val = ConfigManager.getInstance(appContext).getValueByTag("PINEncryptionType", "");
        if (val == null || val.equals("")) {
            ConfigManager.getInstance(appContext).saveTagValue("PINEncryptionType", "1.TDES");
            return "02020101";
        }

        String tlv = "020201";

        if (val.equals("1.TDES")) {
            tlv += "01";
        } else if (val.equals("2.DUKPT")) {
            tlv += "02";
        }
        Log.d(TAG, "tlv = " + tlv);
        return tlv;
    }

    public static String getPinEncIndex(Context appContext) {
        String val = ConfigManager.getInstance(appContext).getValueByTag("PINEncryptionKeyIdx", "");
        if (val == null || val.equals("")) {
            ConfigManager.getInstance(appContext).saveTagValue("PINEncryptionKeyIdx", "02");
            return "02030102";
        }

        String tlv = "020301";

        byte[] data = new byte[1];
        System.arraycopy(Tools.str2Bcd(val), 0, data, 1 - Tools.str2Bcd(val).length, Tools.str2Bcd(val).length);
        tlv += Tools.bcd2Str(data);
        Log.d(TAG, "tlv = " + tlv);
        return tlv;
    }

    public static String getPinblockMode(Context appContext) {
        String val = ConfigManager.getInstance(appContext).getValueByTag("PINBlockMode", "");
        if (val == null || val.equals("")) {
            ConfigManager.getInstance(appContext).saveTagValue("PINBlockMode", "1.0x00: ISO9564 format0");
            return "02040100";
        }

        String tlv = "020401";

        if (val.equals("1.0x00: ISO9564 format0")) {
            tlv += "00";
        } else if (val.equals("2.0x01: ISO9564 format1")) {
            tlv += "01";
        } else if (val.equals("3.0x02: ISO9564 format3")) {
            tlv += "02";
        }
        Log.d(TAG, "tlv = " + tlv);
        return tlv;
    }

    public static String getDataEncryptTypeMode(Context appContext) {
        String val = ConfigManager.getInstance(appContext).getValueByTag("DataEncryptionType", "");
        if (val == null || val.equals("")) {
            ConfigManager.getInstance(appContext).saveTagValue("DataEncryptionType", "1.Plaintext");
            return "02050100";
        }

        String tlv = "020501";

        if (val.equals("1.Plaintext")) {
            tlv += "00";
        } else if (val.equals("2.TDES")) {
            tlv += "01";
        } else if (val.equals("3.RSA")) {
            tlv += "02";
        } else if (val.equals("4.MAC")) {
            tlv += "03";
        } else if (val.equals("5.DUKPT")) {
            tlv += "04";
        }
        Log.d(TAG, "tlv = " + tlv);
        return tlv;
    }

    public static String getDataEncIndex(Context appContext) {
        String val = ConfigManager.getInstance(appContext).getValueByTag("DataEncryptionKeyIdx", "");
        if (val == null || val.equals("")) {
            ConfigManager.getInstance(appContext).saveTagValue("DataEncryptionKeyIdx", "03");
            return "02060103";
        }

        String tlv = "020601";

        byte[] data = new byte[1];
        System.arraycopy(Tools.str2Bcd(val), 0, data, 1 - Tools.str2Bcd(val).length, Tools.str2Bcd(val).length);
        tlv += Tools.bcd2Str(data);
        Log.d(TAG, "tlv = " + tlv);
        return tlv;
    }

    public static String getFallbackAllow(Context appContext) {
        String val = ConfigManager.getInstance(appContext).getValueByTag("FallbackAllowFlag", "");
        if (val == null || val.equals("")) {
            ConfigManager.getInstance(appContext).saveTagValue("FallbackAllowFlag", "1.fallback not allowed");
            return "02070100";
        }

        String tlv = "020701";

        if (val.equals("1.fallback not allowed")) {
            tlv += "00";
        } else if (val.equals("2.fallback allowed")) {
            tlv += "01";
        }
        Log.d(TAG, "tlv = " + tlv);
        return tlv;
    }

    public static String getPanMaskStartPos(Context appContext) {
        String val = ConfigManager.getInstance(appContext).getValueByTag("PANMaskStartPos", "");
        if (val == null || val.equals("")) {
            ConfigManager.getInstance(appContext).saveTagValue("PANMaskStartPos", "1.0");
            return "02080100";
        }

        String tlv = "020801";

        if (val.equals("1.0")) {
            tlv += "00";
        } else if (val.equals("2.1")) {
            tlv += "01";
        } else if (val.equals("3.2")) {
            tlv += "02";
        } else if (val.equals("4.3")) {
            tlv += "03";
        } else if (val.equals("5.4")) {
            tlv += "04";
        } else if (val.equals("6.5")) {
            tlv += "04";
        } else if (val.equals("7.6")) {
            tlv += "05";
        }
        Log.d(TAG, "tlv = " + tlv);
        return tlv;
    }

    public static String getTransMode(Context appContext) {
        String val = ConfigManager.getInstance(appContext).getValueByTag("TransactionProcessingMode", "");
        if (val == null || val.equals("")) {
            ConfigManager.getInstance(appContext).saveTagValue("TransactionProcessingMode", "1.normal");
            return "02090100";
        }

        String tlv = "020901";

        if (val.equals("1.normal")) {
            tlv += "00";
        } else if (val.equals("2.demo")) {
            tlv += "01";
        }
        Log.d(TAG, "tlv = " + tlv);
        return tlv;
    }

    public static String getDukptDesModeMode(Context appContext) {
        String val = ConfigManager.getInstance(appContext).getValueByTag("DUKPTDESmode", "");
        if (val == null || val.equals("")) {
            ConfigManager.getInstance(appContext).saveTagValue("DUKPTDESmode", "1.ECB Decryption");
            return "020A0100";
        }

        String tlv = "020A01";

        if (val.equals("1.ECB Decryption")) {
            tlv += "00";
        } else if (val.equals("2.ECB Encryption")) {
            tlv += "01";
        } else if (val.equals("3.CBC Decryption")) {
            tlv += "02";
        } else if (val.equals("4.CBC Encryption")) {
            tlv += "03";
        }
        Log.d(TAG, "tlv = " + tlv);
        return tlv;
    }
    public static String getLanguage(Context appContext){
        String val = ConfigManager.getInstance(appContext).getValueByTag("Language", "5.Default");
        if (val == null || val.equals("")) {
            ConfigManager.getInstance(appContext).saveTagValue("Language", "5.Default");
            return "0211"+"07" + Tools.bcd2Str("English".getBytes());
        }

        String tlv = "0211";
        String realVal = "Default";
        if (val.equals("1.English")) {
            realVal = "English";
        } else if (val.equals("2.Farsi")) {
            realVal = "Farsi";
        } else if (val.equals("3.Japanese")) {
            realVal = "Japanese";
        } else if (val.equals("4.Greek")) {
            realVal = "Greek";
        }else if(val.equals("5.Default")) {
            return "";
        }else if(val.equals("6.Chinese")) {
            realVal = "Chinese";
        }
        if(realVal.length()< 10){
            tlv += "0"+realVal.length();
        }else{
            tlv += realVal.length();
        }

        Log.d(TAG, "getLanguage  length= "+tlv);
        tlv += Tools.bcd2Str(realVal.getBytes());
        Log.d(TAG, "getLanguage = " + tlv);
        return tlv;
    }

    public static String getMacCalcIndex(Context appContext) {
        String val = ConfigManager.getInstance(appContext).getValueByTag("MacKeyIdx", "");
        if (val == null || val.equals("")) {
            ConfigManager.getInstance(appContext).saveTagValue("MacKeyIdx", "04");
            return "02100104";
        }

        String tlv = "021001";

        byte[] data = new byte[1];
        System.arraycopy(Tools.str2Bcd(val), 0, data, 1 - Tools.str2Bcd(val).length, Tools.str2Bcd(val).length);
        tlv += Tools.bcd2Str(data);
        Log.d(TAG, "tlv = " + tlv);
        return tlv;
    }

    public static String getMacCalcType(Context appContext) {
        String val = ConfigManager.getInstance(appContext).getValueByTag("MacCalcType", "");
        if (val == null || val.equals("")) {
            ConfigManager.getInstance(appContext).saveTagValue("MacCalcType", "1.TAK");
            return "02120101";
        }

        String tlv = "021201";

        if (val.equals("1.TAK")) {
            tlv += "01";
        } else if (val.equals("2.TIKs")) {
            tlv += "02";
        }
        Log.d(TAG, "tlv = " + tlv);
        return tlv;
    }

    public static String getMacCalcMode(Context appContext) {
        String val = ConfigManager.getInstance(appContext).getValueByTag("MacCalcMode", "");
        if (val == null || val.equals("")) {
            ConfigManager.getInstance(appContext).saveTagValue("MacCalcMode", "1.0x00");
            return "02130100";
        }

        String tlv = "021301";

        if (val.equals("1.0x00")) {
            tlv += "00";
        } else if (val.equals("2.0x01")) {
            tlv += "01";
        } else if (val.equals("3.0x02")) {
            tlv += "02";
        } else if (val.equals("4.0x20")) {
            tlv += "20";
        } else if (val.equals("5.0x21")) {
            tlv += "21";
        } else if (val.equals("6.0x22")) {
            tlv += "22";
        } else if (val.equals("7.0x40")) {
            tlv += "40";
        } else if (val.equals("8.0x41")) {
            tlv += "41";
        } else if (val.equals("9.0x42")) {
            tlv += "42";
        }
        Log.d(TAG, "tlv = " + tlv);
        return tlv;
    }

    public static String getCardEntryMode(Context appContext) {
        String val = ConfigManager.getInstance(appContext).getValueByTag("CardEntryMode", "");
        if (val == null || val.equals("")) {
            ConfigManager.getInstance(appContext).saveTagValue("CardEntryMode", "7.SWIPE/INSERT/TAP");
            return "02140107";
        }

        String tlv = "021401";

        if (val.equals("1.MSR SWIPE")) {
            tlv += "01";
        } else if (val.equals("2.ICC INSERT")) {
            tlv += "02";
        } else if (val.equals("3.SWIPE/INSERT")) {
            tlv += "03";
        } else if (val.equals("4.PICC TAP")) {
            tlv += "04";
        } else if (val.equals("5.SWIPE/TAP")) {
            tlv += "05";
        } else if (val.equals("6.INSERT/TAP")) {
            tlv += "06";
        } else if (val.equals("7.SWIPE/INSERT/TAP")) {
            tlv += "07";
        }
        Log.d(TAG, "tlv = " + tlv);
        return tlv;
    }

    public static String getMsrReqPinAuto(Context appContext) {
        String val = ConfigManager.getInstance(appContext).getValueByTag("MsrRequestPINAuto", "");
        if (val == null || val.equals("")) {
            ConfigManager.getInstance(appContext).saveTagValue("MsrRequestPINAuto", "1.REQUEST PIN AUTO");
            return "02150101";
        }

        String tlv = "021501";

        if (val.equals("1.REQUEST PIN AUTO")) {
            tlv += "01";
        } else if (val.equals("2.DO NOT REQUEST PIN")) {
            tlv += "02";
        }
        Log.d(TAG, "tlv = " + tlv);
        return tlv;
    }

    public static String getSupportReport(Context appContext) {
        String val = ConfigManager.getInstance(appContext).getValueByTag("SupportReport", "");
        if (val == null || val.equals("")) {
            ConfigManager.getInstance(appContext).saveTagValue("SupportReport", "1.UNSUPPORT");
            return "02160100";
        }

        String tlv = "021601";

        if (val.equals("1.UNSUPPORT")) {
            tlv += "00";
        } else if (val.equals("2.SUPPORT")) {
            tlv += "01";
        }
        Log.d(TAG, "tlv = " + tlv);
        return tlv;
    }

    public static String getEmvRefundNeedAuthorize(Context appContext) {
        String val = ConfigManager.getInstance(appContext).getValueByTag("EmvRefundNeedAuthorize", "");
        if (val == null || val.equals("")) {
            ConfigManager.getInstance(appContext).saveTagValue("EmvRefundNeedAuthorize", "1.Need Emv Auth");
            return "02170100";
        }

        String tlv = "021701";

        if (val.equals("1.Need Emv Auth")) {
            tlv += "00";
        } else if (val.equals("2.No Need Emv Auth")) {
            tlv += "01";
        }
        Log.d(TAG, "tlv = " + tlv);
        return tlv;
    }

    public static String getContactlessLightStandard(Context appContext) {
        String val = ConfigManager.getInstance(appContext).getValueByTag("ContactlessLightStandard", "");
        if (val == null || val.equals("")) {
            ConfigManager.getInstance(appContext).saveTagValue("ContactlessLightStandard", "1.Standard");
            return "02180100";
        }

        String tlv = "021801";

        if (val.equals("1.Standard")) {
            tlv += "00";
        } else if (val.equals("2.Europe")) {
            tlv += "01";
        }
        Log.d(TAG, "tlv = " + tlv);
        return tlv;
    }
}
