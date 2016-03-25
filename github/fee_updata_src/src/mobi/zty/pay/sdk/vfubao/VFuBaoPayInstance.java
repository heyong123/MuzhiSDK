package mobi.zty.pay.sdk.vfubao;

import java.util.Map;

import mobi.zty.pay.sdk.PayConfig;
import mobi.zty.pay.sdk.PayResultInfo;
import mobi.zty.pay.sdk.PaymentInterf;
import mobi.zty.sdk.game.bean.FeeInfo;
import mobi.zty.sdk.game.bean.MkInfo;
import mobi.zty.sdk.util.Helper;
import mobi.zty.sdk.util.Util_G;

import com.vsofo.vsofopay.IPayResultCallback;
import com.vsofo.vsofopay.SDKAPI;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;

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
		final String orderid = (String) parameters[0];   //订单号
		final int feeIndex = (Integer) parameters[1];
		FeeInfo feeInfo = (FeeInfo) parameters[2];
		MkInfo mkInfo = (MkInfo) parameters[3];
		
		String mz = feeInfo.consume+"";//面值
		String uid = Helper.getIMSI(context);//当前用户id
		String spid = mkInfo.spIdentify;//商户id
		String url = mkInfo.payUrl1;
		String sppwd = "de9018bce2b2434b91";
		String ip = Helper.getIpAddress(context);
		String md5 = Helper.md5(spid+orderid+sppwd+mz+uid+ip);//根据文档说明生成md5加密串
		
		String spurl = url//服务端地址
				+ "?mz=" + mz
				+ "&md5=" + md5
				+ "&uid=" + uid
				+ "&oid=" + orderid
				+ "&spid=" + spid;
		String wzm = feeInfo.name;//商户自定义商品名称
		Util_G.debugE("VFuBaoPay-->",spurl);
		Helper.sendPayMessageToServer(PayConfig.VFUBAO_PAY, "开始V付宝支付请求", feeIndex);
		SDKAPI.startPay((Activity) context, spurl, wzm, new IPayResultCallback() {
			
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
				Helper.sendPayMessageToServer(PayConfig.VFUBAO_PAY, "V付宝支付请求中。。。。", feeIndex);
				if (100 == resultCode) {
					info.resutCode = PayConfig.BIIL_SUCC;
					info.retMsg = "提交验证码成功！";
					Helper.sendPayMessageToServer(PayConfig.RDO_PAY, "支付成功", feeIndex);
					// 处理该订单的业务逻辑。
					Helper.sendPayMessageToServer(PayConfig.VFUBAO_PAY, "V付宝支付请求成功", feeIndex);
				} else if (101 == resultCode) {
					info.resutCode = PayConfig.SP_PAY_FAIL;
					info.retMsg = "提交验证码失败";
					Helper.sendPayMessageToServer(PayConfig.RDO_PAY, "提交验证码失败,支付失败", feeIndex);
					// 处理该订单的业务逻辑。
					Helper.sendPayMessageToServer(PayConfig.VFUBAO_PAY, "V付宝支付请求失败,状态说明："+description, feeIndex);
				} else if (102 == resultCode) {
					// 处理该订单的业务逻辑。
				} else if (109 == resultCode) {
					// 处理该订单的业务逻辑。
					Helper.sendPayMessageToServer(PayConfig.VFUBAO_PAY, "V付宝支付失败：用户退出支付", feeIndex);
				}
			}
		});
	}
	
}
