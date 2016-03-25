package mobi.zty.pay.sdk.qipaSDkFee;

import mobi.zty.pay.sdk.PayConfig;
import mobi.zty.pay.sdk.PayResultInfo;
import mobi.zty.pay.sdk.PaymentInterf;
import mobi.zty.sdk.game.bean.FeeInfo;
import mobi.zty.sdk.util.Helper;
import mobi.zty.sdk.util.Util_G;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.door.frame.DnPayServer;


/**
 * 奇葩sdk支付的管理类
 * @author Administrator
 *
 */
public class QipaSdkInstance extends PaymentInterf{
	private static QipaSdkInstance instance;
	private Handler callBHandler = null;
	public static QipaSdkInstance getInstance(){
		if(instance==null){
			instance = scyQiPaypay();
		}
		return instance;
	}
	
	private Handler appHandler = new Handler(Looper.getMainLooper()){
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			boolean isSucc = false;
			String resultMsg = "";
			Util_G.debugE("ALLPAY", "奇葩支付响应："+msg.toString());
			Helper.sendPayMessageToServer(PayConfig.QIPA_PAY, "支付响应："+msg.what, feeInfo.orderId);
			if (msg.what == 100)
			{
				
				Bundle data = msg.getData();
				int errcode = data.getInt("errcode");
				String extdata = data.getString("extdata");
				if (errcode == 4000) {
					resultMsg = "话费支付：成功";
					isSucc = true;
				}else{
					resultMsg = "话费支付："+errcode;
				}
			}else{
				//应用可以不关心这些错误码
				Bundle data = msg.getData();
				int errcode = data.getInt("errcode");
				resultMsg = "errcode提示："+errcode;
			}
			PayResultInfo info = new PayResultInfo();
			info.orderId = feeInfo.orderId;
			if (isSucc) {
				info.resutCode = PayConfig.BIIL_SUCC;
				info.retMsg = "奇葩sdk支付成功";
				Message message = callBHandler.obtainMessage(PayConfig.NOTIFY_PAYRESULT);
				message.obj = info;
				message.sendToTarget();
				Helper.sendPayMessageToServer(PayConfig.QIPA_PAY, "响应成功", feeInfo.orderId);
			}else{
				info.resutCode = PayConfig.SP_PAY_FAIL;
				info.retMsg = resultMsg;
				Message message = callBHandler.obtainMessage(PayConfig.NOTIFY_PAYRESULT);
				message.obj = info;
				message.sendToTarget();
				Helper.sendPayMessageToServer(PayConfig.QIPA_PAY, "响应失败", feeInfo.orderId);
			}
			Helper.sendPayMessageToServer(PayConfig.QIPA_PAY, "支付响应："+info.retMsg, feeInfo.orderId);
		}
		
	};
	private FeeInfo feeInfo;
	private static synchronized QipaSdkInstance scyQiPaypay(){
		if(instance==null){
			instance =  new QipaSdkInstance();
		}
		return instance;
	}
	@Override
	public void init(Context context, Object... parameters) {
		callBHandler = (Handler) parameters[0];
		DnPayServer.getInstance().init(context, appHandler);
	}

	@Override
	public void pay(final Context context, Object... parameters) {
		feeInfo = (FeeInfo) parameters[0];
		if (feeInfo==null||feeInfo.sdkPayInfo==null) {
			Helper.sendPayMessageToServer(PayConfig.QIPA_PAY, "后台没给计费点", "0000000000");
			return;
		};
		Helper.sendPayMessageToServer(PayConfig.QIPA_PAY, "进入支付：", feeInfo.orderId);
		String payCode = feeInfo.sdkPayInfo.payCode;
		DnPayServer.getInstance().startPayservice(context, payCode, feeInfo.orderId);
	}
}
