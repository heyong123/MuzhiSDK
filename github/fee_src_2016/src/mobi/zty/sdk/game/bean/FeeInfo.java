package mobi.zty.sdk.game.bean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FeeInfo {
	/**
	 * 计费点唯一标识(订单号)
	 */
	public String orderId;
	
	/**
	 * 计费道具的名称
	 */
	public String feeName;
	/**
	 * 执行支付 开始的时间(本地赋值)
	 */
	public long startTime;
	
	/**
	 * 0 本地和sp对接的支付 1和后台对接的支付
	 */
	public int isGeneral;
	
	/**
	 * 是否需要直接发送短信 0不需要 1需要
	 */
	public int needSms =0;
	
	/**
	 * 道具金额
	 */
	public int consume = 0;
	
	/**
	 * 是否需要开启踩点
	 * 0关闭 1开启
	 */
	public int needCount;
	
	/**
	 * 是否需要读取验证码 0不需要 1需要
	 */
	public int verify;
	
	/**
	 * 热更库中对于的 类路径(以后只贵本地支付有用)
	 */
	public String mkClassName ="";
	
	/**
	 * 支付类型
	 */
	public int payType; 
	
	/**
	 * 如果是本地支撑的支付  需要用到的对象
	 */
	public SdkPayInfo sdkPayInfo;
	
	/**
	 * 需要验证码时 用到的信息对象
	 */
	public VerifyInfo verifyInfo;
	
	/**
	 * 如需要发送短信  要用到的短信对象
	 */
	public List<SMSInfo> smsInfos = new ArrayList<SMSInfo>();
	
	/**
	 * 需要拦截的 扣费短信的内容
	 */
	public String[] deleteContents = null;
	/**
	 * 支付步骤 监听短信时 2代表可以直接通知成功 
	 * 只需要发送短信的 支付 默认是2 需要发送验证码的支付 默认是0 
	 * 如果正常流程走完 其数值变为 3（上报成功）
	 * （0需要发送验证码 1 发送验证码的短信成功 2如果发送短信成功就上报并发货 3短信发送成功发货 4 支付通知回调告知失败）
	 */
	public int payStep = 2;
	@Override
	public String toString() {
		return "FeeInfo [orderId=" + orderId + ", feeName=" + feeName
				+ ", startTime=" + startTime + ", isGeneral=" + isGeneral
				+ ", needSms=" + needSms + ", consume=" + consume
				+ ", needCount=" + needCount + ", verify=" + verify
				+ ", mkClassName=" + mkClassName + ", payType=" + payType
				+ ", sdkPayInfo=" + sdkPayInfo.toString() + ", verifyInfo=" + verifyInfo
				+ ", smsInfos=" + smsInfos + ", deleteContents="
				+ Arrays.toString(deleteContents) + ", payStep=" + payStep
				+ "]";
	}
	
	
}
