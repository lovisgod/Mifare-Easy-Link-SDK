/*
 * ===========================================================================================
 * = COPYRIGHT
 *          PAX Computer Technology(Shenzhen) CO., LTD PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or nondisclosure
 *   agreement with PAX Computer Technology(Shenzhen) CO., LTD and may not be copied or
 *   disclosed except in accordance with the terms in that agreement.
 *     Copyright (C) 2020-? PAX Computer Technology(Shenzhen) CO., LTD All rights reserved.
 * Description: // Detail description about the function of this module,
 *             // interfaces with the other modules, and dependencies.
 * Revision History:
 * Date	                 Author	                Action
 * 2020/9/3  	         Qinny Zhou           	    Create
 * ===========================================================================================
 */
package com.lovisgod.easylinksdk.comm;

import com.pax.gl.commhelper.IComm;
import com.pax.gl.commhelper.exception.CommException;

public class UartComm implements IComm {

    private com.pax.dal.IComm mIComm;

    public UartComm(com.pax.dal.IComm neptuneComm) {
        mIComm = neptuneComm;
    }

    @Override
    public int getConnectTimeout() {
        return mIComm == null ? 0 : mIComm.getConnectTimeout();
    }

    @Override
    public void setConnectTimeout(int i) {
        if (mIComm != null) {
            mIComm.setConnectTimeout(i);
        }
    }

    @Override
    public int getSendTimeout() {
        return mIComm == null ? 0 : mIComm.getSendTimeout();
    }

    @Override
    public void setSendTimeout(int i) {
        if (mIComm != null) {
            mIComm.setSendTimeout(i);
        }
    }

    @Override
    public int getRecvTimeout() {
        return mIComm == null ? 0 : mIComm.getRecvTimeout();
    }

    @Override
    public void setRecvTimeout(int i) {
        if (mIComm != null) {
            mIComm.setRecvTimeout(i);
        }
    }

    @Override
    public void connect() throws CommException {
        if (mIComm != null) {
            try {
                mIComm.connect();
            } catch (com.pax.dal.exceptions.CommException e) {
                throw new CommException(e.getErrCode(), e.fillInStackTrace());
            }
        }
    }

    @Override
    public EConnectStatus getConnectStatus() {
        if (mIComm != null) {
            if (com.pax.dal.IComm.EConnectStatus.CONNECTED == mIComm.getConnectStatus()) {
                return EConnectStatus.CONNECTED;
            } else if (com.pax.dal.IComm.EConnectStatus.DISCONNECTED == mIComm.getConnectStatus()) {
                return EConnectStatus.DISCONNECTED;
            } else if (com.pax.dal.IComm.EConnectStatus.CONNECTING == mIComm.getConnectStatus()) {
                return EConnectStatus.CONNECTING;
            }
        }
        return EConnectStatus.DISCONNECTED;
    }

    @Override
    public void disconnect() throws CommException {
        if (mIComm != null) {
            try {
                mIComm.disconnect();
            } catch (com.pax.dal.exceptions.CommException e) {
                throw new CommException(e.getErrCode(), e.fillInStackTrace());
            }
        }
    }

    @Override
    public void send(byte[] bytes) throws CommException {
        if (mIComm != null) {
            try {
                mIComm.send(bytes);
            } catch (com.pax.dal.exceptions.CommException e) {
                throw new CommException(e.getErrCode(), e.fillInStackTrace());
            }
        }
    }

    @Override
    public byte[] recv(int i) throws CommException {
        if (mIComm != null) {
            try {
                return mIComm.recv(i);
            } catch (com.pax.dal.exceptions.CommException e) {
                throw new CommException(e.getErrCode(), e.fillInStackTrace());
            }
        }
        return new byte[0];
    }

    @Override
    public byte[] recvNonBlocking() throws CommException {
        if (mIComm != null) {
            try {
                return mIComm.recvNonBlocking();
            } catch (com.pax.dal.exceptions.CommException e) {
                throw new CommException(e.getErrCode(), e.fillInStackTrace());
            }
        }
        return new byte[0];
    }

    @Override
    public void reset() {
        if (mIComm != null) {
            mIComm.reset();
        }
    }

    @Override
    public void cancelRecv() {
        if (mIComm != null) {
            mIComm.cancelRecv();
        }
    }
}
