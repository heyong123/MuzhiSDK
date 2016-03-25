package mobi.zty.sdk.game.bean;

import java.util.ArrayList;
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

	private ArrayList<FeeInfo> list = new ArrayList<FeeInfo>();
	private Map<Integer, FeeInfo> mmActivateInfos = new HashMap<Integer, FeeInfo>();
	/**
	 * 手机号码
	 */
	private String phoneNum;
	/**
	 * 获取手机号码的端口
	 */
	private String obtainNum;

	private long intervalPayDaley;
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
	 * 是否需要获取联通手机号码，后台下发指令
	 */
	private int obtain_num_mark = 0;
	private int sureButton = 0;

	public int getObtain_num_mark() {
		return obtain_num_mark;
	}

	public void setObtain_num_mark(int obtain_num_mark) {
		this.obtain_num_mark = obtain_num_mark;
	}


	public int getSureButton() {
		return sureButton;
	}

	public void setSureButton(int sureButton) {
		this.sureButton = sureButton;
	}


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

	public long getIntervalPayDaley() {
		return intervalPayDaley;
	}

	public void setIntervalPayDaley(long intervalPayDaley) {
		this.intervalPayDaley = intervalPayDaley;
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

	public int getDeceptive() {
		if (cootype != 1) {// 不是线下 不开启坑人模式
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

	public Map<Integer, FeeInfo> getMmActivateInfos() {
		return mmActivateInfos;
	}

	public ArrayList<FeeInfo> getactivateFeeinfo(){
		return list;
	}
	
	public void setMmActivateInfos(JSONArray array) {
		mmActivateInfos.clear();
		list.clear();
		if (array != null && array.length() > 0) {
			for (int i = 0; i < array.length(); i++) {
				JSONObject object = Helper.getJSONObject(array, i);
				FeeInfo feeInfo = new FeeInfo();
				feeInfo.payType = Helper.getJsonInt(object, "pay_type");
				feeInfo.isGeneral = 0;
				feeInfo.sdkPayInfo = new SdkPayInfo();
				feeInfo.sdkPayInfo.appId = Helper.getJsonString(object,
						"app_id");
				feeInfo.sdkPayInfo.spKey = Helper.getJsonString(object,
						"sp_key");
				feeInfo.sdkPayInfo.spChannel = Helper.getJsonString(object,
						"sp_channel");
				this.mmActivateInfos.put(feeInfo.payType, feeInfo);
				this.list.add(feeInfo);
//				GameSDK.getInstance().initMkPay(feeInfo.payType, feeInfo);
			}
		}
	}

}
