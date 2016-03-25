package mobi.zty.sdk.game.object.parser;

import mobi.zty.sdk.game.bean.MmpayOrderInfo;
import mobi.zty.sdk.http.ResponseParser;

import org.json.JSONException;
import org.json.JSONObject;

public class MmpayOrderInfoParser  implements ResponseParser<MmpayOrderInfo> {
    @Override
    
    public MmpayOrderInfo getResponse(String response) {

        try {

            JSONObject object = new JSONObject(response);
            MmpayOrderInfo orderInfo = new MmpayOrderInfo();
            orderInfo.setRet(object.getInt("ret"));
            orderInfo.setOrderNo(object.getString("order_no"));
            
            return orderInfo;

        } catch (JSONException ex) {
            return null;
        }

    }

}
