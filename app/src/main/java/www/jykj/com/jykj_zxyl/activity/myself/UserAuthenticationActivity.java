package www.jykj.com.jykj_zxyl.activity.myself;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import entity.mySelf.ProvideViewSysUserDoctorInfoAndHospital;
import entity.mySelf.UpLoadImgParment;
import entity.patientmanager.ProvideIdentityNumberInfo;
import netService.HttpNetService;
import netService.entity.NetRetEntity;
import orcameralib.CameraActivity;
import www.jykj.com.jykj_zxyl.R;
import www.jykj.com.jykj_zxyl.activity.home.tjhz.AddPatientQRCodeActivity;
import www.jykj.com.jykj_zxyl.app_base.base_activity.BaseActivity;
import www.jykj.com.jykj_zxyl.application.Constant;
import www.jykj.com.jykj_zxyl.application.JYKJApplication;
import www.jykj.com.jykj_zxyl.custom.MoreFeaturesPopupWindow;
import www.jykj.com.jykj_zxyl.live.adapter.ImageAdapter;
import www.jykj.com.jykj_zxyl.util.ActivityUtil;
import www.jykj.com.jykj_zxyl.util.BitmapUtil;
import www.jykj.com.jykj_zxyl.util.FullyGridLayoutManager;
import www.jykj.com.jykj_zxyl.util.ImageViewUtil;
import www.jykj.com.jykj_zxyl.util.Util;
import yyz_exploit.bean.ProvideDoctorQualification;

import static orcameralib.CameraActivity.KEY_CONTENT_TYPE;
import static orcameralib.CameraActivity.KEY_OUTPUT_FILE_PATH;

public class UserAuthenticationActivity extends BaseActivity {

    private TextView mPhoneLogin;                //手机号登录
    private TextView mUseRegist;                 //用户注册
    private Button mLogin;                     //登录
    private Context mContext;
    private UserAuthenticationActivity mActivity;
    private TextView mDetail;                        //明细
    private LinearLayout mBack;
    private RelativeLayout mIDCardFrontLayout;               //身份证正面

    private String mIDCardFrontPath;                       //身份证正面路径
    private RelativeLayout mIDCardBackLayout;                  //身份证反面
    private int mIDCardFrontRequstCode = 1000;             //身份证正面获取码
    private int mIDCardBackRequstCode = 1001;             //身份证反面获取码
    private JYKJApplication mApp;

    public ProgressDialog mDialogProgress = null;                                  //进度条
    public String mRetString;

    private String mNetRetStr;                 //返回字符串
    private Handler mHandler;
    private ImageView mIDCardFont;                //身份证正面
    private ImageView mIDCardBack;                //身份证反面
    private ImageView mZYZImage;                  //执业证
    private ImageView mZGZImage;                  //资格证
    private ImageView mZCZImage;                  //职称证

    private File mTempFile;              //声明一个拍照结果的临时文件

    private int mCurrentPhoto;              //当前照片类型  1=执业证  2=资格证  3=职称证
    private int mPhotoType;                 //图片类型  1、身份证正面   2、身份证反面  3、执业证  4、资格证  5、职称证
    private String mPhotoBitmapString;             //图片base64字符串
    private Button mCommit;
    private ProvideDoctorQualification provideDoctorQualification;
    private NetRetEntity netRetEntity;
    private LinearLayout lin;
    private ImageView iv_zyz_img;
    private ImageView iv_zgz_img;
    private ImageView iv_zcz_img;
    private LinearLayout lin_data;
    private ImageButton ivAdd;
    private MoreFeaturesPopupWindow mPopupWindow;
    private RecyclerView mImageRecycleView;
    private FullyGridLayoutManager mGridLayoutManager;
    private List<Photo_Info> mPhotoInfos = new ArrayList<>();
    private ImageAdapter mImageAdapter;

    /**
     * 创建临时文件夹 _tempphoto
     */
    private void initDir() {
        // 声明目录
        File tempDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/_tempphoto");
        if (!tempDir.exists()) {
            tempDir.mkdirs();// 创建目录
        }
        mIDCardFrontPath = new File(tempDir, BitmapUtil.getPhotoFileName()).toString();// 生成临时文件
        mTempFile = new File(tempDir, BitmapUtil.getPhotoFileName());// 生成临时文件
    }



    @Override
    protected int setLayoutId() {
        return R.layout.activity_fragmentself_userauthentication;
    }

    @Override
    protected void initView() {
        super.initView();
        mContext = this;
        mActivity = this;
        mApp = (JYKJApplication) getApplication();
        ActivityUtil.setStatusBarMain(mActivity);
        initDir();
        initHandler();
        initLayout();
        getDate();
    }

    /**
     * 加载数据
     */
    private void getDate() {
        getProgressBar("请稍候", "正在加载数据。。。");
        //开始识别
        new Thread() {
            public void run() {
//                //提交数据
                try {
                    UpLoadImgParment upLoadImgParment = new UpLoadImgParment();
                    upLoadImgParment.setLoginDoctorPosition(mApp.loginDoctorPosition);
                    upLoadImgParment.setOperDoctorCode(mApp.mViewSysUserDoctorInfoAndHospital.getDoctorCode());
                    upLoadImgParment.setOperDoctorName(mApp.mViewSysUserDoctorInfoAndHospital.getUserName());
                    String str = new Gson().toJson(upLoadImgParment);
                    mNetRetStr = HttpNetService.urlConnectionService("jsonDataInfo=" + str, Constant.SERVICEURL + "doctorPersonalSetControlle/getUserDoctorQualificationImg");
                    System.out.println("~~~~~~~~~~~~~返回~~~~~~~~~~~~~~" + mRetString);
                    NetRetEntity netRetEntity = new Gson().fromJson(mNetRetStr, NetRetEntity.class);
                    Log.e("tag", "run:vvv "+mNetRetStr );
                    if (netRetEntity.getResCode() == 0) {
                        NetRetEntity retEntity = new NetRetEntity();
                        retEntity.setResCode(0);
                        retEntity.setResMsg("提交失败：" + netRetEntity.getResMsg());
                        mNetRetStr = new Gson().toJson(retEntity);
                        mHandler.sendEmptyMessage(1);
                        return;
                    }

                } catch (Exception e) {
                    NetRetEntity retEntity = new NetRetEntity();
                    retEntity.setResCode(0);
                    retEntity.setResMsg("网络连接异常，请联系管理员：" + e.getMessage());
                    mNetRetStr = new Gson().toJson(retEntity);
                    mHandler.sendEmptyMessage(1);
                    return;
                }

                mHandler.sendEmptyMessage(1);
            }
        }.start();
    }

    /**
     *
     */
    @SuppressLint("HandlerLeak")
    private void initHandler() {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 1:
                        cacerProgress();
                        netRetEntity = JSON.parseObject(mNetRetStr, NetRetEntity.class);
                        if(netRetEntity.getResCode()==0) {
                          //  lin.setVisibility(View.GONE);
                        }else{
                            provideDoctorQualification = JSON.parseObject(JSON.parseObject(mNetRetStr, NetRetEntity.class).getResJsonData(), ProvideDoctorQualification.class);

//                            mIDCardFont = (ImageView) this.findViewById(R.id.iv_idcardFont);
//                            mIDCardBack = (ImageView) this.findViewById(R.id.iv_idcardBack);
//                            iv_zyz_img = findViewById(R.id.iv_zyz_img);
//                            iv_zgz_img = findViewById(R.id.iv_zgz_img);
//                            iv_zcz_img = findViewById(R.id.iv_zcz_img);
//                            mIDCardFont = (ImageView) this.findViewById(R.id.iv_idcardFont);
//                            mIDCardBack = (ImageView) this.findViewById(R.id.iv_idcardBack);
                            //
                            if(!TextUtils.isEmpty(provideDoctorQualification.getIdNumberPositiveImgUrl())){
                                ImageViewUtil.showImageView(mActivity, provideDoctorQualification.getIdNumberPositiveImgUrl(), mIDCardFont);
                            }
                            if(!TextUtils.isEmpty(provideDoctorQualification.getIdNumberSideImgUrl())){
                                ImageViewUtil.showImageView(mActivity, provideDoctorQualification.getIdNumberSideImgUrl(), mIDCardBack);
                            }
                            if(!TextUtils.isEmpty(provideDoctorQualification.getPractisingImgUrl())){
//                                Bitmap bm = BitmapFactory.decodeFile(provideDoctorQualification.getPractisingImgUrl());
//                                mZYZImage.setImageBitmap(bm);
                                ImageViewUtil.showImageView(mActivity, provideDoctorQualification.getPractisingImgUrl(), mZYZImage);
                                iv_zyz_img.setVisibility(View.GONE);
                            }
                            if(!TextUtils.isEmpty(provideDoctorQualification.getProfessionalImgUrl())){
//                                Bitmap bm = BitmapFactory.decodeFile(provideDoctorQualification.getProfessionalImgUrl());
//                                mZGZImage.setImageBitmap(bm);
                                ImageViewUtil.showImageView(mActivity, provideDoctorQualification.getProfessionalImgUrl(),mZGZImage );
                                iv_zgz_img .setVisibility(View.GONE);
                            }
                            if(!TextUtils.isEmpty(provideDoctorQualification.getQualificationImgUrl())){
//                                Bitmap bm = BitmapFactory.decodeFile(provideDoctorQualification.getQualificationImgUrl());
//                                mZCZImage.setImageBitmap(bm);
                                ImageViewUtil.showImageView(mActivity, provideDoctorQualification.getQualificationImgUrl(),mZCZImage );
                                iv_zcz_img  .setVisibility(View.GONE);
                            }
                        }

                        break;
                    case 2:
                        cacerProgress();
                        break;
                    case 3:
                        cacerProgress();
                        netRetEntity = JSON.parseObject(mNetRetStr, NetRetEntity.class);
                        if (netRetEntity.getResCode() == 0) {
                            Log.e("tag", "handleMessage: "+"失败" );
                            Toast.makeText(mContext, netRetEntity.getResMsg(), Toast.LENGTH_SHORT).show();
                            return;
                        } else {
                            Log.e("tag", "handleMessage: "+mPhotoType+"" );
                            switch (mPhotoType) {

                                case 1:
                                    Glide.with(mContext).load(BitmapUtil.stringtoBitmap(mPhotoBitmapString)).into(mIDCardFont);
                                    break;
                                case 2:
                                    Glide.with(mContext).load(BitmapUtil.stringtoBitmap(mPhotoBitmapString)).into(mIDCardBack);
                                    break;
                                case 3:
                                    Glide.with(mContext).load(BitmapUtil.stringtoBitmap(mPhotoBitmapString)).into(mZYZImage);
                                    break;
                                case 4:
                                    Glide.with(mContext).load(BitmapUtil.stringtoBitmap(mPhotoBitmapString)).into(mZCZImage);
                                    break;
                                case 5:
                                    Glide.with(mContext).load(BitmapUtil.stringtoBitmap(mPhotoBitmapString)).into(mZGZImage);
                                    break;
                            }
                        }

                        break;

                    case 5:
                        cacerProgress();
                        netRetEntity = JSON.parseObject(mNetRetStr, NetRetEntity.class);
                        Toast.makeText(mContext, netRetEntity.getResMsg(), Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == mIDCardFrontRequstCode && resultCode == RESULT_OK) {
//            getProgressBar("请稍候", "正在处理。。。");
            new Thread() {
                public void run() {
                    String bitmapString = mApp.gBitMapString;
                    upLoadImg(1, bitmapString);
                }
            }.start();


        }
        if (requestCode == mIDCardBackRequstCode && resultCode == RESULT_OK) {
//            getProgressBar("请稍候", "正在处理。。。");
            new Thread() {
                public void run() {
                    String bitmapString = mApp.gBitMapString;
                    upLoadImg(2, bitmapString);
                }
            }.start();

        }
        try {
            // 如果是直接从相册获取
            if (requestCode == Constant.SELECT_PIC_FROM_ALBUM
                    && resultCode == RESULT_OK
                    && data != null) {
                setPicToView(data);
                final Uri uri = data.getData();//返回相册图片的Uri
                BitmapUtil.startPhotoZoom(mActivity, uri, 450);
                final Bitmap[] photo = new Bitmap[1];
//                getProgressBar("请稍候", "正在处理。。。");
                new Thread() {
                    public void run() {
                        if (uri != null) {
                            try {
                                photo[0] = BitmapUtil.getimageBitmap(BitmapFactory.decodeStream(getContentResolver().openInputStream(data.getData())));//将imageUri对象的图片加载到内存
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                        } else {
                            System.out.println("进来了");
                            try {
                                photo[0] = BitmapUtil.getimageBitmap(BitmapFactory.decodeStream(getContentResolver().openInputStream(Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "test.jpg")))));//将imageUri对象的图片加载到内存
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                        System.out.println("图片：" + photo[0]);
                        switch (mCurrentPhoto) {
                            case 1:
                                upLoadImg(3, BitmapUtil.bitmaptoString(photo[0]));
                                break;
                            case 2:
                                upLoadImg(4, BitmapUtil.bitmaptoString(photo[0]));
                                break;
                            case 3:
                                upLoadImg(5, BitmapUtil.bitmaptoString(photo[0]));
                                break;
                        }

                    }
                }.start();
            }

            // 处理拍照返回
            if (requestCode == Constant.SELECT_PIC_BY_TACK_PHOTO
                    && resultCode == RESULT_OK) {// 拍照成功 RESULT_OK= -1

                new Thread() {
                    public void run() {
                        Bitmap photo = null;
                        try {
                            photo = BitmapUtil.getimageBitmap(BitmapFactory.decodeStream(getContentResolver().openInputStream(Uri.fromFile(mTempFile))));//将imageUri对象的图片加载到内存
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        switch (mCurrentPhoto) {
                            case 1:
                                upLoadImg(3, BitmapUtil.bitmaptoString(photo));
                                break;
                            case 2:
                                upLoadImg(4, BitmapUtil.bitmaptoString(photo));
                                break;
                            case 3:
                                upLoadImg(5, BitmapUtil.bitmaptoString(photo));
                                break;
                        }
                    }
                }.start();
            }
        } catch (Exception e) {
            Log.i("yi", "yichahahaha");
        }
    }
    //listview集合显示
    private void setPicToView(Intent data) {
        Bitmap photo;
        try {
            Uri u = data.getData();
            if (u != null) {
                photo = BitmapFactory.decodeStream(getContentResolver().openInputStream(data.getData()));//将imageUri对象的图片加载到内存
            } else {
                System.out.println("进来了");
                photo = BitmapFactory.decodeStream(getContentResolver().openInputStream(Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "test.jpg"))));//将imageUri对象的图片加载到内存
            }
            System.out.println("图片：" + photo);
            Photo_Info photo_info = new Photo_Info();
            photo_info.setPhoto(BitmapUtil.bitmaptoString(photo));
            mPhotoInfos.add(photo_info);
            mImageAdapter.setDate(mPhotoInfos);
            mImageAdapter.notifyDataSetChanged();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    //回调函数
    public void upLoadImg(int type, String bitmapString) {

        mPhotoType = type;
        mPhotoBitmapString = bitmapString;
//        //开始识别
//        new Thread() {
//            public void run() {
                //提交数据
        try {
            UpLoadImgParment upLoadImgParment = new UpLoadImgParment();
            upLoadImgParment.setLoginDoctorPosition(mApp.loginDoctorPosition);
            upLoadImgParment.setOperDoctorCode(mApp.mViewSysUserDoctorInfoAndHospital.getDoctorCode());
            upLoadImgParment.setOperDoctorName(mApp.mViewSysUserDoctorInfoAndHospital.getUserName());
            upLoadImgParment.setFlagImgType(type + "");
            upLoadImgParment.setImgBase64Data((URLEncoder.encode("data:image/jpg;base64," + bitmapString)));
         //   Log.e("tag", "图片 "+upLoadImgParment.getImgBase64Data().toString() );
         //   Log.e("tag", "图片 "+mPhotoBitmapString );
            String str = new Gson().toJson(upLoadImgParment);
            System.out.println("~~~~~~~~~~~~~开始提交了~~~~~~~~~~~~~~");
            mNetRetStr = HttpNetService.urlConnectionService("jsonDataInfo=" + str, Constant.SERVICEURL + "doctorPersonalSetControlle/operUserDoctorStatus");
            Log.e("tag", "upLoadImg: "+mNetRetStr );
        } catch (Exception e) {
            NetRetEntity retEntity = new NetRetEntity();
            retEntity.setResCode(0);
            retEntity.setResMsg("网络连接异常，请联系管理员：" + e.getMessage());
            mNetRetStr = new Gson().toJson(retEntity);
            mHandler.sendEmptyMessage(3);
            return;
        }
        mHandler.sendEmptyMessage(3);
//            }
//        }.start();
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

    /**
     * 初始化布局
     */
    private void initLayout() {
        //照片listview
        mImageRecycleView = (RecyclerView) this.findViewById(R.id.rv_img);
        mGridLayoutManager = new FullyGridLayoutManager(mContext, 3);
        mGridLayoutManager.setOrientation(LinearLayout.VERTICAL);
        mImageRecycleView.setLayoutManager(mGridLayoutManager);
        //如果可以确定每个item的高度是固定的，设置这个选项可以提高性能
        mImageRecycleView.setHasFixedSize(true);
        //创建并设置Adapter
        final Photo_Info photo_info = new Photo_Info();
        photo_info.setPhotoID("ADDPHOTO");
        mPhotoInfos.add(photo_info);
        mImageAdapter = new ImageAdapter(mPhotoInfos,mApp);
        mImageRecycleView.setAdapter(mImageAdapter);
        mImageAdapter.setOnItemClickListener(new ImageAdapter.OnItemClickListener() {
            @Override
            public void onClick(final int position) {
                if (mPhotoInfos.size() >= 4) {
                    Toast.makeText(mContext, "照片不超过三张", Toast.LENGTH_SHORT).show();
                    return;
                }
                if ("ADDPHOTO".equals(mPhotoInfos.get(position).getPhotoID())) {
                    String[] items = {"拍照", "从相册选择"};
                    Dialog dialog = new AlertDialog.Builder(mContext)
                            .setItems(items, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    switch (i) {
                                        case 0:
                                            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                                            StrictMode.setVmPolicy(builder.build());
                                            builder.detectFileUriExposure();
                                            // 添加Action类型：MediaStore.ACTION_IMAGE_CAPTURE
                                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                            // 指定调用相机拍照后照片(结果)的储存路径
                                            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mTempFile));
                                            // 等待返回结果
                                            startActivityForResult(intent, Constant.SELECT_PIC_BY_TACK_PHOTO);
                                            break;
                                        case 1:
                                            BitmapUtil.selectAlbum(mActivity);//从相册选择
                                            break;
                                    }
                                }
                            }).show();
                } else {
                    Dialog dialog = new AlertDialog.Builder(mContext)
                            .setMessage("删除该照片")
                            .setPositiveButton("是", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    mPhotoInfos.remove(position);
                                    if (mPhotoInfos.size() == 0) {
                                        Photo_Info photo_info1 = new Photo_Info();
                                        photo_info1.setPhotoID("ADDPHOTO");
                                        mPhotoInfos.add(photo_info1);
                                    }
                                    mImageAdapter.setDate(mPhotoInfos);
                                    mImageAdapter.notifyDataSetChanged();
                                }
                            }).setNegativeButton("否", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            }).show();
                }
            }

            @Override
            public void onLongClick(int position) {

            }
        });



       /* lin_data = findViewById(R.id.lin_data);
        //提交状态的布局
        lin = findViewById(R.id.lin);*/
        ivAdd = findViewById(R.id.right_image_search);
        ivAdd.setOnClickListener(new ButtonClick());

        iv_zyz_img = findViewById(R.id.iv_zyz_img);
        iv_zgz_img = findViewById(R.id.iv_zgz_img);
        iv_zcz_img = findViewById(R.id.iv_zcz_img);

        mBack = (LinearLayout) this.findViewById(R.id.li_activityAuthentication_back);
        mBack.setOnClickListener(new ButtonClick());

        mIDCardFrontLayout = (RelativeLayout) this.findViewById(R.id.ri_idcardFront);
        mIDCardFrontLayout.setOnClickListener(new ButtonClick());

        mIDCardBackLayout = (RelativeLayout) this.findViewById(R.id.ri_idcardBack);
        mIDCardBackLayout.setOnClickListener(new ButtonClick());

        mIDCardFont = (ImageView) this.findViewById(R.id.iv_idcardFont);
        mIDCardBack = (ImageView) this.findViewById(R.id.iv_idcardBack);

        mZYZImage = (ImageView) this.findViewById(R.id.iv_zyz);
        mZYZImage.setOnClickListener(new ButtonClick());
        mZGZImage = (ImageView) this.findViewById(R.id.iv_zgz);
        mZGZImage.setOnClickListener(new ButtonClick());
        mZCZImage = (ImageView) this.findViewById(R.id.iv_zcz);
        mZCZImage.setOnClickListener(new ButtonClick());

        mCommit = (Button) this.findViewById(R.id.bt_commit);
        mCommit.setOnClickListener(new ButtonClick());
    }


    /**
     * 点击事件
     */
    class ButtonClick implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.li_activityAuthentication_back:
                    finish();
                    break;
                case R.id.right_image_search:
                    if (mPopupWindow ==null){
                        mPopupWindow = new MoreFeaturesPopupWindow(UserAuthenticationActivity.this);
                    }
                    if (!mPopupWindow.isShowing()) {
                        mPopupWindow.showAsDropDown(ivAdd, 0, 0);
                    }else {
                        mPopupWindow.dismiss();
                    }
                    break;

                case R.id.ri_idcardFront:
                    startActivityForResult(new Intent(mContext, CameraActivity.class)
                            .putExtra(KEY_CONTENT_TYPE, "IDCardFront")
                            .putExtra(KEY_OUTPUT_FILE_PATH, mIDCardFrontPath), mIDCardFrontRequstCode);
                    break;

                case R.id.ri_idcardBack:
                    startActivityForResult(new Intent(mContext, CameraActivity.class)
                            .putExtra(KEY_CONTENT_TYPE, "IDCardBack")
                            .putExtra(KEY_OUTPUT_FILE_PATH, mIDCardFrontPath), mIDCardBackRequstCode);
                    break;

                case R.id.iv_zyz:
                    mCurrentPhoto = 1;
                    String[] items = {"拍照", "从相册选择"};
                    Dialog dialog = new android.support.v7.app.AlertDialog.Builder(mContext)
                            .setItems(items, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    switch (i) {
                                        case 0:
                                            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                                            StrictMode.setVmPolicy(builder.build());
                                            builder.detectFileUriExposure();
                                            // 添加Action类型：MediaStore.ACTION_IMAGE_CAPTURE
                                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                            // 指定调用相机拍照后照片(结果)的储存路径
                                            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mTempFile));
                                            // 等待返回结果
                                            startActivityForResult(intent, Constant.SELECT_PIC_BY_TACK_PHOTO);
                                            break;
                                        case 1:
                                            BitmapUtil.selectAlbum(mActivity);//从相册选择
                                            break;
                                    }
                                }
                            }).show();
                    break;
                case R.id.iv_zgz:
                    mCurrentPhoto = 3;
                    String[] items2 = {"拍照", "从相册选择"};
                    Dialog dialog2 = new android.support.v7.app.AlertDialog.Builder(mContext)
                             .setItems(items2, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    switch (i) {
                                        case 0:
                                            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                                            StrictMode.setVmPolicy(builder.build());
                                            builder.detectFileUriExposure();
                                            // 添加Action类型：MediaStore.ACTION_IMAGE_CAPTURE
                                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                            // 指定调用相机拍照后照片(结果)的储存路径
                                            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mTempFile));
                                            // 等待返回结果
                                            startActivityForResult(intent, Constant.SELECT_PIC_BY_TACK_PHOTO);
                                            break;
                                        case 1:
                                            BitmapUtil.selectAlbum(mActivity);//从相册选择
                                            break;
                                    }
                                }
                            }).show();
                    break;
                case R.id.iv_zcz:
                    mCurrentPhoto = 2;
                    String[] items3 = {"拍照", "从相册选择"};
                    Dialog dialog3 = new android.support.v7.app.AlertDialog.Builder(mContext)
                            .setItems(items3, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    switch (i) {
                                        case 0:
                                            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                                            StrictMode.setVmPolicy(builder.build());
                                            builder.detectFileUriExposure();
                                            // 添加Action类型：MediaStore.ACTION_IMAGE_CAPTURE
                                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                            // 指定调用相机拍照后照片(结果)的储存路径
                                            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mTempFile));
                                            // 等待返回结果
                                            startActivityForResult(intent, Constant.SELECT_PIC_BY_TACK_PHOTO);
                                            break;
                                        case 1:
                                            BitmapUtil.selectAlbum(mActivity);//从相册选择
                                            break;
                                    }
                                }
                            }).show();
                    break;

                case R.id.bt_commit:
                    commit();
                    break;
            }
        }
    }

    /**
     * 提交
     */
    private void commit() {

        getProgressBar("请稍候", "正在提交。。。");
        //开始识别
        new Thread() {
            public void run() {
//                //提交数据
                try {
                    UpLoadImgParment upLoadImgParment = new UpLoadImgParment();
                    upLoadImgParment.setLoginDoctorPosition(mApp.loginDoctorPosition);
                    upLoadImgParment.setOperDoctorCode(mApp.mViewSysUserDoctorInfoAndHospital.getDoctorCode());
                    upLoadImgParment.setOperDoctorName(mApp.mViewSysUserDoctorInfoAndHospital.getUserName());
                    String str = new Gson().toJson(upLoadImgParment);
                    System.out.println("~~~~~~~~~~~~~开始提交了~~~~~~~~~~~~~~");
                    mNetRetStr = HttpNetService.urlConnectionService("jsonDataInfo=" + str, Constant.SERVICEURL + "doctorPersonalSetControlle/operUserDoctorStatus");
                    System.out.println("~~~~~~~~~~~~~返回~~~~~~~~~~~~~~" + mRetString);
                    NetRetEntity netRetEntity = new Gson().fromJson(mNetRetStr, NetRetEntity.class);
                    Log.e("TAG", "run: 提交 "+mNetRetStr );
                    if (netRetEntity.getResCode() == 0) {
                        NetRetEntity retEntity = new NetRetEntity();
                        retEntity.setResCode(0);
                        retEntity.setResMsg("提交失败：" + netRetEntity.getResMsg());

                        mNetRetStr = new Gson().toJson(retEntity);
                        mHandler.sendEmptyMessage(5);
                        return;
                    }

                } catch (Exception e) {
                    NetRetEntity retEntity = new NetRetEntity();
                    retEntity.setResCode(0);
                    retEntity.setResMsg("网络连接异常，请联系管理员：" + e.getMessage());
                    mNetRetStr = new Gson().toJson(retEntity);
                    mHandler.sendEmptyMessage(5);
                    return;
                }

                mHandler.sendEmptyMessage(5);
            }
        }.start();

    }


}
