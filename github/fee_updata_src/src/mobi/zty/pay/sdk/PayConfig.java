package mobi.zty.pay.sdk;

public class PayConfig {
	/**
	 * 正在支付中
	 */
	public static boolean IS_PAYING = false;
	
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
	 * 明天 支付类型
	 */
	public static final int TOMOEEOW_PAY = 17;
	
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
	 * 明天动力 DDO通道支付
	 */
	public static final int POWER_PAY = 21;
	
	/**
	 * 奇葩 sdk支付
	 */
	public static final int QIPA_PAY = 22;
	/**
	 * 易接 sdk支付
	 */
	public static final int YIJIE_PAY = 23;
	
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
	 * 明天
	 */
	public static final int TOMORROW_PAY = 34;
	/**
	 * 电信爱城市（博士通电信）
	 */
	public static final int BOSHITONG_PAY = 35;
	/**
	 * 电信爱城市（博士通电信）
	 */
	public static final int VFUBAO_PAY = 36;
	
	/**
	 * mm初始化失败
	 */
	public static final int MM_INIT_FAIL = 1000;
	/**
	 * mm计费点不存在
	 */
	public static final int MM_CODE_ERRO = 1005;
	/**
	 * mm支付成功
	 */
	public static final int BIIL_SUCC = 1010;
	/**
	 * mm支付取消
	 */
	public static final int BIIL_CANCER = 1015;
	/**
	 * mm支付失败
	 */
	public static final int MM_BIIL_FAIL = 1020;

	/**
	 * 支付失败转支付宝
	 */
	public static final int FIAL_TO_OTHER_PAY = 1030;
	
	/**
	 * 电信初始化失败
	 */
	public static final int TC_INIT_FAIL = 2000;
	
	/**
	 * 电信计费点不存在
	 */
	public static final int TC_CODE_ERRO = 2010;
	
	/**
	 * 电信支付成功
	 */
	public static final int TC_PAY_SUCC = 2020;
	
	/**
	 * 电信支付取消
	 */
	public static final int TC_PAY_CANCER = 2030;
	
	/**
	 * 电信支付失败
	 */
	public static final int TC_PAY_FAIL = 2040;
	

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
	 * cp所传的索引 和后台配置的商品列表不匹配
	 */
	public static final int CP_INDEX_FAIL = 4220;
	/**
	 * 额度不够
	 */
	public static final int LIMIT_FAIL = 4500;
	
	/**
	 *服务端给的数据 造成客户端初始化失败
	 */
	public static final int DATA_INIT_FAIL = 5010; 
	/**
	 *后台未配置mk 列表数据
	 */
	public static final int MK_SET_FAIL = 5011; 
	
	/**
	 *mk列表中未找到 改支付类型
	 */
	public static final int FEE_MK_FAIL = 5012; 
	
	/**
	 *客户端 未联网或者网络差
	 */
	public static final int CONNECT_FAIL = 5015; 
	
	/**
	 *sdk不支持该类型支付
	 */
	public static final int NO_SDK_PAY = 5020; 
	

	/**
	 *时间间隔未到 不让进行本套支付
	 */
	public static final int DALAY_LIMIT_PAY = 5025; 
	/**
	 *含有回调的计费方式，超时视为成功 实际没有成功
	 */
	public static final int YIMEROUT_SCC_PAY = 5030; 
	/**
	 *没有号码 直接走下一套支付
	 */
	public static final int NO_PHONE_NUMBEL = 5045; 
	
	/**
	 * 微信支付失败
	 */
	public static final int WECHAT_PAY_FAIL = 6015;
	
	public  final static int WECAHT_SUCC = 6020;
	public  final static int WECAHT_FAIL = 6025;
	
	/**
	 * 明天支付失败
	 */
	public  final static int TOMORROW_FAIL = 6050;
	
	/**
	 * 不支持该计费点（后台人员配置不让走此计费）
	 */
	public  final static int NO_CODE_FAIL = 6075;
	
	/**
	 * 该拼接组合没有任何计费可用
	 */
	public  final static int NO_FEE_FAIL = 6085;
	
}
