package mobi.zty.pay.sdk.tomorrowFee;

import mobi.zty.pay.sdk.PayConfig;
import mobi.zty.pay.sdk.PayResultInfo;
import mobi.zty.pay.sdk.PaymentInterf;
import mobi.zty.sdk.game.bean.FeeInfo;
import mobi.zty.sdk.game.bean.MkInfo;
import mobi.zty.sdk.util.Util_G;
import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.tppay.sdk.TP_PAY;
import com.tppay.sdk.itf.ResultListener;


/**
 * 明天支付 sdk管理类
 * @author Administrator
 *
 */
public class TomorrowFeeInstance extends PaymentInterf{
	private static TomorrowFeeInstance instance;
	private Handler callBHandler = null;
	public static TomorrowFeeInstance getInstance(){
		if(instance==null){
			instance = scyTomorrowpay();
		}
		return instance;
	}
	private static synchronized TomorrowFeeInstance scyTomorrowpay(){
		if(instance==null){
			instance =  new TomorrowFeeInstance();
		}
		return instance;
	}
	@Override
	public void init(Context context, Object... parameters) {
		callBHandler = (Handler) parameters[0];
		MkInfo mkInfo = (MkInfo) parameters[1];
		String appId = mkInfo.appId;
		String cpid = mkInfo.spIdentify;
		//开启debug模式，上线前需关闭
		TP_PAY.setDebugMode(true);
		//错误码提示,上线可开启
		TP_PAY.setToastSwitchOn(true);
		TP_PAY.init((Activity)context, appId,cpid);
	}

	@Override
	public void pay(final Context context, Object... parameters) {
		final String orderId = (String) parameters[0];
		final int feeIndex = (Integer) parameters[1];
		FeeInfo feeInfo = (FeeInfo) parameters[2];
		String money = feeInfo.consume+"";
		String payCode = feeInfo.payCode;
		TP_PAY.pay((Activity) context,money,orderId,new ResultListener() {
			@Override
			public void payResult(int state, int arg1) {
				final Message message = callBHandler.obtainMessage(PayConfig.NOTIFY_PAYRESULT);
				final PayResultInfo info = new PayResultInfo();
				info.index = feeIndex;
				switch (state) {
				case PAY_SUCCESS :
					info.resutCode = PayConfig.BIIL_SUCC;
					info.retMsg = "明天支付成功";
					message.obj = info;
					message.sendToTarget();
					Util_G.debugE("ALLPAY", info.retMsg);
				break;
				case PAY_FAIL :
					info.resutCode = PayConfig.SP_PAY_FAIL;
					info.retMsg = "明天支付失败!"+arg1;
					message.obj = info;
					message.sendToTarget();
					Util_G.debugE("ALLPAY", info.retMsg);
				break;
				case PAY_CANCEL :
					info.resutCode = PayConfig.BIIL_CANCER;
					info.retMsg = "取消支付!"+arg1;
					message.obj = info;
					message.sendToTarget();
					Util_G.debugE("ALLPAY", info.retMsg);
				break;
				default:
					info.resutCode = PayConfig.SP_PAY_FAIL;
					info.retMsg = "明天支付未知错误!"+arg1;
					message.obj = info;
					message.sendToTarget();
				break;
				}
			}
		},payCode);
	}
}
