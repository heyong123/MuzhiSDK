package mobi.zty.pay.sdk.vfubao;

import java.util.Map;

import com.vsofo.smspay.IPayResultCallback;
import com.vsofo.smspay.VsofoSmsApi;

import mobi.zty.pay.sdk.PayConfig;
import mobi.zty.pay.sdk.PayResultInfo;
import mobi.zty.pay.sdk.PaymentInterf;
import mobi.zty.sdk.game.bean.FeeInfo;
import mobi.zty.sdk.util.Helper;
import mobi.zty.sdk.util.Util_G;
import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

/**
 * 盈华讯方V付宝 支付管理类
 * @author Administrator
 *
 */
public class VFuBaoPayInstance extends PaymentInterf{
	private static VFuBaoPayInstance instance;
	private Handler callBHandler = null;
	private String appid = ""; 
	
	public static VFuBaoPayInstance getInstance(){
		if(instance==null){
			instance = scyMMpay();
		}
		return instance;
	}
	private static synchronized VFuBaoPayInstance scyMMpay(){
		if(instance==null){
			instance =  new VFuBaoPayInstance();
		}
		return instance;
	}
	@Override
	public void init(Context context, Object... parameters) {
		callBHandler = (Handler) parameters[0];
	}

	@Override
	public void pay(final Context context, Object... parameters) {
		callBHandler.removeMessages(PayConfig.NATIVE_FEE_FAIL);
		FeeInfo feeInfo = (FeeInfo) parameters[0];
		if(feeInfo==null||feeInfo.sdkPayInfo==null){
			notiFyResult(PayConfig.SDK_INFO_NULL, "后台没给计费点","");
			Helper.sendPayMessageToServer(PayConfig.VFUBAO_PAY, "后台没给计费点", "0000000000");
			return;
		}
		final String orderid = feeInfo.orderId;   //订单号
		
		String mz = feeInfo.consume+"";//面值
		String uid = Helper.getIMSI(context);//当前用户id
		String spid = feeInfo.sdkPayInfo.spIdentify;//商户id
		String url = feeInfo.sdkPayInfo.payUrl1;
		String sppwd = "de9018bce2b2434b91";
		String md5 = Helper.md5(spid+orderid+sppwd+mz).toUpperCase();//根据文档说明生成md5加密串
		
		String spurl = url//服务端地址
				+ "?mz=" + mz
				+ "&md5=" + md5
				+ "&uid=" + uid
				+ "&oid=" + orderid
				+ "&spid=" + spid;
		String wzm = feeInfo.feeName;//商户自定义商品名称
		Util_G.debugE("VFuBaoPay-->",spurl);
		Helper.sendPayMessageToServer(PayConfig.VFUBAO_PAY, "开始V付宝支付请求", orderid);
		VsofoSmsApi.startPay((Activity) context, spurl, wzm, new IPayResultCallback() {
			
			@Override
			public void onPayResult(int resultCode, String description, String orderNumber) {
				// TODO Auto-generated method stub
				// 状态码
				// 100 提示内容：支付请求已提交，请返回账户查询。
				// 101 失败提示内容：根据SDK返回文本提示。
				// 102手动操作提示内容：请到收件箱接收短信，并根据短信回复确认内容完成支付。
				// 109提示内容：用户退出支付。（未支付就退出）
				Util_G.debugE("onResult", "状态码：" + resultCode);
				Util_G.debugE("onResult", "数据说明：" + description);
				Util_G.debugE("onResult", "订单号：" + orderNumber);
				PayResultInfo info = new PayResultInfo();
				info.orderId = orderid;
				Helper.sendPayMessageToServer(PayConfig.VFUBAO_PAY, "V付宝支付请求中。。。。", orderid);
				if (100 == resultCode) {
					info.resutCode = PayConfig.BIIL_SUCC;
					info.retMsg = "支付成功！";
					notiFyResult(info.resutCode, info.retMsg, info.orderId);
					// 处理该订单的业务逻辑。
					Helper.sendPayMessageToServer(PayConfig.VFUBAO_PAY, "V付宝支付请求成功", orderid);
				}else if (109 == resultCode) {
					// 处理该订单的业务逻辑。
					info.resutCode = PayConfig.SP_PAY_FAIL;
					info.retMsg = "支付取消";
					notiFyResult(info.resutCode, info.retMsg, info.orderId);
					Helper.sendPayMessageToServer(PayConfig.VFUBAO_PAY, "V付宝支付失败：用户退出支付："+resultCode, orderid);
				} else{
					info.resutCode = PayConfig.SP_PAY_FAIL;
					info.retMsg = "支付失败";
					notiFyResult(info.resutCode, info.retMsg, info.orderId);
					// 处理该订单的业务逻辑。
					Helper.sendPayMessageToServer(PayConfig.VFUBAO_PAY, "V付宝支付请求失败,状态说明："+resultCode, orderid);
				} 
			}
		});
	}
	private void notiFyResult(int resutCode,String retMsg,String orderId) {
		PayResultInfo info = new PayResultInfo();
		info.resutCode = resutCode;
		info.retMsg = retMsg;
		info.orderId = orderId;
		Message message = callBHandler.obtainMessage(PayConfig.NOTIFY_PAYRESULT);
		message.obj = info;
		message.sendToTarget();
	}
	
}
