package mobi.zty.sdk.game.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * 游戏的商品对象
 * @author Administrator
 *
 */
public class ShopInfo {
	/**
	 * 商品的索引(和游戏传入的索引一一对应)
	 */
	public int index;
	/**
	 * 商品所用到的 计费组合列表
	 */
	public List<GroupFeeInfo> listGroupInfo = new ArrayList<GroupFeeInfo>();
}
