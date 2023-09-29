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
package com.lovisgod.easylinksdk.MifarePlus;

import android.util.Log;

import com.lovisgod.easylinksdk.utils.*;
import com.paxsz.easylink.api.EasyLinkSdkManager;
import com.paxsz.easylink.model.picc.PiccApduRecv;
import com.paxsz.easylink.model.picc.PiccApduSend;

import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class MifarePlusApdu {

    public static String TAG = "MifarePlusApdu";
    private byte[] transIdentifier;
    private byte[] sessionKeyENC;
    private byte[] sessionKeyMAC;
    private byte[] readCounter;
    private byte[] writeCounter;
    private byte[] pcdIV;
    private byte[] piccIV;
    private byte[] plus_mac;
    private EasyLinkSdkManager easyLinkSdkManager;

    public MifarePlusApdu(EasyLinkSdkManager easyLinkSdkManager){
        transIdentifier = new byte[4];
        sessionKeyENC = new byte[16];
        sessionKeyMAC = new byte[16];
        readCounter = new byte[2];
        writeCounter = new byte[2];
        pcdIV = new byte[16];
        piccIV = new byte[16];
        plus_mac = new byte[65];
        this.easyLinkSdkManager = easyLinkSdkManager;

    }

    private byte[] aesCbcEncrypt(byte[] content, byte[] slatKey, byte[] vectorKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        SecretKey secretKey = new SecretKeySpec(slatKey, "AES");
        IvParameterSpec iv = new IvParameterSpec(vectorKey);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
        byte[] encrypted = cipher.doFinal(content);
        return encrypted;
    }


    private byte[] aesCbcDecrypt(byte[] content, byte[] slatKey, byte[] vectorKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        SecretKey secretKey = new SecretKeySpec(slatKey, "AES");
        IvParameterSpec iv = new IvParameterSpec(vectorKey);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
        byte[] decrypted = cipher.doFinal(content);
        return decrypted;
    }

    //----------------------------------------------------------------------------------
//      P L U S		R O T A T E 	L E F T 	O N E 	B Y T E
//----------------------------------------------------------------------------------
// @buffer		pointer to the buffer of data to be rotated one byte left
    private byte mifarePlusRotateOneByteLeft(byte[] buffer)
    {
        byte  i, temp;

        temp = buffer[0];
        for(i=0; i<15; i++) {
            buffer[i] = buffer[i+1];
        }
        buffer[15] = temp;

        return 0;
    }

    //----------------------------------------------------------------------------------
//      P L U S		R O T A T E 	R I G H T 	O N E 	B Y T E
//----------------------------------------------------------------------------------
// @buffer		pointer to the buffer of data to be rotated one byte right
    private byte mifarePlusRotateOneByteRight(byte[] buffer)
    {
        byte  i, temp;

        temp = buffer[15];
        for(i=15; i>0; i--) {
            buffer[i] = buffer[i-1];
        }
        buffer[0] = temp;

        return 0;
    }

    //----------------------------------------------------------------------------------
//      P L U S		S H I F T 	L E F T 	O N E 	B I T
//----------------------------------------------------------------------------------
// @input		original data
// @output		shifted data
    private void mifarePlusShiftOneBitLeft(byte []input, byte[]output)
    {
        int  i;
        byte  tmp = 0;

        for(i=15; i>=0; i--)
        {
            output[i] = (byte) (input[i] << 1);
            output[i] |= tmp;
            if((input[i] & 0x80) != 0){
                tmp = 1;
            }else{
                tmp = 0;
            }
        }
    }

    public void testAesCbcEncrypt()
    {
        try {
            byte[] encData = aesCbcEncrypt(Utils.str2Bcd("00000000ffffffff000000000AF50AF5"),
                    Utils.str2Bcd("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF"),
                    Utils.str2Bcd("00000000000000000000000000000000"));
            Log.d(TAG, "encData = " + Utils.bcd2Str(encData));

            byte[] decData = aesCbcDecrypt(Utils.str2Bcd("00000000ffffffff000000000AF50AF5"),
                    Utils.str2Bcd("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF"),
                    Utils.str2Bcd("00000000000000000000000000000000"));
            Log.d(TAG, "decData = " + Utils.bcd2Str(decData));
        }catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    //----------------------------------------------------------------------------------
//           P L U S 	S L 3 	 M A C 		C A L C U L A T I O N
//----------------------------------------------------------------------------------
// NOTE:		The MAC calculation is according to SP 800-38B, the sequence is:
//				1. L = Ek(0000...0000)
//				2. If Most left bit of L=0, then K1= L<<1; else K1=(L<<1)XOR Rb
//				   Rb = (00 00 00 00 00 00 00 87)h
//				3. If Most left bit of K1=0, then K2= K1<<1; else K2=(K1<<1)XOR Rb
// 				4. Pad the Message if it is not a multiple of 16, the first byte is 0x80
//				   the following bytes are 00h
//				5. If the original Message is multiple of 16, the LAST block of Message
//				   is XORed with K1, else it is XORed with K2
//				6. Run the AES in CBC mode, with IV = 00h
//				7. Extract MAC from the LAST enciphered block

    // @input		MAC sequence
// @datalen		MAC sequence lenght
// @output		8 byte MAC output
    void mifarePlusCalMAC( byte[] input, byte[] key, byte datalen, byte[] output)
    {
        byte  status = 0;
        byte  round;
        byte  rem;
        byte  cnt;
        byte  i,j;
        byte[]  iv = new byte[16];
        byte[]  tmp = new byte[16];
        byte  base;
        byte[]  buffer = new byte[65];

        byte  var;
        byte[]  L = new byte[16];
        byte[]  subKey1 = new byte[16];
        byte[]  subKey2 = new byte[16];
        byte[] ZERO_B = new byte[16];
        byte[] CMAC_RB = new byte[16];

        Arrays.fill(ZERO_B, 0, 16, (byte)0);
        Arrays.fill(CMAC_RB, 0, 15, (byte)0);
        CMAC_RB[15] = (byte) 0x87;

        // ------------ calculate subkey ------------------
        try {
            byte [] data = aesCbcEncrypt(ZERO_B,
                    key,
                    Utils.str2Bcd("00000000000000000000000000000000"));
            Log.d(TAG, "data enc ZERO_B = " + Utils.bcd2Str(data));
            System.arraycopy(data, 0, L, 0, 16);
        }catch (Exception e){
            e.printStackTrace();
        }
        var = L[0];
        // if MSBit of L[0]=0, sub key 1 = L << 1
        // else subKey1 = (L<<1) XOR Rb
        mifarePlusShiftOneBitLeft(L, subKey1);
        if((var & 0x80) != 0)	// MSBit is 1, then subkey1 = (L<<1)XOR Rb
        {
            subKey1[15] ^= CMAC_RB[15];
        }
        var = subKey1[0];
        // if MSBit of subKey1[0]=0, subKey 2 = subKey1 << 1
        // else subKey2 = (subKey1<<1) XOR Rb
        mifarePlusShiftOneBitLeft(subKey1, subKey2);
        if((var & 0x80) != 0)	// MSBit is 1, then subkey1 = (L<<1)XOR Rb
        {
            subKey2[15] ^= CMAC_RB[15];
        }

        // ----------- calcualte mac -------------
        System.arraycopy(input, 0, buffer, 0, datalen);
        // number of multiple 16 bytes;
        round = (byte)(datalen / 16);
        // remaining bytes, which need to be padded
        rem = (byte)(datalen % 16);

        cnt = datalen;
        // padding
        if(rem != 0)
        {
            // pad the first byte, 0x80
            buffer[cnt++] = (byte)0x80;
            // pad forllowing bytes 0x0
            for(i=0; i<15-rem; i++)
            {
                buffer[cnt++] = 0x0;
            }
            round++;
        }

        // XOR the last Block
        cnt = (byte)(datalen - rem);	// cnt is the starting address of the last block
        // the last block is not a complete block
        // XOR the last block with K2
        if(rem != 0)
        {
            for(i=0; i<16; i++)
            {
                buffer[cnt+i] ^= subKey2[i];
            }
        }
        // the last block is a complete block
        // XOR the last block with K1
        else
        {
            cnt -= 16;	// when datalen is multiple of 16
            for(i=0; i<16; i++)
            {
                buffer[cnt+i] ^= subKey1[i];
            }
        }
        //AES_ExpandKey(key, expKey);
        // AES calculation in CBC mode
        // init IV = 0
        Arrays.fill(iv, 0, 16, (byte)0);
        for(i=0; i<round; i++)
        {
            base = (byte)(i*16);
            // XOR the block with IV
            for(j=0; j<16; j++)
            {
                buffer[base + j] ^= iv[j];	// XOR
            }
            // encipher one block

            try {
                byte [] data = aesCbcEncrypt(Arrays.copyOfRange(buffer, base, base + 16),
                        key,
                        Utils.str2Bcd("00000000000000000000000000000000"));
                Log.d(TAG, "data enc rndA = " + Utils.bcd2Str(data));
                System.arraycopy(data, 0, tmp, 0, 16);
            }catch (Exception e){
                e.printStackTrace();
            }
            // update IV
            System.arraycopy(tmp, 0, iv, 0, 16);
        }

        output[0] = iv[1];
        output[1] = iv[3];
        output[2] = iv[5];
        output[3] = iv[7];
        output[4] = iv[9];
        output[5] = iv[11];
        output[6] = iv[13];
        output[7] = iv[15];
    }

    public int mifarePlusFirstAuth(byte keyBlock, byte pcdCapLen, byte[] pcdCap, byte[] aesKey, byte[] rndA, byte[] resp){
        int  status = 0;
        int  sCnt = 0, rCnt = 0;
        byte  i;

        byte[] buffer = new byte[16];
        byte[] rndB = new byte[16];
        byte[] tmp = new byte[32];

        byte[] sessionKeyEncBase = new byte[16];
        byte[] sessionKeyMacBase = new byte[16];
        byte[] MSndBuffer = new byte[64];
        byte[] MRcvBuffer = new byte[64];

        MSndBuffer[sCnt++] = 0x70;
        MSndBuffer[sCnt++] = keyBlock;
        MSndBuffer[sCnt++] = 0x40;
        MSndBuffer[sCnt++] = pcdCapLen;
        System.arraycopy(pcdCap, 0, MSndBuffer, sCnt, pcdCapLen);
        sCnt += pcdCapLen;

        //send apdu to picc.
        PiccApduSend piccApduSend = new PiccApduSend();
        PiccApduRecv piccApduRecv = new PiccApduRecv();
        piccApduSend.setSendData(MSndBuffer);
        piccApduSend.setSendLen(sCnt);
        piccApduRecv.setRecvData(MRcvBuffer);
        status = easyLinkSdkManager.piccCmdExchange(piccApduSend, piccApduRecv);
        Log.d(TAG,"cmd 0x70 status :" + status);
        Log.d(TAG, "0x70 recv " + Utils.bcd2Str(MRcvBuffer));
        if ((status != 0) || (MRcvBuffer[0] != (byte)0x90)){ // PCB + CID + SC + Ek(RNB)
            if(status == 0 && MRcvBuffer[0] != (byte)0x90){
                System.arraycopy(MRcvBuffer, 0, resp, 0, rCnt);
            }
            return 1;
        }

        try {
            byte [] data = aesCbcDecrypt(Arrays.copyOfRange(MRcvBuffer, 1, 17),
                    aesKey,
                    Utils.str2Bcd("00000000000000000000000000000000"));
            Log.d(TAG, "data dec = " + Utils.bcd2Str(data));
            System.arraycopy(data, 0, rndB, 0, 16);
        }catch (Exception e){
            e.printStackTrace();
        }

        // compose ENC session key base
        System.arraycopy(rndA, 11, sessionKeyEncBase, 0, 5);
        System.arraycopy(rndB, 11, sessionKeyEncBase, 5, 5);
        sessionKeyEncBase[10] = (byte)(rndA[4] ^ rndB[4]);
        sessionKeyEncBase[11] = (byte)(rndA[5] ^ rndB[5]);
        sessionKeyEncBase[12] = (byte)(rndA[6] ^ rndB[6]);
        sessionKeyEncBase[13] = (byte)(rndA[7] ^ rndB[7]);
        sessionKeyEncBase[14] = (byte)(rndA[8] ^ rndB[8]);
        sessionKeyEncBase[15] = 0x11;

        System.arraycopy(rndA, 7, sessionKeyMacBase, 0, 5);
        System.arraycopy(rndB, 7, sessionKeyMacBase, 5, 5);
        sessionKeyMacBase[10] = (byte)(rndA[0] ^ rndB[0]);
        sessionKeyMacBase[11] = (byte)(rndA[1] ^ rndB[1]);
        sessionKeyMacBase[12] = (byte)(rndA[2] ^ rndB[2]);
        sessionKeyMacBase[13] = (byte)(rndA[3] ^ rndB[3]);
        sessionKeyMacBase[14] = (byte)(rndA[4] ^ rndB[4]);
        sessionKeyMacBase[15] = 0x22;

        // rotate rndB one byte left
        mifarePlusRotateOneByteLeft(rndB);

        // encipher RNDA
        try {
            byte [] data = aesCbcEncrypt(rndA,
                    aesKey,
                    Utils.str2Bcd("00000000000000000000000000000000"));
            Log.d(TAG, "data enc rndA = " + Utils.bcd2Str(data));
            System.arraycopy(data, 0, buffer, 0, 16);
        }catch (Exception e){
            e.printStackTrace();
        }

        // push Ek(RNDA) into sending buffer
        System.arraycopy(buffer, 0, MSndBuffer, 1, 16);
        for(i=0; i<16; i++) {
            buffer[i] ^= rndB[i];
        }


        //encipher rotated rndB and push into sending buffer
        try {
            byte [] data = aesCbcEncrypt(buffer,
                    aesKey,
                    Utils.str2Bcd("00000000000000000000000000000000"));
            Log.d(TAG, "data enc buffer = " + Utils.bcd2Str(data));
            System.arraycopy(data, 0, MSndBuffer, 17, 16);
        }catch (Exception e){
            e.printStackTrace();
        }

        MSndBuffer[0] = 0x72;

        // send apdu to picc
        piccApduSend.setSendData(MSndBuffer);
        piccApduSend.setSendLen(33);
        piccApduRecv.setRecvData(MRcvBuffer);
        status = easyLinkSdkManager.piccCmdExchange(piccApduSend, piccApduRecv);
        Log.d(TAG,"cmd 0x72 status :" + status);
        Log.d(TAG, "0x72 recv " + Utils.bcd2Str(MRcvBuffer));

        if ((status != 0) || (MRcvBuffer[0] != (byte)0x90)){ // PCB + CID + SC + Ek(RNB)
            if(status == 0 && (MRcvBuffer[0] != (byte)0x90)){
                System.arraycopy(MRcvBuffer, 0, resp, 0, rCnt);
            }
            return 2;
        }

        try {
            byte [] data = aesCbcDecrypt(Arrays.copyOfRange(MRcvBuffer, 1, 17),
                    aesKey,
                    Utils.str2Bcd("00000000000000000000000000000000"));
            Log.d(TAG, "data dec 1 - 16 = " + Utils.bcd2Str(data));
            System.arraycopy(data, 0, tmp, 0, 16);

            data = aesCbcDecrypt(Arrays.copyOfRange(MRcvBuffer, 17, 33),
                    aesKey,
                    Utils.str2Bcd("00000000000000000000000000000000"));
            Log.d(TAG, "data dec 17 - 32= " + Utils.bcd2Str(data));
            System.arraycopy(data, 0, tmp, 16, 16);
        }catch (Exception e){
            e.printStackTrace();
        }

        for(i=16; i<32; i++){	// distract P2 from IV, which is C1
            tmp[i] ^= MRcvBuffer[i-15];
        }

        System.arraycopy(tmp, 4, buffer, 0, 16);
        mifarePlusRotateOneByteRight(buffer);
        if(!Arrays.equals(buffer, rndA)){
            Log.d(TAG,"PCD to PICC authentication fails");
            return 3;
        }

        // session key
        try {
            byte [] data = aesCbcEncrypt(sessionKeyEncBase,
                    aesKey,
                    Utils.str2Bcd("00000000000000000000000000000000"));
            Log.d(TAG, "data enc buffer = " + Utils.bcd2Str(data));
            System.arraycopy(data, 0, sessionKeyENC, 0, 16);

            data = aesCbcEncrypt(sessionKeyMacBase,
                    aesKey,
                    Utils.str2Bcd("00000000000000000000000000000000"));
            Log.d(TAG, "data enc buffer = " + Utils.bcd2Str(data));
            System.arraycopy(data, 0, sessionKeyMAC, 0, 16);
        }catch (Exception e){
            e.printStackTrace();
        }
        // reset read counter
        readCounter[0] = 0x00;
        readCounter[1] = 0x00;

        // reset write counter
        writeCounter[0] = 0x00;
        writeCounter[1] = 0x00;

        // init PCD init vector
        System.arraycopy(tmp, 0, transIdentifier, 0, 4);
        System.arraycopy(transIdentifier, 0, pcdIV, 0, 4);
        Arrays.fill(pcdIV, 4, 16, (byte)0);
        Arrays.fill(piccIV, 0, 12, (byte)0);
        System.arraycopy(transIdentifier, 0, piccIV, 12, 4);
        return 0;
    }

    public int mifarePlusFollowingAuthTCL(byte keyBlock, byte[] aesKey, byte[] rndA, byte[] resp){
        int  status = 0;
        int  sCnt = 0, rCnt = 0;
        byte  i;

        byte[] buffer = new byte[16];
        byte[] rndB = new byte[16];
        byte[] tmp = new byte[16];

        byte[] sessionKeyEncBase = new byte[16];
        byte[] sessionKeyMacBase = new byte[16];
        byte[] MSndBuffer = new byte[64];
        byte[] MRcvBuffer = new byte[64];

        MSndBuffer[sCnt++] = 0x76;
        MSndBuffer[sCnt++] = keyBlock;
        MSndBuffer[sCnt++] = 0x40;

        //send apdu to picc.
        PiccApduSend piccApduSend = new PiccApduSend();
        PiccApduRecv piccApduRecv = new PiccApduRecv();
        piccApduSend.setSendData(MSndBuffer);
        piccApduSend.setSendLen(sCnt);
        piccApduRecv.setRecvData(MRcvBuffer);
        status = easyLinkSdkManager.piccCmdExchange(piccApduSend, piccApduRecv);
        Log.d(TAG,"mifarePlusFollowingAuthTCL, cmd 0x76 status :" + status);
        Log.d(TAG, "mifarePlusFollowingAuthTCL 0x76 recv " + Utils.bcd2Str(piccApduRecv.getRecvData()));

        if ((status != 0) || (MRcvBuffer[0] != (byte)0x90)){ // PCB + CID + SC + Ek(RNB)
            if(status == 0 && MRcvBuffer[0] != (byte)0x90){
                System.arraycopy(MRcvBuffer, 0, resp, 0, rCnt);
            }
            return 1;
        }

        // decipher the response to get RndB
        try {
            byte [] data = aesCbcDecrypt(Arrays.copyOfRange(MRcvBuffer, 1, 17),
                    aesKey,
                    Utils.str2Bcd("00000000000000000000000000000000"));
            Log.d(TAG, "data dec rndB = " + Utils.bcd2Str(data));
            System.arraycopy(data, 0, rndB, 0, 16);
        }catch (Exception e){
            e.printStackTrace();
        }
        // distract rndB from IV
        for(i=0; i<16; i++) {
            rndB[i] ^= piccIV[i];
        }


        // compose ENC session key base
        System.arraycopy(rndA, 11, sessionKeyEncBase, 0, 5);
        System.arraycopy(rndB, 11, sessionKeyEncBase, 5, 5);
        sessionKeyEncBase[10] = (byte)(rndA[4] ^ rndB[4]);
        sessionKeyEncBase[11] = (byte)(rndA[5] ^ rndB[5]);
        sessionKeyEncBase[12] = (byte)(rndA[6] ^ rndB[6]);
        sessionKeyEncBase[13] = (byte)(rndA[7] ^ rndB[7]);
        sessionKeyEncBase[14] = (byte)(rndA[8] ^ rndB[8]);
        sessionKeyEncBase[15] = 0x11;

        System.arraycopy(rndA, 7, sessionKeyMacBase, 0, 5);
        System.arraycopy(rndB, 7, sessionKeyMacBase, 5, 5);
        sessionKeyMacBase[10] = (byte)(rndA[0] ^ rndB[0]);
        sessionKeyMacBase[11] = (byte)(rndA[1] ^ rndB[1]);
        sessionKeyMacBase[12] = (byte)(rndA[2] ^ rndB[2]);
        sessionKeyMacBase[13] = (byte)(rndA[3] ^ rndB[3]);
        sessionKeyMacBase[14] = (byte)(rndA[4] ^ rndB[4]);
        sessionKeyMacBase[15] = 0x22;

        // XOR rndA with IV(pcdIV)
        for(i=0; i<16; i++) {
            tmp[i] = (byte) (rndA[i] ^ pcdIV[i]);
        }

        // rotate rndB one byte left
        mifarePlusRotateOneByteLeft(rndB);

        // encipher RNDA
        try {
            byte [] data = aesCbcEncrypt(tmp,
                    aesKey,
                    Utils.str2Bcd("00000000000000000000000000000000"));
            Log.d(TAG, "data enc rndA = " + Utils.bcd2Str(data));
            System.arraycopy(data, 0, buffer, 0, 16);
        }catch (Exception e){
            e.printStackTrace();
        }

        // push Ek(RNDA) into sending buffer
        System.arraycopy(buffer, 0, MSndBuffer, 1, 16);
        for(i=0; i<16; i++) {
            buffer[i] ^= rndB[i];
        }


        //encipher rotated rndB and push into sending buffer
        try {
            byte [] data = aesCbcEncrypt(buffer,
                    aesKey,
                    Utils.str2Bcd("00000000000000000000000000000000"));
            Log.d(TAG, "data enc buffer = " + Utils.bcd2Str(data));
            System.arraycopy(data, 0, MSndBuffer, 17, 16);
        }catch (Exception e){
            e.printStackTrace();
        }

        MSndBuffer[0] = 0x72;

        // send apdu to picc
        piccApduSend.setSendData(MSndBuffer);
        piccApduSend.setSendLen(33);
        piccApduRecv.setRecvData(MRcvBuffer);
        status = easyLinkSdkManager.piccCmdExchange(piccApduSend, piccApduRecv);
        Log.d(TAG,"mifarePlusFollowingAuthTCL, cmd 0x72 status :" + status);
        Log.d(TAG, "mifarePlusFollowingAuthTCL 0x72 recv " + Utils.bcd2Str(piccApduRecv.getRecvData()));

        if ((status != 0) || (MRcvBuffer[0] != (byte)0x90)){ // PCB + CID + SC + Ek(RNB)
            if(status == 0 && MRcvBuffer[0] != (byte)0x90){
                System.arraycopy(MRcvBuffer, 0, resp, 0, rCnt);
            }
            return 2;
        }

        try {
            byte [] data = aesCbcDecrypt(Arrays.copyOfRange(MRcvBuffer, 1, 17),
                    aesKey,
                    Utils.str2Bcd("00000000000000000000000000000000"));
            Log.d(TAG, "data dec 1 - 16 = " + Utils.bcd2Str(data));
            System.arraycopy(data, 0, buffer, 0, 16);

        }catch (Exception e){
            e.printStackTrace();
        }

        // retreive rndA' from IV
        for(i=0; i<16; i++) {
            buffer[i] ^= piccIV[i];
        }

        mifarePlusRotateOneByteRight(buffer);
        if(!Arrays.equals(buffer, rndA)){
            Log.d(TAG,"PCD to PICC authentication fails");
            return 3;
        }

        // session key
        try {
            byte [] data = aesCbcEncrypt(sessionKeyEncBase,
                    aesKey,
                    Utils.str2Bcd("00000000000000000000000000000000"));
            Log.d(TAG, "data enc buffer = " + Utils.bcd2Str(data));
            System.arraycopy(data, 0, sessionKeyENC, 0, 16);

            data = aesCbcEncrypt(sessionKeyMacBase,
                    aesKey,
                    Utils.str2Bcd("00000000000000000000000000000000"));
            Log.d(TAG, "data enc buffer = " + Utils.bcd2Str(data));
            System.arraycopy(data, 0, sessionKeyMAC, 0, 16);
        }catch (Exception e){
            e.printStackTrace();
        }

        return 0;
    }

    //----------------------------------------------------------------------------------
//           P L U S 	S L 3 	 W R I T E
//----------------------------------------------------------------------------------
// NOTE:		The typical sequence of the write command is:
//				1. Encrypt the data if required
//				2. Calculate MAC
//				3. send command to the PICC and receive response
//				4. increase W_Ctr and update IV
//				5. PCD calculate MAC and compare with returned MAC, if MAC on response

    // @wtcmd		write command code, 0xA0~0xA3
    // @blockNo		starting block number to be written
    // @noOfBlocks	data length, multiple of 16 bytes
    // @datain		data to be written
    // @resp		return buffer
    public byte mifarePlusSL3Write(byte wtcmd, byte blockNo,	byte noOfBlocks, byte[] datain, byte[] resp)
    {
        int  status = 0;
        int  sCnt = 0, rCnt = 0;
        byte[]  MSndBuffer = new byte[64];
        byte[] MRcvBuffer = new byte[64];
        byte  cnt = 0;
        byte macOnRes = 0;
        byte encWt = 0;
        byte[]  tmp = new byte[8];

        switch(wtcmd)
        {
            case (byte)0xA0:
                macOnRes = 0;
                encWt = 1;
                break;
            case (byte)0xA1:
                macOnRes = 1;
                encWt = 1;
                break;
            case (byte)0xA2:
                macOnRes = 0;
                encWt = 0;
                break;
            case (byte)0xA3:
                macOnRes = 1;
                encWt = 0;
                break;
            default:
                break;
        }

        MSndBuffer[0] = wtcmd;
        MSndBuffer[1] = blockNo;
        MSndBuffer[2] = 0x00;

        cnt = (byte)(16 * noOfBlocks);
        // encrypted write
        /* ----------------------- encipher the data ---------------- */
        if(encWt != 0)
        {
            try {
                byte [] data = aesCbcEncrypt(datain, sessionKeyENC, pcdIV);
                Log.d(TAG, "datain enc data = " + Utils.bcd2Str(data));
                System.arraycopy(data, 0, MSndBuffer, 3, 16);

            }catch (Exception e){
                e.printStackTrace();
            }
        }
        else
        {
            System.arraycopy(datain, 0, MSndBuffer, 3, cnt);
        }

        /* ----------------------- calculate the mac ---------------- */
        // sequence: CC(1) + W_Ctr(2) + TI(4) + BNr(2) + data(16/32/48) + padding(7)
        // command code
        plus_mac[0] = wtcmd;
        // W_Ctr, LSByte
        plus_mac[1] = writeCounter[0];
        // W_Ctr, MSByte
        plus_mac[2] = writeCounter[1];
        // Transaction Identifier
        System.arraycopy(transIdentifier, 0, plus_mac, 3, 4);

        // Block address, LSByte
        plus_mac[7] = blockNo;
        // Block address, MSByte
        plus_mac[8] = 0x00;
        // data in plain or enciphered
        System.arraycopy(MSndBuffer, 3, plus_mac, 9, cnt);

        byte[] outMac = new byte[8];
        mifarePlusCalMAC(plus_mac, sessionKeyMAC, (byte)(cnt+9),outMac);
        System.arraycopy(outMac, 0, MSndBuffer, cnt+3, 8);

        /* ***********  send command to PICC ************* */
        sCnt = cnt + 11;

        /* ***********	send command to PICC ************* */

        PiccApduSend piccApduSend = new PiccApduSend();
        PiccApduRecv piccApduRecv = new PiccApduRecv();
        piccApduSend.setSendData(MSndBuffer);
        piccApduSend.setSendLen(sCnt);
        piccApduRecv.setRecvData(MRcvBuffer);
        status = easyLinkSdkManager.piccCmdExchange(piccApduSend, piccApduRecv);
        Log.d(TAG,"mifarePlusSL3Write,  status :" + status);
        Log.d(TAG, "mifarePlusSL3Write  recv " + Utils.bcd2Str(piccApduRecv.getRecvData()));

        if(macOnRes != 0){
            cnt = 8;
        }else{
            cnt = 0;
        }

        if((status != 0) || MRcvBuffer[0] != (byte)0x90)// PCB+CID+SC(MAC)
        {
            if(status == 0 && MRcvBuffer[0] != (byte)0x90) {

                System.arraycopy(MRcvBuffer, 0, resp, 0, rCnt);
            }
            return 1;
            //status = MRcvBuffer[2];
        }
        /* ***********  increase readCounter ************* */
        if(writeCounter[0] == 0xFF)
        {
            writeCounter[0] = 0;
            writeCounter[1] += 1;
        }
        else
        {
            writeCounter[0] += 1;
        }
        /* ***********  update pcdIV and piccIV ************* */
        pcdIV[6] = writeCounter[0];
        pcdIV[7] = writeCounter[1];
        pcdIV[10] = writeCounter[0];
        pcdIV[11] = writeCounter[1];
        pcdIV[14] = writeCounter[0];
        pcdIV[15] = writeCounter[1];

        piccIV[2] = writeCounter[0];
        piccIV[3] = writeCounter[1];
        piccIV[6] = writeCounter[0];
        piccIV[7] = writeCounter[1];
        piccIV[10] = writeCounter[0];
        piccIV[11] = writeCounter[1];

        /* ***********  PCD calculate and verify MAC ************* */
        if(macOnRes != 0)	// MAC on response
        {
            // MAC sequence: SC(1) W_Ctr(2) TI(4) padding
            // Status Code
            plus_mac[0] = MRcvBuffer[0];
            // W_Ctr LSByte
            plus_mac[1] = writeCounter[0];
            // W_Ctr MSByte
            plus_mac[2] = writeCounter[1];
            // transaction identifier

            System.arraycopy(transIdentifier, 0, plus_mac, 3, 4);

            mifarePlusCalMAC(plus_mac, sessionKeyMAC, (byte)7, tmp);

            if(!Arrays.equals(Arrays.copyOfRange(MRcvBuffer, 1, 9), tmp))
            {
                Log.d(TAG,"Mac error");
                return 2;
            }
        }

        return 0;
    }

    //----------------------------------------------------------------------------------
//           P L U S 	S L 3 	 R E A D
//----------------------------------------------------------------------------------
// NOTE:		The typical sequence of the read command is:
//				1. Calculate the MAC, if MAC on command
//				2. send command to the PICC and receive response
//				3. increase R_Ctr and update IV
//				4. PCD calculate MAC and compare with returned MAC, if MAC on response
//				5. decipher data if Read encrypted

// NOTE:		As there is shortage of RAM, the MAC is always required on response
    // note the mac buffer size, as the unmaced read will accumulate the mac sequence
    // there maybe shortage of RAM if noOfBlocks is large
    // e.g. if noOfBlocks = 1, mac buffer size: 10 + 16 + (3 + 16) + padding = 48
    // e.g. if noOfBlocks = 2, mac buffer size: 10 + 32 + (3 + 32) + padding = 80
    // e.g. if noOfBlocks = 3, mac buffer size: 10 + 48 + (3 + 48) + padding = 112

    // @cmd			read command code, 0x30~x37
// @blockNo		starting block number
// @noOfBlocks	number of blocks to read
// @resp		return buffer
    public byte mifarePlusSL3Read(byte cmd, byte blockNo, byte noOfBlocks, byte[] resp)
    {
        int  status = 0;
        int  sCnt = 0, rCnt = 0;
        byte  cnt = 0;
        byte[]  MSndBuffer = new byte[64];
        byte[] MRcvBuffer = new byte[64];
        byte macOnCmd = 0;
        byte macOnRes = 0;
        byte encRd = 0;
        byte[]  tmp = new byte[8];

        switch(cmd)
        {
		/*
		case 0x30:
			macOnCmd = 1;
			macOnRes = 0;
			encRd = 1;
			break;
		*/
            case 0x31:
                macOnCmd = 1;
                macOnRes = 1;
                encRd = 1;
                break;
		/*
			case 0x32:
			macOnCmd = 1;
			macOnRes = 0;
			encRd = 0;
			break;
		*/
            case 0x33:
                macOnCmd = 1;
                macOnRes = 1;
                encRd = 0;
                break;
		/*
		case 0x34:
			macOnCmd = 0;
			macOnRes = 0;
			encRd = 1;
			break;
		*/
            case 0x35:
                macOnCmd = 0;
                macOnRes = 1;
                encRd = 1;
                break;
		/*
		case 0x36:
			macOnCmd = 0;
			macOnRes = 0;
			encRd = 0;
			break;
		*/
            case 0x37:
                macOnCmd = 0;
                macOnRes = 1;
                encRd = 0;
                break;
            default:
                break;
        }

        MSndBuffer[sCnt++] = cmd;
        MSndBuffer[sCnt++] = blockNo;
        MSndBuffer[sCnt++] = 0x00;
        MSndBuffer[sCnt++] = noOfBlocks;

        /* ***********  calculate MAC on command ************* */
        if(macOnCmd != 0)
        {
            // sequence: CC + R_Ctr + TI + BNr + NoOfBlocks + padding
            // command code
            plus_mac[0] = cmd;
            // R_Ctr, LSByte
            plus_mac[1] = readCounter[0];
            // R_Ctr, MSByte
            plus_mac[2] = readCounter[1];
            // Transaction Identifier
            System.arraycopy(transIdentifier, 0, plus_mac, 3, 4);
            // Block address, LSByte
            plus_mac[7] = blockNo;
            // Block address, MSByte
            plus_mac[8] = 0x00;
            // Ext, number of blocks
            plus_mac[9] = noOfBlocks;
            // calculate MAC, append the MAC to the command

            byte[] outMac = new byte[8];
            mifarePlusCalMAC(plus_mac, sessionKeyMAC, (byte)10,outMac);
            System.arraycopy(outMac, 0, MSndBuffer, sCnt, 8);

            sCnt += 8;
        }

        /* ***********  send command to PICC ************* */

        PiccApduSend piccApduSend = new PiccApduSend();
        PiccApduRecv piccApduRecv = new PiccApduRecv();
        piccApduSend.setSendData(MSndBuffer);
        piccApduSend.setSendLen(sCnt);
        piccApduRecv.setRecvData(MRcvBuffer);
        status = easyLinkSdkManager.piccCmdExchange(piccApduSend, piccApduRecv);
        Log.d(TAG,"mifarePlusSL3Read status" + status);
        Log.d(TAG,"mifarePlusSL3Read recv" + Utils.bcd2Str(piccApduRecv.getRecvData()));

        if(macOnRes == 1){
            cnt = 8;
        }else{
            cnt = 0;
        }

        if( (status != 0) || (MRcvBuffer[0] != (byte)0x90)){// PCB+CID+SC+Ek+(MAC){
            if(status == 0 && MRcvBuffer[0] != (byte)0x90) {
                System.arraycopy(MRcvBuffer, 0, resp, 0, rCnt);
            }
            return 1;
        }


        /* ***********  increase readCounter ************* */
        if(readCounter[0] == (byte)0xFF)
        {
            readCounter[0] = 0;
            readCounter[1] += 1;
        }
        else
        {
            readCounter[0] += 1;
        }
        /* ***********  update pcdIV and piccIV ************* */
        pcdIV[4] = readCounter[0];
        pcdIV[5] = readCounter[1];
        pcdIV[8] = readCounter[0];
        pcdIV[9] = readCounter[1];
        pcdIV[12] = readCounter[0];
        pcdIV[13] = readCounter[1];

        piccIV[0] = readCounter[0];
        piccIV[1] = readCounter[1];
        piccIV[4] = readCounter[0];
        piccIV[5] = readCounter[1];
        piccIV[8] = readCounter[0];
        piccIV[9] = readCounter[1];

        cnt = (byte)(16 * noOfBlocks);
        /* ***********  PCD calculate and verify MAC ************* */
        if(macOnRes == 1)	// MAC on response
        {
            // NOTE: in this program the MAC is always required on response
            // 		 the MAC sequence will be accumulated if the last read does not
            //		 require MAC on response. Make sure there is enough RAM
            // prepare mac sequence
            // MAC sequence: SC(1) R_Ctr(2) TI(4) BNr(1) n.o.b(1) Ek(n.o.b*16) padding
            // Status Code
            plus_mac[0] = MRcvBuffer[0];
            // R_Ctr LSByte
            plus_mac[1] = readCounter[0];
            // R_Ctr MSByte
            plus_mac[2] = readCounter[1];
            // transaction identifier
            System.arraycopy(transIdentifier, 0, plus_mac, 3, 4);
            // Block number, LSByte
            plus_mac[7] = blockNo;
            // Block number, MSByte
            plus_mac[8] = 0x00;
            // Number of blocks
            plus_mac[9] = noOfBlocks;

            // returned encrypted data, with length of 16*noOfBlocks
            System.arraycopy(MRcvBuffer, 1, plus_mac, 10, cnt);

            mifarePlusCalMAC(plus_mac, sessionKeyMAC, (byte)(cnt+10), tmp);

            // response MAC does not match
            if(!Arrays.equals(Arrays.copyOfRange(MRcvBuffer, 1+cnt, 9+cnt), tmp))
            {
                Log.d(TAG,"Mac error");
                return 2;
            }
        }


        System.arraycopy(MRcvBuffer, 1, resp, 0, cnt);

        /* ***********  PCD decipher data ************* */
        if(encRd == 1)	// encrypted
        {
            // prepare IV
            try {
                byte [] data = aesCbcDecrypt(Arrays.copyOfRange(MRcvBuffer, 1, 17), sessionKeyENC, piccIV);
                Log.d(TAG, "datain enc data = " + Utils.bcd2Str(data));
                System.arraycopy(data, 0, resp, 0, 16);

            }catch (Exception e){
                e.printStackTrace();
            }
        }

        return 0;
    }

    //----------------------------------------------------------------------------------
//    P L U S 	S L 3 	 V A L U E 	--> I N C R E M E N T / D E C R E M E N T
//----------------------------------------------------------------------------------
// NOTE:		The typical sequence of the value command is:
//				1. Encrypt the data if required
//				2. Calculate MAC - mandatory
//				3. send command to the PICC and receive response
//				4. increase W_Ctr and update IV
//				5. PCD calculate MAC and compare with returned MAC, if MAC on response

    // @cmd			Increment/Decrement command code
// @SourceBNr	source block number
// @value		4 bytes value data
// @resp		return buffer
    public byte mifarePlusSL3ValueIncrementDecrement(byte cmd, byte SourceBNr, byte[] value, byte[] resp)
    { //增值减值
        int  status = 0;
        byte  cnt = 0;
        byte  macOnRes = 0;
        byte[]  tmp = new byte[16];
        int  sCnt = 0, rCnt = 0;
        byte[]  MSndBuffer = new byte[64];
        byte[] MRcvBuffer = new byte[64];

        switch(cmd)
        {
            case (byte)0xB0:
                macOnRes = 0;
                break;
            case (byte)0xB1:
                macOnRes = 1;
                break;
            case (byte)0xB2:
                macOnRes = 0;
                break;
            case (byte)0xB3:
                macOnRes = 1;
                break;
            default:
                break;
        }

        MSndBuffer[sCnt++] = cmd;
        MSndBuffer[sCnt++] = SourceBNr;
        MSndBuffer[sCnt++] = 0x00;

        // value operation always in Encrypted
        /* ----------------------- encipher the data ---------------- */
        // 4 byte value
        System.arraycopy(value, 0, tmp, 0, 4);
        // pad the first byte 0x80
        tmp[4] = (byte)0x80;
        // pad the following bytes 0x0
        Arrays.fill(tmp, 5, 16, (byte)0);
        // encipher the value block
        try {
            byte [] data = aesCbcEncrypt(tmp, sessionKeyENC, pcdIV);
            Log.d(TAG, "tmp enc data = " + Utils.bcd2Str(data));
            System.arraycopy(data, 0, MSndBuffer, sCnt, 16);

        }catch (Exception e){
            e.printStackTrace();
        }

        /* ----------------------- calculate the mac ---------------- */
        // MAC sequence: CC(1) + W_Ctr(2) + TI(4) + BNr(2) + data(16) + padding(7)
        // command code
        plus_mac[0] = cmd;
        // W_Ctr, LSByte
        plus_mac[1] = writeCounter[0];
        // W_Ctr, MSByte
        plus_mac[2] = writeCounter[1];
        // Transaction Identifier
        System.arraycopy(transIdentifier, 0, plus_mac, 3, 4);
        // Block address, LSByte
        plus_mac[7] = SourceBNr;
        // Block address, MSByte
        plus_mac[8] = 0x00;
        // data in plain or enciphered
        System.arraycopy(MSndBuffer, sCnt, plus_mac, 9, 16);
        sCnt += 16;

        byte[] macTmp = new byte[8];
        mifarePlusCalMAC(plus_mac, sessionKeyMAC, (byte)25, macTmp);
        System.arraycopy(macTmp, 0, MSndBuffer, sCnt, 8);
        sCnt += 8;
        /* ***********  send command to PICC ************* */


        /* ***********	send command to PICC ************* */

        PiccApduSend piccApduSend = new PiccApduSend();
        PiccApduRecv piccApduRecv = new PiccApduRecv();
        piccApduSend.setSendData(MSndBuffer);
        piccApduSend.setSendLen(sCnt);
        piccApduRecv.setRecvData(MRcvBuffer);
        status = easyLinkSdkManager.piccCmdExchange(piccApduSend, piccApduRecv);
        Log.d(TAG,"mifarePlusSL3ValueIncrementDecrement status" + status);
        Log.d(TAG,"mifarePlusSL3ValueIncrementDecrement recv" + Utils.bcd2Str(piccApduRecv.getRecvData()));


        if(macOnRes==1){
            cnt = 8;
        }else{
            cnt = 0;
        }

        if((status != 0) || MRcvBuffer[0] != (byte)0x90)// PCB+CID+SC(MAC)
        {
            if(status == 0 && MRcvBuffer[0] != (byte)0x90) {
                System.arraycopy(MRcvBuffer, 0, resp, 0, rCnt);
            }
            return 1;
            //status = MRcvBuffer[2];
        }

        /* ***********  increase readCounter ************* */
        if(writeCounter[0] == (byte)0xFF)
        {
            writeCounter[0] = 0;
            writeCounter[1] += 1;
        }
        else
        {
            writeCounter[0] += 1;
        }
        /* ***********  update pcdIV and piccIV ************* */
        pcdIV[6] = writeCounter[0];
        pcdIV[7] = writeCounter[1];
        pcdIV[10] = writeCounter[0];
        pcdIV[11] = writeCounter[1];
        pcdIV[14] = writeCounter[0];
        pcdIV[15] = writeCounter[1];

        piccIV[2] = writeCounter[0];
        piccIV[3] = writeCounter[1];
        piccIV[6] = writeCounter[0];
        piccIV[7] = writeCounter[1];
        piccIV[10] = writeCounter[0];
        piccIV[11] = writeCounter[1];

        /* ***********  PCD calculate and verify MAC ************* */
        if(macOnRes == 1)	// MAC on response
        {
            // MAC sequence: SC(1) W_Ctr(2) TI(4) padding
            // Status Code
            plus_mac[0] = MRcvBuffer[0];
            // W_Ctr LSByte
            plus_mac[1] = writeCounter[0];
            // W_Ctr MSByte
            plus_mac[2] = writeCounter[1];
            // transaction identifier

            System.arraycopy(transIdentifier, 0, plus_mac, 3, 4);

            mifarePlusCalMAC(plus_mac, sessionKeyMAC, (byte)7, tmp);


            if(!Arrays.equals(Arrays.copyOfRange(MRcvBuffer, 1, 9), Arrays.copyOfRange(tmp, 0, 8))) {
                Log.d(TAG, "mac error");
                return 2;
            }
        }

        return 0;
    }

    //----------------------------------------------------------------------------------
//    P L U S 	S L 3 	 V A L U E 	--> R E S T O R E / T R A N S F E R
//----------------------------------------------------------------------------------
// NOTE:		The typical sequence of the value command is:
//				1. Encrypt the data if required
//				2. Calculate MAC - mandatory
//				3. send command to the PICC and receive response
//				4. increase W_Ctr and update IV
//				5. PCD calculate MAC and compare with returned MAC, if MAC on response

    // @cmd			Restore/Transfer command code
// @blockNo		block number
// @resp		return buffer
    public byte mifarePlusSL3ValueRestoreTransfer(byte cmd, byte blockNo, byte []resp)
    { //恢复或转移
        int  status = 0;
        byte  cnt = 0;
        byte  macOnRes = 0;
        byte[]  tmp = new byte[16];
        int  sCnt = 0, rCnt = 0;
        byte[]  MSndBuffer = new byte[64];
        byte[] MRcvBuffer = new byte[64];

        switch(cmd)
        {
            case (byte)0xB4:
                macOnRes = 0;
                break;
            case (byte)0xB5:
                macOnRes = 1;
                break;
            case (byte)0xC2:
                macOnRes = 0;
                break;
            case (byte)0xC3:
                macOnRes = 1;
                break;
            default:
                break;
        }

        MSndBuffer[sCnt++] = cmd;
        MSndBuffer[sCnt++] = blockNo;
        MSndBuffer[sCnt++] = 0x00;

        /* ----------------------- calculate the mac ---------------- */
        // MAC sequence: CC(1) + W_Ctr(2) + TI(4) + BNr(2) + data(16) + padding(7)
        // command code
        plus_mac[0] = cmd;
        // W_Ctr, LSByte
        plus_mac[1] = writeCounter[0];
        // W_Ctr, MSByte
        plus_mac[2] = writeCounter[1];
        // Transaction Identifier
        System.arraycopy(transIdentifier, 0, plus_mac, 3, 4);
        // Block address, LSByte
        plus_mac[7] = blockNo;
        // Block address, MSByte
        plus_mac[8] = 0x00;

        byte[] macTmp = new byte[8];
        mifarePlusCalMAC(plus_mac, sessionKeyMAC, (byte)9, macTmp);
        System.arraycopy(macTmp, 0, MSndBuffer, sCnt, 8);

        sCnt += 8;


        /* ***********	send command to PICC ************* */
        PiccApduSend piccApduSend = new PiccApduSend();
        PiccApduRecv piccApduRecv = new PiccApduRecv();
        piccApduSend.setSendData(MSndBuffer);
        piccApduSend.setSendLen(sCnt);
        piccApduRecv.setRecvData(MRcvBuffer);
        status = easyLinkSdkManager.piccCmdExchange(piccApduSend, piccApduRecv);
        Log.d(TAG,"mifarePlusSL3ValueRestoreTransfer status" + status);
        Log.d(TAG,"mifarePlusSL3ValueRestoreTransfer recv" + Utils.bcd2Str(piccApduRecv.getRecvData()));

            if(macOnRes==1){
                cnt = 8;
            }else{
                cnt = 0;
            }
        /* ***********  send command to PICC ************* */
        if((status != 0) || MRcvBuffer[0] != (byte)0x90)// PCB+CID+SC(MAC)
        {
            if(status == 0 && MRcvBuffer[0] != (byte)0x90) {
                System.arraycopy(MRcvBuffer, 0, resp, 0, rCnt);
            }
            return 1;
            //status = MRcvBuffer[2];
        }
        /* ***********  increase readCounter ************* */
        if(writeCounter[0] == (byte)0xFF)
        {
            writeCounter[0] = 0;
            writeCounter[1] += 1;
        }
        else
        {
            writeCounter[0] += 1;
        }
        /* ***********  update pcdIV and piccIV ************* */
        pcdIV[6] = writeCounter[0];
        pcdIV[7] = writeCounter[1];
        pcdIV[10] = writeCounter[0];
        pcdIV[11] = writeCounter[1];
        pcdIV[14] = writeCounter[0];
        pcdIV[15] = writeCounter[1];

        piccIV[2] = writeCounter[0];
        piccIV[3] = writeCounter[1];
        piccIV[6] = writeCounter[0];
        piccIV[7] = writeCounter[1];
        piccIV[10] = writeCounter[0];
        piccIV[11] = writeCounter[1];

        /* ***********  PCD calculate and verify MAC ************* */
        if(macOnRes == 1)	// MAC on response
        {
            // MAC sequence: SC(1) W_Ctr(2) TI(4) padding
            // Status Code
            plus_mac[0] = MRcvBuffer[0];
            // W_Ctr LSByte
            plus_mac[1] = writeCounter[0];
            // W_Ctr MSByte
            plus_mac[2] = writeCounter[1];
            // transaction identifier
            System.arraycopy(transIdentifier, 0, plus_mac, 3, 4);

            mifarePlusCalMAC(plus_mac, sessionKeyMAC, (byte)7, tmp);


            if(!Arrays.equals(Arrays.copyOfRange(MRcvBuffer, 1, 9), Arrays.copyOfRange(tmp, 0, 8))) {
                Log.d(TAG, "mac error");
                return 2;
            }
        }

        return 0;
    }


    public void macTest(){
        byte[] input = new byte[8];
        byte[] output = new byte[8];
        Arrays.fill(input, 0, 8, (byte)0xff);
        mifarePlusCalMAC( input, Utils.str2Bcd("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF"), (byte)8, output);
        Log.d(TAG,"macTest output = " +Utils.bcd2Str(output));
    }


}
