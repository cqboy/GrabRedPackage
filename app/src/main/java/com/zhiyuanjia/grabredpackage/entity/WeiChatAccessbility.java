package com.zhiyuanjia.grabredpackage.entity;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.zhiyuanjia.grabredpackage.BuildConfig;
import com.zhiyuanjia.grabredpackage.base.BaseApplication;
import com.zhiyuanjia.grabredpackage.constent.Config;
import com.zhiyuanjia.grabredpackage.constent.PreferenceKey;
import com.zhiyuanjia.grabredpackage.service.GrabService;
import com.zhiyuanjia.grabredpackage.util.AccessibilityHelper;
import com.zhiyuanjia.grabredpackage.util.NotifyHelper;
import com.zhiyuanjia.grabredpackage.util.PreferenceUtils;

import java.util.List;

/**
 * @Description：TODO(- 类描述： -)
 * @author：wsx
 * @email：heikepianzi@qq.com
 * @date 2016/9/28
 */

public class WeiChatAccessbility extends BaseAccessbility {

    private String TAG = "WeiChatAccessbility";

    // 微信的包名
    public static final String WECHAT_PACKAGENAME = "com.tencent.mm";
    // 红包消息的关键字
    private static final String HONGBAO_TEXT_KEY = "[微信红包]";
    private static final String BUTTON_CLASS_NAME = "android.widget.Button";

    public static List<String> chatList;

    /**
     * 不能再使用文字匹配的最小版本号
     */
    private static final int USE_ID_MIN_VERSION = 700;// 6.3.8 对应code为680,6.3.9对应code为700
    private static final int WINDOW_NONE = 0;
    private static final int WINDOW_LUCKYMONEY_RECEIVEUI = 1;
    private static final int WINDOW_LUCKYMONEY_DETAIL = 2;
    private static final int WINDOW_LAUNCHER = 3;
    private static final int WINDOW_OTHER = -1;

    private int mCurrentWindow = WINDOW_NONE;

    private boolean isReceivingHongbao;
    private PackageInfo mWechatPackageInfo = null;
    private Handler mHandler = null;

    private boolean isGoHome = false;

    /**
     * -- 对象初始化
     *
     * @param service
     */
    @Override
    public void onCreatEntity(GrabService service) {
        super.onCreatEntity(service);
        chatList = PreferenceUtils.getArray(getContext(), PreferenceKey.WHITE_LIST, String.class);
        updatePackageInfo();
        // 添加应用安装、卸载、替换广播监听器
        IntentFilter filter = new IntentFilter();
        filter.addDataScheme("package");
        filter.addAction("android.intent.action.PACKAGE_ADDED");
        filter.addAction("android.intent.action.PACKAGE_REPLACED");
        filter.addAction("android.intent.action.PACKAGE_REMOVED");
        getContext().registerReceiver(broadcastReceiver, filter);
    }

    @Override
    public void onStopEntity() {

        // 释放广播监听
        try {
            getContext().unregisterReceiver(broadcastReceiver);
        } catch (Exception e) {
        }
    }

    // 处理辅助服务AccessibilityEvent的反馈信息
    @Override
    public void onReceiveEntity(AccessibilityEvent event) {
        Log.d(TAG, "onReceiveEntity: event=" + event);
        final int eventType = event.getEventType();
        List<CharSequence> texts = event.getText();  // 获取通知消息
        if (!texts.isEmpty()) {
            String text = String.valueOf(texts.get(0));
            for (String name : chatList) {
                if (text.contains(name) && text.contains(HONGBAO_TEXT_KEY)) {
                    return;
                }
            }
        }
        isGoHome = true;
        if (eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED)

        {  // 通知栏状态变化事件
            Parcelable data = event.getParcelableData();
            if (data == null || !(data instanceof Notification)) {
                return;
            }
            if (GrabService.isNotificationServiceRunning() && getConfig().isEnableNotificationService()) { // 开启快速模式，不处理
                return;
            }
            if (!texts.isEmpty()) {
                String text = String.valueOf(texts.get(0));
                notificationEvent(text, (Notification) data);
            }
        } else if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED)

        {  // 监听是否进入微信红包消息界面
            openHongBao(event);
        } else if (eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED)

        {
            if (mCurrentWindow != WINDOW_LAUNCHER) { //不在聊天界面或聊天列表，不处理
                return;
            }
            if (isReceivingHongbao) {
                handleChatListHongBao();
            }
        }

    }

    /**
     * 通知栏监听回调
     *
     * @param notification
     */
    @Override
    public void onNotificationPosted(IStatusBarNotification notification) {
        // 处理通知栏消息
        Notification nf = notification.getNotification();
        String text = String.valueOf(notification.getNotification().tickerText);
        notificationEvent(text, nf);
    }


    /******************************************
     * ------
     ****************************************/

    /**
     * @return 初始化uihander对象
     */
    private Handler getHandler() {
        if (mHandler == null) {
            mHandler = new Handler(Looper.getMainLooper());
        }
        return mHandler;
    }

    // 更新微信包信息
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updatePackageInfo();
        }
    };

    /**
     * 更新微信包信息
     */
    private void updatePackageInfo() {
        try {
            mWechatPackageInfo = getContext().getPackageManager().getPackageInfo(WECHAT_PACKAGENAME, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理消息通知栏事件
     *
     * @param ticker
     * @param notification
     */
    private void notificationEvent(String ticker, Notification notification) {
        String text = ticker;
        int index = text.indexOf(":");
        if (index != -1) {
            text = text.substring(index + 1);
        }
        text = text.trim();
        if (text.contains(HONGBAO_TEXT_KEY)) { // 判断是否是红包消息
            // 将微信的通知栏消息打开
            isReceivingHongbao = true;
            PendingIntent pendingIntent = notification.contentIntent;
            boolean lock = NotifyHelper.isLockScreen(getContext());
            if (!lock) {
                NotifyHelper.send(pendingIntent);
            } else {
                NotifyHelper.showNotify(getContext(), String.valueOf(notification.tickerText), pendingIntent);
            }
            if (lock || getConfig().getWechatMode() != Config.WX_MODE_0) {
                NotifyHelper.playEffect(getContext(), getConfig());
            }
        }
    }

    /**
     * @return 是否开启微信红包开关
     */
    public boolean isEnable() {
        return getConfig().isEnableWechat();
    }

    /**
     * @return 返回微信包名
     */
    public String getTargetPackageName() {
        return WECHAT_PACKAGENAME;
    }

    /**
     * 获取微信的版本
     */
    private int getWechatVersion() {
        if (mWechatPackageInfo == null) {
            return 0;
        }
        return mWechatPackageInfo.versionCode;
    }

    /**
     * 抢红包操作
     *
     * @param event
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void openHongBao(AccessibilityEvent event) {
        // 微信当前页面类分析
        if ("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI".equals(event.getClassName())) {  // 抢红包页
            mCurrentWindow = WINDOW_LUCKYMONEY_RECEIVEUI;
            // 如果点中了红包，下一步就是去拆红包
            handleLuckyMoneyReceive();
        } else if ("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI".equals(event.getClassName())) {  // 红包详情页
            mCurrentWindow = WINDOW_LUCKYMONEY_DETAIL;
            //拆完红包后看详细的纪录界面
            if (getConfig().getWechatAfterGetHongBaoEvent() == Config.WX_AFTER_GET_GOHOME && isGoHome) { //返回主界面，以便收到下一次的红包通知
                isGoHome = false;
                AccessibilityHelper.performHome(getService());
            }
        } else if ("com.tencent.mm.ui.LauncherUI".equals(event.getClassName())) {  // 聊天列表页
            mCurrentWindow = WINDOW_LAUNCHER;
            // 在聊天界面,去点中红包
            handleChatListHongBao();
        } else {
            mCurrentWindow = WINDOW_OTHER;
        }
    }

    /**
     * 点击聊天里的红包后，显示的界面
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void handleLuckyMoneyReceive() {
        AccessibilityNodeInfo nodeInfo = getService().getRootInActiveWindow();
        if (nodeInfo == null) {
            Log.w(TAG, "rootWindow为空");
            return;
        }

        AccessibilityNodeInfo targetNode = null;

        int event = getConfig().getWechatAfterOpenHongBaoEvent();
        int wechatVersion = getWechatVersion();
        if (event == Config.WX_AFTER_OPEN_HONGBAO) { //拆红包
            if (wechatVersion < USE_ID_MIN_VERSION) {
                targetNode = AccessibilityHelper.findNodeInfosByText(nodeInfo, "拆红包");
            } else {
                String buttonId = "com.tencent.mm:id/b43";

                if (wechatVersion == 700) {
                    buttonId = "com.tencent.mm:id/b2c";
                }

                targetNode = AccessibilityHelper.findNodeInfosById(nodeInfo, buttonId);

                if (targetNode == null) {
                    //分别对应固定金额的红包 拼手气红包
                    AccessibilityNodeInfo textNode = AccessibilityHelper.findNodeInfosByTexts(nodeInfo, "发了一个红包", "给你发了一个红包", "发了一个红包，金额随机");

                    if (textNode != null) {
                        for (int i = 0; i < textNode.getChildCount(); i++) {
                            AccessibilityNodeInfo node = textNode.getChild(i);
                            if (BUTTON_CLASS_NAME.equals(node.getClassName())) {
                                targetNode = node;
                                break;
                            }
                        }
                    }
                }

                if (targetNode == null) { //通过组件查找
                    targetNode = AccessibilityHelper.findNodeInfosByClassName(nodeInfo, BUTTON_CLASS_NAME);
                }
            }
        } else if (event == Config.WX_AFTER_OPEN_SEE) { //看一看
            if (getWechatVersion() < USE_ID_MIN_VERSION) { //低版本才有 看大家手气的功能
                targetNode = AccessibilityHelper.findNodeInfosByText(nodeInfo, "看看大家的手气");
            }
        } else if (event == Config.WX_AFTER_OPEN_NONE) {
            return;
        }

        if (targetNode != null) {
            final AccessibilityNodeInfo n = targetNode;
            long sDelayTime = getConfig().getWechatOpenDelayTime();
            if (sDelayTime != 0) {
                getHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        AccessibilityHelper.performClick(n);
                    }
                }, sDelayTime);
            } else {
                AccessibilityHelper.performClick(n);
            }
            if (event == Config.WX_AFTER_OPEN_HONGBAO) {
                BaseApplication.eventStatistics(getContext(), "open_hongbao");
            } else {
                BaseApplication.eventStatistics(getContext(), "open_see");
            }
        }
    }

    /**
     * 收到聊天里的红包
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void handleChatListHongBao() {
        int mode = getConfig().getWechatMode();
        if (mode == Config.WX_MODE_3) { //只通知模式
            return;
        }

        AccessibilityNodeInfo nodeInfo = getService().getRootInActiveWindow();
        if (nodeInfo == null) {
            Log.w(TAG, "rootWindow为空");
            return;
        }

        if (mode != Config.WX_MODE_0) {
            boolean isMember = isMemberChatUi(nodeInfo);
            if (mode == Config.WX_MODE_1 && isMember) {//过滤群聊
                return;
            } else if (mode == Config.WX_MODE_2 && !isMember) { //过滤单聊
                return;
            }
        }

        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText("领取红包");
        Log.d(TAG, "handleChatListHongBao: list=" + list);
        if (list != null && list.isEmpty()) {  // 聊天列表界面处理
            // 从消息列表查找红包
            AccessibilityNodeInfo node = AccessibilityHelper.findNodeInfosByText(nodeInfo, "[微信红包]");
            if (node != null) {
                if (BuildConfig.DEBUG) {
                    Log.i(TAG, "-->微信红包:" + node);
                }
                isReceivingHongbao = true;
                AccessibilityHelper.performClick(nodeInfo);
            }
        } else if (list != null) {  // 群聊界面处理
            if (isReceivingHongbao) {
                //最新的红包领起
                AccessibilityNodeInfo node = list.get(list.size() - 1);
                AccessibilityHelper.performClick(node);
                isReceivingHongbao = false;
            }
        }
    }

    /**
     * 是否为群聊天
     */
    private boolean isMemberChatUi(AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo == null) {
            return false;
        }
        String id = "com.tencent.mm:id/ces";
        int wv = getWechatVersion();
        if (wv <= 680) {
            id = "com.tencent.mm:id/ew";
        } else if (wv <= 700) {
            id = "com.tencent.mm:id/cbo";
        }
        String title = null;
        AccessibilityNodeInfo target = AccessibilityHelper.findNodeInfosById(nodeInfo, id);
        if (target != null) {
            title = String.valueOf(target.getText());
        }
        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText("返回");

        if (list != null && !list.isEmpty()) {
            AccessibilityNodeInfo parent = null;
            for (AccessibilityNodeInfo node : list) {
                if (!"android.widget.ImageView".equals(node.getClassName())) {
                    continue;
                }
                String desc = String.valueOf(node.getContentDescription());
                if (!"返回".equals(desc)) {
                    continue;
                }
                parent = node.getParent();
                break;
            }
            if (parent != null) {
                parent = parent.getParent();
            }
            if (parent != null) {
                if (parent.getChildCount() >= 2) {
                    AccessibilityNodeInfo node = parent.getChild(1);
                    if ("android.widget.TextView".equals(node.getClassName())) {
                        title = String.valueOf(node.getText());
                    }
                }
            }
        }

        if (title != null && title.endsWith(")")) {
            return true;
        }
        return false;
    }
}
