package com.zhiyuanjia.grabredpackage.ui;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.zhiyuanjia.grabredpackage.constent.Config;

/**
 * @Description：TODO(- 类描述： -)
 * @author：wsx
 * @email：heikepianzi@qq.com
 * @date 2016/9/28
 */
public class BaseSettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(Config.PREFERENCE_NAME);
    }
}