package com.lovisgod.easylinksdk.utils;

import android.util.Log;

import com.pax.gl.utils.impl.Convert;
import com.paxsz.easylink.model.DataModel;
import com.paxsz.easylink.model.TLVDataObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Utils {
    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte1(hexChars[pos]) << 4 | charToByte1(hexChars[pos + 1]));
        }
        return d;
    }

    /**
     * Convert char to byte
     *
     * @param c char
     * @return byte
     */
    private static byte charToByte1(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    public static void ConvIntA2ByteA(int[] iA, byte[] bA) {
        for (int i = 0; i < iA.length; i++) {
            bA[i] = (byte) (iA[i] & 0xFF);
        }
    }

    public static String bcd2Str(byte[] bytes) {
        char temp[] = new char[bytes.length * 2], val;

        for (int i = 0; i < bytes.length; i++) {
            val = (char) (((bytes[i] & 0xf0) >> 4) & 0x0f);
            temp[i * 2] = (char) (val > 9 ? val + 'A' - 10 : val + '0');

            val = (char) (bytes[i] & 0x0f);
            temp[i * 2 + 1] = (char) (val > 9 ? val + 'A' - 10 : val + '0');
        }
        return new String(temp);
    }

    private static int strByte2Int(byte b) {
        int j;
        if ((b >= 'a') && (b <= 'z')) {
            j = b - 'a' + 0x0A;
        } else {
            if ((b >= 'A') && (b <= 'Z'))
                j = b - 'A' + 0x0A;
            else
                j = b - '0';
        }
        return j;
    }

    public static byte[] str2Bcd(String asc) {
        String str = asc;
        if (str.length() % 2 != 0) {
            str = "0" + str;
        }
        int len = str.length();
        if (len >= 2) {
            len /= 2;
        }
        byte[] bbt = new byte[len];
        byte[] abt = str.getBytes();

        for (int p = 0; p < str.length() / 2; p++) {
            bbt[p] = (byte) ((strByte2Int(abt[(2 * p)]) << 4) + strByte2Int(abt[(2 * p + 1)]));
        }
        return bbt;
    }

    //有bug(value大于127时，L表示有错)
    private static void unpackValues(final DataModel.DataType dataType, byte[] data, List<TLVDataObject> tlvDataObjects) {
        int dataLength = data.length;
        int index = 0;
        while (index < dataLength - 1) {
            int tagLength = 0;
            if (dataType == DataModel.DataType.TRANSACTION_DATA) {
                if (dataLength < index + 2) {
                    return;
                }
                // Operate 0x1F to & with the first bit, the tag's length is 1 bit if the result is not equal to 0x1F.
                // 先取第一位和0x1F相与，结果如果不等于0x1F，则Tag为1位，等于的话再进行拆分
                if ((data[index] & 0x1F) != 0x1F) {
                    tagLength = 1;
                } else {
                    if ((data[index + 1] & 0x80) == 0x80) {
                        tagLength = 3;
                    } else {
                        tagLength = 2;
                    }
                }
            } else if (dataType == DataModel.DataType.CONFIGURATION_DATA) {
                if (dataLength < index + 3) {
                    return;
                }

                // The length of custom TLV's tag is 2 bits.
                // 自定义的TLV里，Tag固定为两位
                tagLength = 2;
            }

            int tagStart = index + tagLength;
            if (dataLength <= tagStart) {
                return;
            }

            // data[index] data[index+1]...data[tagStart-1] make up the tag.
            // 前面tagLength位表示Tag，即data[index] data[index+1]...data[tagStart-1]为Tag
            int valueLength = 0;
            int units = 0;
            if ((data[tagStart] & 0x80) != 0x80) {
                // data[tagStart] means the length of the value.
                // 此时data[tagStart]的值即表示value的长度
                valueLength = data[tagStart];
            } else {
                // data[tagStart+1] data[tagStart+2]...data[tagStart+units] is the length of the value.
                // 此时data[tagStart]仅为指示作用，data[tagStart+1], data[tagStart+2]...data[tagStart+units] 这么多位的值为value的长度
                units = data[tagStart] & 0x7F;

                if (dataLength <= tagStart + units) {
                    return;
                }

                for (int i = 1; i <= units; i++) {
                    // 例子：假如units为2，则data[tagStart+1]和data[tagStart+2]组成的值为value的长度，那么计算如下：
                    // data[tagStart+1]乘以2的(8 * (units-1))次方，然后加上data[tagStart+2]，
                    // 即data[tagStart+1] << (8 * (units-1))+data[tagStart+2]
                    valueLength = (data[tagStart + i] << (8 * (units - i))) + valueLength;
                }
            }

            TLVDataObject tv = new TLVDataObject();

            byte[] tags = new byte[tagLength];
            System.arraycopy(data, index, tags, 0, tagLength);
            tv.setTag(tags);

            if (dataLength < tagStart + units + 1 + valueLength) {
                return;
            }

            if (valueLength != 0) {
                // Get the value.
                // 本次value的值
                byte[] values = new byte[valueLength];
                // Copy from data[tagLength + 1 + units] for valueLength bits.
                // 从data的tagLength + 1 + units位开始复制，复制valueLen位数据到values数组里，values数组放置的起始位置为0
                System.arraycopy(data, index + tagLength + 1 + units, values, 0, valueLength);
                tv.setValue(values);
            } else {
                tv.setValue(new byte[0]);
            }

            tlvDataObjects.add(tv);

            index = index + tagLength + 1 + units + valueLength;
        }
    }

    public static ArrayList<TLVDataObject> unpackTLVs(final DataModel.DataType dataType, byte[] tlvs) {
        if (tlvs == null || tlvs.length == 0) {
            return null;
        }
        ArrayList<TLVDataObject> tlvOjbList = new ArrayList<TLVDataObject>();
        int offset = 0;
        while (offset < tlvs.length) {
            byte tag[] = null;
            if (DataModel.DataType.TRANSACTION_DATA == dataType) {
                tag = resolveEmvTag(tlvs, offset);
            } else if (DataModel.DataType.CONFIGURATION_DATA == dataType) {
                tag = resolveCustomTag(tlvs, offset);
            }
            if (tag != null && tag.length > 0) {
                int tlvLen = tag.length;
                byte[] valueLen = resolveValueLen(tlvs, offset + tlvLen);
                if (valueLen != null && valueLen.length > 0) {
                    tlvLen += valueLen.length;
                    int valuenLen = ConvertIntLengthFromByte(valueLen);
                    if (valuenLen >= 0 && (offset + tlvLen + valuenLen) <= tlvs.length) {
                        byte[] value = new byte[valuenLen];
                        System.arraycopy(tlvs, offset + tlvLen, value, 0, valuenLen);

                        tlvLen += valuenLen;

                        offset += tlvLen;

                        TLVDataObject tlvObj = new TLVDataObject(tag, value);
                        tlvOjbList.add(tlvObj);
                    } else {
                        break;
                    }
                } else {
                    TLVDataObject tlvObj = new TLVDataObject(tag, new byte[0]);
                    tlvOjbList.add(tlvObj);
                    break;
                }
            } else {
                break;
            }
        }

        return tlvOjbList;

    }

    private static int ConvertIntLengthFromByte(byte[] length) {
        int value = 0;

        if (length.length == 1) {
            value = length[0] & 0xff;
        } else {
            int lengthSubSequentByte = length[0] & 0x7F;
            if (lengthSubSequentByte > (Integer.SIZE / 8)) {
                return -1;
            }

            for (int count = 0, i = lengthSubSequentByte; i > 0; count++, i--) {
                if (count == 0) {
                    value += length[i] & 0xff;
                } else {
                    value += length[i] << (8 * count);
                }
            }
        }

        return value;
    }

    private static byte[] resolveCustomTag(byte[] paramIn, int offsetIn) {
        if (paramIn == null
                || paramIn.length == 0
                || paramIn.length <= offsetIn
                || (offsetIn + getCustomTagLength() > paramIn.length)) {
            return new byte[0];
        }
        byte[] tag = new byte[getCustomTagLength()];
        System.arraycopy(paramIn, offsetIn, tag, 0, getCustomTagLength());
        Log.d("custom TAG: ", Convert.bcdToStr(tag));
        return tag;
    }

    private static int getCustomTagLength() {
        return 2;
    }


    private static byte[] resolveEmvTag(byte[] paramIn, int offsetIn) {
        if (paramIn == null || paramIn.length == 0 || paramIn.length <= offsetIn) {
            return new byte[0];
        }
        int tagLen = 0;
        if (0x1F == (paramIn[offsetIn] & 0x1F)) {
            if (offsetIn + 1 > paramIn.length) {
                return new byte[0];
            }

            if (0x80 == (paramIn[offsetIn + 1] & 0x80)) {
                tagLen = 3;
            } else {
                tagLen = 2;
            }
        } else {
            tagLen = 1;
        }

        byte[] tag = new byte[tagLen];
        System.arraycopy(paramIn, offsetIn, tag, 0, tagLen);
        Log.d("TAG Length: ", "" + tagLen);
        Log.d("emv TAG: ", "" + Convert.bcdToStr(tag));
        return tag;
    }


    private static byte[] resolveValueLen(byte[] paramIn, int offsetIn) {
        Log.w("TAG", "resolveValueLen: paramIn.length=" + paramIn.length + ",offsetIn:" + offsetIn);
        if (paramIn == null || paramIn.length == 0 || paramIn.length <= offsetIn) {
            return new byte[0];
        }
        byte[] length;
        if (0x80 == (paramIn[offsetIn] & 0x80)) {
            int lengthSubSequentByte = paramIn[offsetIn] & 0x7F;
            if (lengthSubSequentByte > Integer.SIZE / 8) {
                Log.e("TAG", "Resolve error, length overflow " + (int) paramIn[offsetIn]);
                return new byte[0];
            }
            if (offsetIn + lengthSubSequentByte >= paramIn.length) {
                Log.e("TAG", "Resolve error, length invalid");
                return new byte[0];
            }

            length = new byte[lengthSubSequentByte + 1];
            System.arraycopy(paramIn, offsetIn, length, 0, lengthSubSequentByte + 1);
        } else {
            length = new byte[1];
            length[0] = paramIn[offsetIn];
        }

        Log.d("Length: ", Convert.bcdToStr(length));
        return length;
    }


    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }


    public static byte[] splicingData(byte mode,String Pan){
        byte[] dataOut = null;
        if (!Pan.isEmpty()){
            byte[] bytes = Pan.getBytes();
            dataOut = new byte[bytes.length+2];
            dataOut[0]= mode;
            /**
             * The card number will be passed in the datain
             * When using convertpinblock to convert aestpk, a Pan(byte)+1 byte array needs to be passed in. Here, we use pantoken to supplement it
             **/
            System.arraycopy(Pan.getBytes(),0,dataOut,1,Pan.getBytes().length);

            return dataOut;
        }
        return dataOut;
    }


    /**
     * Param:
     *          * @pantoken : cardNo or pantoken
     *          * @pinBlock : PinBlock
     *          * @dataOut : dataOut to use convertPinBlock
     * **/
    public static int getPinFeild(String pantoken , byte[] pinblock , byte[] dataOut){
        int ret = 0;
        int PANLength;
        byte[] PANStr = new byte[20];
        byte[] pinfield = new byte[16];

        PANLength = pantoken.length();
        if (PANLength > 19) return -1;

        //PANField
        Arrays.fill(PANStr, (byte)'0');
        if (PANLength < 12) {
            PANStr[0] = (byte)'0';
            /*If the PAN is less than 12 digits, the digits are right justified and padded to the left with zeros,*/
            System.arraycopy(pantoken.getBytes(), 0, PANStr, 1 + 12 - PANLength, PANLength);
        } else {
            PANStr[0] = (byte)('0' + PANLength - 12);
            System.arraycopy(pantoken.getBytes(), 0, PANStr, 1, PANLength);
        }
        Arrays.fill(pinfield, (byte)0);

        //The purpose of this code is to convert the input ASCII code into hexadecimal code and save it in the output array.
        twobcd2Onebcd(PANStr,pinfield);

        System.arraycopy(pinblock,0,dataOut,0,pinblock.length);
        System.arraycopy(pinfield,0,dataOut,16,pinfield.length);
        return ret;
    }


    public static void twobcd2Onebcd(byte[] in,byte[] out) {
        byte hexbyte1,hexbyte2;

        for (int i = 0; i < in.length; i+=2) {
            if (in[i] > '9')
                hexbyte1 = (byte) ((byte) (Character.toUpperCase(in[i]) - ('A' - 0x0A)) & 0x0f);
            else
                hexbyte1 = (byte) (in[i] & 0x0f);

            if (in[i + 1] > '9')
                hexbyte2 = (byte) ((byte) (Character.toUpperCase(in[i + 1]) - ('A' - 0x0A)) & 0x0f);
            else
                hexbyte2 = (byte) (in[i + 1] & 0x0f);

            out[i / 2] = (byte) ((hexbyte1 << 4) + hexbyte2);
        }
    }
//    /**
//     * Convert hex string to byte[]
//     * @param hexString the hex string
//     * @return byte[]
//     */
//    public static byte[] hexStringToBytes(String hexString) {
//        if (hexString == null || hexString.equals("")) {
//            return null;
//        }
//        hexString = hexString.toUpperCase();
//        int length = hexString.length() / 2;
//        char[] hexChars = hexString.toCharArray();
//        byte[] d = new byte[length];
//        for (int i = 0; i < length; i++) {
//            int pos = i * 2;
//            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
//        }
//        return d;
//    }
//
//
//    /**
//     * Convert char to byte
//     * @param c char
//     * @return byte
//     */
//    private static byte charToByte(char c) {
//        return (byte) "0123456789ABCDEF".indexOf(c);
//    }

}
