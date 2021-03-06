package www.jykj.com.jykj_zxyl.medicalrecord;

import android.app.Activity;

import java.util.List;

import www.jykj.com.jykj_zxyl.app_base.base_bean.BaseReasonBean;
import www.jykj.com.jykj_zxyl.app_base.base_bean.DrugClassificationBean;
import www.jykj.com.jykj_zxyl.app_base.base_bean.DrugDosageBean;
import www.jykj.com.jykj_zxyl.app_base.mvp.BasePresenter;
import www.jykj.com.jykj_zxyl.app_base.mvp.BaseView;

/**
 * Description:
 *
 * @author: qiuxinhai
 * @date: 2020-09-18 10:12
 */
public class AddDrugInfoContract {


    public interface View extends BaseView {


        /**
         * 获取新增药品信息返回结果
         * @param isSucess true or false
         * @param msg 信息
         */
        void getOperUpdDrugInfoResult(boolean isSucess,String msg);

        /**
         * 获取药剂返回结果
         * @param drugDosageBeans 药剂列表
         */
        void getSearchDrugTypeDosageResult(List<DrugDosageBean> drugDosageBeans);

        /**
         * 获取药品分类返回结果
         * @param drugClassificationBeans 药品分类列表
         */
        void getDrugClassificationBeanResult(List<DrugClassificationBean> drugClassificationBeans);


        /**
         * 获取药品信息小单位
         * @param baseReasonBeans 小单位列表
         */
        void getDurgSmallUnitResult(List<BaseReasonBean> baseReasonBeans);

        /**
         * 获取药品信息大单位
         * @param baseReasonBeans 药品大单位列表
         */
        void getDrugBigUnitResult(List<BaseReasonBean> baseReasonBeans);

        /**
         * 获取用法频率返回结果
         * @param baseReasonBeans 频率列表
         */
        void getUsageRateResult(List<BaseReasonBean> baseReasonBeans);

        /**
         * 获取用法时间返回结果
         * @param baseReasonBeans
         */
        void getUsageDayResult(List<BaseReasonBean> baseReasonBeans);
    }

    public interface Presenter extends BasePresenter<View> {

        /**
         * 发送[基础数据]药品信息增加
         * @param drugUseType 药品分类
         * @param drugName 药品名称
         * @param specUnit 药品单位
         * @param specName 药品规格
         * @param activity activity
         */
        void sendOperUpdDrugInfoRequest(String drugUseType, String drugName, String specUnit
                , String specName, Activity activity);

        /**
         * 诊所详情】[基础数据]药品信息增加
         * @param medicineCode 药品分类【选取】
         * @param drugName 药品名称(通用)
         * @param drugNameAlias 商品名称
         * @param specUnit 药品单位
         * @param specName 药品规格
         * @param dosageCode 剂型编码【选取】
         * @param factoryName 厂家名称
         * @param drugUsageDay 药品用法
         * @param drugUsageNum 药品用法
         * @param activity activity
         */
        void sendOperUpdDrugInfo_201116(String medicineCode,String drugName,
                                        String drugNameAlias,String specUnit
                ,String specName,String dosageCode,String factoryName
                ,String drugUsageDay,String drugUsageNum,Activity activity);

        void sendOperUpdDrugInfo_201208(String medicineCode
                ,String  drugName, String drugNameAlias
                ,String specUnitNum,String specUnit
                ,String specUnitBig,String specName
                ,String dosageCode,String factoryName
                ,String drugUsageRateNum
                , String drugUsageRateDay
                ,String drugUsageNumUnit
                ,String drugUsageNumFrequency
                ,String drugUsageDay
                ,String drugUsageNum,Activity activity);

        /**
         * [基础数据]药品剂型信息获取
         * @param activity 获取药剂信息
         */
        void sendSearchDrugTypeDosageRequest(Activity activity);

        /**
         * 发送药品分类请求
         * @param medicineCode 药物分类编码(0:显示全部; -1:一级目录; 显示子目录:如001 )
         * @param activity Activity
         */
        void sendGetDrugTypeMedicineRequest(String medicineCode,Activity activity);


        /**
         * 发送获取药品小单位请求
         * @param baseCode 基础code
         * @param activity Activity
         */
        void sendGetDurgSmallUnitRequest(String baseCode,Activity activity);

        /**
         * 获取药品大单位请求
         * @param baseCode 基础code
         * @param activity Activity
         */
        void sendGetDrugBigUnitRequest(String baseCode,Activity activity);

        /**
         * 获取用法频率
         * @param baseCode 基础code
         * @param activity  Activity
         */
        void sendUsageRateRequest(String baseCode,Activity activity);

        /**
         *
         * @param baseCode
         * @param activity
         */
        void sendUsageDayRequest(String baseCode,Activity activity);



    }
}
