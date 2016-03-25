package mobi.zty.sdk.game.bean;

import java.util.ArrayList;
import java.util.List;

import mobi.zty.sdk.util.StringUtil;

public class MkInfo{
	/**
	 * sp 唯一标识（lg:商户id）
	 */
	public String spIdentify="";
	
	/**
	 * 应用id\产品编号
	 */
	public String appId="";
	
	
	/**
	 * 应用名称
	 */
	public String appName="";
	
	/**
	 * sp key(lg:商户key、MM key等)
	 */
	public String spKey="";
	
	/**
	 * 该套计费属于那种计费类型
	 */
	public int mk;
	
	/**
	 * 对应支付的管理类的 类全名
	 */
	public String mkClassName;
	
	/**
	 * 该套计费的 唯一标识
	 */
	public String payId = "";
	/**
	 * 目前只有页游 有两个key
	 */
	public String spKey2="";
	
	/**
	 * 加密时 加密的类型
	 */
	public String spSignType="";
	
	/**
	 * 支付用到的 Url 1
	 */
	public String payUrl1="";
	
	/**
	 * 支付用到的 Url 2
	 */
	public String payUrl2="";
	
	/**
	 * 计费 所需的渠道号 lg： MM channel
	 */
	public String spChannel="";
	/**
	 * 短信上行端口(需要发送短信的支付 要用到)
	 */
	public String sendNum = "";
	
	/**
	 * 获取验证码 下行端口
	 */
	public String vertifyNum = "";
	
	/**
	 * 回复验证码   上行端口(有时候 收到验证码的端口 和回复的端口是不一样的)
	 */
	public String confimNum = "";
	
	/**
	 * 当本地对应 实现的类初始化成功后 设置为true
	 * false 很有可能是sdk未集成此计费类型，页游可能是初始化参数传入有误
	 */
	public boolean isCanPay = false;
	
	public long timeOut = 30000;
	/**
	 * 0关闭踩点模式 1打开 踩点模式
	 */
	public int needCount = 0;
	
	/**
	 * 拦截内容列表
	 */
	public List<String> interceptContents = new ArrayList<String>();
	public void setInterceptContens(String content){
		interceptContents.clear();
		if (!StringUtil.isEmpty(content)) {
			String[] contents = content.split("_");
			for (int i = 0; i < contents.length; i++) {
				interceptContents.add(contents[i].trim());
			}
		}
	}
}
