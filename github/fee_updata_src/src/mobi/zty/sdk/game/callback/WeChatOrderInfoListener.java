package mobi.zty.sdk.game.callback;

import mobi.zty.pay.sdk.PayConfig;
import mobi.zty.pay.sdk.wecaht.WeChatInstance;
import mobi.zty.sdk.game.bean.WeiXinOrderInfo;
import mobi.zty.sdk.http.HttpCallback;
import mobi.zty.sdk.util.StringUtil;
import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.ipaynow.plugin.api.IpaynowPlugin;

public class WeChatOrderInfoListener implements HttpCallback<WeiXinOrderInfo> {

	private Context mContext;
	private Handler callBackHandler;
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			String result = (String) msg.obj;
//			String result = "appId=1408709961320306&consumerId=456123&consumerName=test&mhtCharset=UTF-8&mhtCurrencyType=156&mhtOrderAmt=1&mhtOrderDetail=Buy PPTV Vip Privilege&mhtOrderName=PPTV_VIP&mhtOrderNo=20150821181629&mhtOrderStartTime=20150821181624&mhtOrderTimeOut=3600&mhtOrderType=01&mhtReserved=test&notifyUrl=http://localhost:10802/&payChannelType=13&mhtSignature=c98a96bd8fedaaea66afd05c17f211e3&mhtSignType=MD5";
			IpaynowPlugin.pay((Activity) mContext,result);
		};
	};
	public WeChatOrderInfoListener(Handler handler ,Context context){
		mContext = context;
		this.callBackHandler = handler;
	}
	
	@Override
	public void onSuccess(WeiXinOrderInfo object) {
		String tn = object.getOrderInfo();
		if (StringUtil.isEmpty(tn)) {
			callBackHandler.sendEmptyMessage(PayConfig.WECAHT_FAIL);
		} else {
			goto_url(tn);
		}
		
	}

	@Override
	public void onFailure(int errorCode, String errorMessage) {
		callBackHandler.sendEmptyMessage(PayConfig.WECAHT_FAIL);
	}

	public void goto_url(String result) {
		Message message = handler.obtainMessage(1);
		message.obj = result;
		message.sendToTarget();
	}
}
