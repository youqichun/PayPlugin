package com.kumo.payplugin.task;
import android.os.AsyncTask;
import java.util.Map;
import android.util.Log;

import com.kumo.payplugin.common.AsyncResponse;
import com.kumo.payplugin.common.Constants;
import com.kumo.payplugin.util.HttpUtil;

/**
 * 推送通知线程
 */
public class PostTask extends AsyncTask<Map<String, String>, Void, String[]> {
        public AsyncResponse asyncResponse;
        public String TAG="NLService";
        public String randomtasknum;
        public void setRandomTaskNum(String num){
                this.randomtasknum=num;
        }
        public void setOnAsyncResponse(AsyncResponse asyncResponse)
        {
                this.asyncResponse = asyncResponse;
        }

        /**
         * 发起通知
         */
        @Override
        protected String[] doInBackground(Map<String,String> ... key) {
                Map<String ,String> postmap=key[0];
                if(postmap==null)
                        return null;
                String[] resultstr=new String[3];
                resultstr[0]=this.randomtasknum;
                resultstr[1]="true";
                String postjson=HttpUtil.map2Json(postmap);
                String url=Constants.NOTIFY_URL;//通知地址
                Log.d(TAG,"通知地址="+url);
                Log.d(TAG,"通知报文="+postjson);
                try{
                        String returnstr="";
                        if(url.startsWith("http://")){
                                returnstr=HttpUtil.httppost(url,postjson);//http请求

                        }else if (url.startsWith("https://")){
                                returnstr=HttpUtil.httpspost(url,postjson);//https请求
                        }
                        Log.d(TAG,"通知返回响应:"+returnstr);
                        resultstr[2]=returnstr;
                        return resultstr;
                }catch (Exception e){
                        e.printStackTrace();
                        Log.d(TAG,"通知异常:"+e.getLocalizedMessage());
                        return null;
                }
        }

        /**
         * 通知返回响应处理
         */
        @Override
        protected void onPostExecute(String[] resultstr) {
                super.onPostExecute(resultstr);
                if (resultstr != null)
                {
                        asyncResponse.onDataReceivedSuccess(resultstr);//将结果传给回调接口中的函数
                }
                else {
                        String [] errstr=new String[3];
                        errstr[0]=this.randomtasknum;
                        errstr[1]="false";
                        errstr[2]="";
                        asyncResponse.onDataReceivedFailed(errstr);
                }

        }

}
