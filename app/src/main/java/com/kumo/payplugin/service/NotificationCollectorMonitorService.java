package com.kumo.payplugin.service;;

import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.Process;
import android.os.Build;
import android.util.Log;

import io.socket.client.Socket;
import java.util.List;
import java.util.Date;
import java.lang.System;
import java.lang.Thread;
import java.text.SimpleDateFormat;

import com.google.gson.Gson;
import com.kumo.payplugin.common.DeviceBean;
import com.kumo.payplugin.common.EchoSocket;
import com.kumo.payplugin.util.DeviceInfoUtil;
import com.kumo.payplugin.util.LogUtil;
import com.kumo.payplugin.util.PreferenceUtil;

import io.socket.emitter.Emitter;

import java.util.Timer;
import java.util.TimerTask;

/**
 *通知收集器或监视器服务
 *
 */
public class NotificationCollectorMonitorService extends Service {
        private static final String TAG = "NotifiCollectorMonitor";
        private Timer timer=null;
        private String echointerval=null;
        private TimerTask echotimertask =null;

        /**
         *      在startService启动一个Service 时会执行onStartCommand(Intent intent, int flags, int startId)
         * @param intent    intent：是startService时传过来的 如：startService(new Intent(this,TestService.class));
         * @param flags
         * @param startId       onStartCommand的启动次数，第一次通过startService启动为是1，不断startService启动依次累加，一般配合stopSelf(startId)使用可以看IntentService中使用
         * @return
         *      START_STICKY：如果service进程被kill掉，保留service的状态为开始状态，但不保留递送的intent对象。随后系统会尝试重新创建service，由 于服务状态为开始状态，所以创建服务后一定会调用onStartCommand(Intent,int,int)方法。如果在此期间没有任何启动命令被传 递到service，那么参数Intent将为null。
         *      START_NOT_STICKY：“非粘性的”。使用这个返回值时，如果在执行完onStartCommand后，服务被异常kill掉，系统不会自动重启该服务
         *      START_REDELIVER_INTENT：重传Intent。使用这个返回值时，如果在执行完onStartCommand后，服务被异常kill掉，系统会自动重启该服务，并将Intent的值传入。
         *      START_STICKY_COMPATIBILITY：START_STICKY的兼容版本，但不保证服务被kill后一定能重启
         */
        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
                return START_STICKY;
        }

        @Override
        public void onCreate() {
                super.onCreate();
                ensureCollectorRunning();
                startEchoTimer();
        }

        private boolean echoServerBySocketio(String echourl,String echojson){
                Socket mSocket= EchoSocket.getInstance(echourl);
                mSocket.connect();
                mSocket.emit("echo",echojson);
                mSocket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
                        @Override
                        public void call(Object... args) {
                            LogUtil.infoLog("socket disconnected,try start echo in 5 secend");
                            try{
					                Thread.sleep(5000);
				                }catch(InterruptedException e){
					                    e.printStackTrace();
				                }
                            echoServer();
                        }
                });
                return true;
        }

        //获取默认时间间隔
        private String getDefaultEchoInterval(){
                if (Build.VERSION.SDK_INT >= 22 ){ return  "300"; }
                else{ return  "100"; }
        }

        //启动计时器
        private void startEchoTimer(){
                PreferenceUtil preference=new PreferenceUtil(getBaseContext());
                String interval=preference.getEchoInterval();
                this.echointerval=(!interval.equals("") ?  interval:getDefaultEchoInterval());
                this.echotimertask=returnEchoTimerTask();
                this.timer=new Timer();
                int intervalmilliseconds = Integer.parseInt(this.echointerval)*1000;
                LogUtil.infoLog("now socketio timer milliseconds:"+intervalmilliseconds);
                timer.schedule(echotimertask,5*1000,intervalmilliseconds);
        }
        private TimerTask returnEchoTimerTask(){
                return new TimerTask() {
                @Override
                public void run() {
                        if(!isIntervalMatchPreference()){
                            restartEchoTimer();
                            return;
                        }
                        LogUtil.debugLog("once socketio timer task run");
                        boolean flag= echoServer();
                        if(!flag){
                                LogUtil.debugLog("socketio timer task not have a server");
                        }
                }
          };
        }
        private void restartEchoTimer(){
                        if (this.timer != null) {  
                            this.timer.cancel();  
                            this.timer = null;  
                        }  
                        if (echotimertask != null) {  
                            echotimertask.cancel();  
                            echotimertask = null;  
                        }   
                        LogUtil.debugLog("restart echo timer task");
                        startEchoTimer();
        }

        private boolean isIntervalMatchPreference(){
                PreferenceUtil preference=new PreferenceUtil(getBaseContext());
                String interval=preference.getEchoInterval();
                if(interval.equals("")){
                        return true;
                }
                if(interval.equals(this.echointerval)){
                        return true;
                }
                return false;
        }


        private boolean echoServer(){
                PreferenceUtil preference=new PreferenceUtil(getBaseContext());
                Gson gson = new Gson();
                if(preference. isEcho()&&(preference.getEchoServer()!=null)){
                        Date date=new Date(System.currentTimeMillis());
                        SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String time=format.format(date);
                        DeviceBean device=new DeviceBean();
                        device.setDeviceid(DeviceInfoUtil.getUniquePsuedoID());
                        device.setTime(time);
                        LogUtil.debugLog("start connect socketio");
                        echoServerBySocketio(preference.getEchoServer(),gson.toJson(device));
                        LogUtil.debugLog(gson.toJson(device));
                        return true;
                }else{
                        return false;
                }
        }

        //---------------------------------------------NotificationListenerService监听时有失败的处理--------------------------------
        //在继承Android系统提供的NotificationListenerService这个类使用时会出现一个问题：应用进程被杀后再次启动时，服务不生效，导致通知栏有内容变更，服务无法感知
        //解决方法：在app每次启动时检测NotificationListenerService是否生效，不生效重新开启
        //1.1确认NotificationMonitor是否开启
        private void ensureCollectorRunning() {
                ComponentName collectorComponent = new ComponentName(this, /*NotificationListenerService Inheritance*/ NLService.class);
                Log.v(TAG, "ensureCollectorRunning collectorComponent: " + collectorComponent);
                ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                boolean collectorRunning = false;
                List<ActivityManager.RunningServiceInfo> runningServices = manager.getRunningServices(Integer.MAX_VALUE);
                if (runningServices == null ) {
                        Log.w(TAG, "ensureCollectorRunning() runningServices is NULL");
                        return;
                }
                for (ActivityManager.RunningServiceInfo service : runningServices) {
                        if (service.service.equals(collectorComponent)) {
                                Log.w(TAG, "ensureCollectorRunning service - pid: " + service.pid + ", currentPID: " + Process.myPid() + ", clientPackage: " + service.clientPackage + ", clientCount: " + service.clientCount
                                                + ", clientLabel: " + ((service.clientLabel == 0) ? "0" : "(" + getResources().getString(service.clientLabel) + ")"));
                                if (service.pid == Process.myPid() /*&& service.clientCount > 0 && !TextUtils.isEmpty(service.clientPackage)*/) {
                                        collectorRunning = true;
                                }
                        }
                }
                if (collectorRunning) {
                        Log.d(TAG, "ensureCollectorRunning: collector is running");
                        return;
                }
                Log.d(TAG, "ensureCollectorRunning: collector not running, reviving...");
                toggleNotificationListenerService();
        }

        //1.2重新开启NotificationMonitor
        private void toggleNotificationListenerService() {
                Log.d(TAG, "启动toggleNotificationListenerService方法");
                ComponentName thisComponent = new ComponentName(this, /*getClass()*/ NLService.class);
                PackageManager pm = getPackageManager();
                pm.setComponentEnabledSetting(thisComponent, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
                pm.setComponentEnabledSetting(thisComponent, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        }

        @Override
        public IBinder onBind(Intent intent) {
                return null;
        }

}
