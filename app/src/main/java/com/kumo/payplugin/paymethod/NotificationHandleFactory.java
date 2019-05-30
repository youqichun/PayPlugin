package com.kumo.payplugin.paymethod;
import android.app.Notification;

import com.kumo.payplugin.paymethod.AlipayNotificationHandle;
import com.kumo.payplugin.common.IDoPost;
import com.kumo.payplugin.paymethod.NotificationHandle;

public  class NotificationHandleFactory{
    public NotificationHandle getNotificationHandle(String pkg, Notification notification, IDoPost postpush){
                //支付宝
                if("com.eg.android.AlipayGphone".equals(pkg)){
                        return new AlipayNotificationHandle("com.eg.android.AlipayGphone",notification,postpush);
                }
                return null;
        }
}


