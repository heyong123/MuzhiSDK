package mobi.zty.pay.sdk;

import android.content.Context;

public abstract class PaymentInterf {

	/**
	 * 统一支付初始化接口
	 * @param context
	 * @param parameters 第一个参数 payHandler 第二个参数 FeeInFo
	 */
	public abstract void init(Context context,Object...parameters);

	/**
	 * 统一支付接口
	 * @param context
	 * @param parameters 第一个参数传  FeeInFo
	 */
	public abstract void pay(Context context,Object...parameters);
	
	/**
	 * 这个方法 只有破解的时候才有用
	 * @param parameters 第一个参数  0通知失败 1通知成功
	 * 第二个参数   orderId 订单号 
	 */
	public void notifyPay(Object... parameters){
		
		 
	}
	/**
	 * 读取到验证码后 统一调用的方法
	 * @param parameters 第一个参数 收到验证码短信的端口 第二个参数是 验证码内容 第三个参数传 计费订单号
	 */
	public void sendVerifyCode(Object... parameters){
	}

}
