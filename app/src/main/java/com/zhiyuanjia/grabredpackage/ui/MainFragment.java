package com.zhiyuanjia.grabredpackage.ui;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.SwitchPreference;
import android.widget.Toast;

import com.zhiyuanjia.grabredpackage.R;
import com.zhiyuanjia.grabredpackage.base.BaseApplication;
import com.zhiyuanjia.grabredpackage.constent.Config;
import com.zhiyuanjia.grabredpackage.service.GrabService;

/**
 * @Description：TODO(- 类描述： -)
 * @author：wsx
 * @email：heikepianzi@qq.com
 * @date 2016/9/28
 */
public class MainFragment extends BaseSettingsFragment {

    private SwitchPreference notificationPref;
    private boolean notificationChangeByUser = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.main);

        //微信红包开关
        Preference wechatPref = findPreference(Config.KEY_ENABLE_WECHAT);
        wechatPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if ((Boolean) newValue && !GrabService.isRunning()) {
                    ((MainActivity) getActivity()).showOpenAccessibilityServiceDialog();
                }
                return true;
            }
        });

        notificationPref = (SwitchPreference) findPreference("KEY_NOTIFICATION_SERVICE_TEMP_ENABLE");
        notificationPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    Toast.makeText(getActivity(), "该功能只支持安卓4.3以上的系统", Toast.LENGTH_SHORT).show();
                    return false;
                }

                if (!notificationChangeByUser) {
                    notificationChangeByUser = true;
                    return true;
                }

                boolean enalbe = (boolean) newValue;

                Config.getConfig(getActivity()).setNotificationServiceEnable(enalbe);

                if (enalbe && !GrabService.isNotificationServiceRunning()) {
                    ((MainActivity) getActivity()).openNotificationServiceSettings();
                    return false;
                }
                BaseApplication.eventStatistics(getActivity(), "notify_service", String.valueOf(newValue));
                return true;
            }
        });

        Preference preference = findPreference("KEY_FOLLOW_ME");
        if (preference != null) {
            preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(getActivity(), AboutMeActivity.class);
                    startActivity(intent);
                    BaseApplication.eventStatistics(getActivity(), "about_author");
                    return true;
                }
            });
        }

        preference = findPreference("KEY_DONATE_ME");
        if (preference != null) {
            preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
//                    ((MainActivity) getActivity()).showDonateDialog();
//                    BaseApplication.eventStatistics(getActivity(), "donate");
                    return true;
                }
            });
        }

        findPreference("WECHAT_SETTINGS").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(getActivity(), WechatSettingsActivity.class));
                return true;
            }
        });

        findPreference("NOTIFY_SETTINGS").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(getActivity(), NotifySettingsActivity.class));
                return true;
            }
        });

        findPreference("WECHAT_WHITELIST").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(getActivity(), WhitelistActivity.class));
                return true;
            }
        });
    }

    /**
     * 更新快速读取通知的设置
     */
    public void updateNotifyPreference() {
        if (notificationPref == null) {
            return;
        }
        boolean running = GrabService.isNotificationServiceRunning();
        boolean enable = Config.getConfig(getActivity()).isEnableNotificationService();
        if (enable && running && !notificationPref.isChecked()) {
            BaseApplication.eventStatistics(getActivity(), "notify_service", String.valueOf(true));
            notificationChangeByUser = false;
            notificationPref.setChecked(true);
        } else if ((!enable || !running) && notificationPref.isChecked()) {
            notificationChangeByUser = false;
            notificationPref.setChecked(false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateNotifyPreference();
    }
}