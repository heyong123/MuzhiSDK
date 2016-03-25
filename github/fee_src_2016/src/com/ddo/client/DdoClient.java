package com.ddo.client;

import static com.ddo.client.HttpUtil.get;
import static com.ddo.client.HttpUtil.post;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.ddo.client.HttpUtil.HttpResult;

public class DdoClient {
	
	public static String PRE_URL = "http://101.201.223.137";
	
	public static String GET_SSO_URL = PRE_URL +"/ddo/get-sso-url?cpId={cpId}&payCode={payCode}&cpMsg={cpMsg}&imsi={imsi}&imei={imei}";
	
	public static String SUBMIT_SSO_TICKET = PRE_URL +"/ddo/submit-sso-ticket?orderId={orderId}&ticket={ticket}";
	
	public static String SET_SMS = PRE_URL +"/ddo/set-sms";
	
	/**
	 *
	 * 
	 * @author felix  @date 2016年1月21日 下午2:18:47
	 * @param payCode
	 * @param cpId
	 * @param imsi
	 * @param imei
	 * @return
	 * @throws IOException 
	 * @throws UnknownHostException 
	 */
	public String genOrder(String payCode, Integer cpId, String cpMsg,String imsi, String imei,Activity activity) throws UnknownHostException, IOException{
		
		//获得单点登录地址
		HttpResult result = get(GET_SSO_URL, cpId.toString(),payCode,cpMsg,imsi,imei);
		JSONObject resultObj = JSONObject.parseObject(result.getBody());
		if(resultObj.getInteger("code") != 1){
			return result.getBody();
		}
		
		String url = resultObj.getString("url");
		String orderId = resultObj.getString("orderId");
		
		//访问sso地址，获得ticket
		NetHelper helper = new NetHelper(activity);
		helper.closeWifi();
	
		result = get(url);
		helper.restoreNet();
		if(result.getHeader().get("Location") == null){
			return "{code:-1,msg:\"请使用移动设备在2G/3G/4G状态下访问网络\"}";
		}
		String location = result.getHeader().get("Location").toString();
		Log.i("location","location:"+location);
		String ticket = getParam(location, "ticket");

		if(ticket == null){
			return "{code:-1,msg:\"ticket获取失败\"}";
		}
		
		//提交ticket
		result = get(SUBMIT_SSO_TICKET, orderId,ticket);
		return result.getBody();
	}
	
	
	/**
	 * 提交短信
	 * 
	 * @author felix  @date 2016年1月21日 下午2:55:59
	 * @return
	 */
	public String submitSms(String orderId,String sms){
		String requestBody = "orderId="+orderId+"&sms="+sms.trim();
		HttpResult result = post(SET_SMS, requestBody);
		return result.getBody();
	}
	
	
	
	/**
	 * 获得对应的参数
	 * 
	 * @author felix  @date 2015年12月14日 上午9:40:56
	 * @param content
	 * @param key
	 * @return
	 */
	private String getParam(String content, String key){
		Pattern  p = Pattern.compile(key + "=(.*)?");
		Matcher m = p.matcher(content);
		if(m.find()){
			return m.group(1);
		}else{
			return null;
		}
	}
	
}
