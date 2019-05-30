package com.kumo.payplugin.service;
import android.content.Intent;
import android.service.notification.NotificationListenerService;
import android.app.Notification;
import android.service.notification.StatusBarNotification;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.os.Bundle;
import android.content.Context;
import android.os.Build;
import com.kumo.payplugin.common.Constants;
import com.kumo.payplugin.paymethod.NotificationHandle;
import com.kumo.payplugin.common.AsyncResponse;
import com.kumo.payplugin.paymethod.NotificationHandleFactory;
import com.kumo.payplugin.task.PostTask;
import com.kumo.payplugin.util.DeviceInfoUtil;
import com.kumo.payplugin.util.HttpUtil;
import com.kumo.payplugin.common.IDoPost;
import com.kumo.payplugin.util.LogUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class NLService extends NotificationListenerService implements AsyncResponse,IDoPost {
        private String TAG="NLService";
        private Context context=null;

        /**
         * 抓取通知数据
         * @param sbn
         */
        @Override
        public void onNotificationPosted(StatusBarNotification sbn) {
                //这里只是获取了包名和通知提示信息，其他数据可根据需求取，注意空指针就行
                Log.d(TAG,"接受到通知消息");
                Notification notification = sbn.getNotification();
                String pkg = sbn.getPackageName();
                if (notification == null) {
                        return;
                }
                Bundle extras = notification.extras;
                if(extras==null){
                    return;
                }
                NotificationHandle notihandle =new NotificationHandleFactory().getNotificationHandle(pkg,notification,this);//接受推送处理
                if(notihandle!=null){
                        notihandle.handleNotification();
                }
                Log.d(TAG,"开始打印通知详情=");
                Log.d(TAG,"包名是"+pkg);
                printNotifyInfo(notification,extras);//打印通知数据
        }

        @Override
        public void onNotificationRemoved(StatusBarNotification sbn) {
                if (Build.VERSION.SDK_INT >19)
                        super.onNotificationRemoved(sbn);
        }

        private void sendBroadcast(String msg) {
                Intent intent = new Intent(getPackageName());
                intent.putExtra("text", msg);
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }

        //发起通知
        public void doPost(Map<String, String> params){
                Log.d(TAG,"开始准备post");
                Map<String, String> tmpmap=params;
                Map<String, String> postmap=null;
                String tasknum=HttpUtil.getRandomTaskNum();
                PostTask mtask = new PostTask();
                mtask.setRandomTaskNum(tasknum);
                mtask.setOnAsyncResponse(this);
                tmpmap.put("deviceid",DeviceInfoUtil.getUniquePsuedoID());//设备id
                //封装请求报文
                postmap=HttpUtil.transferMapValue(tmpmap,Constants.DESKEY);
                LogUtil.postRecordLog(tasknum,"封装请求报文="+tmpmap.toString());
                mtask.execute(postmap);
        }
        //打印通知数据
        private void printNotifyInfo(Notification notification,Bundle extras){
                Date date=new Date(notification.when);
                SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm");
                String notitime=format.format(date);
                Log.d(TAG,"通知时间="+notitime);
                String title=extras.getString(Notification.EXTRA_TITLE, "");
                Log.d(TAG,"通知标题="+title);
                String content=extras.getString(Notification.EXTRA_TEXT, "");
                Log.d(TAG,"通知内容="+content);

        }
        /**
         * 通知成功
         * @param returnstr     返回响应
         */
        public void onDataReceivedSuccess(String[] returnstr) {
                Log.d(TAG,"Post推送成功");
                Log.d(TAG,returnstr[2]);
                LogUtil.postResultLog(returnstr[0],returnstr[1],returnstr[2]);
        }
        /**
         * 通知失败
         * @param returnstr     返回响应
         */
        @Override
        public void onDataReceivedFailed(String[] returnstr) {
                Log.d(TAG,"Post推送失败");
                LogUtil.postResultLog(returnstr[0],returnstr[1],returnstr[2]);
        }
}
