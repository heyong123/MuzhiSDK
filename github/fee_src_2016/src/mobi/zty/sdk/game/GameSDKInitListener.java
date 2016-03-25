package mobi.zty.sdk.game;

public interface GameSDKInitListener {
	/**
	 * @param openDark 2模糊描述加粗1是打开支付模糊描述 0正常状态不变
	 * @param openAlert 1:主动支付弹框打开 0主动弹框关闭
	 * @param openButton 0：按钮购买按钮 1：确认按钮 2：领取按钮
	 * @param openOurAlert 0：关闭二次确认框 1：打开二次确认框
	 * @param cootype 0:线上弹框逻辑 1线下
	 * @param cootype 0:关闭坑人模式 1打开坑人模式
	 */
	public void onOpenDark(int openDark,int openAlert,int openButton,int openOurAlert, int cootype,int deceptive);
}
