package www.jykj.com.jykj_zxyl.mypatient.contract;

import java.util.List;

import entity.patientInfo.ProvideViewPatientLablePunchClockState;
import www.jykj.com.jykj_zxyl.app_base.mvp.BasePresenter;
import www.jykj.com.jykj_zxyl.app_base.mvp.BaseView;

public class NotFragmentContract {
    public interface View extends BaseView {
        /**
         * 数据列表
         * @param provideViewPatientLablePunchClockState
         */
        void getPatientListResult(List<ProvideViewPatientLablePunchClockState> provideViewPatientLablePunchClockState);
        /**
         * 撤销解约
         */
        void getOperRevokeResult();
    }

    public interface Presenter extends BasePresenter<NotFragmentContract.View> {
        /**
         *  患者数据请求
         * @param rowNum
         * @param pageNum
         * @param loginDoctorPosition
         * @param searchDoctorCode
         * @param searchStateType
         */
        void sendPatientListRequest(String rowNum, String pageNum, String loginDoctorPosition
                , String searchDoctorCode, String searchStateType,String patientName,Integer ageStart,
                                    Integer ageEnd);

        /**
         *  撤销解约
         * @param loginDoctorPosition
         * @param mainDoctorCode
         * @param mainDoctorName
         * @param signCode
         * @param signNo
         * @param mainPatientCode
         * @param mainUserName
         */
        void sendOperRevokeRequest(String loginDoctorPosition, String mainDoctorCode, String mainDoctorName
                , String signCode, String signNo, String mainPatientCode, String mainUserName);
    }
}
