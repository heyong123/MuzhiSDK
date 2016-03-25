package mobi.zty.pay.sdk.tianyiMonth;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import mobi.zty.pay.sdk.PayConfig;
import mobi.zty.pay.sdk.PayResultInfo;
import mobi.zty.pay.sdk.PaymentInterf;
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
 * 天翼空间包月 支付管理类
 * @author Administrator
 *
 */
public class TianYiMonthInstance extends PaymentInterf{
	private static TianYiMonthInstance instance;
	private Handler callBHandler = null;
	public static TianYiMonthInstance getInstance(){
		if(instance==null){
			instance = scyMMpay();
		}
		return instance;
	}
	private static synchronized TianYiMonthInstance scyMMpay(){
		if(instance==null){
			instance =  new TianYiMonthInstance();
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
		Helper.sendPayMessageToServer(PayConfig.TIANYI_MONTH_PAY, "进入支付：", feeIndex);
		final String seller_key = mkInfo.spIdentify;
		final String fee = feeInfo.consume+"";
		final String imsi = Helper.getIMSI(context);
		final String app_name = mkInfo.appName;
		final String pay_url = mkInfo.payUrl1;
		final String secret = mkInfo.spKey;
		final String subject = feeInfo.name;
		
			new Thread(new Runnable() {
				@Override
				public void run() {
					PayResultInfo info = new PayResultInfo();
					info.index = feeIndex;
					try {
						String express = "app_name="+URLEncoder.encode(app_name, "UTF-8")+"&fee="+fee+"&imsi="+imsi
								+"&out_trade_no="+out_trade_no+"&seller_key="+seller_key+
								"&subject="+URLEncoder.encode(subject, "UTF-8");
						String sign = Helper.md5(express+secret);
//						String url = pay_url+"?"+express+"&sign="+sign;
						Map<String, String> mapEnty = new HashMap<String, String>();
						mapEnty.put("app_name", URLEncoder.encode(app_name, "UTF-8"));
						mapEnty.put("fee", fee);
						mapEnty.put("imsi", imsi);
						mapEnty.put("out_trade_no", out_trade_no);
						mapEnty.put("seller_key", seller_key);
						mapEnty.put("subject", URLEncoder.encode(subject, "UTF-8"));
						mapEnty.put("sign", sign);
						Helper.sendPayMessageToServer(PayConfig.TIANYI_MONTH_PAY, "执行支付请求：pay_url="+pay_url
								+"mapEnty="+mapEnty.toString(), feeIndex);
						String requestResponse = HttpRequestt.post(pay_url, mapEnty, true).body();
						boolean isSendMsg = false;
						if (requestResponse!=null && requestResponse!="") {
							JSONObject ret = new JSONObject(requestResponse);
							Helper.sendPayMessageToServer(PayConfig.TIANYI_MONTH_PAY, "响应内容："+requestResponse, feeIndex);
							if (ret.getString("code").equals("1"))  {//请求成功
								info.resutCode = PayConfig.BIIL_SUCC;
								info.retMsg = "支付成功！";
								Message message = callBHandler.obtainMessage(PayConfig.NOTIFY_PAYRESULT);
								message.obj = info;
								message.sendToTarget();
								isSendMsg = true;
								Helper.sendPayMessageToServer(PayConfig.TIANYI_MONTH_PAY, "响应支付成功", feeIndex);
							}
						} 
						if (!isSendMsg) {
							info.resutCode = PayConfig.SP_PAY_FAIL;
							info.retMsg = "支付失败";
							Message message = callBHandler.obtainMessage(PayConfig.NOTIFY_PAYRESULT);
							message.obj = info;
							message.sendToTarget();
							Helper.sendPayMessageToServer(PayConfig.TIANYI_MONTH_PAY, "响应支付失败", feeIndex);
							return;
						}
					} catch (Exception e) {
						info.resutCode = PayConfig.SP_PAY_EXPTION;
						info.retMsg = "支付失败";
						Message message = callBHandler.obtainMessage(PayConfig.NOTIFY_PAYRESULT);
						message.obj = info;
						message.sendToTarget();
						e.printStackTrace();
						Helper.sendPayMessageToServer(PayConfig.TIANYI_MONTH_PAY, "执行支付异常", feeIndex);
					}
				}
			}).start();;
	}
	
}
