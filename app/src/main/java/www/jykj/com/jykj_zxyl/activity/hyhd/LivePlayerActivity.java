package www.jykj.com.jykj_zxyl.activity.hyhd;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.ContextMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.easeui.EaseConstant;
import com.hyphenate.easeui.utils.ExtEaseUtils;
import com.jykj.live.lvb.common.activity.QRCodeScanActivity;
import com.tencent.rtmp.*;
import com.tencent.rtmp.ui.TXCloudVideoView;
import com.tencent.ugc.TXRecordCommon;
import www.jykj.com.jykj_zxyl.R;
import www.jykj.com.jykj_zxyl.adapter.HeadImageViewRecycleAdapter;
import www.jykj.com.jykj_zxyl.application.JYKJApplication;
import www.jykj.com.jykj_zxyl.util.CircleImageView;
import www.jykj.com.jykj_zxyl.util.StrUtils;
import www.jykj.com.jykj_zxyl.util.TCConstants;
import ztextviewlib.MarqueeTextView;

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class LivePlayerActivity extends ChatPopDialogActivity implements ITXLivePlayListener, OnClickListener {
    private static final String TAG = LivePlayerActivity.class.getSimpleName();

    private TXLivePlayer mLivePlayer = null;
    private boolean          mVideoPlay;
    private TXCloudVideoView mPlayerView;
    private ImageView mLoadingView;
    private boolean          mHWDecode   = false;
    private android.support.constraint.ConstraintLayout     mRootView;

    private Button mBtnLog;
    private Button mBtnPlay;
    private Button mBtnRenderRotation;
    private Button mBtnRenderMode;
    private Button mBtnHWDecode;
    private ScrollView mScrollView;
    private SeekBar mSeekBar;
    private TextView mTextDuration;
    private TextView mTextStart;

    private static final int  CACHE_STRATEGY_FAST  = 1;  //极速
    private static final int  CACHE_STRATEGY_SMOOTH = 2;  //流畅
    private static final int  CACHE_STRATEGY_AUTO = 3;  //自动

    private static final float  CACHE_TIME_FAST = 1.0f;
    private static final float  CACHE_TIME_SMOOTH = 5.0f;

    private static final float  CACHE_TIME_AUTO_MIN = 5.0f;
    private static final float  CACHE_TIME_AUTO_MAX = 10.0f;

    public static final int ACTIVITY_TYPE_PUBLISH      = 1;
    public static final int ACTIVITY_TYPE_LIVE_PLAY    = 2;
    public static final int ACTIVITY_TYPE_VOD_PLAY     = 3;
    public static final int ACTIVITY_TYPE_LINK_MIC     = 4;

    private int              mCacheStrategy = 0;
    private Button mBtnCacheStrategy;
    private Button mRatioFast;
    private Button mRatioSmooth;
    private Button mRatioAuto;
    private Button mBtnStop;
    private Button mBtnCache;
    private LinearLayout mLayoutCacheStrategy;
    protected EditText mRtmpUrlView;
    public TextView mLogViewStatus;
    public TextView mLogViewEvent;
    protected StringBuffer mLogMsg = new StringBuffer("");
    private final int mLogMsgLenLimit = 3000;

    private int              mCurrentRenderMode;
    private int              mCurrentRenderRotation;

    private long             mTrackingTouchTS = 0;
    private boolean          mStartSeek = false;
    private boolean          mVideoPause = false;
    private int              mPlayType = TXLivePlayer.PLAY_TYPE_LIVE_RTMP;
    private TXLivePlayConfig mPlayConfig;
    private long             mStartPlayTS = 0;
    protected int            mActivityType;
    private ProgressBar mRecordProgressBar;
    private TextView mRecordTimeTV;
    private boolean mRecordFlag = false;
    private boolean mCancelRecordFlag = false;
    private boolean mEnableCache;
    private String mychatid = "113582047035393";
    CircleImageView iv_live_user_head;
    RecyclerView chat_head_imgs;
    TextView tv_chat_num;
    MarqueeTextView mv_chat_content;
    LinearLayoutManager mLayoutManager;
    HeadImageViewRecycleAdapter mImageViewRecycleAdapter;
    List<String> headpics = new ArrayList();
    TextView tv_head_tit;
    Context mContext;
    JYKJApplication mApp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApp = (JYKJApplication)getApplication();
        mContext = LivePlayerActivity.this;
        String sdkver = TXLiveBase.getSDKVersionStr();
        Log.d("liteavsdk", "liteav sdk version is : " + sdkver);
        mCurrentRenderMode     = TXLiveConstants.RENDER_MODE_ADJUST_RESOLUTION;
        mCurrentRenderRotation = TXLiveConstants.RENDER_ROTATION_PORTRAIT;

        mActivityType = getIntent().getIntExtra("PLAY_TYPE", ACTIVITY_TYPE_LIVE_PLAY);

        mPlayConfig = new TXLivePlayConfig();


        setContentView();

        LinearLayout backLL = (LinearLayout)findViewById(R.id.back_ll);
        backLL.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                stopPlayRtmp();
                finish();
            }
        });
        TextView titleTV = (TextView) findViewById(R.id.title_tv);
        titleTV.setText(getIntent().getStringExtra("TITLE"));

        checkPublishPermission();

        registerForContextMenu(findViewById(R.id.btnPlay));
        //createChat();
    }

    @Override
    public void upJoinUsernum(int modnum) {

    }

    boolean isopenchat = false;
    static final int GO_CHAT_ACT = 999;
    static final int LOGIN_CHAT_FAIL = 997;
    @Override
    public void createChat() {
        Bundle parbund = new Bundle();
        parbund.putString(EaseConstant.EXTRA_CHAT_TYPE,"twjz");
        parbund.putInt(EaseConstant.CHAT_TYPE, EaseConstant.CHATTYPE_CHATROOM);
        parbund.putString(EaseConstant.EXTRA_USER_ID, mychatid);
        parbund.putString(EaseConstant.EXTRA_USER_NAME, mychatid);
        initChat(parbund);
        initChatView();
        joinChatroom();
        setUpView();
        isopenchat = true;
    }

    StringBuffer msgnamesb = new StringBuffer();

    @Override
    public void showMessages(EMMessage paramMessage) {
        try {
            String parname = StrUtils.defaulObjToStr(paramMessage.getStringAttribute("nickName"));
            String parhead = StrUtils.defaulObjToStr(paramMessage.getStringAttribute("imageUrl"));

            if(parname.length()>0){
                msgnamesb.append("&#160;&#160;&#160;&#160;");
                EMTextMessageBody txtBody = (EMTextMessageBody) paramMessage.getBody();
                //Spannable span = EaseSmileUtils.(mContext, txtBody.getMessage());
                msgnamesb.append(txtBody.getMessage());
                // 设置内容
                mv_chat_content.setText(msgnamesb);
            }
            if(parhead.length()>0){
                headpics.add(parhead);
                mImageViewRecycleAdapter.setDate(headpics);
                mImageViewRecycleAdapter.notifyDataSetChanged();
                tv_chat_num.setText(String.valueOf(headpics.size())+"人");
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    void goChat(){
        if(isopenchat){
            if(chatViewLayout.getVisibility()== View.VISIBLE) {
                chatViewLayout.setVisibility(View.GONE);
            }else{
                chatViewLayout.setVisibility(View.VISIBLE);
            }
        }else{
            createChat();
            chatViewLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        stopPlayRtmp();
        super.onBackPressed();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        getMenuInflater().inflate(R.menu.player_menu, menu);
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
    void initView() {
        mRtmpUrlView   = (EditText) findViewById(R.id.roomid);
        mLogViewEvent  = (TextView) findViewById(R.id.logViewEvent);
        mLogViewStatus = (TextView) findViewById(R.id.logViewStatus);

        Button scanBtn = (Button)findViewById(R.id.btnScan);
        scanBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LivePlayerActivity.this, QRCodeScanActivity.class);
                startActivityForResult(intent, 100);
            }
        });
        scanBtn.setEnabled(true);
    }


    public static void scroll2Bottom(final ScrollView scroll, final View inner) {
        if (scroll == null || inner == null) {
            return;
        }
        int offset = inner.getMeasuredHeight() - scroll.getMeasuredHeight();
        if (offset < 0) {
            offset = 0;
        }
        scroll.scrollTo(0, offset);
    }

    public void setContentView() {
        super.setContentView(R.layout.activity_play);
        initView();
        iv_live_user_head = findViewById(R.id.iv_live_user_head);
        chat_head_imgs = findViewById(R.id.chat_head_imgs);
        tv_chat_num = findViewById(R.id.tv_chat_num);
        mv_chat_content = findViewById(R.id.mv_chat_content);
        tv_head_tit = findViewById(R.id.tv_head_tit);
        mLayoutManager = new LinearLayoutManager(mContext);
        mLayoutManager.setOrientation(LinearLayout.HORIZONTAL);
        chat_head_imgs.setLayoutManager(mLayoutManager);
        chat_head_imgs.setHasFixedSize(true);
        mImageViewRecycleAdapter = new HeadImageViewRecycleAdapter(headpics,mApp);
        chat_head_imgs.setAdapter(mImageViewRecycleAdapter);
        Glide.with(mContext).load(mApp.mViewSysUserDoctorInfoAndHospital.getUserLogoUrl())
                .apply(RequestOptions.placeholderOf(com.hyphenate.easeui.R.mipmap.docter_heard)
                        .diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(iv_live_user_head);
        String parnickname = ExtEaseUtils.getInstance().getNickName();
        tv_head_tit.setText(parnickname);
        mRootView = (android.support.constraint.ConstraintLayout) findViewById(R.id.root);
        if (mLivePlayer == null){
            mLivePlayer = new TXLivePlayer(this);
        }

        mPhoneListener = new TXPhoneStateListener(mLivePlayer);
        TelephonyManager tm = (TelephonyManager) getApplicationContext().getSystemService(Service.TELEPHONY_SERVICE);
        tm.listen(mPhoneListener, PhoneStateListener.LISTEN_CALL_STATE);

        mPlayerView = (TXCloudVideoView) findViewById(R.id.video_view);
        mPlayerView.disableLog(true);
        mLoadingView = (ImageView) findViewById(R.id.loadingImageView);


        mRtmpUrlView.setHint(" 请输入或扫二维码获取播放地址");
        mRtmpUrlView.setText("");

        mVideoPlay = false;
        mLogViewStatus.setVisibility(View.GONE);
        mLogViewStatus.setMovementMethod(new ScrollingMovementMethod());
        mLogViewEvent.setMovementMethod(new ScrollingMovementMethod());
        mScrollView = (ScrollView)findViewById(R.id.scrollview);
        mScrollView.setVisibility(View.GONE);
        mRecordProgressBar = (ProgressBar) findViewById(R.id.record_progress);
        mRecordTimeTV = (TextView) findViewById(R.id.record_time);

        mBtnPlay = (Button) findViewById(R.id.btnPlay);
        mBtnPlay.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "click playbtn isplay:" + mVideoPlay+" ispause:"+mVideoPause+" playtype:"+mPlayType);
                if (mVideoPlay) {
                    if (mPlayType == TXLivePlayer.PLAY_TYPE_VOD_FLV || mPlayType == TXLivePlayer.PLAY_TYPE_VOD_HLS || mPlayType == TXLivePlayer.PLAY_TYPE_VOD_MP4 || mPlayType == TXLivePlayer.PLAY_TYPE_LOCAL_VIDEO) {
                        if (!mLivePlayer.isPlaying()) {
                            mLivePlayer.resume();
                            mBtnPlay.setBackgroundResource(R.mipmap.play_pause);
                            mRootView.setBackgroundColor(0xff000000);
                        } else {
                            mLivePlayer.pause();
                            mBtnPlay.setBackgroundResource(R.mipmap.play_start);
                        }
                        mVideoPause = !mVideoPause;

                    } else {
                        stopPlayRtmp();
                    }

                } else {
                    mVideoPlay = startPlayRtmp();
                }
            }
        });

        //停止按钮
        mBtnStop = (Button) findViewById(R.id.btnStop);
        mBtnStop.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                stopPlayRtmp();
                mVideoPlay = false;
                mVideoPause = false;
                if (mTextStart != null) {
                    mTextStart.setText("00:00");
                }
                if (mSeekBar != null) {
                    mSeekBar.setProgress(0);
                    mSeekBar.setSecondaryProgress(0);
                }
            }
        });

        mBtnLog = (Button) findViewById(R.id.btnLog);
        mBtnLog.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                /*if (mLogViewStatus.getVisibility() == View.GONE) {
                    mLogViewStatus.setVisibility(View.VISIBLE);
                    mScrollView.setVisibility(View.VISIBLE);
                    mLogViewEvent.setText(mLogMsg);
                    scroll2Bottom(mScrollView, mLogViewEvent);
                    mBtnLog.setBackgroundResource(R.mipmap.log_hidden);
                } else {
                    mLogViewStatus.setVisibility(View.GONE);
                    mScrollView.setVisibility(View.GONE);
                    mBtnLog.setBackgroundResource(R.mipmap.log_show);
                }*/
                goChat();
            }
        });

        //横屏|竖屏
        mBtnRenderRotation = (Button) findViewById(R.id.btnOrientation);
        mBtnRenderRotation.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLivePlayer == null) {
                    return;
                }

                if (mCurrentRenderRotation == TXLiveConstants.RENDER_ROTATION_PORTRAIT) {
                    mBtnRenderRotation.setBackgroundResource(R.mipmap.portrait);
                    mCurrentRenderRotation = TXLiveConstants.RENDER_ROTATION_LANDSCAPE;
                } else if (mCurrentRenderRotation == TXLiveConstants.RENDER_ROTATION_LANDSCAPE) {
                    mBtnRenderRotation.setBackgroundResource(R.mipmap.landscape);
                    mCurrentRenderRotation = TXLiveConstants.RENDER_ROTATION_PORTRAIT;
                }

                mLivePlayer.setRenderRotation(mCurrentRenderRotation);
            }
        });

        //平铺模式
        mBtnRenderMode = (Button) findViewById(R.id.btnRenderMode);
        mBtnRenderMode.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLivePlayer == null) {
                    return;
                }

                if (mCurrentRenderMode == TXLiveConstants.RENDER_MODE_FULL_FILL_SCREEN) {
                    mLivePlayer.setRenderMode(TXLiveConstants.RENDER_MODE_ADJUST_RESOLUTION);
                    mBtnRenderMode.setBackgroundResource(R.mipmap.fill_mode);
                    mCurrentRenderMode = TXLiveConstants.RENDER_MODE_ADJUST_RESOLUTION;
                } else if (mCurrentRenderMode == TXLiveConstants.RENDER_MODE_ADJUST_RESOLUTION) {
                    mLivePlayer.setRenderMode(TXLiveConstants.RENDER_MODE_FULL_FILL_SCREEN);
                    mBtnRenderMode.setBackgroundResource(R.mipmap.adjust_mode);
                    mCurrentRenderMode = TXLiveConstants.RENDER_MODE_FULL_FILL_SCREEN;
                }
            }
        });

        //硬件解码
        mBtnHWDecode = (Button) findViewById(R.id.btnHWDecode);
        mBtnHWDecode.getBackground().setAlpha(mHWDecode ? 255 : 100);
        mBtnHWDecode.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mHWDecode = !mHWDecode;
                mBtnHWDecode.getBackground().setAlpha(mHWDecode ? 255 : 100);

                if (mHWDecode) {
                    Toast.makeText(getApplicationContext(), "已开启硬件解码加速，切换会重启播放流程!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "已关闭硬件解码加速，切换会重启播放流程!", Toast.LENGTH_SHORT).show();
                }

                if (mVideoPlay) {
//                    if (mLivePlayer != null) {
//                        mLivePlayer.enableHardwareDecode(mHWDecode);
//                    }
                    stopPlayRtmp();
                    mVideoPlay = startPlayRtmp();
                    if (mVideoPause) {
                        if (mPlayerView != null){
                            mPlayerView.onResume();
                        }
                        mVideoPause = false;
                    }
                }
            }
        });

        mSeekBar = (SeekBar) findViewById(R.id.seekbar);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean bFromUser) {
                mTextStart.setText(String.format("%02d:%02d",progress/60, progress%60));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mStartSeek = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if ( mLivePlayer != null) {
                    mLivePlayer.seek(seekBar.getProgress());
                }
                mTrackingTouchTS = System.currentTimeMillis();
                mStartSeek = false;
            }
        });

        mTextDuration = (TextView) findViewById(R.id.duration);
        mTextStart = (TextView)findViewById(R.id.play_start);
        mTextDuration.setTextColor(Color.rgb(255, 255, 255));
        mTextStart.setTextColor(Color.rgb(255, 255, 255));
        //缓存策略
        mBtnCacheStrategy = (Button)findViewById(R.id.btnCacheStrategy);
        mLayoutCacheStrategy = (LinearLayout)findViewById(R.id.layoutCacheStrategy);
        mBtnCacheStrategy.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mLayoutCacheStrategy.setVisibility(mLayoutCacheStrategy.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            }
        });

        this.setCacheStrategy(CACHE_STRATEGY_AUTO);

        mRatioFast = (Button)findViewById(R.id.radio_btn_fast);
        mRatioFast.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                LivePlayerActivity.this.setCacheStrategy(CACHE_STRATEGY_FAST);
                mLayoutCacheStrategy.setVisibility(View.GONE);
            }
        });

        mRatioSmooth = (Button)findViewById(R.id.radio_btn_smooth);
        mRatioSmooth.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                LivePlayerActivity.this.setCacheStrategy(CACHE_STRATEGY_SMOOTH);
                mLayoutCacheStrategy.setVisibility(View.GONE);
            }
        });

        mRatioAuto = (Button)findViewById(R.id.radio_btn_auto);
        mRatioAuto.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                LivePlayerActivity.this.setCacheStrategy(CACHE_STRATEGY_AUTO);
                mLayoutCacheStrategy.setVisibility(View.GONE);
            }
        });

        mBtnCache = (Button)findViewById(R.id.btnCache);
        mBtnCache.getBackground().setAlpha(mEnableCache ? 255 : 100);
        mBtnCache.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mEnableCache = !mEnableCache;
                mBtnCache.getBackground().setAlpha(mEnableCache ? 255 : 100);
            }
        });
        View progressGroup = findViewById(R.id.play_progress);

        // 直播不需要进度条和停止按钮，点播不需要缓存策略
        if (mActivityType == ACTIVITY_TYPE_LIVE_PLAY) {
            progressGroup.setVisibility(View.GONE);
            mBtnStop.setVisibility(View.GONE);
            findViewById(R.id.btnCache).setVisibility(View.GONE);
        }
        else if (mActivityType == ACTIVITY_TYPE_VOD_PLAY) {
            mBtnCacheStrategy.setVisibility(View.GONE);
            findViewById(R.id.btnStreamRecord).setVisibility(View.GONE);
        }

        View view = mPlayerView.getRootView();
        view.setOnClickListener(this);
    }

    /**
     * 获取内置SD卡路径
     * @return
     */
    public String getInnerSDCardPath() {
        return Environment.getExternalStorageDirectory().getPath();
    }

    private void streamRecord(boolean runFlag) {
        if (runFlag) {
            mLivePlayer.setVideoRecordListener(new TXRecordCommon.ITXVideoRecordListener() {
                @Override
                public void onRecordEvent(int event, Bundle param) {

                }

                @Override
                public void onRecordProgress(long milliSecond) {
                    Log.d(TAG, "onRecordProgress:" + milliSecond);
                    mRecordTimeTV.setText(String.format("%02d:%02d",milliSecond/1000/60, milliSecond/1000%60));
                    int progress = (int)(milliSecond * 100 / (60 * 1000));
                    if (progress < 100) {
                        mRecordProgressBar.setProgress(progress);
                    } else {
                        mLivePlayer.stopRecord();
                    }
                }

                @Override
                public void onRecordComplete(TXRecordCommon.TXRecordResult result) {
                    Log.d(TAG, "onRecordComplete. errcode = " + result.retCode + ", errmsg = " + result.descMsg + ", output = " + result.videoPath + ", cover = " + result.coverPath);
                    if (mCancelRecordFlag) {
                        if (result.videoPath != null) {
                            File f = new File(result.videoPath);
                            if (f.exists()) f.delete();
                        }
                        if (result.coverPath != null) {
                            File f = new File(result.coverPath);
                            if (f.exists()) f.delete();
                        }
                    } else {
                        if (result.retCode == TXRecordCommon.RECORD_RESULT_OK) {
                            stopPlayRtmp();
                            Intent intent = new Intent(getApplicationContext(), TCVideoPreviewActivity.class);
                            intent.putExtra(TCConstants.VIDEO_RECORD_TYPE, TCConstants.VIDEO_RECORD_TYPE_PUBLISH);
                            intent.putExtra(TCConstants.VIDEO_RECORD_RESULT, result.retCode);
                            intent.putExtra(TCConstants.VIDEO_RECORD_DESCMSG, result.descMsg);
                            intent.putExtra(TCConstants.VIDEO_RECORD_VIDEPATH, result.videoPath);
                            intent.putExtra(TCConstants.VIDEO_RECORD_COVERPATH, result.coverPath);
                            startActivity(intent);
                            finish();
                        }
                    }
                }
            });
            mCancelRecordFlag = false;
            mLivePlayer.startRecord(TXRecordCommon.RECORD_TYPE_STREAM_SOURCE);
            findViewById(R.id.record).setBackgroundResource(R.mipmap.stop_record);
        } else {
            mLivePlayer.stopRecord();
            mRecordTimeTV.setText("00:00");
            mRecordProgressBar.setProgress(0);
            findViewById(R.id.record).setBackgroundResource(R.mipmap.start_record);
        }
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
        switch (v.getId()) {
            case R.id.btnStreamRecord:
                findViewById(R.id.play_pannel).setVisibility(View.GONE);
                findViewById(R.id.record_layout).setVisibility(View.VISIBLE);
                break;
            case R.id.record:
                mRecordFlag = !mRecordFlag;
                streamRecord(mRecordFlag);
                break;
            case R.id.close_record:
                findViewById(R.id.play_pannel).setVisibility(View.VISIBLE);
                findViewById(R.id.record_layout).setVisibility(View.GONE);
            case R.id.retry_record:
                mCancelRecordFlag = true;
                streamRecord(false);
                break;
            default:
                mLayoutCacheStrategy.setVisibility(View.GONE);
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

    protected void clearLog() {
        mLogMsg.setLength(0);
        mLogViewEvent.setText("");
        mLogViewStatus.setText("");
    }

    protected void appendEventLog(int event, String message) {
        String str = "receive event: " + event + ", " + message;
        Log.d(TAG, str);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
        String date = sdf.format(System.currentTimeMillis());
        while(mLogMsg.length() >mLogMsgLenLimit ){
            int idx = mLogMsg.indexOf("\n");
            if (idx == 0)
                idx = 1;
            mLogMsg = mLogMsg.delete(0,idx);
        }
        mLogMsg = mLogMsg.append("\n" + "["+date+"]" + message);
    }

    protected void enableQRCodeBtn(boolean bEnable) {
        //disable qrcode
        Button btnScan = (Button) findViewById(R.id.btnScan);
        if (btnScan != null) {
            btnScan.setEnabled(bEnable);
        }
    }

    private boolean startPlayRtmp() {

        String playUrl = mRtmpUrlView.getText().toString();
        playUrl = "rtmp://historypush.jiuyihtn.com/live/jiuyikeji";

//        //由于iOS AppStore要求新上架的app必须使用https,所以后续腾讯云的视频连接会支持https,但https会有一定的性能损耗,所以android将统一替换会http
//        if (playUrl.startsWith("https://")) {
//            playUrl = "http://" + playUrl.substring(8);
//        }

        if (!checkPlayUrl(playUrl)) {
            return false;
        }

        clearLog();

        mLogMsg.append("liteav sdk version:"+ TXLiveBase.getSDKVersionStr());
        mLogViewEvent.setText(mLogMsg);

        mBtnPlay.setBackgroundResource(R.mipmap.play_pause);
        mRootView.setBackgroundColor(0xff000000);


        mLivePlayer.setPlayerView(mPlayerView);

        mLivePlayer.setPlayListener(this);
//        mLivePlayer.setRate(1.5f);
        // 硬件加速在1080p解码场景下效果显著，但细节之处并不如想象的那么美好：
        // (1) 只有 4.3 以上android系统才支持
        // (2) 兼容性我们目前还仅过了小米华为等常见机型，故这里的返回值您先不要太当真
        mLivePlayer.enableHardwareDecode(mHWDecode);
        mLivePlayer.setRenderRotation(mCurrentRenderRotation);
        mLivePlayer.setRenderMode(mCurrentRenderMode);
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
        if (result != 0) {
            mBtnPlay.setBackgroundResource(R.mipmap.play_start);
            mRootView.setBackgroundResource(R.mipmap.main_bkg);
            return false;
        }

        appendEventLog(0, "点击播放按钮！播放类型：" +mPlayType);
        Log.w("video render","timetrack start play");

        startLoadingAnimation();

        enableQRCodeBtn(false);

        mStartPlayTS = System.currentTimeMillis();

        if (mActivityType == LivePlayerActivity.ACTIVITY_TYPE_VOD_PLAY) {
            findViewById(R.id.playerHeaderView).setVisibility(View.VISIBLE);
        }
        return true;
    }

    private  void stopPlayRtmp() {
        enableQRCodeBtn(true);
        mBtnPlay.setBackgroundResource(R.mipmap.play_start);
        mRootView.setBackgroundResource(R.mipmap.main_bkg);
        stopLoadingAnimation();
        if (mLivePlayer != null) {
            mLivePlayer.stopRecord();
            mLivePlayer.setPlayListener(null);
            mLivePlayer.stopPlay(true);
        }
        mVideoPause = false;
        mVideoPlay = false;
    }

    @Override
    public void onPlayEvent(int event, Bundle param) {
        if (event == TXLiveConstants.PLAY_EVT_PLAY_BEGIN) {
            stopLoadingAnimation();
            Log.d("AutoMonitor", "PlayFirstRender,cost=" +(System.currentTimeMillis()-mStartPlayTS));
        } else if (event == TXLiveConstants.PLAY_EVT_PLAY_PROGRESS ) {
            if (mStartSeek) {
                return;
            }
            int progress = param.getInt(TXLiveConstants.EVT_PLAY_PROGRESS);
            int duration = param.getInt(TXLiveConstants.EVT_PLAY_DURATION);
            int playable = param.getInt(TCConstants.NET_STATUS_PLAYABLE_DURATION);
            long curTS = System.currentTimeMillis();

            // 避免滑动进度条松开的瞬间可能出现滑动条瞬间跳到上一个位置
            if (Math.abs(curTS - mTrackingTouchTS) < 500) {
                return;
            }
            mTrackingTouchTS = curTS;

            if (mSeekBar != null) {
                mSeekBar.setProgress(progress);
                mSeekBar.setSecondaryProgress(playable);
                Log.d(TAG, "progress "+progress+"secondary progress "+playable);
            }
            if (mTextStart != null) {
                mTextStart.setText(String.format("%02d:%02d",progress/60,progress%60));
            }
            if (mTextDuration != null) {
                mTextDuration.setText(String.format("%02d:%02d",duration/60,duration%60));
            }
            if (mSeekBar != null) {
                mSeekBar.setMax(duration);
            }
            return;
        } else if (event == TXLiveConstants.PLAY_ERR_NET_DISCONNECT || event == TXLiveConstants.PLAY_EVT_PLAY_END) {
            stopPlayRtmp();
            mVideoPlay = false;
            mVideoPause = false;
            if (mTextStart != null) {
                mTextStart.setText("00:00");
            }
            if (mSeekBar != null) {
                mSeekBar.setProgress(0);
            }
        } else if (event == TXLiveConstants.PLAY_EVT_PLAY_LOADING){
            startLoadingAnimation();
        } else if (event == TXLiveConstants.PLAY_EVT_RCV_FIRST_I_FRAME) {
            stopLoadingAnimation();
            if (mActivityType == LivePlayerActivity.ACTIVITY_TYPE_VOD_PLAY) {
                findViewById(R.id.playerHeaderView).setVisibility(View.GONE);
            }
        } else if (event == TXLiveConstants.PLAY_EVT_CHANGE_RESOLUTION) {
            streamRecord(false);
        }

        String msg = param.getString(TXLiveConstants.EVT_DESCRIPTION);
        appendEventLog(event, msg);
        if (mScrollView.getVisibility() == View.VISIBLE){
            mLogViewEvent.setText(mLogMsg);
            scroll2Bottom(mScrollView, mLogViewEvent);
        }
//        if(mLivePlayer != null){
//            mLivePlayer.onLogRecord("[event:"+event+"]"+msg+"\n");
//        }
        if (event < 0) {
            Toast.makeText(getApplicationContext(), param.getString(TXLiveConstants.EVT_DESCRIPTION), Toast.LENGTH_SHORT).show();
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
        mLogViewStatus.setText(str);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != 100 || data ==null || data.getExtras() == null || TextUtils.isEmpty(data.getExtras().getString("result"))) {
            return;
        }
        String result = data.getExtras().getString("result");
        if (mRtmpUrlView != null) {
            mRtmpUrlView.setText(result);
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
    private PhoneStateListener mPhoneListener = null;
}