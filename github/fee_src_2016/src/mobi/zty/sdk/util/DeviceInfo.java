package mobi.zty.sdk.util;

import mobi.zty.sdk.game.Constants;

import org.json.JSONException;
import org.json.JSONObject;

public class DeviceInfo {

    private String packageId;
    private int platform;
    private String mac;
    private String imei;
    private String imsi;
    private String model;
    private int osVersion;
    private int networkType;
    private int screenWidth;
    private int screenHeight;
    private String ip;
    
    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
    
    public String getPackageId() {
        return packageId;
    }

    public void setPackageId(String packageId) {
        this.packageId = packageId;
    }

    public int getPlatform() {
        return platform;
    }

    public void setPlatform(int platform) {
        this.platform = platform;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getImsi() {
        return imsi;
    }

    public void setImsi(String imsi) {
        this.imsi = imsi;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(int osVersion) {
        this.osVersion = osVersion;
    }

    public int getNetworkType() {
        return networkType;
    }

    public void setNetworkType(int networkType) {
        this.networkType = networkType;
    }

    public int getScreenWidth() {
        return screenWidth;
    }

    public void setScreenWidth(int screenWidth) {
        this.screenWidth = screenWidth;
    }

    public int getScreenHeight() {
        return screenHeight;
    }

    public void setScreenHeight(int screenHeight) {
        this.screenHeight = screenHeight;
    }

    public String toJSON() {

        JSONObject jso = new JSONObject();
        try {
            jso.put("packageId", packageId);
            jso.put("platform", platform);
            jso.put("mac", mac);
            jso.put("imei", imei);
            jso.put("imsi", imsi);
            jso.put("model", model);
            jso.put("osVersion", osVersion);
            jso.put("networkType", networkType);
            jso.put("screenWidth", screenWidth);
            jso.put("screenHeight", screenHeight);
            jso.put("ver", Constants.GAME_SDK_VERSION);
        } catch (JSONException ex) {
            //Ignore
        }
        return jso.toString();
    }

}
