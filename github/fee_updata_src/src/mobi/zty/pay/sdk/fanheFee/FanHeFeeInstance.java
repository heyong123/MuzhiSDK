package mobi.zty.pay.sdk.fanheFee;

import mobi.zty.pay.sdk.PayConfig;
import mobi.zty.pay.sdk.PayResultInfo;
import mobi.zty.pay.sdk.PaymentInterf;
import mobi.zty.sdk.game.bean.FeeInfo;
import mobi.zty.sdk.game.bean.MkInfo;
import mobi.zty.sdk.util.Helper;
import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.tg52.action.PayMentAction;
import com.tg52.action.PayMentCallBack;


/**
 * 联通支付 饭盒支付的管理类
 * @author Administrator
 *
 */
public class FanHeFeeInstance extends PaymentInterf{
	private static FanHeFeeInstance instance;
	private Handler callBHandler = null;
	public static FanHeFeeInstance getInstance(){
		if(instance==null){
			instance = scyMMpay();
		}
		return instance;
	}
	private static synchronized FanHeFeeInstance scyMMpay(){
		if(instance==null){
			instance =  new FanHeFeeInstance();
		}
		return instance;
	}
	@Override
	public void init(Context context, Object... parameters) {
		callBHandler = (Handler) parameters[0];
		PayMentAction.getInstance().init((Activity)context);
	}

	@Override
	public void pay(final Context context, Object... parameters) {
		final String extentsP = (String) parameters[0];//透传参数
		final int feeIndex = (Integer) parameters[1];
		Helper.sendPayMessageToServer(PayConfig.BEIQING_PAY, "进入支付：", feeIndex);
		FeeInfo feeInfo = (FeeInfo) parameters[2];
		String cpfee = feeInfo.payCode;//计费代码
		cpfee = getPayCode(context, cpfee);
		
		final String cpGoods = feeInfo.name;//渠道商品名称
		Helper.sendPayMessageToServer(PayConfig.BEIQING_PAY, "执行支付cpfee="+cpfee, feeIndex);
		PayMentAction.getInstance().payment(cpfee, extentsP, cpGoods, (Activity) context, new PayMentCallBack() {
			
			@Override
			public void payResult(int resultCode, String msg) {
				PayResultInfo info = new PayResultInfo();
				info.index = feeIndex;
				if (resultCode == 501) {//支付成功
					info.resutCode = PayConfig.BIIL_SUCC;
					info.retMsg = msg;
					Message message = callBHandler.obtainMessage(PayConfig.NOTIFY_PAYRESULT);
					message.obj = info;
					message.sendToTarget();
					Helper.sendPayMessageToServer(PayConfig.BEIQING_PAY, "sdk回调成功", feeIndex);
				}else{
					info.resutCode = PayConfig.SP_PAY_FAIL;
					info.retMsg = msg;
					Message message = callBHandler.obtainMessage(PayConfig.NOTIFY_PAYRESULT);
					message.obj = info;
					message.sendToTarget();
					Helper.sendPayMessageToServer(PayConfig.BEIQING_PAY, "sdk回调失败resultCode="+resultCode, feeIndex);
				}
			}
		});
	}
	private String getPayCode(final Context context, String cpfee) {
		String cpcode = Helper.getConfigString(context, "cpcode");
		cpfee= cpcode+cpfee;
		return cpfee;
	}
}
