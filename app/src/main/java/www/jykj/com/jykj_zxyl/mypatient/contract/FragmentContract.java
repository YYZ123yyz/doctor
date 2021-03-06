package www.jykj.com.jykj_zxyl.mypatient.contract;

import java.util.List;

import entity.patientInfo.ProvideViewPatientLablePunchClockState;
import www.jykj.com.jykj_zxyl.app_base.mvp.BasePresenter;
import www.jykj.com.jykj_zxyl.app_base.mvp.BaseView;
import yyz_exploit.bean.Status;

public class FragmentContract {
    public interface View extends BaseView {
        /**
         * 数据列表
         * @param provideViewPatientLablePunchClockState
         */
        void getOperListResult(List<ProvideViewPatientLablePunchClockState> provideViewPatientLablePunchClockState);
        /**
         * 撤销解约
         */
        void getOperRevokeResult();
    }

    public interface Presenter extends BasePresenter<View> {
        /**
         *  患者数据请求
         * @param rowNum
         * @param pageNum
         * @param loginDoctorPosition
         * @param searchDoctorCode
         * @param searchStateType
         */
        void sendSearchPatientListRequest(String rowNum, String pageNum, String loginDoctorPosition
                , String searchDoctorCode, String searchStateType,String patientName
                ,Integer  ageStart,Integer ageEnd);


    }
}
