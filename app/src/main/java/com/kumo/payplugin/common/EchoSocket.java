package com.kumo.payplugin.common;

import android.os.Build;

import com.kumo.payplugin.task.SSLSocketFactoryCompat;
import com.kumo.payplugin.util.LogUtil;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.net.ssl.SSLSocketFactory;

import io.socket.client.IO;
import io.socket.client.Socket;
import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.TlsVersion;

public  class EchoSocket{
    private static Socket instance1=null;
    private static Socket instance2=null;
    private static Socket instance3=null;
    private static final int maxCount = 3;

    private EchoSocket(){
    }

    public static Socket getThisInstance(int i){
        if(i==1)
            return EchoSocket.instance1;
        if(i==2)
            return EchoSocket.instance2;
        if(i==3)
            return EchoSocket.instance3;
        else
            return null;
    }

    public static Socket getInstance(String socketserverurl){
        Random random = new Random();
        int current = random.nextInt(maxCount)+1;
        if(getThisInstance(current)==null){
            synchronized(EchoSocket.class){
                if(current==1){
                    instance1=getIOSocket(socketserverurl);
                }
                if(current==2){
                    instance2=getIOSocket(socketserverurl);
                }
                if(current==3){
                    instance3=getIOSocket(socketserverurl);
                }
            }
        }
        return getThisInstance(current);
    }

    /**
     * 获取socket对象
     * @param socketserverurl
     * @return
     */
    public static Socket getIOSocket(String socketserverurl){
        try{
            if (Build.VERSION.SDK_INT >= 22 ){
                return IO.socket(socketserverurl);
            }
            else{
                SSLSocketFactory factory = new SSLSocketFactoryCompat();
                ConnectionSpec cs = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS).tlsVersions(TlsVersion.TLS_1_2).build();
                List<ConnectionSpec> specs = new ArrayList<>();
                specs.add(cs);
                specs.add(ConnectionSpec.COMPATIBLE_TLS);
                specs.add(ConnectionSpec.CLEARTEXT);
                OkHttpClient client = new OkHttpClient.Builder().sslSocketFactory(factory).connectionSpecs(specs).build();
                IO.setDefaultOkHttpWebSocketFactory(client);
                IO.setDefaultOkHttpCallFactory(client);
                // set as an option
                IO.Options opts = new IO.Options();
                opts.callFactory = client;
                opts.webSocketFactory = client;
                return IO.socket(socketserverurl, opts);
            }
        }catch(Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            LogUtil.debugLog(sw.toString());
            return null;
        }
    }

}
