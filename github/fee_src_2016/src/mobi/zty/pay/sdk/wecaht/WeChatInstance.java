package mobi.zty.pay.sdk.wecaht;

import mobi.zty.pay.sdk.PayConfig;
import mobi.zty.pay.sdk.PaymentInterf;
import mobi.zty.sdk.game.Constants;
import mobi.zty.sdk.game.GameSDK;
import mobi.zty.sdk.game.bean.WeiXinOrderInfo;
import mobi.zty.sdk.game.callback.WeChatOrderInfoListener;
import mobi.zty.sdk.game.object.parser.WeiXinOrderInfoParser;
import mobi.zty.sdk.http.HttpRequest;

import org.json.JSONObject;

import android.content.Context;
import android.os.Handler;

public class WeChatInstance extends PaymentInterf {
	private Handler callBHandler = null;
	private static WeChatInstance instance;
	private String payOrder;
	public static WeChatInstance getInstance() {
		if (instance == null) {
			instance = scyTaoBaopay();
		}
		return instance;
	}

	private static synchronized WeChatInstance scyTaoBaopay() {
		if (instance == null) {
			instance = new WeChatInstance();
		}
		return instance;
	}

	@Override
	public void init(Context context, Object... parameters) {
		callBHandler = (Handler) parameters[0];
	}

	@Override
	public void pay(final Context context, Object... parameters) {
		payOrder = (String) parameters[0];
		final int costMoney = (Integer) parameters[1];
		final String productName = (String) parameters[2];
		final String payway = (String) parameters[3];
		final String api = String.format(Constants.SERVER_URL, "wxpay_sign");

		HttpRequest<WeiXinOrderInfo> request = new HttpRequest<WeiXinOrderInfo>(
				context, new WeiXinOrderInfoParser(),
				new WeChatOrderInfoListener(callBHandler,context));
		try {
			JSONObject payRequest = new JSONObject();
			payRequest.put("device_id", GameSDK.getInstance().deviceId);
			payRequest.put("packet_id", GameSDK.getInstance().packetId);
			payRequest.put("payway", payway);
			payRequest.put("game_id", GameSDK.getInstance().gameId);
			payRequest.put("cp_order_id", payOrder);
			payRequest.put("total_fee", costMoney);
			payRequest.put("payname", productName);
			payRequest.put("ratio", 10);
			payRequest.put("ver", Constants.GAME_SDK_VERSION);
			request.execute(api, payRequest.toString());
		} catch (Exception ex) {
			ex.printStackTrace();
			callBHandler.sendEmptyMessage(PayConfig.WECHAT_PAY_FAIL);
		}
	}
}
