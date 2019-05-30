package com.kumo.payplugin.common;
import java.util.List;
/**
 * 异步通知
 */
public interface AsyncResponse {
    /**
     * 接收成功通知
     * @param returnstr
     */
	public void onDataReceivedSuccess(String[] returnstr);

    /**
     * 接收失败通知
     * @param returnstr
     */
    public  void onDataReceivedFailed(String[] returnstr);
}
