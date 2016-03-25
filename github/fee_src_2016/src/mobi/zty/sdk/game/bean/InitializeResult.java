package mobi.zty.sdk.game.bean;

import java.io.Serializable;

public class InitializeResult implements  Serializable {

    public String deviceId;
   // public String url;
    public InitializeResult(String deviceId) {
        this.deviceId = deviceId;
        //this.url = url;
    }

   
    public String getDeviceId() {
        return deviceId;
    }

    @Override
    public String toString() {
        return "InitializeResult{" +
                "deviceId='" + deviceId + '\'' +
                '}';
    }

}
