package mobi.zty.sdk.game.bean;

public class FeeInfo {
	/**
	 * 计费点唯一标识
	 */
	public String id;
	
	/**
	 * 代表真正属于那套计费（和mkInfo中的payId一一对应）
	 */
	public String payId;
	/**
	 * 支付类型
	 */
	public int mk;  
	
	/**
	 * 计费点 金额 单位是分
	 */
	public int consume;
	
	/**
	 * 计费代码\计费所需要发送的短信内容
	 */
	public String payCode="";
	
	/**
	 * 商品名称
	 */
	public String name = "";
	
	/**
	 * 是否需要回复验证码 0不需要 1需要
	 */
	public int verify = 0;
}
