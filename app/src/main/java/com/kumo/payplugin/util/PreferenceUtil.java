package com.kumo.payplugin.util;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * 设置工具类
 */
public class PreferenceUtil{
        SharedPreferences sharedPref=null;
        Context context=null;
        public PreferenceUtil(Context context){
                this.context=context;
                init();
        }
        public void init(){
                sharedPref=PreferenceManager.getDefaultSharedPreferences(this.context);

        }
        public boolean isEcho(){
                return this.sharedPref.getBoolean("isecho",false);
        }
        public String getEchoServer(){
                return this.sharedPref.getString("echoserver",null);
        }
        public String getEchoInterval(){
                return this.sharedPref.getString("echointerval","");
        }

}
