package mobi.zty.sdk.game.bean;

public class SdkPayInfo {
	/**
	 * 商户的唯一标识 商户号
	 */
	public String spIdentify = "";
	
	/**
	 * 应用id
	 */
	public String appId = "";
	
	/**
	 * 支付分配的渠道号
	 */
	public String spChannel = "";
	
	/**
	 * 该套计费应用名称
	 */
	public String appName = "";
	
	/**
	 * 计费方分配的key（密钥之类的东西）
	 */
	public String spKey = "";
	
	/**
	 * 多个key的情况
	 */
	public String spKey2 = "";
	
	/**
	 * 加密的方式
	 */
	public String spSignType = "";
	
	/**
	 * 支付要用到的url
	 */
	public String payUrl1 = "";
	
	/**
	 * 支付要用到的另一个url
	 */
	public String payUrl2 = "";
	
	/**
	 * 计费点
	 */
	public String payCode = "";

	@Override
	public String toString() {
		return "SdkPayInfo [spIdentify=" + spIdentify + ", appId=" + appId
				+ ", spChannel=" + spChannel + ", appName=" + appName
				+ ", spKey=" + spKey + ", spKey2=" + spKey2 + ", spSignType="
				+ spSignType + ", payUrl1=" + payUrl1 + ", payUrl2=" + payUrl2
				+ ", payCode=" + payCode + "]";
	}
	
	
}
