package com.zhiyuanjia.grabredpackage.entity;

import android.content.Context;

import com.zhiyuanjia.grabredpackage.constent.Config;
import com.zhiyuanjia.grabredpackage.service.GrabService;

/**
 * @Description：TODO(- 类描述： -)
 * @author：wsx
 * @email：heikepianzi@qq.com
 * @date 2016/9/30
 */

public abstract class BaseAccessbility implements Accessbility {

    private GrabService service;

    @Override
    public void onCreatEntity(GrabService service) {
        this.service = service;
    }

    /**
     * @return
     */
    public Context getContext() {
        return service.getApplicationContext();
    }

    /**
     * @return 获取软件设置
     */
    public Config getConfig() {
        return service.getConfig();
    }

    /**
     * @return
     */
    public GrabService getService() {
        return service;
    }
}
