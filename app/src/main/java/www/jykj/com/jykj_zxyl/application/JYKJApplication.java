package www.jykj.com.jykj_zxyl.application;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.IntentFilter;
import android.os.*;
import android.support.multidex.MultiDex;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.allen.library.RxHttpUtils;
import com.allen.library.config.OkHttpConfig;
import com.allen.library.cookie.store.SPCookieStore;
import com.allen.library.manage.RxUrlManager;
import com.blankj.utilcode.util.AppUtils;
import com.google.gson.Gson;
import com.hyphenate.EMCallBack;
import com.hyphenate.EMConnectionListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMOptions;
import com.hyphenate.easeui.EaseUI;
import com.hyphenate.easeui.hyhd.DemoHelper;
import com.hyphenate.easeui.hyhd.model.CallReceiver;
import com.hyphenate.easeui.utils.ExtEaseUtils;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.DefaultRefreshFooterCreator;
import com.scwang.smartrefresh.layout.api.DefaultRefreshHeaderCreator;
import com.scwang.smartrefresh.layout.api.RefreshFooter;
import com.scwang.smartrefresh.layout.api.RefreshHeader;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.footer.ClassicsFooter;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;
import com.tencent.bugly.crashreport.CrashReport;

import entity.basicDate.ProvideBasicsRegion;
import entity.basicDate.ProvideDoctorPatientUserInfo;
import entity.liveroom.CloseRoomInfo;
import entity.service.ViewSysUserDoctorInfoAndHospital;
import entity.unionInfo.ProvideUnionDoctorOrg;
import entity.user.ProvideDoctorQualification;
import entity.user.UserInfo;
import netService.HttpNetService;
import netService.entity.NetRetEntity;
import okhttp3.OkHttpClient;
import www.jykj.com.jykj_zxyl.R;
import www.jykj.com.jykj_zxyl.activity.MainActivity;

import www.jykj.com.jykj_zxyl.app_base.http.AppUrlConfig;
import www.jykj.com.jykj_zxyl.service.PushIntentService;

import com.tencent.rtmp.TXLiveBase;
import com.umeng.commonsdk.UMConfigure;
import com.umeng.message.IUmengRegisterCallback;
import com.umeng.message.PushAgent;
import com.umeng.message.UmengMessageHandler;
import com.umeng.message.entity.UMessage;

import org.android.agoo.huawei.HuaWeiRegister;
import org.android.agoo.mezu.MeizuRegister;
import org.android.agoo.oppo.OppoRegister;
import org.android.agoo.vivo.VivoRegister;
import org.android.agoo.xiaomi.MiPushRegistar;


public class JYKJApplication extends Application {
    private JYKJApplication instance;
    public boolean gRefreshDate = false;                   //客户管理中是否需要刷新行政区划数据
    public List<Activity> gActivityList = new ArrayList();
    public static Context gContext;
    public ImageLoader imageLoader = ImageLoader.getInstance();
    public DisplayImageOptions mImageOptions;                                                // DisplayImageOptions是用于设置图片显示的类
    public CallReceiver mCallReceiver;
    public SharedPreferences_DataSave m_persist;                                                //数据存储

    public UserInfo mLoginUserInfo;                         //登录需要的用户信息
    public ViewSysUserDoctorInfoAndHospital mViewSysUserDoctorInfoAndHospital;       //登录成功后服务端返回的真实用户信息
    public ProvideDoctorQualification provideDoctorQualification;
    public List<ProvideBasicsRegion> gRegionList = new ArrayList<>();                    //所有区域数据
    public List<ProvideBasicsRegion> gRegionProvideList = new ArrayList<>();            //省级区域数据
    public List<ProvideBasicsRegion> gRegionCityList = new ArrayList<>();                //市级区域数据
    public List<ProvideBasicsRegion> gRegionDistList = new ArrayList<>();                //区县级区域数据

    public String loginDoctorPosition = "20.154455^23.41548512";       //用户所处的经纬度

    public boolean isUpdateReviewUnion = false;                        //加入联盟审核信息是否更新，默认否
    public MainActivity gMainActivity;
    public boolean gNetConnectionHX;
    public boolean gNetWorkTextView = false;                       //是否显示网络状态
    public ProvideUnionDoctorOrg unionSettionChoiceOrg;                          //医生联盟，设置联盟成员联盟层级
    public Integer mMsgTimeInterval = 5;                               //轮询新消息时间，初始设置5分钟
    public String gBitMapString;                                  //身份证拍照bitmap

    private String mNetRetStr;                 //返回字符串


    private List<ProvideDoctorPatientUserInfo> mProvideDoctorPatientUserInfo = new ArrayList<>();

    public int gNewMessageNum = 0;                                  //新消息数量
    public int gMessageNum = 10;                //可发送的消息数量
    public long gVoiceTime = 20;                 //可拨打语音消息时长（单位：秒）
    public long gVedioTime = 30;                 //可拨打视频消息时长（单位：秒）
    public String curdetailcode = "";

    @SuppressLint("HandlerLeak")
    public Handler gHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if (mViewSysUserDoctorInfoAndHospital.getDoctorCode() == null)
                        mViewSysUserDoctorInfoAndHospital.setDoctorCode("");
                    if (mViewSysUserDoctorInfoAndHospital.getUserPass() == null)
                        mViewSysUserDoctorInfoAndHospital.setUserPass("");
                    //登录
                    String userAccountHuanxin = mViewSysUserDoctorInfoAndHospital.getDoctorCode();
                    EMClient.getInstance().login(userAccountHuanxin, mViewSysUserDoctorInfoAndHospital.getQrCode(), new EMCallBack() {
                        @Override
                        public void onSuccess() {

                            System.out.println("登录成功");
                            setNewsMessage();
//                            Toast.makeText(getApplicationContext(),"登录成功",Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(int i, String s) {

                            System.out.println("登录失败");
                        }

                        @Override
                        public void onProgress(int i, String s) {
                            System.out.println("正在登录");
                        }
                    });
                    break;
            }




        }
    };

    public JYKJApplication getApplication() {
        return instance;
    }
    private EMConnectionListener connectionListener;

    static final String IMTAG = "imlog";
    public void loginIM() {
        /*new Thread() {
            private EMConnectionListener connectionListener;

            public void run() {
                //注册
                try {
                    EMClient.getInstance().createAccount(mViewSysUserDoctorInfoAndHospital.getDoctorCode(), mViewSysUserDoctorInfoAndHospital.getQrCode());
                    EMClient.getInstance().addConnectionListener(connectionListener);

                    gHandler.sendEmptyMessage(1);
                } catch (HyphenateException e) {
                    e.printStackTrace();
                    gHandler.sendEmptyMessage(1);
                    System.out.println("~~~~~~~注册失败~~~~~~~~" + e.getDescription());
                }
            }
        }.start();*/
        new Thread() {
            public void run() {

                //注册
                try {
                    EMClient.getInstance().login(mViewSysUserDoctorInfoAndHospital.getDoctorCode(),mViewSysUserDoctorInfoAndHospital.getQrCode(),new EMCallBack() {
                        @Override
                        public void onSuccess() {
                            Log.d(IMTAG, "登录成功");

                            // ** manually load all local groups and conversation
                            EMClient.getInstance().groupManager().loadAllGroups();
                            EMClient.getInstance().chatManager().loadAllConversations();

                            // update current user's display name for APNs
                            boolean updatenick = EMClient.getInstance().pushManager().updatePushNickname(mViewSysUserDoctorInfoAndHospital.getUserName());
                            if (!updatenick) {
                                Log.e(IMTAG, "更新用户昵称");
                            }
                            DemoHelper.getInstance().getUserProfileManager().asyncGetCurrentUserInfo();
                            String retuser = EMClient.getInstance().getCurrentUser();
                            setNewsMessage();
                            Log.e("iis",retuser);
                        }

                        @Override
                        public void onProgress(int progress, String status) {
                            Log.d(IMTAG, "登录中...");
                        }

                        @Override
                        public void onError(final int code, final String message) {
                            if (code == 101 || code==204) {
                                try {
                                    EMClient.getInstance().createAccount(mViewSysUserDoctorInfoAndHospital.getDoctorCode(), mViewSysUserDoctorInfoAndHospital.getQrCode());
                                    gHandler.sendEmptyMessage(1);
                                } catch (Exception logex) {
                                    Log.e(IMTAG, "登录失败: " + code);
                                }
                            }
                            Log.d(IMTAG, "登录失败: " + code);
                        }
                    });
                }catch (Exception ex){
                    Log.e(IMTAG,ex.getMessage());
                }
            }}.start();

    }


    @Override
    public void onTerminate() {
        super.onTerminate();
        /*if(StrUtils.defaulObjToStr(curdetailcode).length()>0){
            CloseRoomInfo subinfo = new CloseRoomInfo();
            subinfo.setDetailsCode(curdetailcode);
            subinfo.setLoginUserPosition(loginDoctorPosition);
            subinfo.setOperUserCode(mViewSysUserDoctorInfoAndHospital.getDoctorCode());
            subinfo.setOperUserName(mViewSysUserDoctorInfoAndHospital.getUserName());
            subinfo.setRequestClientType("1");
            CloseLiveRoomTask closeLiveRoomTask = new CloseLiveRoomTask(subinfo);
            closeLiveRoomTask.execute();
        }*/
    }
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    class CloseLiveRoomTask extends AsyncTask<Void,Void,Boolean> {
        CloseRoomInfo subinfo;
        String retmsg = "";
        CloseLiveRoomTask(CloseRoomInfo subinfo){
            this.subinfo =  subinfo;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                String retstr = HttpNetService.urlConnectionService("jsonDataInfo="+new Gson().toJson(subinfo),"https://www.jiuyihtn.com:41041/broadcastLiveDataControlle/operLiveRoomDetailsNoticeResCloseBroadcasting");
                NetRetEntity retEntity = JSON.parseObject(retstr,NetRetEntity.class);
                if(1==retEntity.getResCode()){
                    retmsg = retEntity.getResMsg();
                    return true;
                }else{
                    retmsg = retEntity.getResMsg();
                }
            } catch (Exception e) {
                e.printStackTrace();
                retmsg = "数据存储异常";
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if(!aBoolean){
                Toast.makeText(gContext,retmsg,Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 退出登录
     */
    public  void LoginOut(final Activity activity) {
        EMClient.getInstance().logout(true, new EMCallBack() {

            @Override
            public void onSuccess() {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("tag", "run: "+"您已退出登录!" );
                    }
                });
            }

            @Override
            public void onProgress(int progress, String status) {
            }

            @Override
            public void onError(int code, final String message) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                    }
                });
            }
        });
    }



    @Override
    public void onCreate() {
        super.onCreate();
        //初始化IM聊天界面
        gContext = getApplicationContext();
        DemoHelper.getInstance().init(gContext);
        //获取本地缓存
        getPersistence();
        initUmengSDK();
        EMOptions options = new EMOptions();
        // 默认添加好友时，是不需要验证的，改成需要验证
        options.setAcceptInvitationAlways(false);
        EaseUI.getInstance().init(gContext, options);
        SpeechUtility.createUtility(gContext, SpeechConstant.APPID +"=5facab57");
//        //注册
//        IntentFilter callFilter = new IntentFilter(EMClient.getInstance().callManager().getIncomingCallBroadcastAction());
//        if (mCallReceiver == null) {
//            mCallReceiver = new CallReceiver();
//        }
//        getApplicationContext().registerReceiver(mCallReceiver, callFilter);
        //初始化图片方法
        /**
         * 配置并初始化ImageLoader
         */
        if (Constant.Config.DEVELOPER_MODE && Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyDialog().build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().penaltyDeath().build());
        }
        initImageLoader(getApplicationContext());

        // 使用DisplayImageOptions.Builder()创建DisplayImageOptions
        mImageOptions = new DisplayImageOptions.Builder()
                .showStubImage(R.mipmap.hztx)                    // 设置图片下载期间显示的图片
                .showImageForEmptyUri(R.mipmap.hztx)             // 设置图片Uri为空或是错误的时候显示的图片
                .showImageOnFail(R.mipmap.hztx)                // 设置图片加载或解码过程中发生错误显示的图片
                .cacheInMemory(true)                        // 设置下载的图片是否缓存在内存中
                .cacheOnDisc(true)                            // 设置下载的图片是否缓存在SD卡中
                .displayer(new RoundedBitmapDisplayer(0))    // 设置成圆角图片
                .build();                                    // 创建配置过得DisplayImageOption对象
        saveIMNumInfo();
        getIMNumInfo();
        initLitesmat();
        //   login();
        initRxHttpUtils();

        //initBugly();
    }


    /**
     * 初始化umeng sdk
     */
    private void initUmengSDK(){
        //UMConfigure.setLogEnabled(true);
        UMConfigure.init(this, Constant.UMENG_APPKEY, "umeng", UMConfigure.DEVICE_TYPE_PHONE,
                Constant.UMENG_APP_SECRET);

        PushAgent pushAgent = PushAgent.getInstance(this);
        pushAgent.register(new IUmengRegisterCallback(){

            @Override
            public void onSuccess(String s) {
                Log.i("walle", "--->>> onSuccess, s is " + s);
                SharedPreferences_DataSave dataSave = new SharedPreferences_DataSave(
                        JYKJApplication.this, "JYKJDOCTER");
                dataSave.putString("deviceToken",s);
                dataSave.commit();
            }

            @Override
            public void onFailure(String s, String s1) {
                Log.i("walle", "--->>> onFailure, s is " + s + ", s1 is " + s1);
            }
        });
        pushAgent.setPushIntentServiceClass(PushIntentService.class);
        pushAgent.setDisplayNotificationNumber(3);

    }
    /**
     * MessageHandler有很多回调方法，根据自己需要选择
     */
    private void setMessageHandler() {
        UmengMessageHandler messageHandler = new UmengMessageHandler() {
            @Override
            public void dealWithNotificationMessage(Context context, UMessage uMessage) {
                super.dealWithNotificationMessage(context, uMessage);
                Log.e("walle", "um notification msg.extra" + uMessage.extra);
//                try {
//                    JSONObject object = new JSONObject(uMessage.extra);
//
//                    int type = object.getInt("type");
//                    Long id = object.getLong("id");
//                    String title = object.getString("title");
//
//                    ExtraBean bean = new ExtraBean();
//                    bean.setId(id);
//                    bean.setType(type);
//                    bean.setTitle(title);
//                    /**
//                     * 应用不在前台时，不弹框，并把推送的数据存起来
//                     *
//                     * 在前台时直接弹框
//                     */
//
//                    if (MyApplacation.getMyApplication().getActivityCount() == 0) {
//                        ExtraBeanDao dao = MyApplacation.getMyApplication().getDaoSession().getExtraBeanDao();
//                        dao.insertOrReplace(bean);
//                        return;
//                    }
//                    Intent intent = new Intent(context, NotificationDialogActivity.class);
//                    intent.putExtra("bean", bean);
//                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    context.startActivity(intent);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
            }
        };
        PushAgent.getInstance(this).setMessageHandler(messageHandler);
    }


    /**
     * 初始化腾讯bug管理平台
     */
    private void initBugly() {
//        /* Bugly SDK初始化
//         * 参数1：上下文对象
//         * 参数2：APPID，平台注册时得到,注意替换成你的appId
//         * 参数3：是否开启调试模式，调试模式下会输出'CrashReport'tag的日志
//         * 注意：如果您之前使用过Bugly SDK，请将以下这句注释掉。
//         */
//        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(getApplicationContext());
//        strategy.setAppVersion(AppUtils.getAppVersionName());
//        strategy.setAppPackageName(AppUtils.getAppPackageName());
//        strategy.setAppReportDelay(20000);                          //Bugly会在启动20s后联网同步数据
//
//        /*  第三个参数为SDK调试模式开关，调试模式的行为特性如下：
//            输出详细的Bugly SDK的Log；
//            每一条Crash都会被立即上报；
//            自定义日志将会在Logcat中输出。
//            建议在测试阶段建议设置成true，发布时设置为false。*/
//
//        CrashReport.initCrashReport(getApplicationContext(), "87aa758834", true ,strategy);
//
//        //Bugly.init(getApplicationContext(), "87aa758834", false);

        Context context = getApplicationContext();
        // 获取当前包名
        String packageName = context.getPackageName();
        // 获取当前进程名
        String processName = getProcessName(android.os.Process.myPid());
        // 设置是否为上报进程
        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(context);
        strategy.setAppVersion(AppUtils.getAppVersionName());
        strategy.setAppPackageName(AppUtils.getAppPackageName());
        strategy.setUploadProcess(processName == null || processName.equals(packageName));
        // 初始化Bugly
        CrashReport.initCrashReport(context, "0d33fd5f66", true, strategy);

    }


    /**
     * 获取进程号对应的进程名
     *
     * @param pid 进程号
     * @return 进程名
     */
    private static String getProcessName(int pid) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("/proc/" + pid + "/cmdline"));
            String processName = reader.readLine();
            if (!TextUtils.isEmpty(processName)) {
                processName = processName.trim();
            }
            return processName;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        return null;
    }

    void initLitesmat(){
        String licenceURL = "http://license.vod2.myqcloud.com/license/v1/6803be91c7f78640a122154f66452db8/TXLiveSDK.licence"; // 获取到的 licence url
        String licenceKey = "55d1d4ae6154fe55d434335e1ddc05fb"; // 获取到的 licence key
        TXLiveBase.setConsoleEnabled(true);
        instance = this;
        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(getApplicationContext());
        strategy.setAppVersion(TXLiveBase.getSDKVersionStr());
        CrashReport.initCrashReport(getApplicationContext(),strategy);

        TXLiveBase.getInstance().setLicence(instance, licenceURL, licenceKey);

        closeAndroidPDialog();
    }

    private void closeAndroidPDialog(){
        try {
            Class aClass = Class.forName("android.content.pm.PackageParser$Package");
            Constructor declaredConstructor = aClass.getDeclaredConstructor(String.class);
            declaredConstructor.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Class cls = Class.forName("android.app.ActivityThread");
            Method declaredMethod = cls.getDeclaredMethod("currentActivityThread");
            declaredMethod.setAccessible(true);
            Object activityThread = declaredMethod.invoke(null);
            Field mHiddenApiWarningShown = cls.getDeclaredField("mHiddenApiWarningShown");
            mHiddenApiWarningShown.setAccessible(true);
            mHiddenApiWarningShown.setBoolean(activityThread, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 保存用户信息
     */
    public void saveUserInfo() {
        //数据存储,以json字符串的格式保存
        m_persist = new SharedPreferences_DataSave(this, "JYKJDOCTER");
        m_persist.putString("loginUserInfo", new Gson().toJson(mLoginUserInfo));
        ExtEaseUtils.getInstance().setNickName(mViewSysUserDoctorInfoAndHospital.getUserName());
        ExtEaseUtils.getInstance().setImageUrl(mViewSysUserDoctorInfoAndHospital.getUserLogoUrl());
        ExtEaseUtils.getInstance().setUserId(mViewSysUserDoctorInfoAndHospital.getDoctorCode());
        m_persist.putString("viewSysUserDoctorInfoAndHospital", new Gson().toJson(mViewSysUserDoctorInfoAndHospital));
        m_persist.commit();
        DemoHelper.getInstance().getUserProfileManager().updateCurrentUserNickName(mViewSysUserDoctorInfoAndHospital.getUserName());
        DemoHelper.getInstance().getUserProfileManager().setCurrentUserAvatar(mViewSysUserDoctorInfoAndHospital.getUserLogoUrl());
        DemoHelper.getInstance().setCurrentUserName(mViewSysUserDoctorInfoAndHospital.getDoctorCode()); // 环信Id
        Log.e("tag", "run: "+"ccccccccccccccccc" );
    }

    /**
     *    保存用户信息
     * @param mViewSysUserDoctorInfoAndHospital
     */
    public void saveUserInfo(ViewSysUserDoctorInfoAndHospital mViewSysUserDoctorInfoAndHospital) {
        //数据存储,以json字符串的格式保存
        m_persist = new SharedPreferences_DataSave(this, "JYKJDOCTER");
        m_persist.putString("userID",mLoginUserInfo.getUserPhone());
        m_persist.putString("userName",mLoginUserInfo.getUserPhone());
        m_persist.putString("loginUserInfo", new Gson().toJson(mLoginUserInfo));
        ExtEaseUtils.getInstance().setNickName(mViewSysUserDoctorInfoAndHospital.getUserName());
        ExtEaseUtils.getInstance().setImageUrl(mViewSysUserDoctorInfoAndHospital.getUserLogoUrl());
        ExtEaseUtils.getInstance().setUserId(mViewSysUserDoctorInfoAndHospital.getDoctorCode());
        m_persist.putString("viewSysUserDoctorInfoAndHospital", new Gson().toJson(mViewSysUserDoctorInfoAndHospital));
        m_persist.commit();
        DemoHelper.getInstance().getUserProfileManager().updateCurrentUserNickName(mViewSysUserDoctorInfoAndHospital.getUserName());
        DemoHelper.getInstance().getUserProfileManager().setCurrentUserAvatar(mViewSysUserDoctorInfoAndHospital.getUserLogoUrl());
        DemoHelper.getInstance().setCurrentUserName(mViewSysUserDoctorInfoAndHospital.getDoctorCode()); // 环信Id
        Log.e("tag", "run: "+"ccccccccccccccccc" );
    }

    /**
     * 保存图文音视频通话限制信息
     */
    public void saveIMNumInfo() {
        //数据存储,以json字符串的格式保存
        m_persist = new SharedPreferences_DataSave(this, "JYKJDOCTER");
        m_persist.putInt("messageNum", gMessageNum);
        m_persist.putLong("voiceTime", gVoiceTime);
        m_persist.putLong("vedioTime", gVedioTime);
        m_persist.commit();
    }

    //获取图文音视频通话限制信息
    public void getIMNumInfo() {
        m_persist = new SharedPreferences_DataSave(this, "JYKJDOCTER");
        gMessageNum = m_persist.getInt("messageNum", 0);
        gVoiceTime = m_persist.getLong("voiceTime", 0);
        gVedioTime = m_persist.getLong("vedioTime", 0);
        System.out.println(gMessageNum);
        System.out.println(gVoiceTime);
        System.out.println(gVedioTime);
        System.out.println(gVedioTime);
    }


    //清空数据
    public void cleanPersistence() {
        m_persist = new SharedPreferences_DataSave(this, "JYKJDOCTER");
        m_persist.remove("loginUserInfo");
        m_persist.remove("viewSysUserDoctorInfoAndHospital");
        mLoginUserInfo.setUserPhone("");
        mLoginUserInfo.setUserPwd("");
        m_persist.commit();
    }

    //获取缓存数据
    public void getPersistence() {
        m_persist = new SharedPreferences_DataSave(this, "JYKJDOCTER");
        String userInfoLogin = m_persist.getString("loginUserInfo", "");
        String userInfoSuLogin = m_persist.getString("viewSysUserDoctorInfoAndHospital", "");
        mLoginUserInfo = new Gson().fromJson(userInfoLogin, UserInfo.class);
        mViewSysUserDoctorInfoAndHospital = new Gson().fromJson(userInfoSuLogin, ViewSysUserDoctorInfoAndHospital.class);
        System.out.println(mViewSysUserDoctorInfoAndHospital);
    }


    /**
     * 初始化ImageLoader
     *
     * @param context
     */
    public static void initImageLoader(Context context) {
        // This configuration tuning is custom. You can tune every option, you may tune some of them,
        // or you can create default configuration by
        //  ImageLoaderConfiguration.createDefault(this);
        // method.
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .discCacheFileNameGenerator(new Md5FileNameGenerator())
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .writeDebugLogs() // Remove for release app
                .build();
        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config);
    }

    /**
     * 设置新消息
     */
    public void setNewsMessage() {
       /* Map<String, EMConversation> conversationMap = EMClient.getInstance().chatManager().getAllConversations();
        List<String> userCodeList = new ArrayList<>();
        //遍历map中的键,获取用户Code列表
        for (String key : conversationMap.keySet()) {
            userCodeList.add(key);
        }

        for (int i = 0; i < userCodeList.size(); i++) {
            //获取未读消息数
            EMConversation conversation = EMClient.getInstance().chatManager().getConversation(userCodeList.get(i));
            if (conversation != null)
                gNewMessageNum = conversation.getUnreadMsgCount();
            if (gNewMessageNum > 0)
                return;
        }*/

    }


    public static boolean isApplicationBroughtToBackground(final Context context) {
        //创建activity管理对象
        ActivityManager activityManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);

        //取出RunningTask栈
        List<ActivityManager.RunningTaskInfo> list = activityManager.getRunningTasks(1);
        //判断是否为空
        if (!list.isEmpty()) {
            //取出运行在栈顶的任务进程
            ComponentName componentName = list.get(0).topActivity;
            //判断任务进程的包名是否和想要判断的程序包名相同
            if (componentName.getPackageName().equals(context.getPackageName())) {
                //相同则说明你该程序运行在前台，则返回ture
                return true;
            }
        }
        //否则返回false
        return false;
    }

    /**
     * 设置环信网络状态
     */
    public void setNetConnectionStateHX() {
        if (gMainActivity != null)
            gMainActivity.setHXNetWorkState();
    }

    //static 代码段可以防止内存泄露
    static {
        //设置全局的Header构建器
        SmartRefreshLayout.setDefaultRefreshHeaderCreator(new DefaultRefreshHeaderCreator() {
            @Override
            public RefreshHeader createRefreshHeader(Context context, RefreshLayout layout) {
                layout.setPrimaryColorsId(R.color.white, android.R.color.black);//全局设置主题颜色
                return new ClassicsHeader(context);//.setTimeFormat(new DynamicTimeFormat("更新于 %s"));//指定为经典Header，默认是 贝塞尔雷达Header
            }
        });
        //设置全局的Footer构建器
        SmartRefreshLayout.setDefaultRefreshFooterCreator(new DefaultRefreshFooterCreator() {
            @Override
            public RefreshFooter createRefreshFooter(Context context, RefreshLayout layout) {
                //指定为经典Footer，默认是 BallPulseFooter
                return new ClassicsFooter(context).setDrawableSize(20);
            }
        });
    }

    /**
     * 全局请求的统一配置（以下配置根据自身情况选择性的配置即可）
     */
    private void initRxHttpUtils() {

        //一个项目多url的配置方法
        RxUrlManager.getInstance().setMultipleUrl(AppUrlConfig.getAllApiUrl());

        RxHttpUtils
                .getInstance()
                .init(this)
                .config()
                //自定义factory的用法
                //.setCallAdapterFactory(RxJava2CallAdapterFactory.create())
                //.setConverterFactory(ScalarsConverterFactory.create(),GsonConverterFactory.create(GsonAdapter.buildGson()))
                //配置全局baseUrl
                .setBaseUrl(AppUrlConfig.SERVICE_API_ONLINE_URL)
                //开启全局配置
                .setOkClient(createOkHttp());


//        TODO: 2018/5/31 如果以上OkHttpClient的配置满足不了你，传入自己的 OkHttpClient 自行设置
//        OkHttpClient.Builder builder = new OkHttpClient.Builder();
//
//        builder
//                .addInterceptor(log_interceptor)
//                .readTimeout(10, TimeUnit.SECONDS)
//                .writeTimeout(10, TimeUnit.SECONDS)
//                .connectTimeout(10, TimeUnit.SECONDS);
//
//        RxHttpUtils
//                .getInstance()
//                .init(this)
//                .config()
//                .setBaseUrl(BuildConfig.BASE_URL)
//                .setOkClient(builder.build());

    }

    private OkHttpClient createOkHttp() {
        //        获取证书
//        InputStream cerInputStream = null;
//        InputStream bksInputStream = null;
//        try {
//            cerInputStream = getAssets().open("YourSSL.cer");
//            bksInputStream = getAssets().open("your.bks");
//        } catch (IOExceptio
//        n e) {
//            e.printStackTrace();
//        }

        OkHttpClient okHttpClient = new OkHttpConfig
                .Builder(this)
                //添加公共请求头
                .setHeaders(() -> {
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("requestClientVerify", HttpNetService.requestClientVerify);
                    hashMap.put("requestLoginTokenValue", HttpNetService.requestLoginTokenValue);
                    return hashMap;

                })
                //添加自定义拦截器
                //.setAddInterceptor()
                //开启缓存策略(默认false)
                //1、在有网络的时候，先去读缓存，缓存时间到了，再去访问网络获取数据；
                //music_btn_paus、在没有网络的时候，去读缓存中的数据。
                .setCache(true)
                .setHasNetCacheTime(1)//默认有网络时候缓存60秒
                //全局持久话cookie,保存到内存（new MemoryCookieStore()）或者保存到本地（new SPCookieStore(this)）
                //不设置的话，默认不对cookie做处理
                .setCookieType(new SPCookieStore(this))

                //可以添加自己的拦截器(比如使用自己熟悉三方的缓存库等等)
                //.setAddInterceptor(null)
                //全局ssl证书认证
                //1、信任所有证书,不安全有风险（默认信任所有证书）
                //.setSslSocketFactory()
                //music_btn_paus、使用预埋证书，校验服务端证书（自签名证书）
                //.setSslSocketFactory(cerInputStream)
                //3、使用bks证书和密码管理客户端证书（双向认证），使用预埋证书，校验服务端证书（自签名证书）
                //.setSslSocketFactory(bksInputStream,"123456",cerInputStream)
                //设置Hostname校验规则，默认实现返回true，需要时候传入相应校验规则即可
                //.setHostnameVerifier(null)
                //全局超时配置
                .setReadTimeout(60)
                //全局超时配置
                .setWriteTimeout(60)
                //全局超时配置
                .setConnectTimeout(60)
                //全局是否打开请求log日志
//                .setDebug(BuildConfig.DEBUG)
                .build();

        return okHttpClient;
    }

}
