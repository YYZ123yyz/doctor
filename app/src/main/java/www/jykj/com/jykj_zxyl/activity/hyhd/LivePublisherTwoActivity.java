package www.jykj.com.jykj_zxyl.activity.hyhd;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.*;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Surface;
import android.view.View;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMChatRoom;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.easeui.EaseConstant;
import com.hyphenate.easeui.utils.ExtEaseUtils;
import com.hyphenate.exceptions.HyphenateException;
import com.tencent.rtmp.ITXLivePushListener;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXLivePushConfig;
import com.tencent.rtmp.TXLivePusher;
import com.tencent.rtmp.ui.TXCloudVideoView;
import entity.conditions.OpenLiveCond;
import entity.liveroom.CloseRoomInfo;
import entity.liveroom.OpenLiveResp;
import netService.HttpNetService;
import netService.entity.NetRetEntity;
import www.jykj.com.jykj_zxyl.R;
import www.jykj.com.jykj_zxyl.adapter.HeadImageViewRecycleAdapter;
import www.jykj.com.jykj_zxyl.adapter.LiveFragmentAdapter;
import www.jykj.com.jykj_zxyl.application.JYKJApplication;
import www.jykj.com.jykj_zxyl.fragment.liveroom.IntroductionFragment;
import www.jykj.com.jykj_zxyl.fragment.liveroom.LiveChatFragment;
import www.jykj.com.jykj_zxyl.fragment.liveroom.LiveProgromFragment;
import www.jykj.com.jykj_zxyl.util.CircleImageView;
import www.jykj.com.jykj_zxyl.util.ImageViewUtil;
import www.jykj.com.jykj_zxyl.util.StrUtils;
import ztextviewlib.MarqueeTextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class LivePublisherTwoActivity extends ChatPopDialogActivity implements View.OnClickListener , ITXLivePushListener {
    private Context mContext;
    private TXLivePushConfig mLivePushConfig;
    private TXLivePusher mLivePusher;
    private TXCloudVideoView mCaptureView;
    private Button mBtnOrientation;
    private boolean          mFrontCamera = true;
    private boolean          mVideoPublish;
    private static final int VIDEO_SRC_CAMERA = 0;
    private static final int VIDEO_SRC_SCREEN = 1;
    private int              mCurrentVideoResolution = TXLiveConstants.VIDEO_RESOLUTION_TYPE_360_640;
    private Bitmap mBitmap;
    private TextView mNetBusyTips;
    private Button btnMessage;
    private Button btnShare;
    private Button btnShut;
    // 关注系统设置项“自动旋转”的状态切换
    private RotationObserver mRotationObserver = null;
    String chatRoomName = "";
    String mychatid = "";
    String liveTitle = "";
    private int mNetBusyCount = 0;
    private Handler mNetBusyHandler;
    private int              mVideoSrc = VIDEO_SRC_CAMERA;
    LinearLayout mBottomLinear = null;
    private PhoneStateListener mPhoneListener = null;
    private boolean          mPortrait = true;         //手动切换，横竖屏推流
    String rtmpUrl = "";
    String mdetailcode = "";
    public static final String LIVE_TYPE_PRELIVE = "1";
    public static final String LIVE_TYPE_HOTLIVE = "2";
    public static final String LIVE_TYPE_SUBJECTLIVE = "3";
    public static final String PUSH_VIDEO = "1";
    public static final String PUSH_AUDIO = "2";
    CircleImageView iv_live_user_head;
    RecyclerView chat_head_imgs;
    TextView tv_chat_num;
    MarqueeTextView mv_chat_content;
    LinearLayoutManager mLayoutManager;
    HeadImageViewRecycleAdapter mImageViewRecycleAdapter;
    List<String> headpics = new ArrayList();
    TextView tv_head_tit;
    public ProgressDialog mDialogProgress = null;
    int joinUserNum = 0;
    private TabLayout tab_layout;
    private ViewPager live_publish_page;

    //private ImageView close_video;
    private Bitmap decodeResource(Resources resources, int id) {
        TypedValue value = new TypedValue();
        resources.openRawResource(id, value);
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inTargetDensity = value.density;
        return BitmapFactory.decodeResource(resources, id, opts);
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApp = (JYKJApplication)getApplication();
        mContext = LivePublisherTwoActivity.this;
        rtmpUrl = getIntent().getStringExtra("pushUrl");
        chatRoomName = getIntent().getStringExtra("chatRoomName");
        liveTitle = getIntent().getStringExtra("liveTitle");
        mychatid = getIntent().getStringExtra("chatId");
        mdetailcode = getIntent().getStringExtra("detailCode");
        mLivePusher     = new TXLivePusher(this);
        mLivePushConfig = new TXLivePushConfig();
        mLivePushConfig.setTouchFocus(false);
        mLivePusher.setConfig(mLivePushConfig);
        mBitmap         = decodeResource(getResources(), R.mipmap.watermark);
        mRotationObserver = new RotationObserver(new Handler());
        mRotationObserver.startObserver();
        setContentView();
        mBottomLinear = (LinearLayout)findViewById(R.id.btns_video);
        checkPublishPermission();

        mPhoneListener = new TXPhoneStateListener(mLivePusher);

        statLive();
        createChat();
        //goChat();
    }

    static final int UP_JOINNUM_ACT = 511;
    @Override
    public void upJoinUsernum(int modnum) {
        Message semsg = new Message();
        semsg.what = UP_JOINNUM_ACT;
        semsg.obj = modnum;
        myHandler.sendMessage(semsg);
    }

    void statLive(){
        if(!rtmpUrl.contains("txSecret") && !rtmpUrl.contains("&txTime")){
            getProgressBar("开启直播","正在开启直播间，请稍后...");
            OpenLiveCond opencond = new OpenLiveCond();
            opencond.setDetailsCode(mdetailcode);
            opencond.setLoginUserPosition(mApp.loginDoctorPosition);
            opencond.setOperUserCode(mApp.mViewSysUserDoctorInfoAndHospital.getDoctorCode());
            opencond.setOperUserName(mApp.mViewSysUserDoctorInfoAndHospital.getUserName());
            opencond.setRequestClientType("1");
            OpenLiveTask opentask = new OpenLiveTask(opencond);
            opentask.execute();
        }else{
            startPublishRtmp();
        }
    }

    /**
     * 获取进度条
     */

    public void getProgressBar(String title, String progressPrompt) {
        if (mDialogProgress == null) {
            mDialogProgress = new ProgressDialog(this);
        }
        mDialogProgress.setTitle(title);
        mDialogProgress.setMessage(progressPrompt);
        mDialogProgress.setCancelable(false);
        mDialogProgress.show();
    }

    /**
     * 取消进度条
     */
    public void cacerProgress() {
        if (mDialogProgress != null) {
            mDialogProgress.dismiss();
        }
    }

    class OpenLiveTask extends AsyncTask<Void, Void, Boolean> {
        private OpenLiveCond opencond;
        public OpenLiveTask(OpenLiveCond opencond){
            this.opencond = opencond;
        }
        @Override
        protected Boolean doInBackground(Void... voids) {
            try{
                String repjson = HttpNetService.urlConnectionService("jsonDataInfo="+new Gson().toJson(opencond),"https://www.jiuyihtn.com:41041/broadcastLiveDataControlle/operLiveRoomDetailsNoticeResStartBroadcasting");
                NetRetEntity retent = JSON.parseObject(repjson,NetRetEntity.class);
                if(retent.getResCode()==1){
                    String subrepjson = retent.getResJsonData();
                    OpenLiveResp retliveresp = JSON.parseObject(subrepjson,OpenLiveResp.class);
                    mychatid = retliveresp.getChatRoomCode();
                    rtmpUrl = retliveresp.getPushUrl();
                    return true;
                }
            }catch (Exception ex){
                ex.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            cacerProgress();
            if(aBoolean){
                startPublishRtmp();
                //goChat();
            }else{
                Toast.makeText(mContext,"开启直播失败",Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                mBottomLinear.setVisibility(View.VISIBLE);
        }
    }


    static class TXPhoneStateListener extends PhoneStateListener {
        WeakReference<TXLivePusher> mPusher;
        public TXPhoneStateListener(TXLivePusher pusher) {
            mPusher = new WeakReference<TXLivePusher>(pusher);
        }
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            TXLivePusher pusher = mPusher.get();
            switch(state){
                //电话等待接听
                case TelephonyManager.CALL_STATE_RINGING:
                    if (pusher != null) pusher.pausePusher();
                    break;
                //电话接听
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    if (pusher != null) pusher.pausePusher();
                    break;
                //电话挂机
                case TelephonyManager.CALL_STATE_IDLE:
                    if (pusher != null) pusher.resumePusher();
                    break;
            }
        }
    };

    StringBuffer msgnamesb = new StringBuffer();

    static final int SHOW_MESSAGE_FLAG = 101;

    @SuppressLint("HandlerLeak")
    Handler myHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case SHOW_MESSAGE_FLAG:
                    try {
                        EMMessage paramMessage = (EMMessage) msg.obj;
                        String parname = StrUtils.defaulObjToStr(paramMessage.getStringAttribute("nickName"));
                        String parhead = StrUtils.defaulObjToStr(paramMessage.getStringAttribute("imageUrl"));
                        if (null != parname && "" != parname) {
                            if (!msgmap.containsKey(parname)) {
                                msgmap.put(parname, "1");
                                if (null != msgmap.keySet() && msgmap.keySet().size() > 0) {
                                    //tv_chat_num.setText(String.valueOf(msgmap.keySet().size()) + "人");
                                }
                                if (parhead.length() > 0) {
                                    headpics.add(parhead);
                                    mImageViewRecycleAdapter.setDate(headpics);
                                    mImageViewRecycleAdapter.notifyDataSetChanged();
                                }
                            }
                        }
                        mv_chat_content.setText(msgnamesb);
                    }catch (Exception ex){
                        ex.printStackTrace();
                    }
                    break;
                case GO_CHAT_ACT:
                    doChat();
                    break;
                case ENTER_CHAT_ACT:
                    if(isopenchat){
                        if(chatViewLayout.getVisibility()== View.VISIBLE) {
                            chatViewLayout.setVisibility(View.GONE);
                        }else{
                            chatViewLayout.setVisibility(View.VISIBLE);
                        }
                    }else{
                        createChat();
                    };
                    break;
                case LOGIN_CHAT_FAIL:
                    Toast.makeText(mContext,"登录聊天室失败，请稍后重试！",Toast.LENGTH_SHORT);
                    break;
                case UP_JOINNUM_ACT:
                    Integer paincnum = (Integer)msg.obj;
                    joinUserNum = joinUserNum + paincnum;
                    if(joinUserNum>=0) {
                        tv_chat_num.setText(String.valueOf(joinUserNum) + "人");
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    public void showMessages(EMMessage paramMessage) {
        try {
            String parname = StrUtils.defaulObjToStr(paramMessage.getStringAttribute("nickName"));
            if(parname.length()>0){
                msgnamesb.append("  ");
                EMTextMessageBody txtBody = (EMTextMessageBody) paramMessage.getBody();
                //Spannable span = EaseSmileUtils.(mContext, txtBody.getMessage());
                msgnamesb.append(txtBody.getMessage());
                // 设置内容
            }
            Message parmsg = new Message();
            parmsg.what = SHOW_MESSAGE_FLAG;
            parmsg.obj = paramMessage;
            myHandler.sendMessage(parmsg);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    IntroductionFragment introductionFragment = null;
    LiveChatFragment liveChatFragment = null;

    public void setContentView() {
        setContentView(R.layout.activity_publish_two);
        tab_layout = findViewById(R.id.tab_layout);
        live_publish_page = findViewById(R.id.live_publish_page);
        mCaptureView = (TXCloudVideoView) findViewById(R.id.video_view);
        mNetBusyTips = (TextView) findViewById(R.id.netbusy_tv);
        mVideoPublish = false;
        btnMessage = findViewById(R.id.btnMessage);
        btnShare = findViewById(R.id.btnShare);
        btnShut = findViewById(R.id.btnShut);
        mBtnOrientation = findViewById(R.id.btnOriention);
        //close_video = findViewById(R.id.close_video);
        btnShut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle("提示");
                builder.setMessage("观众正在赶来的路上哦确认关闭直播吗?");
                builder.setNegativeButton("取消", null);
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        shutVideo();
                    }
                });
                builder.show();
            }
        });

        /*close_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shutVideo();
            }
        });*/

        btnMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goChat();
            }
        });

        mBtnOrientation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFrontCamera = !mFrontCamera;

                if (mLivePusher.isPushing()) {
                    mLivePusher.switchCamera();
                }
                mLivePushConfig.setFrontCamera(mFrontCamera);
                mBtnOrientation.setBackgroundResource(mFrontCamera ? R.mipmap.camerarienthg : R.mipmap.camera_change);
            }
        });
        goTab();
    }

    private List<Fragment> fragmentList = new ArrayList();
    private LiveFragmentAdapter liveFragmentAdapter;
    private List<String> mTitles = new ArrayList();
    void goTab(){
        fragmentList=new ArrayList<>();
        mTitles=new ArrayList<>();
        mTitles.add("简介");
        mTitles.add("互动");
        introductionFragment = new IntroductionFragment();
        fragmentList.add(LiveProgromFragment.newInstance(mdetailcode));
        liveChatFragment = new LiveChatFragment();
        fragmentList.add(liveChatFragment);
        liveFragmentAdapter = new LiveFragmentAdapter(getSupportFragmentManager(),fragmentList,mTitles);
        live_publish_page.setAdapter(liveFragmentAdapter);
        tab_layout.setupWithViewPager(live_publish_page);
    }

    void shutVideo(){
        if(StrUtils.defaulObjToStr(mdetailcode).length()>0){
            CloseRoomInfo subinfo = new CloseRoomInfo();
            subinfo.setDetailsCode(mdetailcode);
            subinfo.setLoginUserPosition(mApp.loginDoctorPosition);
            subinfo.setOperUserCode(mApp.mViewSysUserDoctorInfoAndHospital.getDoctorCode());
            subinfo.setOperUserName(mApp.mViewSysUserDoctorInfoAndHospital.getUserName());
            subinfo.setRequestClientType("1");
            CloseLiveRoomTask closeLiveRoomTask = new CloseLiveRoomTask(subinfo);
            closeLiveRoomTask.execute();
            this.finish();
        }
    }

    boolean isopenchat = false;
    static final int GO_CHAT_ACT = 999;
    static final int LOGIN_CHAT_FAIL = 997;
    @Override
    public void createChat() {
        isopenchat = true;
        LogimImTask logimImTask = new LogimImTask();
        logimImTask.execute();
    }

    String IMTAG = "IMLOG";
    class LogimImTask extends AsyncTask<Void,Void,Boolean>{
        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                EMClient.getInstance().login(mApp.mViewSysUserDoctorInfoAndHospital.getDoctorCode(),mApp.mViewSysUserDoctorInfoAndHospital.getQrCode(),new EMCallBack() {
                    @Override
                    public void onSuccess() {
                        Log.d(IMTAG, "登录成功");

                        // ** manually load all local groups and conversation
                        //EMClient.getInstance().groupManager().loadAllGroups();
                        //EMClient.getInstance().chatManager().loadAllConversations();

                        // update current user's display name for APNs
                        /*boolean updatenick = EMClient.getInstance().pushManager().updatePushNickname(ExtEaseUtils.getInstance().getNickName());
                        if (!updatenick) {
                            Log.e(IMTAG, "更新用户昵称");
                        }
                        DemoHelper.getInstance().getUserProfileManager().asyncGetCurrentUserInfo();
                        String retuser = EMClient.getInstance().getCurrentUser();*/
                        Message sendmsg = new Message();
                        sendmsg.what = GO_CHAT_ACT;
                        myHandler.sendMessage(sendmsg);
                    }

                    @Override
                    public void onProgress(int progress, String status) {
                        Log.d(IMTAG, "登录中...");
                    }

                    @Override
                    public void onError(final int code, final String message) {
                        /*if (code == 101 || code==204) {
                            try {
                                EMClient.getInstance().createAccount(mApp.mViewSysUserDoctorInfoAndHospital.getDoctorCode(), mApp.mViewSysUserDoctorInfoAndHospital.getQrCode());
                                gHandler.sendEmptyMessage(1);
                            } catch (Exception logex) {
                                Log.e(IMTAG, "登录失败: " + code);
                            }
                        }*/
                        Log.d(IMTAG, "登录失败: " + code);
                        Message sendmsg = new Message();
                        sendmsg.what = LOGIN_CHAT_FAIL;
                        myHandler.sendMessage(sendmsg);
                    }
                });
            }catch (Exception ex){
                Log.e(IMTAG,ex.getMessage());
            }
            return true;
        }
    }

    void doChat(){
        Bundle parbund = new Bundle();
        parbund.putString(EaseConstant.EXTRA_CHAT_TYPE,"twjz");
        parbund.putInt(EaseConstant.CHAT_TYPE, EaseConstant.CHATTYPE_CHATROOM);
        parbund.putString(EaseConstant.EXTRA_USER_ID, mychatid);
        parbund.putString(EaseConstant.EXTRA_USER_NAME, mychatid);
        initChat(parbund);
        initChatHeadView();
        initFragmentChatView(liveChatFragment);
        chatViewLayout.setVisibility(View.VISIBLE);
        joinChatroom();
        setUpView();
        isopenchat = true;
    }

    void initChatHeadView(){
        iv_live_user_head = liveChatFragment.getView().findViewById(R.id.iv_live_user_head);
        chat_head_imgs = liveChatFragment.getView().findViewById(R.id.chat_head_imgs);
        tv_chat_num = liveChatFragment.getView().findViewById(R.id.tv_chat_num);
        mv_chat_content = liveChatFragment.getView().findViewById(R.id.mv_chat_content);
        tv_head_tit = liveChatFragment.getView().findViewById(R.id.tv_head_tit);
        mLayoutManager = new LinearLayoutManager(mContext);
        mLayoutManager.setOrientation(LinearLayout.HORIZONTAL);
        chat_head_imgs.setLayoutManager(mLayoutManager);
        chat_head_imgs.setHasFixedSize(true);
        mImageViewRecycleAdapter = new HeadImageViewRecycleAdapter(headpics,mApp);
        chat_head_imgs.setAdapter(mImageViewRecycleAdapter);

        ImageViewUtil.showImageView(LivePublisherTwoActivity.this, mApp.mViewSysUserDoctorInfoAndHospital.getUserLogoUrl(), iv_live_user_head);
        String parnickname = mApp.mViewSysUserDoctorInfoAndHospital.getUserName();
        tv_head_tit.setText(parnickname);
    }

    static final int ENTER_CHAT_ACT = 888;

    void goChat(){
        if(StrUtils.defaulObjToStr(ExtEaseUtils.getInstance().getUserId()).length()==0){
            mApp.saveUserInfo();
            EMClient.getInstance().login(mApp.mViewSysUserDoctorInfoAndHospital.getDoctorCode(),mApp.mViewSysUserDoctorInfoAndHospital.getQrCode(),new EMCallBack() {
                @Override
                public void onSuccess() {
                    Message parmsg = new Message();
                    parmsg.what = ENTER_CHAT_ACT;
                    myHandler.sendMessage(parmsg);
                }

                @Override
                public void onError(int i, String s) {

                }

                @Override
                public void onProgress(int i, String s) {

                }
            });
        }else {
            if (isopenchat) {
                if (chatViewLayout.getVisibility() == View.VISIBLE) {
                    chatViewLayout.setVisibility(View.GONE);
                } else {
                    chatViewLayout.setVisibility(View.VISIBLE);
                }
            } else {
                createChat();
            }
        }
    }



    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mCaptureView != null) {
            mCaptureView.onResume();
        }

        if (mVideoPublish && mLivePusher != null && mVideoSrc == VIDEO_SRC_CAMERA) {
            mLivePusher.resumePusher();
            mLivePusher.resumeBGM();
        }
    }

    @Override
    public void onStop(){
        super.onStop();
        if (mCaptureView != null) {
            mCaptureView.onPause();
        }

        if (mVideoPublish && mLivePusher != null && mVideoSrc == VIDEO_SRC_CAMERA) {
            mLivePusher.pausePusher();
            mLivePusher.pauseBGM();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopPublishRtmp();
        if (mCaptureView != null) {
            mCaptureView.onDestroy();
        }

        mRotationObserver.stopObserver();
        TelephonyManager tm = (TelephonyManager) getApplicationContext().getSystemService(Service.TELEPHONY_SERVICE);
        tm.listen(mPhoneListener, PhoneStateListener.LISTEN_NONE);

        /*if(StrUtils.defaulObjToStr(mdetailcode).length()>0){
            CloseRoomInfo subinfo = new CloseRoomInfo();
            subinfo.setDetailsCode(mdetailcode);
            subinfo.setLoginUserPosition(mApp.loginDoctorPosition);
            subinfo.setOperUserCode(mApp.mViewSysUserDoctorInfoAndHospital.getDoctorCode());
            subinfo.setOperUserName(mApp.mViewSysUserDoctorInfoAndHospital.getUserName());
            subinfo.setRequestClientType("1");
            CloseLiveRoomTask closeLiveRoomTask = new CloseLiveRoomTask(subinfo);
            closeLiveRoomTask.execute();
        }*/
    }

    class CloseLiveRoomTask extends AsyncTask<Void,Void,Boolean>{
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
                Toast.makeText(mContext,retmsg,Toast.LENGTH_SHORT).show();
            }
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 100:
                for (int ret : grantResults) {
                    if (ret != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                }
                break;
            default:
                break;
        }
    }

    private boolean checkPublishPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            List<String> permissions = new ArrayList<>();
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)) {
                permissions.add(Manifest.permission.CAMERA);
            }
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)) {
                permissions.add(Manifest.permission.RECORD_AUDIO);
            }
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)) {
                permissions.add(Manifest.permission.READ_PHONE_STATE);
            }
            if (permissions.size() != 0) {
                ActivityCompat.requestPermissions(this,
                        permissions.toArray(new String[0]),
                        100);
                return false;
            }
        }

        return true;
    }

    private  boolean startPublishRtmp() {
        if (TextUtils.isEmpty(rtmpUrl) || (!rtmpUrl.trim().toLowerCase().startsWith("rtmp://"))) {
            Toast.makeText(getApplicationContext(), "推流地址不合法，目前支持rtmp推流!", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(mVideoSrc != VIDEO_SRC_SCREEN){
            mCaptureView.setVisibility(View.VISIBLE);
        }
        int customModeType = 0;
        onActivityRotation();
        mLivePushConfig.setCustomModeType(customModeType);
        mLivePusher.setPushListener(this);
        mLivePushConfig.setPauseImg(300,5);
        Bitmap bitmap = decodeResource(getResources(),R.mipmap.shut_video);
        mLivePushConfig.setPauseImg(bitmap);
        mLivePushConfig.setPauseFlag(TXLiveConstants.PAUSE_FLAG_PAUSE_VIDEO | TXLiveConstants.PAUSE_FLAG_PAUSE_AUDIO);
        if(mVideoSrc != VIDEO_SRC_SCREEN){
            mLivePushConfig.setFrontCamera(mFrontCamera);
            mLivePusher.setConfig(mLivePushConfig);
            mLivePusher.startCameraPreview(mCaptureView);
        }
        else{
            mLivePusher.setConfig(mLivePushConfig);
            mLivePusher.startScreenCapture();
        }
        mLivePusher.startPusher(rtmpUrl.trim());
        return true;
    }

    private void stopPublishRtmp() {
        mVideoPublish = false;
        mLivePusher.stopBGM();
        mLivePusher.stopCameraPreview(true);
        mLivePusher.stopScreenCapture();
        mLivePusher.setPushListener(null);
        mLivePusher.stopPusher();
        mCaptureView.setVisibility(View.GONE);
        if(mLivePushConfig != null) {
            mLivePushConfig.setPauseImg(null);
        }
    }

    private void showNetBusyTips() {
        if (null == mNetBusyHandler) {
            mNetBusyHandler = new Handler(Looper.getMainLooper());
        }
        if (mNetBusyTips.isShown()) {
            return;
        }
        mNetBusyTips.setVisibility(View.VISIBLE);
        mNetBusyHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mNetBusyTips.setVisibility(View.GONE);
            }
        }, 5000);
    }

    @Override
    public void onPushEvent(int event, Bundle param) {
        Log.e("NotifyCode","LivePublisherActivity :" + event);
        //错误还是要明确的报一下
        if (event < 0) {
            Toast.makeText(getApplicationContext(), param.getString(TXLiveConstants.EVT_DESCRIPTION), Toast.LENGTH_SHORT).show();
            if(event == TXLiveConstants.PUSH_ERR_OPEN_CAMERA_FAIL || event == TXLiveConstants.PUSH_ERR_OPEN_MIC_FAIL){
                stopPublishRtmp();
            }
        }
        if (event == TXLiveConstants.PUSH_ERR_NET_DISCONNECT) {
            stopPublishRtmp();
        }
        else if (event == TXLiveConstants.PUSH_WARNING_HW_ACCELERATION_FAIL) {
            Toast.makeText(getApplicationContext(), param.getString(TXLiveConstants.EVT_DESCRIPTION), Toast.LENGTH_SHORT).show();
            mLivePushConfig.setHardwareAcceleration(TXLiveConstants.ENCODE_VIDEO_SOFTWARE);
            mLivePusher.setConfig(mLivePushConfig);
        }else if (event == TXLiveConstants.PUSH_ERR_SCREEN_CAPTURE_UNSURPORT) {
            stopPublishRtmp();
        }
        else if (event == TXLiveConstants.PUSH_ERR_SCREEN_CAPTURE_START_FAILED) {
            stopPublishRtmp();
        } else if (event == TXLiveConstants.PUSH_EVT_CHANGE_RESOLUTION) {
            Log.d(TAG, "change resolution to " + param.getInt(TXLiveConstants.EVT_PARAM2) + ", bitrate to" + param.getInt(TXLiveConstants.EVT_PARAM1));
        } else if (event == TXLiveConstants.PUSH_EVT_CHANGE_BITRATE) {
            Log.d(TAG, "change bitrate to" + param.getInt(TXLiveConstants.EVT_PARAM1));
        } else if (event == TXLiveConstants.PUSH_WARNING_NET_BUSY) {
            ++mNetBusyCount;
            Log.d(TAG, "net busy. count=" + mNetBusyCount);
            showNetBusyTips();
        } else if (event == TXLiveConstants.PUSH_EVT_START_VIDEO_ENCODER) {
            int encType = param.getInt(TXLiveConstants.EVT_PARAM1);
        }
    }

    @Override
    public void onNetStatus(Bundle status) {
        Log.d(TAG, "Current status, CPU:"+status.getString(TXLiveConstants.NET_STATUS_CPU_USAGE)+
                ", RES:"+status.getInt(TXLiveConstants.NET_STATUS_VIDEO_WIDTH)+"*"+status.getInt(TXLiveConstants.NET_STATUS_VIDEO_HEIGHT)+
                ", SPD:"+status.getInt(TXLiveConstants.NET_STATUS_NET_SPEED)+"Kbps"+
                ", FPS:"+status.getInt(TXLiveConstants.NET_STATUS_VIDEO_FPS)+
                ", ARA:"+status.getInt(TXLiveConstants.NET_STATUS_AUDIO_BITRATE)+"Kbps"+
                ", VRA:"+status.getInt(TXLiveConstants.NET_STATUS_VIDEO_BITRATE)+"Kbps");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        onActivityRotation();
        super.onConfigurationChanged(newConfig);
    }

    protected void onActivityRotation()
    {
        // 自动旋转打开，Activity随手机方向旋转之后，需要改变推流方向
        int mobileRotation = this.getWindowManager().getDefaultDisplay().getRotation();
        int pushRotation = TXLiveConstants.VIDEO_ANGLE_HOME_DOWN;
        boolean screenCaptureLandscape = false;
        switch (mobileRotation) {
            case Surface.ROTATION_0:
                pushRotation = TXLiveConstants.VIDEO_ANGLE_HOME_DOWN;
                break;
            case Surface.ROTATION_90:
                pushRotation = TXLiveConstants.VIDEO_ANGLE_HOME_RIGHT;
                screenCaptureLandscape = true;
                break;
            case Surface.ROTATION_270:
                pushRotation = TXLiveConstants.VIDEO_ANGLE_HOME_LEFT;
                screenCaptureLandscape = true;
                break;
            default:
                break;
        }
        mLivePusher.setRenderRotation(0); //因为activity也旋转了，本地渲染相对正方向的角度为0。
        mLivePushConfig.setHomeOrientation(pushRotation);
        if (mLivePusher.isPushing()) {
            if(VIDEO_SRC_CAMERA == mVideoSrc){
                mLivePusher.setConfig(mLivePushConfig);
                mLivePusher.stopCameraPreview(true);
                mLivePusher.startCameraPreview(mCaptureView);
            }
            else if(VIDEO_SRC_SCREEN == mVideoSrc){
                //录屏横竖屏推流的判断条件是，视频分辨率取360*640还是640*360
                switch (mCurrentVideoResolution){
                    case TXLiveConstants.VIDEO_RESOLUTION_TYPE_360_640:
                        if(screenCaptureLandscape)
                            mLivePushConfig.setVideoResolution(TXLiveConstants.VIDEO_RESOLUTION_TYPE_640_360);
                        else mLivePushConfig.setVideoResolution(TXLiveConstants.VIDEO_RESOLUTION_TYPE_360_640);
                        break;
                    case TXLiveConstants.VIDEO_RESOLUTION_TYPE_540_960:
                        if(screenCaptureLandscape)
                            mLivePushConfig.setVideoResolution(TXLiveConstants.VIDEO_RESOLUTION_TYPE_960_540);
                        else mLivePushConfig.setVideoResolution(TXLiveConstants.VIDEO_RESOLUTION_TYPE_540_960);
                        break;
                    case TXLiveConstants.VIDEO_RESOLUTION_TYPE_720_1280:
                        if(screenCaptureLandscape)
                            mLivePushConfig.setVideoResolution(TXLiveConstants.VIDEO_RESOLUTION_TYPE_1280_720);
                        else mLivePushConfig.setVideoResolution(TXLiveConstants.VIDEO_RESOLUTION_TYPE_720_1280);
                        break;
                }
                mLivePusher.setConfig(mLivePushConfig);
                mLivePusher.stopScreenCapture();
                mLivePusher.startScreenCapture();
            }
        }
    }

    /**
     * 判断Activity是否可旋转。只有在满足以下条件的时候，Activity才是可根据重力感应自动旋转的。
     * 系统“自动旋转”设置项打开；
     * @return false---Activity可根据重力感应自动旋转
     */
    protected boolean isActivityCanRotation()
    {
        // 判断自动旋转是否打开
        int flag = Settings.System.getInt(this.getContentResolver(),Settings.System.ACCELEROMETER_ROTATION, 0);
        if (flag == 0) {
            return false;
        }
        return true;
    }

    class CreateChatTask extends AsyncTask<Void,Void,Boolean>{
        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                EMChatRoom chatRoom = EMClient.getInstance().chatroomManager().createChatRoom(chatRoomName, liveTitle, liveTitle, 500, null);
                mychatid = chatRoom.getId();
                return true;
            } catch (HyphenateException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
        }
    }

    //观察屏幕旋转设置变化，类似于注册动态广播监听变化机制
    private class RotationObserver extends ContentObserver {
        ContentResolver mResolver;
        public RotationObserver(Handler handler)
        {
            super(handler);
            mResolver = LivePublisherTwoActivity.this.getContentResolver();
        }
        //屏幕旋转设置改变时调用
        @Override
        public void onChange(boolean selfChange)
        {
            super.onChange(selfChange);
            //更新按钮状态
            if (isActivityCanRotation()) {
                mBtnOrientation.setVisibility(View.GONE);
                onActivityRotation();
            } else {
                mBtnOrientation.setVisibility(View.VISIBLE);
                mPortrait = true;
                mLivePushConfig.setHomeOrientation(TXLiveConstants.VIDEO_ANGLE_HOME_DOWN);
                //mBtnOrientation.setBackgroundResource(R.mipmap.landscape);
                mLivePusher.setRenderRotation(0);
                mLivePusher.setConfig(mLivePushConfig);
            }

        }

        public void startObserver()
        {
            mResolver.registerContentObserver(Settings.System.getUriFor(Settings.System.ACCELEROMETER_ROTATION), false, this);
        }

        public void stopObserver()
        {
            mResolver.unregisterContentObserver(this);
        }
    }
}
