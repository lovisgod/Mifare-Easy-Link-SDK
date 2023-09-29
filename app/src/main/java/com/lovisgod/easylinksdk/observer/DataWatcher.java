package com.lovisgod.easylinksdk.observer;

import com.paxsz.easylink.device.DeviceInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhanzc on 2017/7/18.
 * 观察者类
 */
public class DataWatcher {

    private static DataWatcher dataWatcher;

    private DataWatcher(){

    }
    public static DataWatcher getInstance() {
        if (dataWatcher == null) {
            initWatcher();
        }

        return dataWatcher;
    }

    private static synchronized void initWatcher() {
        if (dataWatcher == null) {
            dataWatcher = new DataWatcher();
        }
    }

    // 一般来说，这应该是一个集合，但我这里只监听可能发生内存泄露的Activity，所以只用了一个observable
    private List<IObservable> observable = new ArrayList<IObservable>();

    /**
     * 添加被观察者
     * @param observable
     */
    public void register(IObservable observable) {
        this.observable.add(observable);
    }

    /**
     * 解除监听
     */
    public void unregister(IObservable ob) {
        this.observable.remove(ob);
    }

    /**
     * 通知被观察者更新数据
     */
    public void notifyObservable(DeviceInfo paramDeviceInfo) {
        if (this.observable != null) {
            for (IObservable observable : this.observable) {
                observable.update(paramDeviceInfo);
            }

        }
    }

    /**
     * 通知被观察者更新数据完成
     */
    public void notifyFinish() {
        if (this.observable != null) {
            for (IObservable observable : this.observable) {
                observable.onSearchFinish();

            }

        }
    }
}