package mobi.zty.sdk.game.callback;

import mobi.zty.sdk.game.bean.ActivateResult;

public interface ActivateCallback {

    public void onActivateSuccess(ActivateResult activateResult);

    public void onFailure(int errorCode, String errorMessage);

}
