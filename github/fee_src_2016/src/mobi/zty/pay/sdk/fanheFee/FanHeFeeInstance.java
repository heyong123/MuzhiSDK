package mobi.zty.pay.sdk.fanheFee;

import mobi.zty.pay.sdk.PayConfig;
import mobi.zty.pay.sdk.PayResultInfo;
import mobi.zty.pay.sdk.PaymentInterf;
import mobi.zty.sdk.game.bean.FeeInfo;
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
		final FeeInfo feeInfo =  (FeeInfo) parameters[0];
		if (feeInfo==null||feeInfo.sdkPayInfo==null) {
			notiFyResult(PayConfig.SDK_INFO_NULL, "后台没给计费点","");
			Helper.sendPayMessageToServer(PayConfig.LEYOU_FEE, "后台没给计费点", "0000000000");
			return;
		};
		String cpfee = feeInfo.sdkPayInfo.payCode;//计费代码
		cpfee = getPayCode(context, cpfee);
		
		final String cpGoods = feeInfo.feeName;//渠道商品名称
		Helper.sendPayMessageToServer(PayConfig.BEIQING_PAY, "执行支付cpfee="+cpfee, feeInfo.orderId);
		PayMentAction.getInstance().payment(cpfee, feeInfo.orderId, cpGoods, (Activity) context, new PayMentCallBack() {
			
			@Override
			public void payResult(int resultCode, String msg) {
				PayResultInfo info = new PayResultInfo();
				info.orderId = feeInfo.orderId;
				if (resultCode == 501) {//支付成功
					notiFyResult(PayConfig.BIIL_SUCC, msg, feeInfo.orderId);
					Helper.sendPayMessageToServer(PayConfig.BEIQING_PAY, "sdk回调成功", feeInfo.orderId);
				}else{
					notiFyResult(PayConfig.SP_PAY_FAIL, msg, feeInfo.orderId);
					Helper.sendPayMessageToServer(PayConfig.BEIQING_PAY, "sdk回调失败resultCode="+resultCode, feeInfo.orderId);
				}
			}
		});
	}
	
	private void notiFyResult(int resutCode,String retMsg,String orderId) {
		PayResultInfo info = new PayResultInfo();
		info.resutCode = resutCode;
		info.retMsg = retMsg;
		info.orderId = orderId;
		Message message = callBHandler.obtainMessage(PayConfig.NOTIFY_PAYRESULT);
		message.obj = info;
		message.sendToTarget();
	}
	private String getPayCode(final Context context, String cpfee) {
		String cpcode = Helper.getConfigString(context, "cpcode");
		cpfee= cpcode+cpfee;
		return cpfee;
	}
}
