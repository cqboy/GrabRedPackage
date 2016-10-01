package com.zhiyuanjia.grabredpackage.ui;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.zhiyuanjia.grabredpackage.R;
import com.zhiyuanjia.grabredpackage.base.BaseApplication;
import com.zhiyuanjia.grabredpackage.base.BaseSettingsActivity;
import com.zhiyuanjia.grabredpackage.constent.Config;
import com.zhiyuanjia.grabredpackage.service.GrabService;

/**
 * @Description：TODO(- 类描述： -)
 * @author：wsx
 * @email：heikepianzi@qq.com
 * @date 2016/9/28
 */
public class MainActivity extends BaseSettingsActivity {

    private Dialog mTipsDialog;
    private MainFragment mMainFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String version = "";
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = " v" + info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        setTitle(getString(R.string.app_name) + version);

        BaseApplication.activityStartMain(this);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Config.ACTION_QIANGHONGBAO_SERVICE_CONNECT);
        filter.addAction(Config.ACTION_QIANGHONGBAO_SERVICE_DISCONNECT);
        filter.addAction(Config.ACTION_NOTIFY_LISTENER_SERVICE_DISCONNECT);
        filter.addAction(Config.ACTION_NOTIFY_LISTENER_SERVICE_CONNECT);
        registerReceiver(qhbConnectReceiver, filter);
    }

    private BroadcastReceiver qhbConnectReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (isFinishing()) {
                return;
            }
            String action = intent.getAction();
            Log.d("MainActivity", "receive-->" + action);
            if (Config.ACTION_QIANGHONGBAO_SERVICE_CONNECT.equals(action)) {
                if (mTipsDialog != null) {
                    mTipsDialog.dismiss();
                }
            } else if (Config.ACTION_QIANGHONGBAO_SERVICE_DISCONNECT.equals(action)) {
                showOpenAccessibilityServiceDialog();
            } else if (Config.ACTION_NOTIFY_LISTENER_SERVICE_CONNECT.equals(action)) {
                if (mMainFragment != null) {
                    mMainFragment.updateNotifyPreference();
                }
            } else if (Config.ACTION_NOTIFY_LISTENER_SERVICE_DISCONNECT.equals(action)) {
                if (mMainFragment != null) {
                    mMainFragment.updateNotifyPreference();
                }
            }
        }
    };

    @Override
    public Fragment getSettingsFragment() {
        mMainFragment = new MainFragment();
        return mMainFragment;
    }

    @Override
    protected boolean isShowBack() {
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (GrabService.isRunning()) {
            if (mTipsDialog != null) {
                mTipsDialog.dismiss();
            }
        } else {
            showOpenAccessibilityServiceDialog();
        }

        boolean isAgreement = Config.getConfig(this).isAgreement();
        if (!isAgreement) {
            showAgreementDialog();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(qhbConnectReceiver);
        } catch (Exception e) {
        }
        mTipsDialog = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuItem item = menu.add(0, 0, 1, R.string.open_service_button);
        item.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_NEVER);

        MenuItem notifyitem = menu.add(0, 3, 2, R.string.open_notify_service);
        notifyitem.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_NEVER);

        MenuItem about = menu.add(0, 4, 4, R.string.about_title);
        about.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_NEVER);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                openAccessibilityServiceSettings();
                BaseApplication.eventStatistics(this, "menu_service");
                return true;
            case 3:
                openNotificationServiceSettings();
                BaseApplication.eventStatistics(this, "menu_notify");
                break;
            case 4:
                startActivity(new Intent(this, AboutMeActivity.class));
                BaseApplication.eventStatistics(this, "menu_about");
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 显示免责声明的对话框
     */
    private void showAgreementDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle(R.string.agreement_title);
        builder.setMessage(getString(R.string.agreement_message, getString(R.string.app_name)));
        builder.setPositiveButton("同意", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Config.getConfig(getApplicationContext()).setAgreement(true);
                BaseApplication.eventStatistics(MainActivity.this, "agreement", "true");
            }
        });
        builder.setNegativeButton("不同意", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Config.getConfig(getApplicationContext()).setAgreement(false);
                BaseApplication.eventStatistics(MainActivity.this, "agreement", "false");
                finish();
            }
        });
        builder.show();
    }

    /**
     * 分享
     */
    private void showShareDialog() {
        BaseApplication.showShare(this);
    }


    /**
     * 显示未开启辅助服务的对话框
     */
    protected void showOpenAccessibilityServiceDialog() {
        if (mTipsDialog != null && mTipsDialog.isShowing()) {
            return;
        }
        View view = getLayoutInflater().inflate(R.layout.dialog_tips_layout, null);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAccessibilityServiceSettings();
            }
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.open_service_title);
        builder.setView(view);
        builder.setPositiveButton(R.string.open_service_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                openAccessibilityServiceSettings();
            }
        });
        mTipsDialog = builder.show();
    }

    /**
     * 打开辅助服务的设置
     */
    private void openAccessibilityServiceSettings() {
        try {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
            Toast.makeText(this, R.string.tips, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 打开通知栏设置
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    protected void openNotificationServiceSettings() {
        try {
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
            startActivity(intent);
            Toast.makeText(this, R.string.tips, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
