package mobi.zty.pay.sdk;

public class PayResultInfo {
	/**
	 * 支付返回码
	 * 1000 为初始化
	 * 1005计费点不存在
	 * 1010支付成功
	 * 1015取消支付
	 */
	public int resutCode;
	/**
	 * 如果支付成功是 mm订单号  如果失败是错误信息
	 */
	public String retMsg = "";
	
	public String orderId = "";
}
