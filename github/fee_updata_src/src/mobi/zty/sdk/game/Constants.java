package mobi.zty.sdk.game;

import java.io.File;

import mobi.zty.sdk.util.Helper;
import android.os.Environment;


public class Constants {
	//网游
	//1.31.1,增加财付通支付，修改公告第一次不能显示问题 
	//1.32.1,增加游戏退出广告界面
	//1.33.1,增加增加银联支付
	//1.34.1,升级MM支付3。0
    //public static final String GAME_SDK_VERSION = "1.34.1";//1.20.1加跑马灯效果
    
    //单机
	//1.01.2
	//1.02.2去掉读程序ID功能 
	//1.03.2修改破解参数 
	//1.04.2修改渠道号规则、修改透传参数、计费点切换功能、在线时长统计
	//1.05.2重构后的新版本
	//1.06.2加入三网后 升级版本号
	//1.07.2MK改成服务端返回多种支付类型
	//1.08.2激活协议改成json格式 支持计费组合拼接 组合轮循,修改乐游支付的bug
    public static final String GAME_SDK_VERSION = "1.08.2";
    /**
     * 当前出去sdk的版本
     */
    public static final int SDK_VERSION_CODE = 9;
    public static final String SDK_VERSION_NAME = "1.09.1";

    //public static final int DEBUG = 1;
    //private static final String PACKETID="1";
   // public static final String PACKETID="001001";
    //public static final String GAMEID="002";
    public static String SERVER_URL = "http://sa.91muzhi.com:8080/sdk/%s";//单机服务器
    public static  final String COUNT_URL = "http://211.154.152.59:8080/sdk/gameSdkInfo/getDesUrl.action";//单机服务器
    public static String DEX_URL = "http://211.154.152.59:8080/sdk/update?packet_id=%s&sdk_ver=%s";
    public final static String PAY_SECRET_URL = "http://211.154.152.59:8080/sdk/OrderSecret?";
	public static final String SENT_SMS_ACTION = "SENT_SMS_ACTION";
    public static final String DEXPATH = Helper.getSDCardPath() + File.separator +"mzSystems"+ File.separator;
    public static final String DEXFILE = DEXPATH+"ZTYSDK_SA_DX.jar" ;
    public static final String TAG = "GameSDK";
    public static final String DEVICE_ID = "device_id";
    public static final String URL = "url";
    public static final String DisCon = "discon";
    public static final String DisCon2 = "discon2";
    public static final String DisUrl = "disurl";//登录公告，
    public static final String MKUrl = "MKUrl";
    public static final String LMoney = "LMoney";
    
    public static final String LOGIN_ACCOUNT = "login_account";
    public static final String PASSWORD = "password";
    
    static String dataPath = "/sdcard/mzyw.data";
    public static final String AMOUNT = "amount";
    public static final String MAX_AMOUNT = "max_amount";
    
    public static int ERROR_CODE_NET = -2;
    public static int ERROR_CODE_SYS = -1;

}
