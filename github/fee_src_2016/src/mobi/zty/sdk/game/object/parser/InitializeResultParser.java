package mobi.zty.sdk.game.object.parser;

import mobi.zty.sdk.game.bean.InitializeResult;
import mobi.zty.sdk.http.ResponseParser;

import org.json.JSONException;
import org.json.JSONObject;

public class InitializeResultParser implements ResponseParser<InitializeResult> {
    @Override
    public InitializeResult getResponse(String response) {

        try {
            JSONObject object = new JSONObject(response);
            return new InitializeResult(object.getString("device_id"));
        } catch (JSONException ex) {
            return null;
        }

    }
}
