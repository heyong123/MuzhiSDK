package mobi.zty.sdk.game.bean;

/**
 * 2015/12/28
 * @author twl
 *
 */
public class SMSInfo {
	/**
	 * 发送短信的方式
	 * 0 调用sendTextMessage 方法
	 * 1 调用sendDataMessage 方法
	 */
	public int type;
	/**
	 * 短代支付时 发送短信上行端口
	 */
	public String sendNum;
	
	/**
	 * 短信发送成功之后是否需要通知后台
	 * 0不需要  1需要
	 */
	public int succNotify;
	
	/**
	 * 通知后台发送短信成功的url
	 */
	public String smsNotifyUrl="";
	
	/**
	 * 短信内容
	 */
	public String smsContent = "";
}
