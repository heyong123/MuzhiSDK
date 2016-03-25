package mobi.zty.sdk.game;

import mobi.zty.pay.sdk.PayResultInfo;

public interface GameSDKPaymentListener {
	 public void onPayFinished(int mount);
	 public void onPayCancelled();
	 public void onPayFail(PayResultInfo info);
}
