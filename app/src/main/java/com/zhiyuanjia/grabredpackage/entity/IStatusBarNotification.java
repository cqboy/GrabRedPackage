package com.zhiyuanjia.grabredpackage.entity;

import android.app.Notification;

/**
 * @Description：TODO(- 类描述： -)
 * @author：wsx
 * @email：heikepianzi@qq.com
 * @date 2016/9/28
 */
public interface IStatusBarNotification {

    String getPackageName();
    Notification getNotification();
}
