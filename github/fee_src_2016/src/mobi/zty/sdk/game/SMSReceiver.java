package mobi.zty.sdk.game;

//mobi.zty.sdk.game.SMSReceiver
import mobi.zty.pay.sdk.PayConfig;
import mobi.zty.pay.sdk.PayResultInfo;
import mobi.zty.pay.sdk.factory.PaymentFactoy;
import mobi.zty.sdk.game.bean.FeeInfo;
import mobi.zty.sdk.util.Helper;
import mobi.zty.sdk.util.Util_G;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Message;
import android.telephony.SmsManager;

/**
 * 目前该类只负责 监听短信是否发送成功
 * @author Administrator
 *
 */
public final class SMSReceiver extends BroadcastReceiver
{
  public SMSReceiver()
  {
  }

  
  
  public final void onReceive(Context context, Intent intent)
  {
	  if(intent.getAction()
              .equals("SENT_SMS_ACTION"))
	  {
			synchronized (intent) {
				String orderId = intent.getStringExtra("order_id");
				if (orderId==null) {//没有订单 不做处理
					return;
				}
				int ret = getResultCode();
				PayResultInfo info = new PayResultInfo();
				info.orderId = orderId;
				GameSDK instance = GameSDK.getInstance();
				if (instance == null)
					return;
				FeeInfo feeInfo = instance.getFeeInfoByOrderId(orderId);
				if (feeInfo == null){
					Util_G.debugE("AllPay", "error 该订单的计费点已经不存在");
					Helper.sendPayMessageToServer(0, "error 该订单的计费点已经不存在", orderId);
					return;
				}
				Integer payType = feeInfo.payType;
				switch (ret) {
				case Activity.RESULT_OK:
					// 支付成功
					if (feeInfo.payStep == 0) {
						feeInfo.payStep = 1;
						Helper.sendPayMessageToServer(payType, "请求验证码的短信已经发出！", orderId);
						Util_G.debugE("AllPay", "请求验证码的短信已经发出！");
					} else {
						Util_G.debugE("AllPay", "支付成功的短信发出！");
						Helper.sendPayMessageToServer(payType, "支付成功的短信发出！", orderId);
					}
					
					if (feeInfo.payStep == 2) {
						try {
							PaymentFactoy.producePay(feeInfo.mkClassName,
									GameSDK.getInstance().context).notifyPay(1,
									feeInfo.orderId);
						} catch (Throwable e) {
							e.printStackTrace();
						}
						Util_G.debugE("Allpay", "mkSucc=" + payType + " orderId ="
								+ orderId);
						info.resutCode = PayConfig.BIIL_SUCC;
						info.retMsg = "支付成功！";
						Message message = instance.payResultHandle
								.obtainMessage(PayConfig.NOTIFY_PAYRESULT);
						message.obj = info;
						message.sendToTarget();

					}
					break;
				case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
				case SmsManager.RESULT_ERROR_RADIO_OFF:
				case SmsManager.RESULT_ERROR_NULL_PDU:
				default:
					try {
						PaymentFactoy.producePay(feeInfo.mkClassName,GameSDK.getInstance().context)
								.notifyPay(0,orderId);
					} catch (Throwable e) {
						e.printStackTrace();
					}
					info.resutCode = PayConfig.SEND_MSG_FAIL;
					info.retMsg = "短信发送失败：";
					Message message = instance.payResultHandle
							.obtainMessage(PayConfig.NOTIFY_PAYRESULT);
					message.obj = info;
					message.sendToTarget();
					Helper.sendPayMessageToServer(payType, info.retMsg , orderId);
					break;
				}
			}

      }
  }

  
}

