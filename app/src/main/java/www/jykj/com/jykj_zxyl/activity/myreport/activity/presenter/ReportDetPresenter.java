package www.jykj.com.jykj_zxyl.activity.myreport.activity.presenter;


import com.allen.library.interceptor.Transformer;
import com.allen.library.interfaces.ILoadingView;
import com.blankj.utilcode.util.LogUtils;

import java.util.HashMap;
import java.util.List;

import www.jykj.com.jykj_zxyl.activity.myreport.activity.Contract.ReportDetContract;
import www.jykj.com.jykj_zxyl.activity.myreport.activity.bean.DepartDetBean;
import www.jykj.com.jykj_zxyl.activity.myreport.activity.bean.DepartmentListBean;
import www.jykj.com.jykj_zxyl.activity.myreport.activity.bean.ReportBean;
import www.jykj.com.jykj_zxyl.app_base.base_bean.BaseBean;
import www.jykj.com.jykj_zxyl.app_base.http.ApiHelper;
import www.jykj.com.jykj_zxyl.app_base.http.CommonDataObserver;
import www.jykj.com.jykj_zxyl.app_base.http.RetrofitUtil;
import www.jykj.com.jykj_zxyl.app_base.mvp.BasePresenterImpl;
import www.jykj.com.jykj_zxyl.util.GsonUtils;

public class ReportDetPresenter extends BasePresenterImpl<ReportDetContract.View> implements ReportDetContract.Presenter {
    @Override
    protected Object[] getRequestTags() {
        return new Object[0];
    }

    @Override
    public void sendyReportRequest(String params) {

        ApiHelper.getApiService().getMyReport(params)
                .compose(Transformer.switchSchedulers(new ILoadingView() {
                    @Override
                    public void showLoadingView() {
                        if (mView != null) {
                            mView.showLoading(101);
                        }

                    }

                    @Override
                    public void hideLoadingView() {
                        if (mView != null) {
                            mView.hideLoading();
                        }

                    }
                })).subscribe(new CommonDataObserver() {
            @Override
            protected void onSuccessResult(BaseBean baseBean) {
                if (mView != null) {
                    int resCode = baseBean.getResCode();
                    if (resCode == 1) {
                        LogUtils.e("疾病 xx"  +baseBean.getResJsonData());
                        List<ReportBean> reportBeans = GsonUtils.jsonToList(baseBean.getResJsonData(), ReportBean.class);
                        mView.getmyReportResult(reportBeans);
                        //   mView.getSearchMyClinicDetailResPatientMessageContentResult(diagnosisReplayBean);
                    }else {
                        LogUtils.e("疾病  xx"  +baseBean.getResMsg());
                    }
                }

            }

        });
    }

    @Override
    public void getDetList(String params) {
        ApiHelper.getApiService().getDepList(params)
                .compose(Transformer.switchSchedulers(new ILoadingView() {
                    @Override
                    public void showLoadingView() {
                        if (mView != null) {
                            mView.showLoading(101);
                        }

                    }

                    @Override
                    public void hideLoadingView() {
                        if (mView != null) {
                            mView.hideLoading();
                        }

                    }
                })).subscribe(new CommonDataObserver() {
            @Override
            protected void onSuccessResult(BaseBean baseBean) {
                if (mView != null) {
                    int resCode = baseBean.getResCode();
                    if (resCode == 1) {
                        LogUtils.e("xx"  +baseBean.getResJsonData());
                        List<DepartmentListBean> reportBeans = GsonUtils.jsonToList(baseBean.getResJsonData(), DepartmentListBean.class);
                        mView.getDetListSucess(reportBeans);
                        //   mView.getSearchMyClinicDetailResPatientMessageContentResult(diagnosisReplayBean);
                    }
                }

            }

        });
    }

    @Override
    public void getDet(String params) {
        ApiHelper.getApiService().getDoctorReport(params)
                .compose(Transformer.switchSchedulers(new ILoadingView() {
                    @Override
                    public void showLoadingView() {
                        if (mView != null) {
                            mView.showLoading(101);
                        }

                    }

                    @Override
                    public void hideLoadingView() {
                        if (mView != null) {
                            mView.hideLoading();
                        }

                    }
                })).subscribe(new CommonDataObserver() {
            @Override
            protected void onSuccessResult(BaseBean baseBean) {
                if (mView != null) {
                    int resCode = baseBean.getResCode();
                    if (resCode == 1) {
                        LogUtils.e("详情  xx"  +baseBean.getResJsonData());
                        DepartDetBean departDetBean = GsonUtils.fromJson(baseBean.getResJsonData(), DepartDetBean.class);
                        mView.getDetSucess(departDetBean);
                        //   mView.getSearchMyClinicDetailResPatientMessageContentResult(diagnosisReplayBean);
                    }else {
                        LogUtils.e("详情  xx"  +baseBean.getResMsg());
                    }
                }

            }

        });
    }
}

