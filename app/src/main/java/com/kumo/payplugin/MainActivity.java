package com.kumo.payplugin;

import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.Menu;
import android.content.SharedPreferences;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationManagerCompat;
import android.view.View;
import android.view.MenuInflater;

import com.kumo.payplugin.common.Constants;
import com.github.pedrovgs.lynx.LynxConfig;
import com.github.pedrovgs.lynx.LynxActivity;

/**
 * 首页
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener{
        private static final String TAG = "MainActivity";
        private Toolbar myToolbar;
        private FloatingActionButton btnshowlog;
        private SharedPreferences sp ;

        /**
         *onCreate()在活动第一次创建时被调用，主要用于加载布局
         * @param savedInstanceState
         */
        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_main);
                initView();
        }

        /**
         * 初始化页面
         */
        private void initView() {
                sp = getSharedPreferences("url", Context.MODE_PRIVATE);
                myToolbar= (Toolbar) findViewById(R.id.my_toolbar);
                myToolbar.setTitle(Constants.TOOLBAR_TITLE);
                setSupportActionBar(myToolbar);
                btnshowlog=(FloatingActionButton) findViewById(R.id.floatingshowlog);
                btnshowlog.setOnClickListener(this);
        }

        /**
         * 监听按钮点击事件
         * @param v
         */
        @Override
        public void onClick(View v) {
                switch (v.getId()) {
                        case R.id.floatingshowlog: showLog();break;
                }
        }

        /**
         * 当 Activity 进入前台时，在调用 onStart()之后就会调用 onResume()，onResume这个方法在活动准备好和用户进行交互的时候调用
         */
        @Override
        protected void onResume() {
                super.onResume();
                //获取是否已授权
                boolean isAuthor=NotificationManagerCompat.getEnabledListenerPackages(this).contains(getPackageName());
                if (!isAuthor){
                        startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));//直接跳转通知授权界面
                }
        }

        /**
         * 跳转到日志Activity
         */
        private void showLog() {
                LynxConfig lynxConfig = new LynxConfig();
                lynxConfig.setMaxNumberOfTracesToShow(4000).setFilter("NLService");
                Intent lynxActivityIntent = LynxActivity.getIntent(this, lynxConfig);
                startActivity(lynxActivityIntent);
        }

        /**
         * 创建选项框
         * @param menu
         * @return
         */
        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
                MenuInflater inflater = getMenuInflater();
                inflater.inflate(R.menu.main, menu);
                return true;
        }

        /**
         * 选项框选中事件
         * @param item
         * @return
         */
        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
                switch (item.getItemId()) {
                        case R.id.action_settings: openSettingActivity();return true;
                        default: return super.onOptionsItemSelected(item);
                }
        }
        /**
         *跳转到设置Activity
         */
        private void openSettingActivity(){
                Intent intent = new Intent(MainActivity.this, PreferenceActivity.class);
                startActivity(intent);
        }



        /**
         * 如果 Activity 退出了，就会调用 onDestroy()方法。
         */
        @Override
        protected void onDestroy() {
                super.onDestroy();
        }
}
