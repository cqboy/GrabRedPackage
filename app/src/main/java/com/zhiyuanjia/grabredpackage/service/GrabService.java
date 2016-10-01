package com.zhiyuanjia.grabredpackage.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.Toast;

import com.zhiyuanjia.grabredpackage.BuildConfig;
import com.zhiyuanjia.grabredpackage.constent.Config;
import com.zhiyuanjia.grabredpackage.entity.Accessbility;
import com.zhiyuanjia.grabredpackage.entity.IStatusBarNotification;
import com.zhiyuanjia.grabredpackage.entity.WeiChatAccessbility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * @Description：TODO(- 类描述：抢红包监听操作类 -)
 * @author：wsx
 * @email：heikepianzi@qq.com
 * @date 2016/9/28
 */

public class GrabService extends AccessibilityService {

    private static final String TAG = "GrabService";
    private static final Class[] ACCESSBILITY_JOBS = {
            WeiChatAccessbility.class,
    };
    private static GrabService service;
    private List<Accessbility> mAccessbilitys;
    private HashMap<String, Accessbility> mPkgAccessbilityMap;

    @Override
    public void onCreate() {
        super.onCreate();

        mAccessbilitys = new ArrayList<>();
        mPkgAccessbilityMap = new HashMap<>();
        //初始化辅助插件工具
        for (Class clazz : ACCESSBILITY_JOBS) {
            try {
                Object object = clazz.newInstance();
                if (object instanceof Accessbility) {
                    Accessbility job = (Accessbility) object;
                    job.onCreatEntity(this);
                    mAccessbilitys.add(job);
                    mPkgAccessbilityMap.put(job.getTargetPackageName(), job);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 断开辅助服务
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        if (mPkgAccessbilityMap != null) {
            mPkgAccessbilityMap.clear();
        }
        if (mAccessbilitys != null && !mAccessbilitys.isEmpty()) {
            for (Accessbility job : mAccessbilitys) {
                job.onStopEntity();
            }
            mAccessbilitys.clear();
        }
        service = null;
        mAccessbilitys = null;
        mPkgAccessbilityMap = null;
        //发送广播，已经断开辅助服务
        Intent intent = new Intent(Config.ACTION_QIANGHONGBAO_SERVICE_DISCONNECT);
        sendBroadcast(intent);
    }

    /**
     * 连接服务后,一般是在授权成功后会接收到
     */
    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        service = this;
        //发送广播，已经连接上了
        Intent intent = new Intent(Config.ACTION_QIANGHONGBAO_SERVICE_CONNECT);
        sendBroadcast(intent);
        Toast.makeText(this, "已连接抢红包服务", Toast.LENGTH_SHORT).show();
    }

    /**
     * 接收事件,如触发了通知栏变化、界面变化等
     *
     * @param event
     */
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "辅助服务反馈事件--->" + event);
        }
        String pkn = String.valueOf(event.getPackageName());
        if (mAccessbilitys != null && !mAccessbilitys.isEmpty()) {
            if (!getConfig().isAgreement()) {
                return;
            }
            for (Accessbility job : mAccessbilitys) {
                if (pkn.equals(job.getTargetPackageName()) && job.isEnable()) {
                    job.onReceiveEntity(event);
                }
            }
        }
    }

    /**
     * 接收按键事件
     *
     * @param event
     * @return
     */
    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        Log.d(TAG, "按键事件onKeyEvent: event=" + event);
        return super.onKeyEvent(event);
    }

    /**
     * 服务中断，如授权关闭或者将服务杀死
     */
    @Override
    public void onInterrupt() {
        Log.d(TAG, "onInterrupt: 服务中断，如授权关闭或者将服务杀死");
        Toast.makeText(this, "中断抢红包服务", Toast.LENGTH_SHORT).show();
    }

    /**
     * @return 获取设置
     */
    public Config getConfig() {
        return Config.getConfig(this);
    }

    /**
     * 判断当前服务是否正在运行
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static boolean isRunning() {
        if (service == null) {
            return false;
        }
        AccessibilityManager accessibilityManager = (AccessibilityManager) service.getSystemService(Context.ACCESSIBILITY_SERVICE);
        AccessibilityServiceInfo info = service.getServiceInfo();
        if (info == null) {
            return false;
        }
        List<AccessibilityServiceInfo> list = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC);
        Iterator<AccessibilityServiceInfo> iterator = list.iterator();

        boolean isConnect = false;
        while (iterator.hasNext()) {
            AccessibilityServiceInfo i = iterator.next();
            if (i.getId().equals(info.getId())) {
                isConnect = true;
                break;
            }
        }
        if (!isConnect) {
            return false;
        }
        return true;
    }

    /**
     * 快速读取通知栏服务是否启动
     */
    public static boolean isNotificationServiceRunning() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            return false;
        }
        //部份手机没有NotificationService服务
        try {
            return GrabNotificationService.isRunning();
        } catch (Throwable t) {
        }
        return false;
    }

    /**
     * 接收通知栏事件
     */
    public static void handeNotificationPosted(IStatusBarNotification notificationService) {
        if (notificationService == null) {
            return;
        }
        if (service == null || service.mPkgAccessbilityMap == null) {
            return;
        }
        String pack = notificationService.getPackageName();
        Accessbility job = service.mPkgAccessbilityMap.get(pack);
        if (job == null) {
            return;
        }
        job.onNotificationPosted(notificationService);
    }
}
