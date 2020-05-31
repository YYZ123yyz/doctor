package www.jykj.com.jykj_zxyl.fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.tbruyelle.rxpermissions.RxPermissions;


import entity.home.newsMessage.ProvideMsgPushReminderCount;
import entity.shouye.OperScanQrCodeInside;
import netService.HttpNetService;
import netService.entity.NetRetEntity;
import rx.functions.Action1;
import www.jykj.com.jykj_zxyl.R;
import www.jykj.com.jykj_zxyl.activity.MainActivity;
import www.jykj.com.jykj_zxyl.activity.home.DoctorsUnionActivity;
import www.jykj.com.jykj_zxyl.activity.home.MyClinicActivity;
import www.jykj.com.jykj_zxyl.activity.home.MyLiveRoomActivity;
import www.jykj.com.jykj_zxyl.activity.home.MyPatientActivity;
import www.jykj.com.jykj_zxyl.activity.home.NewsActivity;
import www.jykj.com.jykj_zxyl.activity.home.QRCodeActivity;
import www.jykj.com.jykj_zxyl.activity.home.tjhz.AddPatientActivity;
import www.jykj.com.jykj_zxyl.activity.hyhd.BindDoctorFriend;
import www.jykj.com.jykj_zxyl.activity.myself.UserAuthenticationActivity;
import www.jykj.com.jykj_zxyl.application.JYKJApplication;
import www.jykj.com.jykj_zxyl.custom.MoreFeaturesPopupWindow;
import zxing.android.CaptureActivity;
import zxing.common.Constant;

import static android.app.Activity.RESULT_OK;


/**
 * 首页fragment
 * Created by admin on 2016/6/1.
 */
public class FragmentShouYe extends Fragment implements View.OnClickListener {
    private Context mContext;
    private MainActivity mActivity;
    private String mNetRetStr;                 //返回字符串
    private Handler mHandler;
    private JYKJApplication mApp;
    private LinearLayout mQrCode;
    private LinearLayout mNews;
    private LinearLayout mDoctorUnion;
    private LinearLayout mYQTH;//邀请同行
    private LinearLayout mMyComments;//我的评价
    private LinearLayout mMyLiveRoom;//我的直播间
    private LinearLayout mAddPatient;                   //添加患者
    private LinearLayout mScan;      //扫一扫
    private LinearLayout mMyClinic;//我的诊所
    private LinearLayout mMyPatient;
    public static final int REQUEST_CODE_SCAN = 0x123;

    private TextView mUserNameText;                //用户名
    private TextView mUserTitleText;               //医生职称

    private TextView mNewMessage;                  //新消息提醒
    private LinearLayout mNewMessageLayout;          //新消息提醒
    private LinearLayout llQuickApplication;//快应用
    private MoreFeaturesPopupWindow mPopupWindow;


    private FragmentShouYe mFragment;

    public ProgressDialog mDialogProgress = null;

    private String qrCode;                         //需要绑定的二维码


    private ImageView mUserHead;

    private TextView home_Tv;


    public ProvideMsgPushReminderCount mProvideMsgPushReminderCount = new ProvideMsgPushReminderCount();
    private LinearLayout home_certification;

    private SharedPreferences sp;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_activitymain_shouyefragment, container, false);

        mContext = getContext();
        mActivity = (MainActivity) getActivity();
        mFragment = this;
        mApp = (JYKJApplication) getActivity().getApplication();
        initHandler();
        initView(v);
        initListener();

        return v;
    }


    private void initHandler() {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 0:
                        cacerProgress();
                        NetRetEntity netRetEntity = JSON.parseObject(mNetRetStr, NetRetEntity.class);
                        if (netRetEntity.getResCode() == 0)
                            Toast.makeText(mContext, netRetEntity.getResMsg(), Toast.LENGTH_SHORT).show();
                        else {
                            if ("1".equals(netRetEntity.getResData())) {
                                //医生扫医生二维码，绑定医生好友
                                final EditText et = new EditText(mContext);
                                new AlertDialog.Builder(mContext).setTitle("请输入申请描述")
                                        .setView(et)
                                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                //按下确定键后的事件
                                                bindDoctorFriend(netRetEntity.getResMsg(), et.getText().toString(), qrCode);
                                            }
                                        }).setNegativeButton("取消", null).show();
//
                            }
                            if ("2".equals(netRetEntity.getResMsg())) {
                                //医生扫医生联盟二维码
                            }
                            if ("3".equals(netRetEntity.getResMsg())) {
                                //医生扫患者二维码
                            }

                        }
                        break;
                    case 1:
                        cacerProgress();
                        netRetEntity = JSON.parseObject(mNetRetStr, NetRetEntity.class);
                        Toast.makeText(mContext, netRetEntity.getResMsg(), Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };
    }
    /**
     * 设置新消息提醒
     *
     * @param string
     */
    public void setNewMessageView(String string) {
        if ("".equals(string))
            mNewMessageLayout.setVisibility(View.GONE);
        else {
            mNewMessageLayout.setVisibility(View.VISIBLE);
            mNewMessage.setText(string);
            Log.e("ppp", "setNewMessageView: "+string );
        }
    }
    private void initView(View view) {
        mQrCode = view.findViewById(R.id.ll_qr_code);
        mNews = view.findViewById(R.id.ll_news);
        mDoctorUnion = view.findViewById(R.id.ll_doctor_union);
        mYQTH = view.findViewById(R.id.ll_yqth);
        mMyComments = view.findViewById(R.id.ll_my_comment);
        //添加患者
        mAddPatient = view.findViewById(R.id.li_home_addPatient);

        mMyLiveRoom = view.findViewById(R.id.ll_my_liveroom);
        mScan = view.findViewById(R.id.ll_sys);
        mMyClinic = view.findViewById(R.id.ll_wdzs);

        //用户头像

        mUserNameText = (TextView) view.findViewById(R.id.tv_fragmentShouYe_userNameText);

//        mUserNameText = (TextView) view.findViewById(R.id.tv_fragmentShouYe_userNameText);

        mUserTitleText = (TextView) view.findViewById(R.id.tv_fragmentShouYe_userTitleText);
        mMyPatient = view.findViewById(R.id.ll_wdhz);
        mNewMessage = (TextView) view.findViewById(R.id.tv_fragmentShouYe_NewMessage);
        if (mApp.mViewSysUserDoctorInfoAndHospital != null) {
            mUserNameText.setText(mApp.mViewSysUserDoctorInfoAndHospital.getUserName());
            mUserTitleText.setText(mApp.mViewSysUserDoctorInfoAndHospital.getDoctorTitleName());
        }

        mNewMessageLayout = (LinearLayout) view.findViewById(R.id.li_fragmentShouYe_newMessage);
        llQuickApplication = (LinearLayout) view.findViewById(R.id.ll_quick_application);
        //用户头像
        mUserHead = (ImageView) view.findViewById(R.id.iv_userhead);


//        //消息条数
//        ProvideMsgPushReminderCount provideMsgPushReminderCount = new ProvideMsgPushReminderCount();
//        Integer msgTypeCountSum = provideMsgPushReminderCount.getMsgTypeCountSum();
//
//        //有新消息
//        if (msgTypeCountSum != null && msgTypeCountSum != 0) {
//            String str = Integer.toString(msgTypeCountSum);
//            setNewMessageView(str);
//        }
        //医师资格认证
        home_certification = view.findViewById(R.id.home_certification);

        if (mApp.mViewSysUserDoctorInfoAndHospital != null) {
            if (mApp.mViewSysUserDoctorInfoAndHospital.getUserLogoUrl() != null && !"".equals(mApp.mViewSysUserDoctorInfoAndHospital.getUserLogoUrl())) {
                try {
                    int avatarResId = Integer.parseInt(mApp.mViewSysUserDoctorInfoAndHospital.getUserLogoUrl());
                    Glide.with(mContext).load(avatarResId).into(mUserHead);
                } catch (Exception e) {
                    //use default avatar
                    Glide.with(mContext).load(mApp.mViewSysUserDoctorInfoAndHospital.getUserLogoUrl())
                            .apply(RequestOptions.placeholderOf(com.hyphenate.easeui.R.mipmap.docter_heard)
                                    .diskCacheStrategy(DiskCacheStrategy.ALL))
                            .into(mUserHead);
                }
            }

        }

    }

    private void initListener() {
        mQrCode.setOnClickListener(this);
        mNews.setOnClickListener(this);
        mDoctorUnion.setOnClickListener(this);
//        mTWJZ.setOnClickListener(this);
        mYQTH.setOnClickListener(this);
        mMyComments.setOnClickListener(this);
        //添加患者
        mAddPatient.setOnClickListener(this);
        mMyLiveRoom.setOnClickListener(this);
        mScan.setOnClickListener(this);
        mMyClinic.setOnClickListener(this);
        mMyPatient.setOnClickListener(this);
        mNewMessageLayout.setOnClickListener(this);
        llQuickApplication.setOnClickListener(this);
        //医师资格认证
        home_certification.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_qr_code:
                startActivity(new Intent(getActivity(), QRCodeActivity.class));
                break;
            case R.id.ll_news:
                startActivity(new Intent(getActivity(), NewsActivity.class).putExtra("newMessage", mActivity.mProvideMsgPushReminderCount));
                break;
            case R.id.li_fragmentShouYe_newMessage:
                startActivity(new Intent(getActivity(), NewsActivity.class).putExtra("newMessage", mActivity.mProvideMsgPushReminderCount));
                break;
            case R.id.ll_doctor_union:
                startActivity(new Intent(getActivity(), DoctorsUnionActivity.class));
                break;

            case R.id.ll_yqth:
//                startActivity(new Intent(getActivity(),InvitepeersActivity.class));
                break;
            case R.id.ll_my_comment:
//                startActivity(new Intent(getActivity(),MyCommentActivity.class));
                break;

//            case R.id.li_fragmentShouYe_addPatient:
////                startActivity(new Intent(getActivity(),AddPatientActivity.class));
//                break;
            case R.id.ll_my_liveroom:
                startActivity(new Intent(getActivity(), MyLiveRoomActivity.class));
                break;
            case R.id.ll_sys:
                scan();
                break;
            //我的诊所
            case R.id.ll_wdzs:
                startActivity(new Intent(getActivity(), MyClinicActivity.class));
                break;
            case R.id.ll_wdhz:
                startActivity(new Intent(getActivity(), MyPatientActivity.class));
                break;
            case R.id.ll_quick_application:
                mPopupWindow = new MoreFeaturesPopupWindow(mActivity);
                mPopupWindow.setSouYeFragment(mFragment);
                if (mPopupWindow != null && !mPopupWindow.isShowing()) {
                    mPopupWindow.showAsDropDown(llQuickApplication, 0, 0);
                }
                break;
            //医师资格认证
            case R.id.home_certification:
                Intent intent = new Intent(getContext(), UserAuthenticationActivity.class);
                startActivity(intent);
                break;
            //添加患者
            case R.id.li_home_addPatient:
                mContext.startActivity(new Intent(mContext, AddPatientActivity.class));
                break;
        }
    }

    /**
     * 扫一扫
     */
    private void scan() {
        RxPermissions.getInstance(getActivity())
                .request(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE)
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {
                        if (aBoolean) {//允许权限，6.0以下默认true
                            Intent intent = new Intent(getActivity(), CaptureActivity.class);
                            startActivityForResult(intent, REQUEST_CODE_SCAN);
                        } else {
                            Toast.makeText(getActivity(), "获取权限失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 扫描二维码/条码回传
        if (requestCode == REQUEST_CODE_SCAN && resultCode == RESULT_OK) {
            if (data != null) {
                String content = data.getStringExtra(Constant.CODED_CONTENT);
                qrCode = content;
                operScanQrCodeInside(content);
            }
        }
    }

    private void operScanQrCodeInside(String content) {
        getProgressBar("请稍候", "正在处理");
        OperScanQrCodeInside operScanQrCodeInside = new OperScanQrCodeInside();
        operScanQrCodeInside.setLoginDoctorPosition(mApp.loginDoctorPosition);
        operScanQrCodeInside.setUserUseType("5");
        operScanQrCodeInside.setOperUserCode(mApp.mViewSysUserDoctorInfoAndHospital.getDoctorCode());
        operScanQrCodeInside.setOperUserName(mApp.mViewSysUserDoctorInfoAndHospital.getUserName());
        operScanQrCodeInside.setScanQrCode(content);

        new Thread() {
            public void run() {
                try {
                    String string = new Gson().toJson(operScanQrCodeInside);
                    mNetRetStr = HttpNetService.urlConnectionService("jsonDataInfo=" + string, www.jykj.com.jykj_zxyl.application.Constant.SERVICEURL + "doctorDataControlle/operScanQrCodeInside");
                } catch (Exception e) {
                    NetRetEntity retEntity = new NetRetEntity();
                    retEntity.setResCode(0);
                    retEntity.setResMsg("网络连接异常，请联系管理员：" + e.getMessage());
                    mNetRetStr = new Gson().toJson(retEntity);
                    e.printStackTrace();
                }
                mHandler.sendEmptyMessage(0);
            }
        }.start();
    }

    /**
     * 医生好友绑定
     */
    private void bindDoctorFriend(String url, String reason, String qrCode) {
        getProgressBar("请稍候", "正在处理");
        BindDoctorFriend bindDoctorFriend = new BindDoctorFriend();
        bindDoctorFriend.setLoginDoctorPosition(mApp.loginDoctorPosition);
        bindDoctorFriend.setBindingDoctorQrCode(qrCode);
        bindDoctorFriend.setOperDoctorCode(mApp.mViewSysUserDoctorInfoAndHospital.getDoctorCode());
        bindDoctorFriend.setOperDoctorName(mApp.mViewSysUserDoctorInfoAndHospital.getUserName());
        bindDoctorFriend.setApplyReason(reason);

        new Thread() {
            public void run() {
                try {
                    String string = new Gson().toJson(bindDoctorFriend);
                    mNetRetStr = HttpNetService.urlConnectionService("jsonDataInfo=" + string, www.jykj.com.jykj_zxyl.application.Constant.SERVICEURL + "/" + url);

                } catch (Exception e) {
                    NetRetEntity retEntity = new NetRetEntity();
                    retEntity.setResCode(0);
                    retEntity.setResMsg("网络连接异常，请联系管理员：" + e.getMessage());
                    mNetRetStr = new Gson().toJson(retEntity);
                    e.printStackTrace();
                }
                mHandler.sendEmptyMessage(1);
            }
        }.start();
    }

    /**
     * 获取进度条
     */

    public void getProgressBar(String title, String progressPrompt) {
        if (mDialogProgress == null) {
            mDialogProgress = new ProgressDialog(mContext);
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



}
