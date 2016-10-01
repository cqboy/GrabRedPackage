package com.zhiyuanjia.grabredpackage.entity;

import android.view.accessibility.AccessibilityEvent;

import com.zhiyuanjia.grabredpackage.service.GrabService;

/**
 * @Description：TODO(- 类描述： -)
 * @author：wsx
 * @email：heikepianzi@qq.com
 * @date 2016/9/28
 */

public interface Accessbility {

    /**
     * @return 包名
     */
    String getTargetPackageName();

    /**
     * @return 是否开启该应用的抢红包功能
     */
    boolean isEnable();

    /**
     * --------------------
     **/
    void onCreatEntity(GrabService service);

    void onReceiveEntity(AccessibilityEvent event);

    void onStopEntity();

    void onNotificationPosted(IStatusBarNotification notification);
}
