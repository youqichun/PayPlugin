package com.kumo.payplugin;

import android.app.Application;
import android.content.Intent;

import com.kumo.payplugin.service.NotificationCollectorMonitorService;
import com.tao.admin.loglib.IConfig;
import com.tao.admin.loglib.TLogApplication;

/**
 * 初始化加载Application
 */
public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        initLogConfig();//初始化日志
        startNotificationService();//加载通知收集器或监视器服务
    }

    private void initLogConfig(){
        TLogApplication.initialize(this);
        IConfig.getInstance().isShowLog(false)//是否在logcat中打印log,默认不打印
                .isWriteLog(false)//是否在文件中记录，默认不记录
                .tag("GoFileService");//logcat 日志过滤tag tag : 一般传入当前的类名
    }
    private void startNotificationService(){
        startService(new Intent(this, NotificationCollectorMonitorService.class));
    }
}
