package mobi.zty.sdk.game.bean;

import java.util.HashMap;
import java.util.Map;

/**
 * 道具商品 对象
 * @author Administrator
 *
 */
public class ShopInfo {
	/**
	 * 当前是第几个商品
	 */
	public int index;
	/**
	 * 每个计费的 执行的间隔
	 */
	public int delay;
	/**
	 *该商品支付超时的时长
	 */
	public int timeOut;
	
	/**
	 * 服务端给的是第几套计费
	 */
	public int groupStep;
	
	/**
	 * 该商品服务端一共有几套计费 可以论顺
	 */
	public int groupCount;
	
	/**
	 * 是否可以直接发货
	 */
	public int  directDelivery;
	
	/**
	 * 该商品的 计费点组合
	 */
	public Map<String, FeeInfo> mapFeeInfos = new HashMap<String, FeeInfo>();
}
