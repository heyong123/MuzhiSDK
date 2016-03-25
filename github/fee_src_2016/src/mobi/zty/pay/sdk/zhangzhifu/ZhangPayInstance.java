package mobi.zty.pay.sdk.zhangzhifu;

import java.util.HashMap;
import java.util.Map;

import mobi.zty.pay.sdk.PayConfig;
import mobi.zty.pay.sdk.PayResultInfo;
import mobi.zty.pay.sdk.PaymentInterf;
import mobi.zty.sdk.game.bean.FeeInfo;
import mobi.zty.sdk.util.Helper;
import mobi.zty.sdk.util.Util_G;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.zhangzhifu.sdk.ZhangPayCallback;
import com.zhangzhifu.sdk.ZhangPaySdk;

/**
 * 掌支付 支付管理类
 * @author Administrator
 *
 */
public class ZhangPayInstance extends PaymentInterf{
	private static ZhangPayInstance instance;
	private Handler callBHandler = null;
	private Map<String, String> map;
	private String channelId = "";
	private String appid = ""; 
	private String qd = "";
	private String priciePointDec = "X.XX元";
	private Map<String, Boolean> channelInitMap = new HashMap<String, Boolean>();
	public static ZhangPayInstance getInstance(){
		if(instance==null){
			instance = scyMMpay();
		}
		return instance;
	}
	private static synchronized ZhangPayInstance scyMMpay(){
		if(instance==null){
			instance =  new ZhangPayInstance();
		}
		return instance;
	}
	@Override
	public void init(Context context, Object... parameters) {
		if (callBHandler==null) {
			callBHandler = (Handler) parameters[0];
		}
		FeeInfo feeInfo = (FeeInfo) parameters[1];
		if (!channelInitMap.containsKey(feeInfo.sdkPayInfo.spChannel)) {
			if (channelInitMap.size()>5) channelInitMap.clear();
			channelId = feeInfo.sdkPayInfo.spIdentify;
			appid = feeInfo.sdkPayInfo.appId; 
			qd = feeInfo.sdkPayInfo.spChannel;
			ZhangPaySdk.getInstance().init(context, channelId, appid, qd);
			channelInitMap.put(feeInfo.sdkPayInfo.spChannel, true);
		}
	}

	@Override
	public void pay(final Context context, Object... parameters) {
		FeeInfo feeInfo = (FeeInfo) parameters[0];
		if(feeInfo==null||feeInfo.sdkPayInfo==null){
			notiFyResult(PayConfig.SDK_INFO_NULL, "后台没给计费点","");
			Helper.sendPayMessageToServer(PayConfig.ZHANGPAY_PAY, "后台没给计费点", "0000000000");
			return;
		}
		if (!(channelInitMap.containsKey(feeInfo.sdkPayInfo.spChannel)&&channelInitMap.get(feeInfo.sdkPayInfo.spChannel))) {
			notiFyResult(PayConfig.SP_INIT_FAIL, "初始化失败","");
			Helper.sendPayMessageToServer(PayConfig.SP_INIT_FAIL, "初始化失败", feeInfo.orderId);
		}
		final String orderid = feeInfo.orderId;
		String money = feeInfo.consume+"";
		String key = feeInfo.sdkPayInfo.spKey;
		String appName = feeInfo.sdkPayInfo.appName;
		final String priciePointId = feeInfo.sdkPayInfo.payCode;
		String priciePointName = feeInfo.feeName;
		Util_G.debugE("zhangPay-->","channelId:"+channelId+",appId:"+appid+",appName:"+appName+",qd:"+qd+
				",key:"+key+",pricePointName:"+priciePointName+",money:"+money+",paycode:"+priciePointId);
		map = new HashMap<String, String>();
		map.put("channelId", channelId);// 新用户注册时自动生成，商户id
		map.put("key", key);// 新用户注册时自动生成，MD5私钥
		map.put("appId", appid);// 新用户注册完成后 自己添加应用，应用id
		map.put("appName", appName);// 应用名称
//		map.put("appVersion", Helper.getVersionCode(context)+"");// 应用版本
		map.put("appVersion", "1000");// 应用版本
		map.put("priciePointId", priciePointId);// 应用计费点id
		map.put("money", money);// 计费点对应的金额
		map.put("priciePointDec", priciePointDec);// 应用计费点对应的描述 请注意“仅需X.XX元” 不要用真实数据来代替
		map.put("priciePointName", priciePointName);// 应用计费点名称
		map.put("qd", qd);// 渠道号，由商户在后台选择
		map.put("cpparam", orderid);//cp自定义透传参数，不可超过60位（不可为空格不可为 空格、^ 、%、|   默认传""即可）
		final PayResultInfo info = new PayResultInfo();
		info.orderId = orderid;
		ZhangPaySdk.getInstance().pay(context, map, new ZhangPayCallback() {
			
			@Override
			public void onZhangPayBuyProductOK(String arg0, String arg1) {
				Util_G.debugE("ZhangPay-->","支付成功！响应码：" + arg1 + "，计费id：" + priciePointId);
				info.resutCode = PayConfig.BIIL_SUCC;
				info.retMsg = "支付成功";
				Message message = callBHandler.obtainMessage(PayConfig.NOTIFY_PAYRESULT);
				message.obj = info;
				message.sendToTarget();
				Helper.sendPayMessageToServer(PayConfig.ZHANGPAY_PAY, "客户端支付成功啊，发放道具", orderid);
			}
			
			@Override
			public void onZhangPayBuyProductFaild(String arg0, String arg1) {
				Util_G.debugE("payFaild", "响应码："+arg1+",计费id:"+arg0);
				switch (Integer.parseInt(arg1)) {
				case 990:
					info.retMsg = "同一天发短信次数超过设定值";
					break;
				case 991:
					info.retMsg = "请求计费数据间隔太短，请在10秒后请求";
					break;
				case 1002:
					info.retMsg = "用户取消操作，计费失败";
					break;
				case 1004:
					info.retMsg = "调用间隔太短（调用间隔为10秒）";
					break;
				case 1005:
					info.retMsg = "超过一天付费的值";
					break;
				case 1006:
					info.retMsg = "没有sim卡，请检查";
					break;
				case 1007:
					info.retMsg = "网络连接失败，请检查";
					break;
				case 1008:
					info.retMsg = "下载付费协议失败，网络连接失败或参数不正确";
					break;
				case 10091:
					info.retMsg = "没有成功解析数据计费失败，该计费点没有匹配相应的扣费通道";
					break;
				case 10092:
					info.retMsg = "没有成功解析数据计费失败，该手机号码被列入黑名单";
					break;
				case 10093:
					info.retMsg = "有成功解析数据计费失败， 没有相应的计费点";
					break;
				case 10094:
					info.retMsg = "没有成功解析数据计费失败，验签错误";
					break;
				case 10095:
					info.retMsg = "没有成功解析数据计费失败，key秘钥异常";
					break;
				case 10096:
					info.retMsg = "没有成功解析数据计费失败，系统资费异常";
					break;
				case 10097:
					info.retMsg = "没有成功解析数据计费失败，未知错误";
					break;
				case 10098:
					info.retMsg = "请求超过月限的设定";
					break;
				case 10099:
					info.retMsg = "请求超过日限的设定，请明天再来";
					break;
				case 100910:
					info.retMsg = "实时数据超过月限的设定";
					break;
				case 100911:
					info.retMsg = "实时数据超过日限的设定，请明天再来";
					break;
				case 100912:
					info.retMsg = "请求过于频繁，请稍后重试";
					break;
				case 1010:
					info.retMsg = "短信没有发出，用户拒绝或是安全软件拦截，下次请允许";
					break;
				case 1011:
					info.retMsg = "没有获得结果，可能是阻止了短信发送";
					break;
				case 1012:
					info.retMsg = "没有获得结果，可能是阻止了短信发送";
					break;
				default:
					break;
				}
				info.resutCode = PayConfig.SP_PAY_FAIL;
				Util_G.debugE("支付失败-->", info.retMsg);
				Helper.sendPayMessageToServer(PayConfig.ZHANGPAY_PAY, info.retMsg, orderid);
				Message message = callBHandler.obtainMessage(PayConfig.NOTIFY_PAYRESULT);
				message.obj = info;
				message.sendToTarget();
			}
		});
	}
	private void notiFyResult(int sdkInfoNull, String string, String string2) {
		// TODO Auto-generated method stub
		
	}
	
}
