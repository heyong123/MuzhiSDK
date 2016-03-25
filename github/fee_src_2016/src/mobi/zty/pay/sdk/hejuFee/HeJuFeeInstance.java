package mobi.zty.pay.sdk.hejuFee;

import java.util.HashMap;

import org.hj20150901.lib.HejuExit;
import org.hj20150901.lib.HejuHuafeiCallback;
import org.hj20150901.lib.HejuInit;
import org.hj20150901.lib.HejuInstance;
import org.json.JSONException;
import org.json.JSONObject;






import mobi.zty.pay.sdk.PayConfig;
import mobi.zty.pay.sdk.PayResultInfo;
import mobi.zty.pay.sdk.PaymentInterf;
import mobi.zty.sdk.game.bean.FeeInfo;
import mobi.zty.sdk.util.Helper;
import mobi.zty.sdk.util.Util_G;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

/**
 * 冰锋谷支付管理类
 * @author Administrator
 *
 */
public class HeJuFeeInstance extends PaymentInterf{
	private static HeJuFeeInstance instance;
	private Handler callBHandler = null;
	private String code;
	private String extraInfo;
	public static HeJuFeeInstance getInstance(){
		if(instance==null){
			instance = scyMMpay();
		}
		return instance;
	}
	private static synchronized HeJuFeeInstance scyMMpay(){
		if(instance==null){
			instance =  new HeJuFeeInstance();
		}
		return instance;
	}
	@Override
	public void init(final Context context, Object... parameters) {
		if (callBHandler==null) {
			callBHandler = (Handler) parameters[0];
		}
		HejuInit mHejuInit = new HejuInit(context);
		mHejuInit.start();

	}
	

	@Override
	public void pay(final Context context, Object... parameters) {
		final FeeInfo feeinfo = (FeeInfo) parameters[0];
		final String productName = feeinfo.feeName;
		final String appName = feeinfo.sdkPayInfo.appName;
		final String point = String.valueOf(feeinfo.consume);
		extraInfo = feeinfo.orderId;
		
		HashMap<String , String> params = new HashMap<String , String>();
		params.put("productName", productName);//商品名
		params.put("appName", appName);//应用名
		params.put("point", point); //计费点数  不为空
		params.put("extraInfo", extraInfo); //CP扩展信息 可为空	
		params.put("debug", "1");//调试信息toast0关闭1开启
		params.put("activityName", "mobi.zty.sdk.util.HejuActivity");//寄主activity名称
		HejuInstance mHejuInstance = new HejuInstance();
		mHejuInstance.pay(context, params, new HejuHuafeiCallback() {

			@Override
			public void onFail(JSONObject payResult) {
				// TODO Auto-generated method stub
				
				try {
					code = payResult.getString("code");
					extraInfo = payResult.getString("extraInfo");
					
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Util_G.debugE("HEJU_PAY", "支付失败，code："+code);
				Helper.sendPayMessageToServer(PayConfig.HEJU_PAY, "鬼吹灯SDK返回失败", "0000000000");
				notiFyResult(PayConfig.SP_PAY_FAIL, "支付失败", extraInfo);					
			}

			@Override
			public void onSuccess(JSONObject payResult) {
				// TODO Auto-generated method stub
				try {
					code = payResult.getString("code");
					extraInfo = payResult.getString("extraInfo");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Util_G.debugE("HEJU_PAY", "支付成功，code："+code+",订单号："+extraInfo);
				Helper.sendPayMessageToServer(PayConfig.HEJU_PAY, "支付成功并发货", "0000000000");
				notiFyResult(PayConfig.BIIL_SUCC, "支付成功", extraInfo);	
			}
			
		});
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
	//冰封谷SDK退出方法
	public void exitsdk(Context context){
		
		HejuExit he = new HejuExit(context);
		he.exit();

	}
}
