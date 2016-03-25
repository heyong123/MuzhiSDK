package mobi.zty.sdk.game.callback;

import mobi.zty.sdk.game.bean.InitializeResult;

public interface InitializeCallback {

    public void onInitSuccess(InitializeResult initializeResult);

    public void onFailure(int errorCode, String errorMessage);

}
