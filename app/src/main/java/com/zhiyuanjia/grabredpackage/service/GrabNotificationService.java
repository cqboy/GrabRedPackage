package com.zhiyuanjia.grabredpackage.service;

import android.annotation.TargetApi;
import android.app.Notification;
import android.content.Intent;
import android.os.Build;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.zhiyuanjia.grabredpackage.BuildConfig;
import com.zhiyuanjia.grabredpackage.constent.Config;
import com.zhiyuanjia.grabredpackage.entity.IStatusBarNotification;

/**
 * @Description：TODO(- 类描述：通知栏服务监听类 -)
 * @author：wsx
 * @email：heikepianzi@qq.com
 * @date 2016/9/28
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class GrabNotificationService extends NotificationListenerService {

    private static final String TAG = "QHBNotificationService";

    private static GrabNotificationService service;

    @Override
    public void onCreate() {
        super.onCreate();
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            onListenerConnected();
        }
    }

    private Config getConfig() {
        return Config.getConfig(this);
    }

    @Override
    public void onNotificationPosted(final StatusBarNotification sbn) {
        if(BuildConfig.DEBUG) {
            Log.i(TAG, "onNotificationRemoved");
        }
        if(!getConfig().isAgreement()) {
            return;
        }
        if(!getConfig().isEnableNotificationService()) {
            return;
        }
        GrabService.handeNotificationPosted(new IStatusBarNotification() {
            @Override
            public String getPackageName() {
                return sbn.getPackageName();
            }

            @Override
            public Notification getNotification() {
                return sbn.getNotification();
            }
        });
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            super.onNotificationRemoved(sbn);
        }
        if(BuildConfig.DEBUG) {
            Log.i(TAG, "onNotificationRemoved");
        }
    }

    @Override
    public void onListenerConnected() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            super.onListenerConnected();
        }

        Log.i(TAG, "onListenerConnected");
        service = this;
        //发送广播，已经连接上了
        Intent intent = new Intent(Config.ACTION_NOTIFY_LISTENER_SERVICE_CONNECT);
        sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
        service = null;
        //发送广播，已经连接上了
        Intent intent = new Intent(Config.ACTION_NOTIFY_LISTENER_SERVICE_DISCONNECT);
        sendBroadcast(intent);
    }

    /** 是否启动通知栏监听*/
    public static boolean isRunning() {
        if(service == null) {
            return false;
        }
        return true;
    }
}
