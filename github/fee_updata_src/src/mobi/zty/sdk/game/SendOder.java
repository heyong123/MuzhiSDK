package mobi.zty.sdk.game;

import mobi.zty.sdk.game.bean.GroupFeeInfo;
import mobi.zty.sdk.game.bean.MmpayOrderInfo;
import mobi.zty.sdk.game.object.parser.MmpayOrderInfoParser;
import mobi.zty.sdk.http.HttpCallback;
import mobi.zty.sdk.http.HttpRequest;
import mobi.zty.sdk.util.Util_G;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

public class SendOder {
	private static SendOder instance;
	/**
	 * 0发起支付  1计费成功 2通知服务端计费失败并带上错误码
	 */
	public int payState;
	public int amount; //这次支付的金额
	public String payname;
	private int payway;
	private String feeId;
	private String payId;
	private String payOrder;
	private int feeIndex;
	private String shopGroupIndex;
	/**
	 * mm支付信息上传失败次数，允许上传失败三次
	 */
	public int nettimer;
	public static String mthrirdno;
	private Context context;
	public void init(Context context) {
		this.context =  context;
	}
	public static SendOder getInstance(){
		
		if (instance == null) {
			instance = sycOder();
		}
		return instance;
	}
	private static synchronized SendOder sycOder(){
		if (instance == null) {
			instance = new SendOder();
		}
		return instance;
	}
	
	/**
	  * 
	  * @param thrirdno mm专用订单号
	  * @param payOrder 我们自己生成的订单号
	  * @param state 订单的状态 0 未支付成功 1支付成功 2代表通知给服务端成功
	  */
	 public void sapay_ret(String thrirdno,int state,int index)
	 {
		 mthrirdno = thrirdno;
		 sapay_ret(state,index);
	 }
	 
	/**
	  *  只有破解支付调用这个方法
	  * @param state 
	  */
	 public void sapay_ret(int state,int index)
	 {	 
		 this.payState = state;
		 sapayOrder(state,0,index);
	  }
	 
	 /**
	  * 向服务端发送错误码
	  * @param erroCode
	  */
	 public void notifyErrorCode(int erroCode,int index)
	 {	 
		 sapayOrder(this.payState,erroCode,index);
	 }
	 
	 
	 public synchronized void sapayOrder(int state,int erroCode,int index)
	 {
		 if (GameSDK.getInstance().indexOrderMap.containsKey(index)) {
			 nettimer ++;   
			 this.payOrder = GameSDK.getInstance().indexOrderMap.get(index);
			 GroupFeeInfo groupFeeInfo = GameSDK.getInstance().getCurgGroupFeeInfo();
			 if (groupFeeInfo!=null&&groupFeeInfo.feeInfos.size()>index) {
				 this.payway = groupFeeInfo.feeInfos.get(index).mk;
				 this.feeId = groupFeeInfo.feeInfos.get(index).id;
				 this.payId = groupFeeInfo.feeInfos.get(index).payId;
			 }
			 this.feeIndex = index;
			 this.shopGroupIndex = GameSDK.getInstance().payindex+"_"+GameSDK.getInstance().repeatPayIndex;
		 }else{
			 return;
		 }
		 
		 MmpayOrderInfoListener infoListener = new MmpayOrderInfoListener();
		 infoListener.feeIndex = index;
		 infoListener.errorCode = erroCode;
	   	 String api = String.format(Constants.SERVER_URL, "sapay_sign");
	     HttpRequest<MmpayOrderInfo> request = new HttpRequest<MmpayOrderInfo>(context, new MmpayOrderInfoParser(),
	                infoListener);
	    
        try {
        
            JSONObject payRequest = new JSONObject();
            
            payRequest.put("total_fee", this.amount);
            payRequest.put("tradeID", mthrirdno);
            payRequest.put("cp_order_id", this.payOrder);
            payRequest.put("state", state);
            payRequest.put("shop_group_index", this.shopGroupIndex);
            payRequest.put("index", feeId);
            payRequest.put("ver", Constants.GAME_SDK_VERSION);
            if (erroCode>0) {
            	payRequest.put("error_code", erroCode);
			}
            payRequest.put("device_id", GameSDK.getInstance().deviceId);
            payRequest.put("packet_id", GameSDK.getInstance().packetId);
            payRequest.put("payway", payway);
            payRequest.put("payname", payname);
            payRequest.put("game_id", GameSDK.getInstance().gameId);
            payRequest.put("user_id", "");
//                payRequest.put("ext", GameSDK.getInstance().pc_ext);//有cp需要传自己透传参数的时候 再要
            payRequest.put("app_pay_id", this.payId);
                request.execute(
                        api, payRequest.toString());
            
            Util_G.debugE("ALLPAY", "sapay_sign====>1"+payRequest.toString());
        } catch (Exception e) {
        	e.printStackTrace();
//        	GameSDK.getInstance().makeToast("支付信息提交错误！");
        }
	  }
	 private class MmpayOrderInfoListener implements HttpCallback<MmpayOrderInfo> {
		public int feeIndex=-1;
		public int errorCode = 0;
		@Override
		public void onSuccess(MmpayOrderInfo object) {
			nettimer = 0;
			feeIndex = -1;
			errorCode=0;
		}
		@Override
		public void onFailure(int errorCode, String errorMessage) {
			// Toast.makeText(PaymentActivity.this, errorMessage,
			// Toast.LENGTH_SHORT).show();
			if (nettimer < 3&&this.feeIndex==SendOder.this.feeIndex) {
				sapayOrder(payState, errorCode, this.feeIndex);
			}else{
				nettimer = 0;
				this.feeIndex = -1;
			}
		}

	 }
	 
}
