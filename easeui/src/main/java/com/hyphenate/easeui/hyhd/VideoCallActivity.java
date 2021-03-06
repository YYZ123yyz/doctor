package com.hyphenate.easeui.hyhd;

import android.media.AudioManager;
import android.media.RingtoneManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.Vibrator;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.hyphenate.chat.EMCallSession;
import com.hyphenate.chat.EMCallStateChangeListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMVideoCallHelper;
import com.hyphenate.easeui.EaseConstant;
import com.hyphenate.easeui.R;
import com.hyphenate.exceptions.EMNoActiveCallException;
import com.hyphenate.exceptions.HyphenateException;
import com.hyphenate.media.EMCallSurfaceView;
import com.hyphenate.util.EMLog;
import com.superrtc.sdk.VideoView;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import www.jykj.com.jykj_zxyl.app_base.base_utils.PeterTimeCountRefresh;
import www.jykj.com.jykj_zxyl.app_base.base_utils.ToastCommonUtil;
import www.jykj.com.jykj_zxyl.app_base.base_view.ZoomTextView;


public class VideoCallActivity extends CallActivity implements OnClickListener {

    private boolean isMuteState;
    private boolean isHandsfreeState;
    private boolean isAnswered;
    private boolean endCallTriggerByMe = false;
    private boolean monitor = true;

    // 视频通话画面显示控件，这里在新版中使用同一类型的控件，方便本地和远端视图切换
    protected EMCallSurfaceView localSurface;
    protected EMCallSurfaceView oppositeSurface;
    private int surfaceState = -1;

    private TextView callStateTextView;

    private LinearLayout comingBtnContainer;
    private LinearLayout refuseBtn;
    private LinearLayout answerBtn;
    private Button hangupBtn;
    private ImageView muteImage;
    private ImageView handsFreeImage;
    private TextView nickTextView;
    private Chronometer chronometer;
    private LinearLayout voiceContronlLayout;
    private RelativeLayout rootContainer;
    private LinearLayout topContainer;
    private LinearLayout bottomContainer;
    private TextView monitorTextView;
    private TextView netwrokStatusVeiw;
    private RelativeLayout rlAddTimeBtn;
    private Handler uiHandler;

    private boolean isInCalling;
    //    private Button recordBtn;
    private EMVideoCallHelper callHelper;
    private Button toggleVideoBtn;

    private             int mVedioTime;                //可拨打语音时长（单位：秒）
    private Handler mHandler;
    private RelativeLayout msgRel;
    private LinearLayout hangUpLayout;
    private LinearLayout muitLayout;
    private LinearLayout noHandLayout;
    private ImageView ivMuit;
    private ImageView ivNoHand;
    private ZoomTextView tvCountDownTime;
    private String headUrl;
    private ImageView headView;
    private RelativeLayout layoutSending;
    private PeterTimeCountRefresh petterTimer;
    private boolean isOneMinuteVibrator;
    private boolean isThreeMinuteVibrator;
    private long currentMillisUntilFinished;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null){
            finish();
            return;
        }
        setContentView(R.layout.em_activity_video_call);
        mVedioTime = getIntent().getIntExtra(EaseConstant.EXTRA_VEDIO_NUM, 0);
        if (mVedioTime == 0)
            mVedioTime = 1000000;
        DemoHelper.getInstance().isVideoCalling = true;
        callType = 1;

        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        uiHandler = new Handler();
        headView = findViewById(R.id.swing_card);
        callStateTextView = (TextView) findViewById(R.id.tv_call_state);
        comingBtnContainer = (LinearLayout) findViewById(R.id.linyout_is_accept);
        rootContainer = (RelativeLayout) findViewById(R.id.root_layout);

        refuseBtn = (LinearLayout) findViewById(R.id.linyout_refuse);
        answerBtn = (LinearLayout) findViewById(R.id.linyout_accept);
        hangupBtn = (Button) findViewById(R.id.btn_hangup_call);
        muteImage = (ImageView) findViewById(R.id.iv_mute);
        handsFreeImage = (ImageView) findViewById(R.id.iv_handsfree);
        callStateTextView = (TextView) findViewById(R.id.tv_call_state);
        nickTextView = (TextView) findViewById(R.id.tv_nick);
        chronometer = (Chronometer) findViewById(R.id.chronometer);

        voiceContronlLayout = (LinearLayout) findViewById(R.id.linyout_state);
        RelativeLayout btnsContainer = (RelativeLayout) findViewById(R.id.ll_btns);
        topContainer = (LinearLayout) findViewById(R.id.ll_top_container);
        bottomContainer = (LinearLayout) findViewById(R.id.ll_bottom_container);
//        monitorTextView = (TextView) findViewById(R.id.tv_call_monitor);
        netwrokStatusVeiw = (TextView) findViewById(R.id.tv_network_status);

        findViewById(R.id.layout_muit).setOnClickListener(this);
        msgRel = (RelativeLayout)findViewById(R.id.relayout_msg);
        hangUpLayout = (LinearLayout) findViewById(R.id.layout_hang_up);
//        muitLayout = (LinearLayout) findViewById(R.id.layout_muit);
        noHandLayout = (LinearLayout) findViewById(R.id.layout_no_hand);
        ivMuit =(ImageView)findViewById(R.id.iv_muit);
        ivNoHand =(ImageView)findViewById(R.id.iv_no_hand);
        layoutSending = (RelativeLayout) findViewById(R.id.layout_sending);
        tvCountDownTime=findViewById(R.id.tv_count_down_time);
        rlAddTimeBtn=findViewById(R.id.rl_add_time_btn);
        findViewById(R.id.layout_cancel_call).setOnClickListener(this);

        refuseBtn.setOnClickListener(this);
        answerBtn.setOnClickListener(this);
        hangupBtn.setOnClickListener(this);
        muteImage.setOnClickListener(this);
        handsFreeImage.setOnClickListener(this);
        rootContainer.setOnClickListener(this);
        hangUpLayout.setOnClickListener(this);
//        muitLayout.setOnClickListener(this);
        noHandLayout.setOnClickListener(this);
        rlAddTimeBtn.setOnClickListener(this);

//        switchCameraBtn.setOnClickListener(this);

        msgid = UUID.randomUUID().toString();
        isInComingCall = getIntent().getBooleanExtra("isComingCall", false);
        username = getIntent().getStringExtra("username");
        nickName = getIntent().getStringExtra("nickName");
        surplusDuration=getIntent().getIntExtra("surplusDuration",0);
        if (getIntent().hasExtra("headUrl")){
            headUrl = getIntent().getStringExtra("headUrl");
            Glide.with(this).load(headUrl).into(headView);
        }

        nickTextView.setText(nickName);

        // local surfaceview
        localSurface = (EMCallSurfaceView) findViewById(R.id.local_surface);
        localSurface.setOnClickListener(this);
        localSurface.setZOrderMediaOverlay(true);
        localSurface.setZOrderOnTop(true);

        // remote surfaceview
        oppositeSurface = (EMCallSurfaceView) findViewById(R.id.opposite_surface);

        // set call state listener
        addCallStateListener();
        if (!isInComingCall) {// outgoing call
            soundPool = new SoundPool(1, AudioManager.STREAM_RING, 0);
            outgoing = soundPool.load(this, R.raw.em_outgoing, 1);

            comingBtnContainer.setVisibility(View.INVISIBLE);
            hangupBtn.setVisibility(View.VISIBLE);
            String st = getResources().getString(R.string.Are_connected_to_each_other);
            callStateTextView.setText(st);
            EMClient.getInstance().callManager().setSurfaceView(localSurface, oppositeSurface);
            handler.sendEmptyMessage(MSG_CALL_MAKE_VIDEO);
            handler.postDelayed(new Runnable() {
                public void run() {
                    streamID = playMakeCallSounds();
                }
            }, 300);
        } else { // incoming call

            callStateTextView.setText("");
            if(EMClient.getInstance().callManager().getCallState() == EMCallStateChangeListener.CallState.IDLE
                    || EMClient.getInstance().callManager().getCallState() == EMCallStateChangeListener.CallState.DISCONNECTED) {
                // the call has ended
                finish();
                return;
            }
            voiceContronlLayout.setVisibility(View.INVISIBLE); //声音控制
            tvCountDownTime.setVisibility(View.INVISIBLE);
            msgRel.setVisibility(View.VISIBLE);
            localSurface.setVisibility(View.INVISIBLE);
            Uri ringUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            audioManager.setMode(AudioManager.MODE_RINGTONE);
            audioManager.setSpeakerphoneOn(true);
            ringtone = RingtoneManager.getRingtone(this, ringUri);
            ringtone.play();
            EMClient.getInstance().callManager().setSurfaceView(localSurface, oppositeSurface);
        }

        final int MAKE_CALL_TIMEOUT = 50 * 1000;
        handler.removeCallbacks(timeoutHangup);
        handler.postDelayed(timeoutHangup, MAKE_CALL_TIMEOUT);

        // get instance of call helper, should be called after setSurfaceView was called
        callHelper = EMClient.getInstance().callManager().getVideoCallHelper();
        initHandler();
    }

    private void initHandler() {
        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what)
                {
                    case 1:
                        /**
                         * 挂断通话
                         */
                        try {
                            if (petterTimer!=null) {
                                petterTimer.cancel();
                            }
                            EMClient.getInstance().callManager().endCall();
                        } catch (EMNoActiveCallException e) {
                            e.printStackTrace();
                        }
                        Toast.makeText(VideoCallActivity.this,"视频时长已用尽",Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };
    }

    /**
     * 切换通话界面，这里就是交换本地和远端画面控件设置，以达到通话大小画面的切换
     */
    private void changeCallView() {
        if (surfaceState == 0) {
            surfaceState = 1;
            EMClient.getInstance().callManager().setSurfaceView(oppositeSurface, localSurface);
        } else {
            surfaceState = 0;
            EMClient.getInstance().callManager().setSurfaceView(localSurface, oppositeSurface);
        }
    }

    /**
     * set call state listener
     */
    void addCallStateListener() {
        callStateListener = new EMCallStateChangeListener() {

            @Override
            public void onCallStateChanged(final CallState callState, final CallError error) {
                switch (callState) {

                    case CONNECTING: // is connecting
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                callStateTextView.setText(R.string.Are_connected_to_each_other);
                            }

                        });
                        break;
                    case CONNECTED: // connected
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
//                            callStateTextView.setText(R.string.have_connected_with);
                            }

                        });
                        break;

                    case ACCEPTED: // call is accepted
                        surfaceState = 0;
                        handler.removeCallbacks(timeoutHangup);
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                try {
                                    if (soundPool != null)
                                        soundPool.stop(streamID);
                                    EMLog.d("EMCallManager", "soundPool stop ACCEPTED");
                                } catch (Exception e) {
                                }
                                openSpeakerOn();

                                layoutSending.setVisibility(View.GONE);
                                voiceContronlLayout.setVisibility(View.VISIBLE);
                                tvCountDownTime.setVisibility(View.VISIBLE);
                                ivNoHand.setSelected(true);
                                localSurface.setVisibility(View.VISIBLE);
//                            ((TextView)findViewById(R.id.tv_is_p2p)).setText(EMClient.getInstance().callManager().isDirectCall()
//                                    ? R.string.direct_call : R.string.relay_call);
                                ((TextView)findViewById(R.id.tv_is_p2p)).setText(EMClient.getInstance().callManager().isDirectCall()
                                        ? nickName : nickName);
                                msgRel.setVisibility(View.GONE);
                                handsFreeImage.setImageResource(R.mipmap.em_icon_speaker_on);
                                isHandsfreeState = true;
                                isInCalling = true;
//                                chronometer.setVisibility(View.VISIBLE);
//                                chronometer.setBase(SystemClock.elapsedRealtime());
//                                // call durations start
//                                chronometer.start();
                                nickTextView.setVisibility(View.INVISIBLE);
                                callStateTextView.setText(R.string.In_the_call);
//                            recordBtn.setVisibility(View.VISIBLE);
                                callingState = CallingState.NORMAL;
                                startMonitor();
                                // Start to watch the phone call state.
                                PhoneStateManager.get(VideoCallActivity.this).addStateCallback(phoneStateCallback);
                                //启动定时器，监听剩余时间
                                final Timer timer = new Timer();
                                final TimerTask task = new TimerTask() {
                                    @Override
                                    public void run() {
                                        if (mVedioTime <= 0)
                                        {
                                            //挂断电话
                                            mHandler.sendEmptyMessage(1);
                                            //结束计时器
                                            timer.cancel();

                                        }
                                        else
                                            mVedioTime -= 1;
                                    }
                                };
                                timer.schedule(task, 0, 1000);

                                petterTimer = new PeterTimeCountRefresh(surplusDuration * 1000 * 60,
                                        1000, tvCountDownTime, new PeterTimeCountRefresh.OnTimerListener() {
                                    @Override
                                    public void onTickTime(long millisUntilFinished) {
                                        int  minute =(int) Math.floor(millisUntilFinished / 60000);
                                        currentMillisUntilFinished=millisUntilFinished;
                                        if(minute<3&&!isThreeMinuteVibrator){
                                            tvCountDownTime.amplify();
                                            Vibrator vibrator = (Vibrator)
                                                    VideoCallActivity.this
                                                            .getSystemService(VideoCallActivity.this.VIBRATOR_SERVICE);
                                            vibrator.vibrate(1000);
                                            ToastCommonUtil.showToastCustom(VideoCallActivity.this
                                                    ,"通话即将到时，到时后通话将被中断", Gravity.CENTER);
                                            isThreeMinuteVibrator=true;
                                        }

                                        if (minute<1&&!isOneMinuteVibrator) {
                                            tvCountDownTime.amplify();
                                            Vibrator vibrator = (Vibrator)
                                                    VideoCallActivity.this
                                                            .getSystemService(VideoCallActivity.this.VIBRATOR_SERVICE);
                                            vibrator.vibrate(1000);
                                            ToastCommonUtil.showToastCustom(VideoCallActivity.this
                                                    ,"通话即将到时，到时后通话将被中断", Gravity.CENTER);
                                            isOneMinuteVibrator=true;
                                        }

                                    }

                                    @Override
                                    public void onFinish() {
                                        mHandler.sendEmptyMessage(1);
                                    }
                                });
                                petterTimer.start();


                            }

                        });
                        break;
                    case NETWORK_DISCONNECTED:
                        runOnUiThread(new Runnable() {
                            public void run() {
                                netwrokStatusVeiw.setVisibility(View.VISIBLE);
                                netwrokStatusVeiw.setText(R.string.network_unavailable);
                            }
                        });
                        break;
                    case NETWORK_UNSTABLE:
                        runOnUiThread(new Runnable() {
                            public void run() {
                                netwrokStatusVeiw.setVisibility(View.VISIBLE);
                                if(error == CallError.ERROR_NO_DATA){
                                    netwrokStatusVeiw.setText("没有通话数据");
                                }else{
                                    netwrokStatusVeiw.setText("网络不稳定");
                                }
                            }
                        });
                        break;
                    case NETWORK_NORMAL:
                        runOnUiThread(new Runnable() {
                            public void run() {
                                netwrokStatusVeiw.setVisibility(View.INVISIBLE);
                            }
                        });
                        break;
                    case VIDEO_PAUSE:
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), "VIDEO_PAUSE", Toast.LENGTH_SHORT).show();
                            }
                        });
                        break;
                    case VIDEO_RESUME:
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), "VIDEO_RESUME", Toast.LENGTH_SHORT).show();
                            }
                        });
                        break;
                    case VOICE_PAUSE:
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), "VOICE_PAUSE", Toast.LENGTH_SHORT).show();
                            }
                        });
                        break;
                    case VOICE_RESUME:
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), "VOICE_RESUME", Toast.LENGTH_SHORT).show();
                            }
                        });
                        break;
                    case DISCONNECTED: // call is disconnected
                        handler.removeCallbacks(timeoutHangup);
                        @SuppressWarnings("UnnecessaryLocalVariable")final CallError fError = error;
                        runOnUiThread(new Runnable() {
                            private void postDelayedCloseMsg() {
                                uiHandler.postDelayed(new Runnable() {

                                    @Override
                                    public void run() {
                                        removeCallStateListener();

                                        // Stop to watch the phone call state.
                                        PhoneStateManager.get(VideoCallActivity.this).removeStateCallback(phoneStateCallback);

                                        saveCallRecord();
                                        Animation animation = new AlphaAnimation(1.0f, 0.0f);
                                        animation.setDuration(1200);
                                        rootContainer.startAnimation(animation);
                                        finish();
                                    }

                                }, 200);
                            }

                            @Override
                            public void run() {
                                chronometer.stop();
                                callDruationText = chronometer.getText().toString();
                                String s1 = getResources().getString(R.string.The_other_party_refused_to_accept);
                                String s2 = getResources().getString(R.string.Connection_failure);
                                String s3 = getResources().getString(R.string.The_other_party_is_not_online);
                                String s4 = getResources().getString(R.string.The_other_is_on_the_phone_please);
                                String s5 = getResources().getString(R.string.The_other_party_did_not_answer);

                                String s6 = getResources().getString(R.string.hang_up);
                                String s7 = getResources().getString(R.string.The_other_is_hang_up);
                                String s8 = getResources().getString(R.string.did_not_answer);
                                String s9 = getResources().getString(R.string.Has_been_cancelled);
                                String s10 = getResources().getString(R.string.Refused);
                                String st12 = "service not enable";
                                String st13 = "service arrearages";
                                String st14 = "service forbidden";

                                if (fError == CallError.REJECTED) {
                                    callingState = CallingState.BEREFUSED;
                                    callStateTextView.setText(s1);
                                } else if (fError == CallError.ERROR_TRANSPORT) {
                                    callStateTextView.setText(s2);
                                } else if (fError == CallError.ERROR_UNAVAILABLE) {
                                    callingState = CallingState.OFFLINE;
                                    callStateTextView.setText(s3);
                                } else if (fError == CallError.ERROR_BUSY) {
                                    callingState = CallingState.BUSY;
                                    callStateTextView.setText(s4);
                                } else if (fError == CallError.ERROR_NORESPONSE) {
                                    callingState = CallingState.NO_RESPONSE;
                                    callStateTextView.setText(s5);
                                }else if (fError == CallError.ERROR_LOCAL_SDK_VERSION_OUTDATED || fError == CallError.ERROR_REMOTE_SDK_VERSION_OUTDATED){
                                    callingState = CallingState.VERSION_NOT_SAME;
                                    callStateTextView.setText("通话协议版本不一致");
                                } else if(fError == CallError.ERROR_SERVICE_NOT_ENABLE) {
                                    callingState = CallingState.SERVICE_NOT_ENABLE;
                                    callStateTextView.setText(st12);
                                } else if(fError == CallError.ERROR_SERVICE_ARREARAGES) {
                                    callingState = CallingState.SERVICE_ARREARAGES;
                                    callStateTextView.setText(st13);
                                } else if(fError == CallError.ERROR_SERVICE_FORBIDDEN) {
                                    callingState = CallingState.SERVICE_NOT_ENABLE;
                                    callStateTextView.setText(st14);
                                } else {
                                    if (isRefused) {
                                        callingState = CallingState.REFUSED;
                                        callStateTextView.setText(s10);
                                    }
                                    else if (isAnswered) {
                                        callingState = CallingState.NORMAL;
                                        if (endCallTriggerByMe) {
//                                        callStateTextView.setText(s6);
                                        } else {
                                            callStateTextView.setText(s7);
                                        }
                                    } else {
                                        if (isInComingCall) {
                                            callingState = CallingState.UNANSWERED;
                                            callStateTextView.setText(s8);
                                        } else {
                                            if (callingState != CallingState.NORMAL) {
                                                callingState = CallingState.CANCELLED;
                                                callStateTextView.setText(s9);
                                            } else {
                                                callStateTextView.setText(s6);
                                            }
                                        }
                                    }
                                }
                                Toast.makeText(VideoCallActivity.this, callStateTextView.getText(), Toast.LENGTH_SHORT).show();
                                postDelayedCloseMsg();
                            }

                        });

                        break;

                    default:
                        break;
                }

            }
        };
        EMClient.getInstance().callManager().addCallStateChangeListener(callStateListener);
    }

    void removeCallStateListener() {
        EMClient.getInstance().callManager().removeCallStateChangeListener(callStateListener);
    }

    PhoneStateManager.PhoneStateCallback phoneStateCallback = new PhoneStateManager.PhoneStateCallback() {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:   // 电话响铃
                    break;
                case TelephonyManager.CALL_STATE_IDLE:      // 电话挂断
                    // resume current voice conference.
                    if (isMuteState) {
                        try {
                            EMClient.getInstance().callManager().resumeVoiceTransfer();
                        } catch (HyphenateException e) {
                            e.printStackTrace();
                        }
                        try {
                            EMClient.getInstance().callManager().resumeVideoTransfer();
                        } catch (HyphenateException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:   // 来电接通 或者 去电，去电接通  但是没法区分
                    // pause current voice conference.
                    if (!isMuteState) {
                        try {
                            EMClient.getInstance().callManager().pauseVoiceTransfer();
                        } catch (HyphenateException e) {
                            e.printStackTrace();
                        }
                        try {
                            EMClient.getInstance().callManager().pauseVideoTransfer();
                        } catch (HyphenateException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
            }
        }
    };

    @Override
    public void onClick(View v) {
        int i = v.getId();// decline the call
// answer the call
// hangup
// mute
// handsfree
//switch camera
        if (i == R.id.local_surface) {
            changeCallView();

        } else if (i == R.id.linyout_refuse) {
            isRefused = true;
            refuseBtn.setEnabled(false);
            handler.sendEmptyMessage(MSG_CALL_REJECT);

        } else if (i == R.id.linyout_accept) {
            EMLog.d(TAG, "btn_answer_call clicked");
            answerBtn.setEnabled(false);
            ivNoHand.setSelected(true);
            openSpeakerOn();
            if (ringtone != null)
                ringtone.stop();

            callStateTextView.setText("answering...");
            handler.sendEmptyMessage(MSG_CALL_ANSWER);
            handsFreeImage.setImageResource(R.mipmap.em_icon_speaker_on);
            isAnswered = true;
            isHandsfreeState = true;
            comingBtnContainer.setVisibility(View.INVISIBLE);
            hangupBtn.setVisibility(View.VISIBLE);
            voiceContronlLayout.setVisibility(View.VISIBLE);
            tvCountDownTime.setVisibility(View.VISIBLE);
            msgRel.setVisibility(View.VISIBLE);
            localSurface.setVisibility(View.VISIBLE);

        } else if (i == R.id.layout_hang_up  || i== R.id.layout_cancel_call) {//挂断
            hangupBtn.setEnabled(false);
            chronometer.stop();
            endCallTriggerByMe = true;
            callStateTextView.setText(getResources().getString(R.string.hanging_up));
            EMLog.d(TAG, "btn_hangup_call");
            handler.sendEmptyMessage(MSG_CALL_END);

        } /*else if (i == R.id.layout_muit) {    //静音
            if (isMuteState) {
                // resume voice transfer
//                muteImage.setImageResource(R.mipmap.em_icon_mute_normal);
                ivMuit.setSelected(false);
                try {
                    EMClient.getInstance().callManager().resumeVoiceTransfer();
                } catch (HyphenateException e) {
                    e.printStackTrace();
                }
                isMuteState = false;
            } else {
                // pause voice transfer
//                muteImage.setImageResource(R.mipmap.em_icon_mute_on);
                ivMuit.setSelected(true);
                try {
                    EMClient.getInstance().callManager().pauseVoiceTransfer();
                } catch (HyphenateException e) {
                    e.printStackTrace();
                }
                isMuteState = true;
            }

        }*/ else if (i == R.id.layout_no_hand) {
            if (isHandsfreeState) {
                // turn off speaker
//                handsFreeImage.setImageResource(R.mipmap.em_icon_speaker_normal);
                ivNoHand.setSelected(false);
                closeSpeakerOn();
                isHandsfreeState = false;
            } else {
//                handsFreeImage.setImageResource(R.mipmap.em_icon_speaker_on);
                ivNoHand.setSelected(true);
                openSpeakerOn();
                isHandsfreeState = true;
            }

        /*
        case R.id.btn_record_video: //record the video
            if(!isRecording){
//                callHelper.startVideoRecord(PathUtil.getInstance().getVideoPath().getAbsolutePath());
                callHelper.startVideoRecord("/sdcard/");
                EMLog.d(TAG, "startVideoRecord:" + PathUtil.getInstance().getVideoPath().getAbsolutePath());
                isRecording = true;
                recordBtn.setText(R.string.stop_record);
            }else{
                String filepath = callHelper.stopVideoRecord();
                isRecording = false;
                recordBtn.setText(R.string.recording_video);
                Toast.makeText(getApplicationContext(), String.format(getString(R.string.record_finish_toast), filepath), Toast.LENGTH_LONG).show();
            }
            break;
        */
        } else if (i == R.id.root_layout) {
            if (callingState == CallingState.NORMAL) {
                if (bottomContainer.getVisibility() == View.VISIBLE) {
                    bottomContainer.setVisibility(View.GONE);
                    topContainer.setVisibility(View.GONE);
                    oppositeSurface.setScaleMode(VideoView.EMCallViewScaleMode.EMCallViewScaleModeAspectFill);

                } else {
                    bottomContainer.setVisibility(View.VISIBLE);
                    //暂时隐藏
                    topContainer.setVisibility(View.GONE);
                    oppositeSurface.setScaleMode(VideoView.EMCallViewScaleMode.EMCallViewScaleModeAspectFit);
                }
            }

        } else if (i == R.id.layout_muit) {

            handler.sendEmptyMessage(MSG_CALL_SWITCH_CAMERA);

        } else if(i==R.id.rl_add_time_btn){
            onClickAddTime();
        }
    }

    @Override
    protected void againCalculationTime() {
        if (petterTimer!=null) {
            petterTimer.cancel();
        }
        petterTimer = new PeterTimeCountRefresh(currentMillisUntilFinished+1000 * 60,
                1000, tvCountDownTime, new PeterTimeCountRefresh.OnTimerListener() {
            @Override
            public void onTickTime(long millisUntilFinished) {
                int  minute =(int) Math.floor(millisUntilFinished / 60000);
                currentMillisUntilFinished=millisUntilFinished;
                if(minute<3&&!isThreeMinuteVibrator){
                    Vibrator vibrator = (Vibrator)
                            VideoCallActivity.this
                                    .getSystemService(VideoCallActivity.this.VIBRATOR_SERVICE);
                    vibrator.vibrate(1000);
                    ToastCommonUtil.showToastCustom(VideoCallActivity.this
                            ,"通话即将到时，到时后通话将被中断", Gravity.CENTER);
                    isThreeMinuteVibrator=true;
                }

                if (minute<1&&!isOneMinuteVibrator) {
                    Vibrator vibrator = (Vibrator)
                            VideoCallActivity.this
                                    .getSystemService(VideoCallActivity.this.VIBRATOR_SERVICE);
                    vibrator.vibrate(1000);
                    ToastCommonUtil.showToastCustom(VideoCallActivity.this
                            ,"通话即将到时，到时后通话将被中断", Gravity.CENTER);
                    isOneMinuteVibrator=true;
                }

            }

            @Override
            public void onFinish() {
                mHandler.sendEmptyMessage(1);
            }
        });
        petterTimer.start();
    }

    @Override
    protected void onDestroy() {
        DemoHelper.getInstance().isVideoCalling = false;
        stopMonitor();
        localSurface.getRenderer().dispose();
        localSurface = null;
        oppositeSurface.getRenderer().dispose();
        oppositeSurface = null;
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        callDruationText = chronometer.getText().toString();
        super.onBackPressed();
    }

    /**
     * for debug & testing, you can remove this when release
     */
    void startMonitor(){
        monitor = true;
        EMCallSession callSession = EMClient.getInstance().callManager().getCurrentCallSession();
        final boolean isRecord = callSession.isRecordOnServer();
        final String serverRecordId = callSession.getServerRecordId();
        EMLog.e(TAG, "server record id: " + serverRecordId);

        EMLog.e(TAG, "server record: " + isRecord);
        if (isRecord) {
            EMLog.e(TAG, "server record id: " + serverRecordId);
        }
        final String recordString = " record? " + isRecord + " id: " + serverRecordId;
        new Thread(new Runnable() {
            public void run() {
                while(monitor){
                    runOnUiThread(new Runnable() {
                        public void run() {
//                            monitorTextView.setText("WidthxHeight："+callHelper.getVideoWidth()+"x"+callHelper.getVideoHeight()
//                                    + "\nDelay：" + callHelper.getVideoLatency()
//                                    + "\nFramerate：" + callHelper.getVideoFrameRate()
//                                    + "\nLost：" + callHelper.getVideoLostRate()
//                                    + "\nLocalBitrate：" + callHelper.getLocalBitrate()
//                                    + "\nRemoteBitrate：" + callHelper.getRemoteBitrate()
//                                    + "\n" + recordString);

//                            ((TextView)findViewById(R.id.tv_is_p2p)).setText(EMClient.getInstance().callManager().isDirectCall()
//                                    ? R.string.direct_call : R.string.relay_call);
                            ((TextView)findViewById(R.id.tv_is_p2p)).setText(EMClient.getInstance().callManager().isDirectCall()
                                    ? nickName : nickName);
                        }
                    });
                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException e) {
                    }
                }
            }
        }, "CallMonitor").start();
    }

    void stopMonitor(){
        monitor = false;
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        if(isInCalling){
            try {
                EMClient.getInstance().callManager().pauseVideoTransfer();
            } catch (HyphenateException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(isInCalling){
            try {
                EMClient.getInstance().callManager().resumeVideoTransfer();
            } catch (HyphenateException e) {
                e.printStackTrace();
            }
        }
    }
}