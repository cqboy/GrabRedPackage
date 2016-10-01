package com.zhiyuanjia.grabredpackage.ui;

import android.app.Fragment;
import android.os.Bundle;
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
public class NotifySettingsActivity extends BaseSettingsActivity {

    @Override
    public Fragment getSettingsFragment() {
        return new NotifySettingsFragment();
    }

    public static class NotifySettingsFragment extends BaseSettingsFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.notify_settings);

            findPreference(Config.KEY_NOTIFY_SOUND).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    BaseApplication.eventStatistics(getActivity(), "notify_sound", String.valueOf(newValue));
                    return true;
                }
            });

            findPreference(Config.KEY_NOTIFY_VIBRATE).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    BaseApplication.eventStatistics(getActivity(), "notify_vibrate", String.valueOf(newValue));
                    return true;
                }
            });

            findPreference(Config.KEY_NOTIFY_NIGHT_ENABLE).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    BaseApplication.eventStatistics(getActivity(), "notify_night", String.valueOf(newValue));
                    return true;
                }
            });
        }
    }
}