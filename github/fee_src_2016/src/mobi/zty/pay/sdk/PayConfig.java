package mobi.zty.pay.sdk;


public class PayConfig {
	/**
	 * 支付通知总接口 消息号
	 */
	public static final int NOTIFY_PAYRESULT = 0;
	/**
	 * 移动手机
	 */
	public static final int CMCC_MOBLE = 1;
	
	/**
	 * 不支持的运营商
	 */
	public static final int NO_THIS_PAY = 15;

	/**
	 * 移动MM安安破解
	 */
	public static final int ANAN_FEE = 4;
	
	/**
	 * 移动MM乐游破解
	 */
	public static final int LEYOU_FEE =5;
	
	/**
	 * 联通手机
	 */
	public static final int UNICOM_MOBLE = 5;
	
	/**
	 * 十分科技 联通wo+破解
	 */
	public static final int UNICOM_FEE =6;
	/**
	 * 电信手机
	 */
	public static final int TIANYI_MOBLE = 10;
	
	/**
	 * * 接入天翼空间的破解支付
	 */
	public static final int TC_FEE = 7;
	
	
	/**
	 * 电信全网支付
	 */
	public static final int TC_ALL_FEE = 9;
	
	/**页游
	 */
	public static final int YEYOU_FEE = 11;
	
	/**
	 * 乐动支付(线下暂不接入)
	 */
	public static final int LEDONG_PAY = 12;
	
	/**
	 * 饭盒支付
	 */
	public static final int FANHE_PAY = 13;
	/**
	 * 微信支付类型
	 */
	public static final int WECHAT_PAY = 16;
	/**
	 * 东风 支付类型（支付 后台必须给  需要拦截验证码中的内容）
	 */
	public static final int DONGFENG_PAY = 18;
	/**
	 * 易迅支付类型
	 */
	public static final int YIXUN_PAY = 19;
	/**
	 * 动漫包月
	 */
	public static final int DONGMA_PAY = 20;
	
	/**
	 * 奇葩 sdk支付
	 */
	public static final int QIPA_PAY = 22;
	
	/**
	 * 动漫包月2
	 */
	public static final int DONGMA_MONTH_PAY = 24;
	
	/**
	 * 圆林支付
	 */
	public static final int YUANLIN_PAY = 25;

	/**
	 * 中科支付
	 */
	public static final int ZHONGKE_PAY = 26;
	
	/**
	 * 北青支付
	 */
	public static final int BEIQING_PAY = 27;
	
	/**
	 * 天翼空间包月支付
	 */
	public static final int TIANYI_MONTH_PAY = 28;
	
	/**
	 * 联通沃阅读支付
	 */
	public static final int WOYUEDU_PAY = 29;
	
	/**
	 * 动漫DDO支付
	 */
	public static final int DONGMANDDO_PAY = 30;
	/**
	 * 福多多音乐包月支付
	 */
	public static final int FUDUODUO_PAY = 31;
	/**
	 * 福多多RDO支付
	 */
	public static final int RDO_PAY = 32;
	/**
	 * 掌支付
	 */
	public static final int ZHANGPAY_PAY = 33;
	/**
	 * 冰峰谷支付
	 */
	public static final int HEJU_PAY = 42;
	
	/**
	 * 盈华讯方
	 */
	public static final int VFUBAO_PAY = 36;
	
	/**
	 * 服务端支撑的 可动态更新的所有支付 对应的class key
	 */
	public static final int GENERA_KEY = 35;
	/**
	 * 服务端支撑的 可动态更新的所有支付 对应的class key
	 */
	public static final int ZHANGLONG_PAY = 39;
	
	/**
	 * 支付成功
	 */
	public static final int BIIL_SUCC = 1010;
	
	/**
	 * 支付超时后 用来判断结果    
	 * 
	 */
	public static final int NATIVE_FEE_FAIL = 4000;
	
	/**
	 * 短信发送失败
	 */
	public static final int SEND_MSG_FAIL = 4001;
	
	/**
	 * 直接发货
	 */
	public static final int GIVE_ITEM_BEFIN = 4003;
	
	/**
	 * 重复点击支付 取消
	 */
	public static final int PAY_REPEAT_CANER= 4005;
	
	/**
	 * 客户端代码支付报错
	 */
	public static final int PAY_EXPTION_FAIL = 4200;
	
	/**
	 *代码提供方 sdk执行支付逻辑时 抛异常
	 */
	public static final int SP_PAY_EXPTION = 4201;
	
	/**
	 * 代码提供方 sdk初始化失败(可能是后台参数配置错误导致)
	 */
	public static final int SP_INIT_FAIL = 4205;
	
	/**
	 * 代码提供方 sdk支付回调 告知失败
	 */
	public static final int SP_PAY_FAIL = 4210;
	
	/**
	 * 本地sdk初始化 未成功
	 */
	public static final int SDK_INIT_FAIL = 5010;
	
	/**
	 *含有回调的计费方式，超时视为成功 实际没有成功
	 */
	public static final int YIMEROUT_SCC_PAY = 5030; 
	/**
	 * 微信支付失败
	 */
	public static final int WECHAT_PAY_FAIL = 6015;
	
	public  final static int WECAHT_SUCC = 6020;
	public  final static int WECAHT_CANCER = 6021;
	public  final static int WECAHT_FAIL = 6025;
	
	/**
	 * 后台未返回 任何可用计费
	 */
	public  final static int NO_FEE_FAIL = 10005;
	
	/**
	 * 后台返回的 计费组合 某个计费点 为空
	 */
	public  final static int FEE_ISNULL = 10010;
	
	/**
	 * 后台返回计费响应，整体解析失败不能往下执行支付
	 */
	public  final static int FEE_RESPONSE_FAIL= 10015;
	
	/**
	 *解析后台 响应回来的计费信息 报异常
	 */
	public  final static int FEE_RESPONSE_EXCEPTION= 10018;
	
	/**
	 * 服务端告知 前端执行发送短信，可未给对应的短信信息
	 */
	public final static int SMS_INFO_NULL = 10020;
	
	/**
	 *后台未给 本地支付所需的  计费信息
	 */
	public final static int SDK_INFO_NULL = 10025;
	
	/**
	 *后台给的计费信息 前端解析 出现问题 最后未拿到可用计费
	 */
	public final static int FEE_PARSE_FAIL = 10030;
	
}
