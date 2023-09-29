package com.lovisgod.easylinksdk.observer;

import com.paxsz.easylink.device.DeviceInfo;

/**
 * Created by zhanzc on 2017/7/18.
 * 被观察者
 */
public interface IObservable {
    void update(DeviceInfo paramDeviceInfo);

    void onSearchFinish();
}