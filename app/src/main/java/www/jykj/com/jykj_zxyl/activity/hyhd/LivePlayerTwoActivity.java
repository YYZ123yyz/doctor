package www.jykj.com.jykj_zxyl.activity.hyhd;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.*;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;
import com.alibaba.fastjson.JSON;
import com.allen.library.interceptor.Transformer;
import com.allen.library.utils.ToastUtils;
import com.blankj.utilcode.util.LogUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.easeui.EaseConstant;
import com.hyphenate.easeui.utils.ExtEaseUtils;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.rtmp.ITXLivePlayListener;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXLivePlayConfig;
import com.tencent.rtmp.TXLivePlayer;
import com.tencent.rtmp.ui.TXCloudVideoView;
import entity.conditions.OpenLiveCond;
import entity.liveroom.RoomDetailInfo;
import netService.HttpNetService;
import netService.entity.NetRetEntity;
import util.NetWorkUtils;
import util.QRCodeUtil;
import wechatShare.WechatShareManager;
import www.jykj.com.jykj_zxyl.R;
import www.jykj.com.jykj_zxyl.adapter.HeadImageViewRecycleAdapter;
import www.jykj.com.jykj_zxyl.adapter.LiveFragmentAdapter;
import www.jykj.com.jykj_zxyl.app_base.base_bean.BaseBean;
import www.jykj.com.jykj_zxyl.app_base.base_bean.LiveStatusBean;
import www.jykj.com.jykj_zxyl.app_base.http.ApiHelper;
import www.jykj.com.jykj_zxyl.app_base.http.CommonDataObserver;
import www.jykj.com.jykj_zxyl.app_base.http.ParameUtil;
import www.jykj.com.jykj_zxyl.app_base.http.RetrofitUtil;
import www.jykj.com.jykj_zxyl.application.JYKJApplication;
import www.jykj.com.jykj_zxyl.custom.ShareDialog;
import www.jykj.com.jykj_zxyl.fragment.liveroom.IntroductionFragment;
import www.jykj.com.jykj_zxyl.fragment.liveroom.LiveChatFragment;
import www.jykj.com.jykj_zxyl.fragment.liveroom.LiveProgromFragment;
import www.jykj.com.jykj_zxyl.util.CircleImageView;
import www.jykj.com.jykj_zxyl.util.DateUtils;
import www.jykj.com.jykj_zxyl.util.GsonUtils;
import www.jykj.com.jykj_zxyl.util.StrUtils;
import www.jykj.com.jykj_zxyl.util.TCConstants;
import ztextviewlib.MarqueeTextView;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LivePlayerTwoActivity extends ChatPopDialogActivity implements ITXLivePlayListener, View.OnClickListener {
    Activity mActivity;
    Context mContext;
    public ProgressDialog mDialogProgress = null;
    RoomDetailInfo mRoomDetailInfo = null;
    private String mychatid = "";
    private TXCloudVideoView mPlayerView;
    //private LinearLayout video_hold_layout;
    private TXLivePlayConfig mPlayConfig;
    private PhoneStateListener mPhoneListener = null;
    private TXLivePlayer mLivePlayer = null;
    protected int            mActivityType;
    public static final int ACTIVITY_TYPE_LIVE_PLAY    = 2;
    public static final int ACTIVITY_TYPE_VOD_PLAY     = 3;
    public static final int ACTIVITY_TYPE_LINK_MIC     = 4;
    private ImageView mLoadingView;
    private boolean          mVideoPlay;
    private boolean          mVideoPause = false;
    private String playUrl = "";
    private int              mPlayType = TXLivePlayer.PLAY_TYPE_LIVE_RTMP;
    private static final int  CACHE_STRATEGY_FAST  = 1;  //极速
    private static final int  CACHE_STRATEGY_SMOOTH = 2;  //流畅
    private static final int  CACHE_STRATEGY_AUTO = 3;  //自动

    private static final float  CACHE_TIME_FAST = 1.0f;
    private static final float  CACHE_TIME_SMOOTH = 5.0f;

    private static final float  CACHE_TIME_AUTO_MIN = 5.0f;
    private static final float  CACHE_TIME_AUTO_MAX = 10.0f;
    private int              mCurrentRenderMode;
    private int              mCurrentRenderRotation;

    private long             mTrackingTouchTS = 0;
    private boolean          mHWDecode   = false;
    private boolean mEnableCache;
    private long             mStartPlayTS = 0;
    private int              mCacheStrategy = 0;
    private Button btnMessage;
    private Button btnShare;
    private Button btnShut;
    private Button mBtnOrientation;
    CircleImageView iv_live_user_head;
    RecyclerView chat_head_imgs;
    TextView tv_chat_num;
    ImageView mIvLiveRoomShare;
    MarqueeTextView mv_chat_content;
    LinearLayoutManager mLayoutManager;
    HeadImageViewRecycleAdapter mImageViewRecycleAdapter;
    List<String> headpics = new ArrayList();
    TextView tv_head_tit;
    String mdetailCode = "";
    int joinUserNum = 1;
    private TabLayout tab_layout;
    private LinearLayout tab_hold_layout;
    private ViewPager live_publish_page;
    TextView tv_liveroom_name;
    TextView tv_landscape_liveroom_name;
    ImageView iv_zoom_btn;
    ImageView iv_miniaml_zoom_btn;
    RelativeLayout live_rl;
    TextView tvPlayErrorMsg;
    ShareDialog shareDialog = null;
    RelativeLayout rlHorizontalRoot;
    int videowidth = 0;
    int videoheight = 0;
    WechatShareManager mShareManager;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApp = (JYKJApplication)getApplication();
        mychatid = getIntent().getStringExtra("chatId");
        playUrl = getIntent().getStringExtra("pullUrl");
        mdetailCode = getIntent().getStringExtra("detailCode");
        mActivity = LivePlayerTwoActivity.this;
        mContext = LivePlayerTwoActivity.this;
        mCurrentRenderRotation = TXLiveConstants.RENDER_ROTATION_PORTRAIT;
        //mCurrentRenderMode = TXLiveConstants.RENDER_MODE_ADJUST_RESOLUTION;
        mCurrentRenderMode = TXLiveConstants.RENDER_MODE_ADJUST_RESOLUTION;
        loadLive();
        setContentView(R.layout.activity_two_player);
        mActivityType = getIntent().getIntExtra("PLAY_TYPE", ACTIVITY_TYPE_LIVE_PLAY);
        mPlayConfig = new TXLivePlayConfig();
        mShareManager = WechatShareManager.getInstance(mContext);
        checkPublishPermission();
        initview();
        createChat();
        sendGetLiveStatusRequest(mdetailCode);
    }

    RoomDetailInfo myroominfo = null;
    void initChatHeadView(){
        View setv = liveChatFragment.getView();
        if(null==setv)
            return;
        iv_live_user_head = setv.findViewById(R.id.iv_live_user_head);
        chat_head_imgs = setv.findViewById(R.id.chat_head_imgs);
        tv_chat_num = setv.findViewById(R.id.tv_chat_num);
        mv_chat_content = setv.findViewById(R.id.mv_chat_content);
        tv_head_tit = setv.findViewById(R.id.tv_head_tit);
        mLayoutManager = new LinearLayoutManager(mContext);
        mLayoutManager.setOrientation(LinearLayout.HORIZONTAL);
        chat_head_imgs.setLayoutManager(mLayoutManager);
        chat_head_imgs.setHasFixedSize(true);
        mImageViewRecycleAdapter = new HeadImageViewRecycleAdapter(headpics,mApp);
        chat_head_imgs.setAdapter(mImageViewRecycleAdapter);
        /*Glide.with(mContext).load(mApp.mViewSysUserDoctorInfoAndHospital.getUserLogoUrl())
                .apply(RequestOptions.placeholderOf(com.hyphenate.easeui.R.mipmap.docter_heard)
                        .diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(iv_live_user_head);
        String parnickname = mApp.mViewSysUserDoctorInfoAndHospital.getUserName();
        tv_head_tit.setText(parnickname);*/
        if(null!=mApp.mViewSysUserDoctorInfoAndHospital.getUserLogoUrl() && mApp.mViewSysUserDoctorInfoAndHospital.getUserLogoUrl().length()>0){
            headpics.add(mApp.mViewSysUserDoctorInfoAndHospital.getUserLogoUrl());
            mImageViewRecycleAdapter.setDate(headpics);
            mImageViewRecycleAdapter.notifyDataSetChanged();
        }

        if(null!=myroominfo) {
            Glide.with(mContext).load(myroominfo.getBroadcastUserLogoUrl())
                    .apply(RequestOptions.placeholderOf(com.hyphenate.easeui.R.mipmap.docter_heard)
                            .diskCacheStrategy(DiskCacheStrategy.ALL))
                    .into(iv_live_user_head);
            String parnickname = myroominfo.getBroadcastUserName();
            tv_head_tit.setText(parnickname);
            tv_liveroom_name.setText(myroominfo.getBroadcastTitle());
            tv_landscape_liveroom_name.setText(myroominfo.getBroadcastTitle());
//            int flagAnchorStates = myroominfo.getFlagAnchorStates();
//            if (flagAnchorStates==0) {
//                tvPlayErrorMsg.setVisibility(View.VISIBLE);
//                tvPlayErrorMsg.setText("主播还未开播");
//            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
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

    static final int UP_JOINNUM_ACT = 511;
    @Override
    public void upJoinUsernum(int modnum) {
        Message semsg = new Message();
        semsg.what = UP_JOINNUM_ACT;
        semsg.obj = modnum;
        myHandler.sendMessage(semsg);
    }

    /**
     * 取消进度条
     */
    public void cacerProgress() {
        if (mDialogProgress != null) {
            mDialogProgress.dismiss();
        }
    }


    /**
     * 发送获取直播状态
     * @param detailsCode 获取直播状态
     */
    void sendGetLiveStatusRequest(String detailsCode){
        HashMap<String, Object> hashMap = ParameUtil.buildBaseParam();
        hashMap.put("detailsCode",detailsCode);
        String s = RetrofitUtil.encodeParam(hashMap);
        ApiHelper.getLiveApi().getDetailsState(s).compose(Transformer.switchSchedulers())
                .subscribe(new CommonDataObserver() {
            @Override
            protected void onSuccessResult(BaseBean baseBean) {
                int resCode = baseBean.getResCode();
                if (resCode==1) {
                    LiveStatusBean liveStatusBean =
                            GsonUtils.fromJson(baseBean.getResJsonData(), LiveStatusBean.class);
                    if (liveStatusBean!=null) {
                        int flagAnchorStates = liveStatusBean.getFlagAnchorStates();
                        if (flagAnchorStates==0) {
                            tvPlayErrorMsg.setVisibility(View.VISIBLE);
                            tvPlayErrorMsg.setText("主播还未开播");
                        }
                    }

                }
            }
        });
    }

    void loadLive(){
        getProgressBar("打开直播间","正在打开直播间，请稍后...");
        OpenLiveCond opencond = new OpenLiveCond();
        opencond.setDetailsCode(mdetailCode);
        opencond.setLoginUserPosition(mApp.loginDoctorPosition);
        opencond.setOperUserCode(mApp.mViewSysUserDoctorInfoAndHospital.getDoctorCode());
        opencond.setOperUserName(mApp.mViewSysUserDoctorInfoAndHospital.getUserName());
        opencond.setRequestClientType("1");
        LoadLiveTask loadtask = new LoadLiveTask(opencond);
        loadtask.execute();
    }

    class LoadLiveTask extends AsyncTask<Void, Void, Boolean> {
        OpenLiveCond queCond;
        RoomDetailInfo queroominfo = null;
        LoadLiveTask(OpenLiveCond queCond){
            this.queCond =queCond;
        }
        @Override
        protected Boolean doInBackground(Void... voids) {
            try{
                String repjson = HttpNetService.urlConnectionService("jsonDataInfo="+new Gson().toJson(queCond),"https://www.jiuyihtn.com:41041/broadcastLiveDataControlle/getLiveRoomDetailsByDetailsCode");
                NetRetEntity retent = JSON.parseObject(repjson,NetRetEntity.class);
                if(retent.getResCode()==1){
                    String subrepjson = retent.getResJsonData();
                    RoomDetailInfo retliveresp = JSON.parseObject(subrepjson,RoomDetailInfo.class);
                    mychatid = retliveresp.getChatRoomCode();
                    playUrl = retliveresp.getPullUrl();
                    queroominfo = retliveresp;
                    mRoomDetailInfo=retliveresp;
                    return true;
                }
            }catch (Exception ex){
                ex.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if(aBoolean){
                if(null!=queroominfo){
                    myroominfo = queroominfo;
                }
            }
            cacerProgress();
        }
    }

    boolean isminal_screen = true;
    void initview(){
        if (mLivePlayer == null){
            mLivePlayer = new TXLivePlayer(this);
        }
        mPhoneListener = new TXPhoneStateListener(mLivePlayer);
        TelephonyManager tm = (TelephonyManager) getApplicationContext().getSystemService(Service.TELEPHONY_SERVICE);
        tm.listen(mPhoneListener, PhoneStateListener.LISTEN_CALL_STATE);
        rlHorizontalRoot=findViewById(R.id.rl_horizontal_root);
        tvPlayErrorMsg=findViewById(R.id.tv_play_error_msg);
        live_rl = findViewById(R.id.live_rl);
        tab_layout = findViewById(R.id.tab_layout);
        live_publish_page = findViewById(R.id.live_publish_page);
        mPlayerView = (TXCloudVideoView) findViewById(R.id.video_view);
        mIvLiveRoomShare=findViewById(R.id.iv_live_room_share);
        //video_hold_layout = findViewById(R.id.video_hold_layout);
        mPlayerView.disableLog(true);
        //mLivePlayer.setRenderRotation(mCurrentRenderRotation);
        //mLivePlayer.setRenderMode(mCurrentRenderMode);
        mLoadingView = (ImageView) findViewById(R.id.loadingImageView);
        mVideoPlay = startPlayRtmp();
        //btnMessage = findViewById(R.id.btnMessage);
        btnShare = findViewById(R.id.btnShare);
        btnShut = findViewById(R.id.btnShut);
        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                share_dialog();
            }
        });
        tab_hold_layout = findViewById(R.id.tab_hold_layout);
        mBtnOrientation = findViewById(R.id.btnOriention);
//        btnMessage.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                goChat();
//            }
//        });
        btnShut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
//                builder.setTitle("提示");
//                builder.setMessage("确认退出直播间吗?");
//                builder.setNegativeButton("取消", null);
//                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        LivePlayerTwoActivity.this.finish();
//                    }
//                });
//                builder.show();
                isminal_screen = true;
                goMinimalscreen();

            }
        });
        tv_liveroom_name = findViewById(R.id.tv_portrait_title_name);
        tv_landscape_liveroom_name=findViewById(R.id.tv_landscape_liveroom_name);
        findViewById(R.id.rl_back_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopPlayRtmp();
                mActivity.finish();
            }
        });
        findViewById(R.id.rl_landscape_back_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isminal_screen = true;
                goMinimalscreen();
            }
        });
        goTab();
        iv_zoom_btn = findViewById(R.id.iv_zoom_btn);
        iv_miniaml_zoom_btn = findViewById(R.id.iv_miniaml_zoom_btn);
        iv_zoom_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    isminal_screen = false;
                    goFullscreen();
            }
        });
        iv_miniaml_zoom_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isminal_screen = true;
                goMinimalscreen();
            }
        });
        mIvLiveRoomShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                share_dialog();
            }
        });
        live_rl.bringToFront();
        mPlayerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOrHide();
            }
        });

        //3、获取屏幕的默认分辨率
        /*Display display = getWindowManager().getDefaultDisplay();
        int scrheight = display.getHeight();
        int scrwid = display.getWidth();
        FrameLayout.LayoutParams parlivevparams = (FrameLayout.LayoutParams)mPlayerView.getLayoutParams();
        LinearLayout.LayoutParams tablayparams = (LinearLayout.LayoutParams)tab_layout.getLayoutParams();
        int subheighgt = parlivevparams.height+tablayparams.height;
        FrameLayout.LayoutParams pagerparams = new FrameLayout.LayoutParams(scrwid,scrheight-subheighgt);
        pagerparams.setMargins(0,scrheight-subheighgt,0,30);
        live_publish_page.setLayoutParams(pagerparams);*/
    }


    /**
     * 显示隐藏动画
     */
    protected void showOrHide() {
        Configuration mConfiguration = this.getResources().getConfiguration();
        int ori = mConfiguration.orientation; //获取屏幕方向
        if (ori == Configuration.ORIENTATION_LANDSCAPE) {
            if (rlHorizontalRoot.getVisibility() == View.VISIBLE) {
                hideAnim();
            } else {
                showAnim();
            }
        }else{

            if(live_rl.getVisibility()== View.VISIBLE){
                hideAnim();
            }else{
                showAnim();
            }
        }
    }

    /**
     * 隐藏动画
     */
    private void hideAnim(){
        Configuration mConfiguration = this.getResources().getConfiguration(); //获取设置的配置信息
        int ori = mConfiguration.orientation; //获取屏幕方向
        if (ori == Configuration.ORIENTATION_LANDSCAPE) {
            Animation animation1 = AnimationUtils.loadAnimation(this
                    , R.anim.anim_exit_bottom);
            animation1.setAnimationListener(new AnimationImp() {
                @Override
                public void onAnimationEnd(Animation animation) {
                    super.onAnimationEnd(animation);
                    //隐藏锚点

                    rlHorizontalRoot.setVisibility(View.GONE);

                }
            });
            rlHorizontalRoot.startAnimation(animation1);
        } else if (ori == Configuration.ORIENTATION_PORTRAIT) {
            Animation animation1 = AnimationUtils.loadAnimation(this
                    , R.anim.anim_exit_bottom);
            animation1.setAnimationListener(new AnimationImp() {
                @Override
                public void onAnimationEnd(Animation animation) {
                    super.onAnimationEnd(animation);
                    //隐藏锚点

                    live_rl.setVisibility(View.GONE);

                }
            });
            live_rl.startAnimation(animation1);

        }

    }


    /**
     * 展示动画
     */
    private void showAnim(){
        Configuration mConfiguration = this.getResources().getConfiguration(); //获取设置的配置信息
        int ori = mConfiguration.orientation; //获取屏幕方向

        if (ori == Configuration.ORIENTATION_LANDSCAPE) {
            if (rlHorizontalRoot.getVisibility() == View.GONE) {
                rlHorizontalRoot.setVisibility(View.VISIBLE);
                rlHorizontalRoot.clearAnimation();
                Animation animation = AnimationUtils.loadAnimation(this,
                        R.anim.anim_enter_top);
                rlHorizontalRoot.startAnimation(animation);
            }
        }else if (ori == Configuration.ORIENTATION_PORTRAIT) {
            if (live_rl.getVisibility()== View.GONE) {
                live_rl.setVisibility(View.VISIBLE);
                live_rl.clearAnimation();
                Animation animation = AnimationUtils.loadAnimation(this,
                        R.anim.anim_enter_top);
                live_rl.startAnimation(animation);
            }
        }

    }

    /**
     * 动画实现类
     */
    private class AnimationImp implements Animation.AnimationListener {

        @Override
        public void onAnimationEnd(Animation animation) {
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

        @Override
        public void onAnimationStart(Animation animation) {
        }

    }


    void share_dialog(){
        if(null==mRoomDetailInfo){
            Toast.makeText(mContext,"分享信息为空",Toast.LENGTH_SHORT).show();
            return;
        }
        shareDialog = new ShareDialog(mActivity);
        shareDialog.show();
        Glide.with(mContext).load(mRoomDetailInfo.getBroadcastUserLogoUrl())
                .apply(RequestOptions.placeholderOf(R.mipmap.def_head)
                        .diskCacheStrategy(DiskCacheStrategy.ALL))
                .into((CircleImageView)shareDialog.findViewById(R.id.iv_userhead));
        ((TextView)shareDialog.findViewById(R.id.tv_live_room_speaker))
                .setText(String.format("主讲人：%s", mRoomDetailInfo.getBroadcastUserName()));
        ((ImageView)shareDialog.findViewById(R.id.iv_gb)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareDialog.dismiss();
            }
        });
        ((ImageView)shareDialog.findViewById(R.id.save_pic_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "jiuyikeji.jpg");
                Bitmap bitmap = screenShot(mActivity);
                try {
                    if (!file.exists())
                        file.createNewFile();
                    boolean ret = save(bitmap, file, Bitmap.CompressFormat.JPEG, true);
                    if (ret) {
                        Toast.makeText(getApplicationContext(), "截图已保持至 " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
        ((ImageView)shareDialog.findViewById(R.id.link_pic_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copy_link();
                Toast.makeText(mContext,"已复制链接至剪贴板",Toast.LENGTH_SHORT).show();
            }
        });
        ((ImageView)shareDialog.findViewById(R.id.friend_pic_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToShare(SendMessageToWX.Req.WXSceneSession);
            }
        });
        ((ImageView)shareDialog.findViewById(R.id.webchat_pic_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToShare(SendMessageToWX.Req.WXSceneTimeline);
            }

        });
        TextView tvLiveRoomTitle = shareDialog.findViewById(R.id.tv_live_room_title);
        tvLiveRoomTitle.setText(mRoomDetailInfo.getBroadcastTitle());
        ImageView ivAuthorQrcode = shareDialog.findViewById(R.id.iv_author_qrcode);
        produceQrBitmap(mRoomDetailInfo.getShare(),ivAuthorQrcode);
        TextView tvLiveRoomTime = shareDialog.findViewById(R.id.tv_live_room_time);
        String date = DateUtils.getDateToStringYYYMMDDHHMM(mRoomDetailInfo.getBroadcastDate());
        tvLiveRoomTime.setText(date);
    }


    /**
     * 获取当前屏幕截图，不包含状态栏（Status Bar）。
     *
     * @param activity activity
     * @return Bitmap
     */
    public static Bitmap screenShot(Activity activity) {
        View view = activity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bmp = view.getDrawingCache();
        int statusBarHeight = getStatusBarHeight(activity);
        int width = (int) getDeviceDisplaySize(activity)[0];
        int height = (int) getDeviceDisplaySize(activity)[1];

        Bitmap ret = Bitmap.createBitmap(bmp, 0, statusBarHeight, width, height - statusBarHeight);
        view.destroyDrawingCache();

        return ret;
    }

    /**
     * 分享到微信
     *
     * @param type            SendMessageToWX.Req.WXSceneSession  //会话    SendMessageToWX.Req.WXSceneTimeline //朋友圈
     */
    public void ToShare(int type) {
        //i   0是会话  1是朋友圈
        if (isWeixinAvilible(mContext)) {
            Bitmap bitmap = screenShot(mActivity);
            WechatShareManager.ShareContentPicture mShareContent =
                    (WechatShareManager.ShareContentPicture) mShareManager.getShareContentPicture(R.mipmap.logo, bitmap);

            mShareManager.shareByWebchat(mShareContent, type);
        } else {
            Toast.makeText(mContext, "您还没有安装微信，请先安装微信客户端", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * 判断 用户是否安装微信客户端
     */
    public static boolean isWeixinAvilible(Context context) {
        final PackageManager packageManager = context.getPackageManager();// 获取packagemanager
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);// 获取所有已安装程序的包信息
        if (pinfo != null) {
            for (int i = 0; i < pinfo.size(); i++) {
                String pn = pinfo.get(i).packageName;
                if (pn.equals("com.tencent.mm")) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 生成二维码
     */
    void produceQrBitmap(String qrUrl,ImageView ivAuthorQuCode){
        Bitmap bitmap = NetWorkUtils.getHttpBitmap(qrUrl);
        new Thread(new Runnable() {
            @Override
            public void run() {
                Bitmap bm = QRCodeUtil.getImageBitmap(bitmap, qrUrl, 360);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ivAuthorQuCode.setImageBitmap(bm);
                    }
                });
            }
        }).start();
    }

    public static int getStatusBarHeight(Context context) {
        int height = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            height = context.getResources().getDimensionPixelSize(resourceId);
        }

        return height;
    }

    public static float[] getDeviceDisplaySize(Context context) {
        Resources resources = context.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        int width = dm.widthPixels;
        int height = dm.heightPixels;

        float[] size = new float[2];
        size[0] = width;
        size[1] = height;

        return size;
    }


    void copy_link(){
        //获取剪贴板管理器：
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
// 创建普通字符型ClipData
        //ClipData mClipData = ClipData.newPlainText("Label", "这里是要复制的文字");
        ClipData mClipData = ClipData.newRawUri(mRoomDetailInfo.getBroadcastTitle(), Uri.parse(mRoomDetailInfo.getPullUrl()));
        cm.setPrimaryClip(mClipData);
    }

    /**
     * 保存图片到文件File。
     *
     * @param src     源图片
     * @param file    要保存到的文件
     * @param format  格式
     * @param recycle 是否回收
     * @return true 成功 false 失败
     */
    public static boolean save(Bitmap src, File file, Bitmap.CompressFormat format, boolean recycle) {
        if (isEmptyBitmap(src))
            return false;

        OutputStream os;
        boolean ret = false;
        try {
            os = new BufferedOutputStream(new FileOutputStream(file));
            ret = src.compress(format, 100, os);
            if (recycle && !src.isRecycled())
                src.recycle();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ret;
    }


    /**
     * Bitmap对象是否为空。
     */
    public static boolean isEmptyBitmap(Bitmap src) {
        return src == null || src.getWidth() == 0 || src.getHeight() == 0;
    }

    @Override
    protected void onAnchorLeave() {
        super.onAnchorLeave();
        tvPlayErrorMsg.setVisibility(View.VISIBLE);
        tvPlayErrorMsg.setText("主播已经离开");
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    void goFullscreen(){
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        RelativeLayout.LayoutParams fullparams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
        mPlayerView.setLayoutParams(fullparams);
        //mLivePlayer.setRenderMode(TXLiveConstants.RENDER_MODE_ADJUST_RESOLUTION);
        mLivePlayer.setRenderRotation(TXLiveConstants.RENDER_ROTATION_PORTRAIT);
        tab_hold_layout.setVisibility(View.GONE);
        live_publish_page.setVisibility(View.GONE);
        live_rl.setVisibility(View.GONE);
        iv_zoom_btn.setVisibility(View.GONE);
        iv_miniaml_zoom_btn.setVisibility(View.VISIBLE);
        rlHorizontalRoot.setVisibility(View.VISIBLE);
        //mLivePlayer.setRenderRotation(TXLiveConstants.RENDER_ROTATION_LANDSCAPE);
        //mPlayerView.setRenderMode(TXLiveConstants.RENDER_MODE_FULL_FILL_SCREEN);
        //mPlayerView.setRenderMode(TXLiveConstants.RENDER_MODE_ADJUST_RESOLUTION);
        /*mLivePlayer.setRenderMode(TXLiveConstants.RENDER_MODE_FULL_FILL_SCREEN);
            mLivePlayer.setRenderRotation(TXLiveConstants.RENDER_ROTATION_LANDSCAPE);
            if(mLivePlayer.isPlaying()){
                mLivePlayer.resume();
        }*/
    }
    void goMinimalscreen(){
        //mLivePlayer.setRenderMode(TXLiveConstants.RENDER_MODE_ADJUST_RESOLUTION);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        RelativeLayout.LayoutParams fullparams = new RelativeLayout.LayoutParams(videowidth,videoheight);
        mPlayerView.setLayoutParams(fullparams);
        mLivePlayer.setRenderRotation(TXLiveConstants.RENDER_ROTATION_PORTRAIT);
        tab_hold_layout.setVisibility(View.VISIBLE);
        live_publish_page.setVisibility(View.VISIBLE);
        live_rl.setVisibility(View.VISIBLE);
        rlHorizontalRoot.setVisibility(View.GONE);
        //mLivePlayer.setRenderRotation(TXLiveConstants.RENDER_ROTATION_PORTRAIT);
        //mPlayerView.setRenderMode(TXLiveConstants.RENDER_MODE_FULL_FILL_SCREEN);

        /*FrameLayout.LayoutParams fullparams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,R.dimen.dp_340);
        mPlayerView.setLayoutParams(fullparams);*/
        /*mLivePlayer.setRenderMode(TXLiveConstants.RENDER_MODE_ADJUST_RESOLUTION);
        mLivePlayer.setRenderRotation(TXLiveConstants.RENDER_ROTATION_PORTRAIT);
        if(mLivePlayer.isPlaying()){
            mLivePlayer.resume();
        }*/
        iv_zoom_btn.setVisibility(View.VISIBLE);
        iv_miniaml_zoom_btn.setVisibility(View.GONE);
        //live_rl.bringToFront();
    }
    private List<Fragment> fragmentList = new ArrayList();
    private LiveFragmentAdapter liveFragmentAdapter;
    private List<String> mTitles = new ArrayList();
    IntroductionFragment introductionFragment = new IntroductionFragment();
    LiveChatFragment liveChatFragment = new LiveChatFragment();
    void goTab(){
        //if(null==liveFragmentAdapter) {
            mTitles.add("简介");
            mTitles.add("互动");
            introductionFragment = new IntroductionFragment();
            fragmentList.add(LiveProgromFragment.newInstance(mdetailCode));
            liveChatFragment = new LiveChatFragment();
            fragmentList.add(liveChatFragment);
            liveFragmentAdapter = new LiveFragmentAdapter(getSupportFragmentManager(), fragmentList, mTitles);
            live_publish_page.setAdapter(liveFragmentAdapter);
            tab_layout.setupWithViewPager(live_publish_page);
        //}
    }

    boolean isopenchat = false;
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
        parbund.putString(EaseConstant.EXTRA_CHAT_TYPE,"");
        parbund.putInt(EaseConstant.CHAT_TYPE, EaseConstant.CHATTYPE_CHATROOM);
        parbund.putString(EaseConstant.EXTRA_USER_ID, mychatid);
        parbund.putString(EaseConstant.EXTRA_USER_NAME, mychatid);
        initChat(parbund);
        initChatHeadView();
        //initChatView();
        initFragmentChatView(liveChatFragment);
        chatViewLayout.setVisibility(View.VISIBLE);
        joinChatroom();
        setUpView();
        isopenchat = true;
        messageList.setShowChatRoomNew(true);
        hideEnojicon();
        inputMenu.setVisibility(View.GONE);
        rlInputRoot.setVisibility(View.VISIBLE);
    }

    static final int GO_CHAT_ACT = 999;
    static final int LOGIN_CHAT_FAIL = 997;
    @Override
    public void createChat() {
        LogimImTask logimImTask = new LogimImTask();
        logimImTask.execute();
    }

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
                //msgnamesb = new StringBuffer();
                EMTextMessageBody txtBody = (EMTextMessageBody) paramMessage.getBody();
                //Spannable span = EaseSmileUtils.(mContext, txtBody.getMessage());
                String msguser = "";
                if(paramMessage.direct() == EMMessage.Direct.RECEIVE){
                    msguser = paramMessage.getFrom();
                }else{
                    msguser = paramMessage.getTo();
                }
                if(toChatUsername.equals(msguser)){
                    msguser = mApp.mViewSysUserDoctorInfoAndHospital.getUserName();
                }
                if(StrUtils.defaulObjToStr(txtBody.getMessage()).endsWith("加入直播间了") || StrUtils.defaulObjToStr(txtBody.getMessage()).endsWith("离开直播间了")) {
                    msgnamesb.append(msguser).append(txtBody.getMessage());
                }
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

    @Override
    public void onBackPressed() {
        stopPlayRtmp();
        super.onBackPressed();
    }

    private  void stopPlayRtmp() {
        stopLoadingAnimation();
        if (mLivePlayer != null) {
            mLivePlayer.stopRecord();
            mLivePlayer.setPlayListener(null);
            mLivePlayer.stopPlay(true);
        }
        mVideoPause = false;
        mVideoPlay = false;
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
            if (permissions.size() != 0) {
                ActivityCompat.requestPermissions(this,
                        permissions.toArray(new String[0]),
                        100);
                return false;
            }
        }

        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mLivePlayer != null) {
            mLivePlayer.stopPlay(true);
            mLivePlayer = null;
        }
        if (mPlayerView != null){
            mPlayerView.onDestroy();
            mPlayerView = null;
        }
        mPlayConfig = null;
        Log.d(TAG,"vrender onDestroy");
        TelephonyManager tm = (TelephonyManager) getApplicationContext().getSystemService(Service.TELEPHONY_SERVICE);
        tm.listen(mPhoneListener, PhoneStateListener.LISTEN_NONE);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop(){
        super.onStop();

        if (mPlayType == TXLivePlayer.PLAY_TYPE_VOD_FLV || mPlayType == TXLivePlayer.PLAY_TYPE_VOD_HLS || mPlayType == TXLivePlayer.PLAY_TYPE_VOD_MP4 || mPlayType == TXLivePlayer.PLAY_TYPE_LOCAL_VIDEO) {
            if (mLivePlayer != null) {
                mLivePlayer.pause();
            }
        } else {
            if (mPlayerView != null){
                mPlayerView.onPause();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mVideoPlay && !mVideoPause) {
            if (mPlayType == TXLivePlayer.PLAY_TYPE_VOD_FLV || mPlayType == TXLivePlayer.PLAY_TYPE_VOD_HLS || mPlayType == TXLivePlayer.PLAY_TYPE_VOD_MP4 || mPlayType == TXLivePlayer.PLAY_TYPE_LOCAL_VIDEO) {
                if (mLivePlayer != null) {
                    mLivePlayer.resume();
                }
            }
            else {
                //startPlayRtmp();
                if (mPlayerView != null){
                    mPlayerView.onResume();
                }
            }
        }
    }

    @Override
    public void onClick(View v) {

    }

    /**
     * 获取内置SD卡路径
     * @return
     */
    public String getInnerSDCardPath() {
        return Environment.getExternalStorageDirectory().getPath();
    }


    private boolean startPlayRtmp() {

//        //由于iOS AppStore要求新上架的app必须使用https,所以后续腾讯云的视频连接会支持https,但https会有一定的性能损耗,所以android将统一替换会http
//        if (playUrl.startsWith("https://")) {
//            playUrl = "http://" + playUrl.substring(8);
//        }

        if (!checkPlayUrl(playUrl)) {
            return false;
        }


        mLivePlayer.setAudioRawDataListener(new TXLivePlayer.ITXAudioRawDataListener() {
            @Override
            public void onPcmDataAvailable(byte[] bytes, long l) {
              //  LogUtils.e("音频   "+bytes.length);
            }

            @Override
            public void onAudioInfoChanged(int i, int i1, int i2) {
//                LogUtils.e("音频 1111  "+i);
//                LogUtils.e("音频 2222  "+i1);
//                LogUtils.e("音频 3333  "+i2);
            }
        });

        mLivePlayer.setPlayerView(mPlayerView);
        /*Display display = getWindowManager().getDefaultDisplay();
        int height = display.getWidth();
        int width = display.getHeight();
        mLivePlayer.setSurfaceSize(width,R.dimen.dp_340);*/
        mLivePlayer.setPlayListener(this);
//        mLivePlayer.setRate(1.5f);
        // 硬件加速在1080p解码场景下效果显著，但细节之处并不如想象的那么美好：
        // (1) 只有 4.3 以上android系统才支持
        // (2) 兼容性我们目前还仅过了小米华为等常见机型，故这里的返回值您先不要太当真
        mLivePlayer.enableHardwareDecode(mHWDecode);
        //mLivePlayer.setRenderRotation(mCurrentRenderRotation);
        //mLivePlayer.setRenderMode(mCurrentRenderMode);
        //mPlayerView.setRenderMode(mCurrentRenderMode);
        //mPlayerView.setRenderRotation(mCurrentRenderRotation);
        //设置播放器缓存策略
        //这里将播放器的策略设置为自动调整，调整的范围设定为1到4s，您也可以通过setCacheTime将播放器策略设置为采用
        //固定缓存时间。如果您什么都不调用，播放器将采用默认的策略（默认策略为自动调整，调整范围为1到4s）
        //mLivePlayer.setCacheTime(5);

        if (mEnableCache) {
            mPlayConfig.setCacheFolderPath(getInnerSDCardPath()+"/txcache");
            mPlayConfig.setMaxCacheItems(2);
        } else {
            mPlayConfig.setCacheFolderPath(null);
        }

        mLivePlayer.setConfig(mPlayConfig);
        mLivePlayer.setConfig(mPlayConfig);
        mLivePlayer.setAutoPlay(true);

        int result = mLivePlayer.startPlay(playUrl,mPlayType); // result返回值：0 success;  -1 empty url; -2 invalid url; -3 invalid playType;

        startLoadingAnimation();

        mStartPlayTS = System.currentTimeMillis();

        if (mActivityType == LivePlayerActivity.ACTIVITY_TYPE_VOD_PLAY) {
            findViewById(R.id.playerHeaderView).setVisibility(View.VISIBLE);
        }
        RelativeLayout.LayoutParams mlayoutparams = (RelativeLayout.LayoutParams)mPlayerView.getLayoutParams();
        videowidth = mlayoutparams.width;
        videoheight = mlayoutparams.height;
        return true;
    }

    @Override
    public void onPlayEvent(int event, Bundle param) {
        if (event == TXLiveConstants.PLAY_EVT_PLAY_BEGIN) {
            stopLoadingAnimation();
            Log.d("AutoMonitor", "PlayFirstRender,cost=" +(System.currentTimeMillis()-mStartPlayTS));
        } else if (event == TXLiveConstants.PLAY_EVT_PLAY_PROGRESS ) {
            int progress = param.getInt(TXLiveConstants.EVT_PLAY_PROGRESS);
            int duration = param.getInt(TXLiveConstants.EVT_PLAY_DURATION);
            int playable = param.getInt(TCConstants.NET_STATUS_PLAYABLE_DURATION);
            long curTS = System.currentTimeMillis();

            // 避免滑动进度条松开的瞬间可能出现滑动条瞬间跳到上一个位置
            if (Math.abs(curTS - mTrackingTouchTS) < 500) {
                return;
            }
            mTrackingTouchTS = curTS;
            return;
        } else if (event == TXLiveConstants.PLAY_ERR_NET_DISCONNECT || event == TXLiveConstants.PLAY_EVT_PLAY_END) {
            stopPlayRtmp();
            mVideoPlay = false;
            mVideoPause = false;
            onAnchorLeave();
            //ToastUtils.showToast("主播退出了，主播退出了");

        } else if (event == TXLiveConstants.PLAY_EVT_PLAY_LOADING){
            startLoadingAnimation();
        } else if (event == TXLiveConstants.PLAY_EVT_RCV_FIRST_I_FRAME) {
            stopLoadingAnimation();
            if (mActivityType == LivePlayerActivity.ACTIVITY_TYPE_VOD_PLAY) {
                findViewById(R.id.playerHeaderView).setVisibility(View.GONE);
            }
        } else if (event == TXLiveConstants.PLAY_EVT_CHANGE_RESOLUTION) {
            //streamRecord(false);
        }

        String msg = param.getString(TXLiveConstants.EVT_DESCRIPTION);
//        if(mLivePlayer != null){
//            mLivePlayer.onLogRecord("[event:"+event+"]"+msg+"\n");
//        }
        if (event < 0) {
           // Toast.makeText(getApplicationContext(), param.getString(TXLiveConstants.EVT_DESCRIPTION), Toast.LENGTH_SHORT).show();
        }

        else if (event == TXLiveConstants.PLAY_EVT_PLAY_BEGIN) {
            stopLoadingAnimation();
        }
    }

    //公用打印辅助函数
    protected String getNetStatusString(Bundle status) {
        String str = String.format("%-14s %-14s %-12s\n%-14s %-14s %-12s\n%-14s %-14s %-12s\n%-14s %-12s %-12s",
                "CPU:"+status.getString(TXLiveConstants.NET_STATUS_CPU_USAGE),
                "RES:"+status.getInt(TXLiveConstants.NET_STATUS_VIDEO_WIDTH)+"*"+status.getInt(TXLiveConstants.NET_STATUS_VIDEO_HEIGHT),
                "SPD:"+status.getInt(TXLiveConstants.NET_STATUS_NET_SPEED)+"Kbps",
                "JIT:"+status.getInt(TXLiveConstants.NET_STATUS_NET_JITTER),
                "FPS:"+status.getInt(TXLiveConstants.NET_STATUS_VIDEO_FPS),
                "ARA:"+status.getInt(TXLiveConstants.NET_STATUS_AUDIO_BITRATE)+"Kbps",
                "QUE:"+status.getInt(TCConstants.NET_STATUS_CODEC_CACHE)+"|"+status.getInt(TCConstants.NET_STATUS_CACHE_SIZE),
                "DRP:"+status.getInt(TCConstants.NET_STATUS_CODEC_DROP_CNT)+"|"+status.getInt(TCConstants.NET_STATUS_DROP_SIZE),
                "VRA:"+status.getInt(TXLiveConstants.NET_STATUS_VIDEO_BITRATE)+"Kbps",
                "SVR:"+status.getString(TXLiveConstants.NET_STATUS_SERVER_IP),
                "AVRA:"+status.getInt(TCConstants.NET_STATUS_SET_VIDEO_BITRATE),
                "PLA:"+status.getInt(TCConstants.NET_STATUS_PLAYABLE_DURATION));
        return str;
    }

    @Override
    public void onNetStatus(Bundle status) {
        String str = getNetStatusString(status);
        Log.d(TAG, "Current status, CPU:"+status.getString(TXLiveConstants.NET_STATUS_CPU_USAGE)+
                ", RES:"+status.getInt(TXLiveConstants.NET_STATUS_VIDEO_WIDTH)+"*"+status.getInt(TXLiveConstants.NET_STATUS_VIDEO_HEIGHT)+
                ", SPD:"+status.getInt(TXLiveConstants.NET_STATUS_NET_SPEED)+"Kbps"+
                ", FPS:"+status.getInt(TXLiveConstants.NET_STATUS_VIDEO_FPS)+
                ", ARA:"+status.getInt(TXLiveConstants.NET_STATUS_AUDIO_BITRATE)+"Kbps"+
                ", VRA:"+status.getInt(TXLiveConstants.NET_STATUS_VIDEO_BITRATE)+"Kbps");
        //Log.d(TAG, "Current status: " + status.toString());
//        if (mLivePlayer != null){
//            mLivePlayer.onLogRecord("[net state]:\n"+str+"\n");
//        }
    }

    public void setCacheStrategy(int nCacheStrategy) {
        if (mCacheStrategy == nCacheStrategy)   return;
        mCacheStrategy = nCacheStrategy;

        switch (nCacheStrategy) {
            case CACHE_STRATEGY_FAST:
                mPlayConfig.setAutoAdjustCacheTime(true);
                mPlayConfig.setMaxAutoAdjustCacheTime(CACHE_TIME_FAST);
                mPlayConfig.setMinAutoAdjustCacheTime(CACHE_TIME_FAST);
                mLivePlayer.setConfig(mPlayConfig);
                break;

            case CACHE_STRATEGY_SMOOTH:
                mPlayConfig.setAutoAdjustCacheTime(false);
                mPlayConfig.setCacheTime(CACHE_TIME_SMOOTH);
                mLivePlayer.setConfig(mPlayConfig);
                break;

            case CACHE_STRATEGY_AUTO:
                mPlayConfig.setAutoAdjustCacheTime(true);
                mPlayConfig.setMaxAutoAdjustCacheTime(CACHE_TIME_SMOOTH);
                mPlayConfig.setMinAutoAdjustCacheTime(CACHE_TIME_FAST);
                mLivePlayer.setConfig(mPlayConfig);
                break;

            default:
                break;
        }
    }

    private boolean checkPlayUrl(final String playUrl) {
        if (TextUtils.isEmpty(playUrl) || (!playUrl.startsWith("http://") && !playUrl.startsWith("https://") && !playUrl.startsWith("rtmp://")  && !playUrl.startsWith("/"))) {
            if (mActivityType == ACTIVITY_TYPE_LIVE_PLAY) {
                Toast.makeText(getApplicationContext(), "播放地址不合法，直播目前仅支持rtmp,flv播放方式!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "播放地址不合法，目前仅支持flv,hls,mp4播放方式和本地播放方式（绝对路径，如\"/sdcard/test.mp4\"）!", Toast.LENGTH_SHORT).show();
            }

            return false;
        }

        switch (mActivityType) {
            case ACTIVITY_TYPE_LIVE_PLAY:
            {
                if (playUrl.startsWith("rtmp://")) {
                    mPlayType = TXLivePlayer.PLAY_TYPE_LIVE_RTMP;
                } else if ((playUrl.startsWith("http://") || playUrl.startsWith("https://"))&& playUrl.contains(".flv")) {
                    mPlayType = TXLivePlayer.PLAY_TYPE_LIVE_FLV;
                } else {
                    Toast.makeText(getApplicationContext(), "播放地址不合法，直播目前仅支持rtmp,flv播放方式!", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
            break;
            case ACTIVITY_TYPE_VOD_PLAY:
            {
                if (playUrl.startsWith("http://") || playUrl.startsWith("https://")) {
                    if (playUrl.contains(".flv")) {
                        mPlayType = TXLivePlayer.PLAY_TYPE_VOD_FLV;
                    } else if (playUrl.contains(".m3u8")) {
                        mPlayType = TXLivePlayer.PLAY_TYPE_VOD_HLS;
                    } else if (playUrl.toLowerCase().contains(".mp4")) {
                        mPlayType = TXLivePlayer.PLAY_TYPE_VOD_MP4;
                    } else {
                        Toast.makeText(getApplicationContext(), "播放地址不合法，点播目前仅支持flv,hls,mp4播放方式!", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                } else if (playUrl.startsWith("/")) {
                    if (playUrl.contains(".mp4") || playUrl.contains(".flv")) {
                        mPlayType = TXLivePlayer.PLAY_TYPE_LOCAL_VIDEO;
                    } else {
                        Toast.makeText(getApplicationContext(), "播放地址不合法，目前本地播放器仅支持播放mp4，flv格式文件", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "播放地址不合法，点播目前仅支持flv,hls,mp4播放方式!", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
            break;
            default:
                Toast.makeText(getApplicationContext(), "播放地址不合法，目前仅支持rtmp,flv,hls,mp4播放方式!", Toast.LENGTH_SHORT).show();
                return false;
        }
        return true;
    }



    private void startLoadingAnimation() {
        if (mLoadingView != null) {
            mLoadingView.setVisibility(View.VISIBLE);
            ((AnimationDrawable)mLoadingView.getDrawable()).start();
        }
    }

    private void stopLoadingAnimation() {
        if (mLoadingView != null) {
            mLoadingView.setVisibility(View.GONE);
            ((AnimationDrawable)mLoadingView.getDrawable()).stop();
        }
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
        }else{
            if (isopenchat) {
                if (chatViewLayout.getVisibility() == View.VISIBLE) {
                    chatViewLayout.setVisibility(View.GONE);
                } else {
                    chatViewLayout.setVisibility(View.VISIBLE);
                }
            } else {
                try {
                    createChat();
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        }
    }

    static class TXPhoneStateListener extends PhoneStateListener {
        WeakReference<TXLivePlayer> mPlayer;
        public TXPhoneStateListener(TXLivePlayer player) {
            mPlayer = new WeakReference<TXLivePlayer>(player);
        }
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            TXLivePlayer player = mPlayer.get();
            switch(state){
                //电话等待接听
                case TelephonyManager.CALL_STATE_RINGING:
                    if (player != null) player.setMute(true);
                    break;
                //电话接听
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    if (player != null) player.setMute(true);
                    break;
                //电话挂机
                case TelephonyManager.CALL_STATE_IDLE:
                    if (player != null) player.setMute(false);
                    break;
            }
        }
    };
}

