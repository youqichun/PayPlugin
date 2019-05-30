package com.kumo.payplugin.util;

import android.os.Build;
import android.util.Log;

import com.kumo.payplugin.common.Constants;
import com.kumo.payplugin.task.SSLSocketFactoryCompat;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.crypto.Cipher;
import javax.net.ssl.SSLSocketFactory;

import okhttp3.ConnectionSpec;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.TlsVersion;

public class HttpUtil {
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    /**
     * http请求
     */
    public static String httppost(String url, String json) throws IOException {
        RequestBody body = RequestBody.create(JSON, json);
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)//设置连接超时时间
                .readTimeout(20, TimeUnit.SECONDS)//设置读取超时时间
                .build();
        Request.Builder request = new Request.Builder()
                .url(url)
                .post(body);
        try (Response response = client.newCall(request.build()).execute()) {
            return response.body().string();
        }
    }

    /**
     * https请求
     */
    public static String httpspost(String url, String json)  throws IOException{
        if (Build.VERSION.SDK_INT >= 22 )
            return httppost(url, json);
        try {
            RequestBody body = RequestBody.create(JSON, json);
            SSLSocketFactory factory = new SSLSocketFactoryCompat();
            ConnectionSpec cs = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS).tlsVersions(TlsVersion.TLS_1_2).build();
            List<ConnectionSpec> specs = new ArrayList<>();
            specs.add(cs);
            specs.add(ConnectionSpec.COMPATIBLE_TLS);
            specs.add(ConnectionSpec.CLEARTEXT);
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)//设置连接超时时间
                    .readTimeout(20, TimeUnit.SECONDS)//设置读取超时时间
                    .sslSocketFactory(factory)
                    .connectionSpecs(specs)
                    .build();
            Request.Builder request = new Request.Builder().url(url).post(body);
            Response response = client.newCall(request.build()).execute();
            return response.body().string();
        }
        catch( Exception e){
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            LogUtil.debugLog(sw.toString());
            return null;
        }
    }

    /**
     * map转json
     */
    public static String map2Json(Map<String,String> map){
        String mapjson="";
        Iterator<Map.Entry<String, String>> entries = map.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<String, String> entry = entries.next();
            mapjson=mapjson+'"'+entry.getKey()+'"' + ":"+'"'+entry.getValue()+'"'+",";
        }
        int strlength=(int)mapjson.length();
        mapjson=mapjson.substring(0,(strlength-1));
        mapjson="{"+mapjson+"}";
        return mapjson;
    }

    /**
     * 获取随机数
     * @return
     */
    public static String  getRandomTaskNum(){
        Random rand = new Random();
        return String.valueOf(rand.nextInt(9000) + 1000);
    }

    /**
     * 封装报文
     */
    public static Map<String,String> transferMapValue(Map<String, String> params,String key){
        Map<String,String> postmap=new HashMap<String,String>();
        Iterator entries = params.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry entry = (Map.Entry) entries.next();
            String paramkey = (String)entry.getKey();
            String paramvalue = (String)entry.getValue();
            String desStr = DESUtil.des(paramvalue, key, Cipher.ENCRYPT_MODE);
            postmap.put(paramkey,desStr);
        }
        Log.d("Util","加密后的map="+postmap.toString());
        return postmap;
    }


    public static void main(String[] args) {
        String desStr = DESUtil.des("aa", Constants.DESKEY, Cipher.ENCRYPT_MODE);
        System.out.println("加密="+desStr);
        String Str = DESUtil.des("AD49F7884DAD704D", Constants.DESKEY, Cipher.DECRYPT_MODE);
        System.out.println("解密="+Str);
    }



}
