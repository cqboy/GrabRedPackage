package com.zhiyuanjia.grabredpackage.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.zhiyuanjia.grabredpackage.R;
import com.zhiyuanjia.grabredpackage.base.BaseActivity;
import com.zhiyuanjia.grabredpackage.constent.PreferenceKey;
import com.zhiyuanjia.grabredpackage.util.PreferenceUtils;

import static com.zhiyuanjia.grabredpackage.entity.WeiChatAccessbility.chatList;

/**
 * @Description：TODO(- 类描述： -)
 * @author：wsx
 * @email：heikepianzi@qq.com
 * @date 2016/9/30
 */

public class WhitelistActivity extends BaseActivity {

    private ArrayAdapter arrayAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ListView listView = new ListView(this);
        setContentView(listView);

        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_expandable_list_item_1, chatList);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {

                new AlertDialog.Builder(WhitelistActivity.this)
                        .setTitle("删除白名单提示！")//提示框标题
                        .setMessage("是否需要将该人加入枪红包行列！")
                        .setPositiveButton("确定",//提示框的两个按钮
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Log.d("TAG", "onClick: position=" + position);
                                        chatList.remove(position);
                                        arrayAdapter.notifyDataSetChanged();
                                        PreferenceUtils.setObject(getApplicationContext(), PreferenceKey.WHITE_LIST, chatList);
                                    }
                                }).setNegativeButton("取消", null).create().show();

                return true;
            }
        });

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem about = menu.add(0, 0, 1, R.string.add_whiteChat);
        about.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_NEVER);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 0) {
            final EditText editText = new EditText(this);
            new AlertDialog.Builder(this)
                    .setTitle("请输入屏蔽人的昵称！")
                    .setView(editText)
                    .setPositiveButton("确定",
                            new android.content.DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (editText.length() == 0) {
                                        Toast.makeText(getApplicationContext(), "请输入昵称！", Toast.LENGTH_LONG).show();
                                        return;
                                    }
                                    chatList.add(editText.getText().toString());
                                    arrayAdapter.notifyDataSetChanged();
                                    PreferenceUtils.setObject(getApplicationContext(), PreferenceKey.WHITE_LIST, chatList);
                                }
                            }).setNegativeButton("取消", null).create().show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
