package www.jykj.com.jykj_zxyl.mypatient.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;

import entity.shouye.OperScanQrCodeInside;
import netService.HttpNetService;
import netService.entity.NetRetEntity;
import www.jykj.com.jykj_zxyl.R;
import www.jykj.com.jykj_zxyl.activity.hyhd.BindDoctorFriend;
import www.jykj.com.jykj_zxyl.app_base.base_activity.BaseActivity;
import www.jykj.com.jykj_zxyl.application.JYKJApplication;
import www.jykj.com.jykj_zxyl.custom.MoreFeaturesPopupWindow;
import www.jykj.com.jykj_zxyl.fragment.MyPatientFragment;
import www.jykj.com.jykj_zxyl.fragment.MyPatientNotSignetFragment;
import www.jykj.com.jykj_zxyl.fragment.MyReviewFragment;
import www.jykj.com.jykj_zxyl.mypatient.fragment.NotSignedPatientFragment;
import www.jykj.com.jykj_zxyl.mypatient.fragment.SignedPatientFragment;
import www.jykj.com.jykj_zxyl.util.ActivityUtil;
import zxing.common.Constant;

/**
 * 我的患者
 */
public class PatientActivity extends BaseActivity {

    private Context mContext;
    private PatientActivity mActivity;
    private TextView mMyPatient;
    private TextView mMyReview;
    private TextView mMyPatientN;
    private LinearLayout llBack;

    private FragmentManager fragmentManager;
    private MyPatientFragment myPatientFragment;
    private MyReviewFragment myReviewFragment;
    private MyPatientNotSignetFragment myPatientNotSignetFragment;
    private ImageView ivAdd;
    private MoreFeaturesPopupWindow mPopupWindow;

    public static final int REQUEST_CODE_SCAN = 0x123;
    private String qrCode;                         //需要绑定的二维码
    private String mNetRetStr;                 //返回字符串
    private Handler mHandler;
    public ProgressDialog mDialogProgress = null;
    private JYKJApplication mApp;
    private SignedPatientFragment signedPatientFragment;
    private NotSignedPatientFragment notSignedPatientFragment;
    @Override
    protected int setLayoutId() {
        return R.layout.activity_my_patient2;
    }

    @Override
    protected void initView() {
        super.initView();
        ActivityUtil.setStatusBarMain(this);
        mApp = (JYKJApplication) getApplication();
        mContext = this;
        mActivity = this;
        initLayout();
        initHandler();
        setIndex(0);
    }

    /**
     * 初始化布局
     */
    private void initLayout() {
        mMyPatient = findViewById(R.id.tv_my_patient);
        ivAdd = findViewById(R.id.iv_add);
        llBack = findViewById(R.id.ll_back);
        mMyPatientN = this.findViewById(R.id.tv_my_patient_n);
        fragmentManager = getSupportFragmentManager();
        mMyPatient.setOnClickListener(new ButtonClick());
        llBack.setOnClickListener(new ButtonClick());
        ivAdd.setOnClickListener(new ButtonClick());
        mMyPatientN.setOnClickListener(new ButtonClick());

    }


    /**
     * 点击事件
     */
    class ButtonClick implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.tv_my_patient:
                    setIndex(0);
                    mMyPatient.setBackgroundResource(R.mipmap.pg_messagetitle);
                    mMyPatientN.setBackgroundResource(0);
                    mMyPatientN.setTextColor(getResources().getColor(R.color.writeColor));
                    mMyPatient.setTextColor(getResources().getColor(R.color.tabColor_nomal));
                    break;
                case R.id.tv_my_patient_n:
                    setIndex(1);
                    mMyPatient.setBackgroundResource(0);
                    mMyPatientN.setBackgroundResource(R.mipmap.pg_messagetitle);
                    mMyPatientN.setTextColor(getResources().getColor(R.color.tabColor_nomal));
                    mMyPatient.setTextColor(getResources().getColor(R.color.writeColor));
                    break;
                case R.id.ll_back:
                    finish();
                    break;
                case R.id.iv_add:
                    mPopupWindow = new MoreFeaturesPopupWindow(mActivity);
                    mPopupWindow.setMyPatientActivity(mActivity);
                    if (mPopupWindow != null && !mPopupWindow.isShowing()) {
                        mPopupWindow.showAsDropDown(ivAdd, 0, 0);
                    }
                    break;
            }
        }
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

    @SuppressLint("HandlerLeak")
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
                            Toast.makeText(context, netRetEntity.getResMsg(), Toast.LENGTH_SHORT).show();
                        else {
                            if ("1".equals(netRetEntity.getResData())) {
                                //医生扫医生二维码，绑定医生好友
                                final EditText et = new EditText(context);
                                new AlertDialog.Builder(context).setTitle("请输入申请描述")
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
                        Toast.makeText(context, netRetEntity.getResMsg(), Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };
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
     * 获取进度条
     */

    public void getProgressBar(String title, String progressPrompt) {
        if (mDialogProgress == null) {
            mDialogProgress = new ProgressDialog(context);
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

    private void setIndex(int index) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        hideFragments(transaction);
        switch (index) {
            case 0:
                if (signedPatientFragment == null) {
                    signedPatientFragment = new SignedPatientFragment();
                    transaction.add(R.id.content, signedPatientFragment);
                } else {
                    transaction.show(signedPatientFragment);
                }
                break;
            case 1:
                if (notSignedPatientFragment == null) {
                    notSignedPatientFragment = new NotSignedPatientFragment();
                    transaction.add(R.id.content, notSignedPatientFragment);
                } else {
                    transaction.show(notSignedPatientFragment);
                }

                break;

        }
        transaction.commit();
    }


    private void hideFragments(FragmentTransaction transaction) {
        if (signedPatientFragment != null) {
            transaction.hide(signedPatientFragment);
        }
        if (notSignedPatientFragment != null) {
            transaction.hide(notSignedPatientFragment);
        }
//        if (myReviewFragment != null) {
//            transaction.hide(myReviewFragment);
//        }

    }

}