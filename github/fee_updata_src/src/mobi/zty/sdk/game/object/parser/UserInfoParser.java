package mobi.zty.sdk.game.object.parser;

import mobi.zty.sdk.game.Constants;
import mobi.zty.sdk.game.bean.UserInfo;
import mobi.zty.sdk.http.ResponseParser;
import mobi.zty.sdk.util.Util_G;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class UserInfoParser implements ResponseParser<UserInfo> {
    @Override
    public UserInfo getResponse(String response) {

        try {

            JSONObject object = new JSONObject(response);
            UserInfo userInfo = new UserInfo();
            userInfo.setResult(object.getInt("result"));
            userInfo.setMessage(object.getString("message"));
            if (userInfo.getResult() == 1) {
//                userInfo.setUserId(object.getInt("ID"));
                userInfo.setLoginAccount(object.getString("LOGIN_ACCOUNT"));
                userInfo.setUserId(object.getInt("ACCOUNT_ID"));
                userInfo.setSign(object.getString("sign"));
                
                userInfo.setAmount(object.getInt("amount"));
                userInfo.setMax_amount(object.getInt("max_amount"));
                Util_G.debug_i("test", "amount="+object.getInt("amount"));
                Util_G.debug_i("test", "max_amount="+object.getInt("max_amount"));
            }
            return userInfo;

        } catch (JSONException ex) {
            Log.e(Constants.TAG, ex.toString());
            ex.printStackTrace();
            return null;
        }

    }
}