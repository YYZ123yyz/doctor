package www.jykj.com.jykj_zxyl.mypatient.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.allen.library.utils.ToastUtils;
import com.google.gson.Gson;
import com.hyphenate.easeui.hyhd.model.Constant;
import com.hyphenate.easeui.jykj.activity.SigningDetailsActivity;
import com.hyphenate.easeui.jykj.bean.OrderMessage;
import com.hyphenate.easeui.netService.HttpNetService;
import com.hyphenate.easeui.utils.CollectionUtils;
import com.scwang.smart.refresh.footer.ClassicsFooter;
import com.scwang.smart.refresh.header.ClassicsHeader;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.Unbinder;
import entity.patientInfo.ProvideViewPatientLablePunchClockState;
import netService.entity.NetRetEntity;
import www.jykj.com.jykj_zxyl.R;
import www.jykj.com.jykj_zxyl.activity.hyhd.ChatActivity;
import www.jykj.com.jykj_zxyl.activity.hzgl.HZGLHZZLActivity;
import www.jykj.com.jykj_zxyl.activity.hzgl.HZGLTXHZActivity;
import www.jykj.com.jykj_zxyl.adapter.MyPatientRecyclerAdapter;
import www.jykj.com.jykj_zxyl.app_base.base_html5.H5Activity;
import www.jykj.com.jykj_zxyl.app_base.mvp.AbstractMvpBaseFragment;
import www.jykj.com.jykj_zxyl.application.JYKJApplication;
import www.jykj.com.jykj_zxyl.mypatient.contract.FragmentContract;
import www.jykj.com.jykj_zxyl.mypatient.presenter.FragmentPresenter;
import www.jykj.com.jykj_zxyl.personal.activity.StateDetActivity;
import www.jykj.com.jykj_zxyl.util.DateUtils;
import yyz_exploit.activity.activity.RefuseActivity;
import yyz_exploit.activity.activity.TerminationActivity;

/**
 * Description:
 *
 * @author: qiuxinhai
 * @date: 2020-12-26 15:04
 */
public class  SignedPatientChildFragment  extends AbstractMvpBaseFragment<FragmentContract.View,
        FragmentPresenter> implements FragmentContract.View {

    @BindView(R.id.all_recy)
    RecyclerView mAllRecy;
    Unbinder unbinder;
    @BindView(R.id.refreshLayout)
    SmartRefreshLayout refreshLayout;
    @BindView(R.id.tvNoData)
    TextView tvNoData;
    private JYKJApplication mApp;
    private LinearLayoutManager mLayoutManager;
    private List<ProvideViewPatientLablePunchClockState> mDatas;
    private MyPatientRecyclerAdapter myPatientRecyclerAdapter;
    private Handler mHandler;
    private String mNetRetStr;
    private String sourceType;
    private String patientName;
    private Integer ageStart;
    private Integer ageEnd;
    @Override
    protected int setLayoutId() {
        return R.layout.item_allfragment;
    }

    @Override
    protected void initView(View view) {
        super.initView(view);
        mDatas = new ArrayList<>();
        mApp = (JYKJApplication) getActivity().getApplication();
        //recy
        mLayoutManager = new LinearLayoutManager(getContext());
        mLayoutManager.setOrientation(LinearLayout.VERTICAL);
        mAllRecy.setLayoutManager(mLayoutManager);
        myPatientRecyclerAdapter = new MyPatientRecyclerAdapter(mDatas, getContext());
        mAllRecy.setAdapter(myPatientRecyclerAdapter);

        myPatientRecyclerAdapter.setOnClickItemListener(
                new MyPatientRecyclerAdapter.OnClickItemListener() {
                    @Override
                    public void onClickItem(int pos) {
                        Intent intent = new Intent();
                        intent.putExtra("patientInfo", mDatas.get(pos));
                        intent.setClass(Objects.requireNonNull(getContext()), HZGLHZZLActivity.class);
                        startActivity(intent);
                    }

                    @Override
                    public void onClickRemindPatient(int pos) {
                        Intent intent = new Intent();
                        intent.setClass(getContext(), HZGLTXHZActivity.class);
                        intent.putExtra("patientLable", mDatas.get(pos));
                        startActivity(intent);
                    }

                    @Override
                    public void onClickCancelContract(int pos) {
                        Intent intent = new Intent();
                        intent.setClass(getContext(), TerminationActivity.class);
                        intent.putExtra("patientLable", mDatas.get(pos));
                        startActivity(intent);
                    }

                    @Override
                    public void onClickSignUpContract(int pos) {
                        ProvideViewPatientLablePunchClockState
                                patientLablePunchClockState = mDatas.get(pos);
                        int isSigning = patientLablePunchClockState.getIsSigning();
                        if (isSigning==0) {
                            startActivity(new Intent(getActivity(), SigningDetailsActivity.class)
                                    .putExtra("patientCode", patientLablePunchClockState.getPatientCode())
                                    .putExtra("patientName", patientLablePunchClockState.getUserName())
                                    .putExtra("singCode", "")
                                    .putExtra("patientUrl",patientLablePunchClockState.getUserLogoUrl())
                                    .putExtra("doctorUrl", Constant.doctorUrl)
                            );
                        }else{
                            ToastUtils.showToast("已存在未支付签约，不可再次发起");
                        }

                    }

                    @Override
                    public void onClickAgreeCancelContract(int pos) {
                        agree(pos);
                    }

                    @Override
                    public void onClickRevokeCancelContract(int pos) {
                        Revoke(pos);
                    }

                    @Override
                    public void onClickRefuseCancelContract(int pos) {
                        Intent intent = new Intent();
                        intent.setClass(getContext(), RefuseActivity.class);
                        intent.putExtra("patientLable", mDatas.get(pos));
                        startActivity(intent);
                    }

                    @Override
                    public void onClickStatisticsChart(int pos) {
                        ProvideViewPatientLablePunchClockState
                                patientLablePunchClockState = mDatas.get(pos);
                        Bundle bundle=new Bundle();
                        bundle.putString("url",patientLablePunchClockState.getReportUrl());
                        startActivity(H5Activity.class,bundle);
                    }

                    @Override
                    public void onClickCurrentStatus(int pos) {
                        ProvideViewPatientLablePunchClockState state = mDatas.get(pos);
                        Bundle bundle = new Bundle();
                        bundle.putString("patentCode", state.getPatientCode());
                        bundle.putString("patentName", state.getUserName());
                        bundle.putString("linkPhone", state.getLinkPhone());
                        startActivity(StateDetActivity.class,bundle);
                    }
                });

        //患者资料点击事件
        myPatientRecyclerAdapter.setOnItemClickListener(new MyPatientRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position) {
                Intent intent = new Intent();
                intent.putExtra("patientInfo", mDatas.get(position));
                intent.setClass(getContext(), HZGLHZZLActivity.class);
                startActivity(intent);
            }

            @Override
            public void onLongClick(int position) {

            }
        });

        //解约点击事件
        myPatientRecyclerAdapter.setOnYYItemClickListener(new MyPatientRecyclerAdapter.OnYYItemClickListener() {
            @Override
            public void onClick(int position) {
                if (TextUtils.isEmpty(mDatas.get(position).getSignStatus())) {
                    Toast.makeText(getContext(), "暂无订单", Toast.LENGTH_SHORT).show();
                } else if (mDatas.get(position).getSignStatus().equals("140")) {
                    agree(position);
                } else {
                    Intent intent = new Intent();
                    intent.setClass(getContext(), TerminationActivity.class);
                    intent.putExtra("patientLable", mDatas.get(position));
                    startActivity(intent);
                }
            }

            @Override
            public void onLongClick(int position) {

            }

        });

        //血压点击事件
        myPatientRecyclerAdapter.setOnXYItemClickListener(new MyPatientRecyclerAdapter.OnXYItemClickListener() {
            @Override
            public void onClick(int position) {
                Intent intent = new Intent();
                intent.putExtra("patientInfo", mDatas.get(position));
                intent.setClass(getContext(), HZGLHZZLActivity.class);
                startActivity(intent);
            }

            @Override
            public void onLongClick(int position) {

            }
        });
        //提醒患者点击事件
        myPatientRecyclerAdapter.setOnTXHZItemClickListener(new MyPatientRecyclerAdapter.OnTXHZItemClickListener() {
            @Override
            public void onClick(int position) {
                if (TextUtils.isEmpty(mDatas.get(position).getSignStatus())) {
                    Intent intent = new Intent();
                    intent.setClass(getContext(), HZGLTXHZActivity.class);
                    intent.putExtra("patientLable", mDatas.get(position));
                    startActivity(intent);
                    return;
                }
                if (mDatas.get(position).getSignStatus().equals("140")) {
                    Intent intent = new Intent();
                    intent.setClass(getContext(), RefuseActivity.class);
                    intent.putExtra("patientLable", mDatas.get(position));
                    startActivity(intent);
                } else if (mDatas.get(position).getSignStatus().equals("150")) {
                    Revoke(position);
                } else {
                    Intent intent = new Intent();
                    intent.setClass(getContext(), HZGLTXHZActivity.class);
                    intent.putExtra("patientLable", mDatas.get(position));
                    startActivity(intent);
                }

            }

            @Override
            public void onLongClick(int position) {

            }
        });

        addListener();

        initHandler();
        Bundle arguments = this.getArguments();
        if (arguments!=null) {
            sourceType = arguments.getString("sourceType");
        }

    }

    //同意
    private void agree(int position) {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("loginDoctorPosition", "108.93425^34.23053");
        map.put("mainDoctorCode", mApp.mViewSysUserDoctorInfoAndHospital.getDoctorCode());
        map.put("mainDoctorName", mApp.mViewSysUserDoctorInfoAndHospital.getUserName());
        map.put("signCode", mDatas.get(position).getSignCode());
        map.put("signNo", mDatas.get(position).getSignNo());
        map.put("confimresult", "1");
        map.put("mainPatientCode", mDatas.get(position).getPatientCode());
        map.put("mainUserName", mDatas.get(position).getUserName());

        new Thread() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            public void run() {
                try {
                    mNetRetStr = HttpNetService.urlConnectionService("jsonDataInfo=" + new Gson().toJson(map), Constant.SERVICEURL + "doctorSignControlle/operTerminationConfim");
                    Log.e("tag", "同意" + mNetRetStr);
                    NetRetEntity netRetEntity1 = new Gson().fromJson(mNetRetStr, NetRetEntity.class);
                    if (netRetEntity1.getResCode() == 1) {
                        Intent intent = new Intent(getContext(), ChatActivity.class);
                        //患者
                        intent.putExtra("userCode", mDatas.get(position).getPatientCode());
                        intent.putExtra("userName", mDatas.get(position).getUserName());
                        //医生
                        intent.putExtra("usersName", mApp.mViewSysUserDoctorInfoAndHospital.getUserName());
                        intent.putExtra("userUrl", mApp.mViewSysUserDoctorInfoAndHospital.getUserLogoUrl());
                        //URL
                        intent.putExtra("doctorUrl", mDatas.get(position).getUserLogoUrl());
                        intent.putExtra("patientCode", mDatas.get(position).getPatientCode());
                        intent.putExtra("patientSex", mDatas.get(position).getGender());
                        if (mDatas.get(position).getBirthday() == 0) {
                            Toast.makeText(getContext(), netRetEntity1.getResMsg(), Toast.LENGTH_SHORT).show();
                        } else {
                            intent.putExtra("patientAge", DateUtils.getDateToString(mDatas.get(position).getBirthday())
                            );
                        }
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("orderMsg", new OrderMessage(mApp.mViewSysUserDoctorInfoAndHospital.getUserName(), mApp.mViewSysUserDoctorInfoAndHospital.getUserLogoUrl()
                                ,mDatas.get(position).getOrderCode(), mDatas.get(position).getSignCode(), getMonitorTypeSize(mDatas.get(position).getSignOtherServiceCode()) + "项", mDatas.get(position).getDetectRate() + "天/" + mDatas.get(position).getDetectRateUnitCode() + mDatas.get(position).getDetectRateUnitName(), mDatas.get(position).getSignDuration() + "个" + mDatas.get(position).getSignDurationUnit(), mDatas.get(position).getSignPrice() + "", mDatas.get(position).getSignNo(), "1", "terminationOrder", mDatas.get(position).getPatientCode()));
                        intent.putExtras(bundle);
                        startActivity(intent);
                        Toast.makeText(getContext(), netRetEntity1.getResMsg(), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), netRetEntity1.getResMsg(), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    com.hyphenate.easeui.netService.entity.NetRetEntity retEntity = new com.hyphenate.easeui.netService.entity.NetRetEntity();
                    retEntity.setResCode(0);
                    retEntity.setResMsg("网络连接异常，请联系管理员：" + e.getMessage());
                    mNetRetStr = new Gson().toJson(retEntity);
                    e.printStackTrace();

                }
                mHandler.sendEmptyMessage(30);
            }
        }.start();
    }

    //撤销解约
    private void Revoke(int position) {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("loginDoctorPosition", "108.93425^34.23053");
        map.put("mainDoctorCode", mApp.mViewSysUserDoctorInfoAndHospital.getDoctorCode());
        map.put("mainDoctorName", mApp.mViewSysUserDoctorInfoAndHospital.getUserName());
        map.put("signCode", mDatas.get(position).getSignCode());
        map.put("signNo", mDatas.get(position).getSignNo());
        map.put("mainPatientCode", mDatas.get(position).getPatientCode());
        map.put("mainUserName", mDatas.get(position).getUserName());
        new Thread() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            public void run() {
                String mNetRetStr = "";
                try {
                    mNetRetStr = HttpNetService.urlConnectionService("jsonDataInfo=" + new Gson().toJson(map), Constant.SERVICEURL + "doctorSignControlle/operTerminationRevoke");
                    NetRetEntity netRetEntity = new Gson().fromJson(mNetRetStr, NetRetEntity.class);
                    if (netRetEntity.getResCode() == 1) {
                        Intent intent = new Intent(getContext(), ChatActivity.class);
                        //患者
                        intent.putExtra("userCode", mDatas.get(position).getPatientCode());
                        intent.putExtra("userName", mDatas.get(position).getUserName());
                        //医生
                        intent.putExtra("usersName", mApp.mViewSysUserDoctorInfoAndHospital.getUserName());
                        intent.putExtra("userUrl", mApp.mViewSysUserDoctorInfoAndHospital.getUserLogoUrl());
                        //URL
                        intent.putExtra("doctorUrl", mDatas.get(position).getUserLogoUrl());
                        //intent.putExtra("patientAlias", mHZEntyties.get(position).getan);
                        intent.putExtra("patientCode", mDatas.get(position).getPatientCode());
                        intent.putExtra("patientSex", mDatas.get(position).getGender());
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("orderMsg", new OrderMessage(mApp.mViewSysUserDoctorInfoAndHospital.getUserName(), mApp.mViewSysUserDoctorInfoAndHospital.getUserLogoUrl(),
                                mDatas.get(position).getOrderCode(), mDatas.get(position).getSignCode(), getMonitorTypeSize(mDatas.get(position).getSignOtherServiceCode()) + "项", "1次/" + mDatas.get(position).getDetectRateUnitCode() + mDatas.get(position).getDetectRateUnitName(), mDatas.get(position).getSignDuration() + "个" + mDatas.get(position).getSignDurationUnit(), mDatas.get(position).getSignPrice() + "", mDatas.get(position).getSignNo(), "3", "terminationOrder", mDatas.get(position).getPatientCode()));
                        intent.putExtras(bundle);
                        startActivity(intent);
                        myPatientRecyclerAdapter.notifyDataSetChanged();
                    } else {
                    }

                } catch (Exception e) {
                    com.hyphenate.easeui.netService.entity.NetRetEntity retEntity = new com.hyphenate.easeui.netService.entity.NetRetEntity();
                    retEntity.setResCode(0);
                    retEntity.setResMsg("网络连接异常，请联系管理员：" + e.getMessage());
                    mNetRetStr = new Gson().toJson(retEntity);
                    e.printStackTrace();

                }
                mHandler.sendEmptyMessage(20);
            }
        }.start();
    }

    @SuppressLint("HandlerLeak")
    private void initHandler() {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                String result = "";
                NetRetEntity netRetEntity;
                Bundle data = msg.getData();
                if (data != null) {
                    result = data.getString("result");
                }
                switch (msg.what) {
                    case 20:

                        break;
                    case 30:

                        break;
                }
            }
        };
    }


    private void addListener() {
        refreshLayout.setRefreshHeader(new ClassicsHeader(getContext()));
        refreshLayout.setRefreshFooter(new ClassicsFooter(getContext()));
        refreshLayout.setOnRefreshListener(refreshlayout -> {
            pageIndex = 1;
            mPresenter.sendSearchPatientListRequest(pageSize + "", pageIndex + ""
                    , mApp.loginDoctorPosition,
                    mApp.mViewSysUserDoctorInfoAndHospital.getDoctorCode(), sourceType,patientName,ageStart,ageEnd);
        });

        refreshLayout.setOnLoadMoreListener(refreshlayout -> {
            pageIndex++;
            mPresenter.sendSearchPatientListRequest(pageSize + "", pageIndex + ""
                    , mApp.loginDoctorPosition, mApp.mViewSysUserDoctorInfoAndHospital.getDoctorCode(),
                    sourceType,patientName,ageStart,ageEnd);

        });
    }

    public static SignedPatientChildFragment newInstance(String type) {
        SignedPatientChildFragment fragment = new SignedPatientChildFragment();
        Bundle args = new Bundle();
        args.putString("sourceType", type);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * 搜索数据
     * @param patientName 患者名称
     * @param ageStart 开始年龄
     * @param ageEnd 结束年龄
     */
    public void searchData(String patientName,Integer ageStart,Integer ageEnd ){
        this.patientName=patientName;
        this.ageStart=ageStart;
        this.ageEnd=ageEnd;
        pageIndex = 1;
        mPresenter.sendSearchPatientListRequest(pageSize + "", pageIndex + "",
                mApp.loginDoctorPosition, mApp.mViewSysUserDoctorInfoAndHospital.getDoctorCode(),
                sourceType,patientName,ageStart,ageEnd);
    }

    public void resetData(String patientName,Integer ageStart,Integer ageEnd){
        this.patientName=patientName;
        this.ageStart=ageStart;
        this.ageEnd=ageEnd;
    }

    @Override
    protected void initData() {
        super.initData();
        Bundle arguments = this.getArguments();
        if (arguments!=null) {
            sourceType= arguments.getString("sourceType");

        }

    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.sendSearchPatientListRequest(pageSize + "", pageIndex + "",
                mApp.loginDoctorPosition, mApp.mViewSysUserDoctorInfoAndHospital.getDoctorCode(),
                sourceType,patientName,ageStart,ageEnd);
    }

    @Override
    public void getOperListResult(List<ProvideViewPatientLablePunchClockState> provideViewPatientLablePunchClockState) {

        if (provideViewPatientLablePunchClockState != null) {
            if (pageIndex == 1) {
                mDatas.clear();
                refreshLayout.finishRefresh(200);
            }

            if (!CollectionUtils.isEmpty(provideViewPatientLablePunchClockState)) {
                mDatas.addAll(provideViewPatientLablePunchClockState);
                myPatientRecyclerAdapter.setDate(mDatas);
                mAllRecy.setAdapter(myPatientRecyclerAdapter);
                myPatientRecyclerAdapter.notifyDataSetChanged();
                refreshLayout.finishLoadMore();
                tvNoData.setVisibility(View.GONE);
            } else {
                if (pageIndex == 1) {
                    myPatientRecyclerAdapter.setData(mDatas);
                    myPatientRecyclerAdapter.notifyDataSetChanged();
                    tvNoData.setVisibility(View.VISIBLE);
                } else {
                    refreshLayout.finishLoadMore();
                }
            }


        }
    }

    @Override
    public void getOperRevokeResult() {

    }



    /**
     * 获取监测类型项目数量
     *
     * @param monitorType 监测类型
     * @return size
     */
    private int getMonitorTypeSize(String monitorType) {
        if (!TextUtils.isEmpty(monitorType)) {
            String[] split = monitorType.split(",");
            return split.length;
        }
        return 0;
    }
}

