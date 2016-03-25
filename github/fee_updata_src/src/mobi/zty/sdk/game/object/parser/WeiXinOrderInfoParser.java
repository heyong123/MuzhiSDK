package mobi.zty.sdk.game.object.parser;

import mobi.zty.sdk.game.bean.WeiXinOrderInfo;
import mobi.zty.sdk.http.ResponseParser;


public class WeiXinOrderInfoParser implements ResponseParser<WeiXinOrderInfo> {
    @Override
    public WeiXinOrderInfo getResponse(String response) {

        try {
            WeiXinOrderInfo orderInfo = new WeiXinOrderInfo();
            orderInfo.setOrderInfo(response);
            return orderInfo;

        } catch (Exception ex) {
            return null;
        }

    }
}
