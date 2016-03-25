package mobi.zty.sdk.game.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Handler;
import android.os.Looper;

/**
 * 商品计费组合对象
 * @author Administrator
 *
 */
public class GroupFeeInfo {
	private final int CUTDOWN_TIMER = 1;
	Handler handler = new Handler(Looper.getMainLooper()){
		public void handleMessage(android.os.Message msg) {
			if (msg.what == CUTDOWN_TIMER) {
				cutDownTime = 0;
			}
		};
	};
	/**
	 * 该套代码 执行的 时间间隔(秒)
	 */
	public long delay;
	
	private long cutDownTime = 0;
	/**
	 * 该组合计费 所要用到的 计费列表
	 */
	public List<FeeInfo> feeInfos = new ArrayList<FeeInfo>();
	
	/**
	 * 各个计费点 支付结果的 记录map
	 * key feeInfos 的索引
	 * vale 默认状态是 0  支付成功 1 失败2
	 */
	public Map<Integer, Integer> recodePayResult = new HashMap<Integer, Integer>(0);
	
	/**
	 * key  feeInfos 的索引
	 * value mk
	 */
	public Map<Integer, Integer> intdexMkMap = new HashMap<Integer, Integer>(0);
	/**
	 * key  feeInfos 的索引
	 * value 验证码计费步骤 0 未发送请求 1发送了请求验证码的短信 2已经回复了验证码的短信
	 */
	private Map<Integer, Integer> intdexVerifyMap = new HashMap<Integer, Integer>(0);
	
	public void setIntdexMkMap(){
		intdexMkMap.clear();
		for (int i = 0; i < feeInfos.size(); i++) {
			intdexMkMap.put(i, feeInfos.get(i).mk);
		}
	}
	/**
	 * 重置时间间隔
	 */
	public void reSetDelay() {
		this.cutDownTime = delay;
		if (delay>0) {
			handler.removeMessages(CUTDOWN_TIMER);
			handler.sendEmptyMessageDelayed(CUTDOWN_TIMER, delay*1000);
		}
	}
	/**
	 * false 该组合暂时不能试用（倒计时中）
	 * true 可以正常使用
	 * @return
	 */
	public boolean isCanUse(){
		if (this.cutDownTime<=0) {
			return true;
		}
		return false;
	}
	/**
	 * 支付取消 或者 拒绝发送短信 就马上恢复可支付状态
	 */
	public void setCancerDelay(){
		this.cutDownTime = 0;
	}
	public Map<Integer, Integer> getIntdexVerifyMap() {
		return intdexVerifyMap;
	}
	public void setIntdexVerifyMap(Map<Integer, Integer> intdexVerifyMap) {
		this.intdexVerifyMap = intdexVerifyMap;
	}
}
