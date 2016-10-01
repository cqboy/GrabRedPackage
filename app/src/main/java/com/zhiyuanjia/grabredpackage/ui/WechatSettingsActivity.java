package com.zhiyuanjia.grabredpackage.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;

import com.zhiyuanjia.grabredpackage.R;
import com.zhiyuanjia.grabredpackage.base.BaseApplication;
import com.zhiyuanjia.grabredpackage.base.BaseSettingsActivity;
import com.zhiyuanjia.grabredpackage.constent.Config;

/**
 * @Description：TODO(- 类描述： -)
 * @author：wsx
 * @email：heikepianzi@qq.com
 * @date 2016/9/28
 */
public class WechatSettingsActivity extends BaseSettingsActivity {

    @Override
    public Fragment getSettingsFragment() {
        return new WechatSettingsFragment();
    }

    public static class WechatSettingsFragment extends BaseSettingsFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.wechat_settings);

            //微信红包模式
            final ListPreference wxMode = (ListPreference) findPreference(Config.KEY_WECHAT_MODE);
            wxMode.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    int value = Integer.parseInt(String.valueOf(newValue));
                    preference.setSummary(wxMode.getEntries()[value]);
                    BaseApplication.eventStatistics(getActivity(), "wx_mode", String.valueOf(newValue));
                    return true;
                }
            });
            wxMode.setSummary(wxMode.getEntries()[Integer.parseInt(wxMode.getValue())]);

            //打开微信红包后
            final ListPreference wxAfterOpenPre = (ListPreference) findPreference(Config.KEY_WECHAT_AFTER_OPEN_HONGBAO);
            wxAfterOpenPre.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    int value = Integer.parseInt(String.valueOf(newValue));
                    preference.setSummary(wxAfterOpenPre.getEntries()[value]);
                    BaseApplication.eventStatistics(getActivity(), "wx_after_open", String.valueOf(newValue));
                    return true;
                }
            });
            wxAfterOpenPre.setSummary(wxAfterOpenPre.getEntries()[Integer.parseInt(wxAfterOpenPre.getValue())]);

            //获取微信红包后
            final ListPreference wxAfterGetPre = (ListPreference) findPreference(Config.KEY_WECHAT_AFTER_GET_HONGBAO);
            wxAfterGetPre.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    int value = Integer.parseInt(String.valueOf(newValue));
                    preference.setSummary(wxAfterGetPre.getEntries()[value]);
                    BaseApplication.eventStatistics(getActivity(), "wx_after_get", String.valueOf(newValue));
                    return true;
                }
            });
            wxAfterGetPre.setSummary(wxAfterGetPre.getEntries()[Integer.parseInt(wxAfterGetPre.getValue())]);

            final EditTextPreference delayEditTextPre = (EditTextPreference) findPreference(Config.KEY_WECHAT_DELAY_TIME);
            delayEditTextPre.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if ("0".equals(String.valueOf(newValue))) {
                        preference.setSummary("");
                    } else {
                        preference.setSummary("已延时" + newValue + "毫秒");
                    }
                    BaseApplication.eventStatistics(getActivity(), "wx_delay_time", String.valueOf(newValue));
                    return true;
                }
            });
            String delay = delayEditTextPre.getText();
            if ("0".equals(String.valueOf(delay))) {
                delayEditTextPre.setSummary("");
            } else {
                delayEditTextPre.setSummary("已延时" + delay + "毫秒");
            }
        }
    }
}
