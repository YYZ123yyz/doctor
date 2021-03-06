package www.jykj.com.jykj_zxyl.app_base.base_bean;

/**
 * Description:
 *
 * @author: qiuxinhai
 * @date: 2020-09-22 19:51
 */
public class CheckImResultBean {
    private String isBinding;//是否存在绑定关系 1存在，0不存在（存在绑定关系可以出现签约按钮）

    private String isSigning;//1已签约 0未签约 （未签约状态出现签约按钮）

    private String isReserveing;//是否存在接诊中的预约 1存在 0不存在 (存在则出现发送病历按钮)
    private String orderCode;//orderCode 订单编号
    private String reserveCode;//预约编号
    private long reserveConfigStart;//预约开始时间
    private long reserveConfigEnd;// 预约结束时间
    private String sumDuration;//总的可以发送消息的次数 -1为不限制
    private String useDuration;//已发送消息次数
    private int surplusDuration;
    private int reserveType;
    private int treatmentType;
    public String getIsBinding() {
        return isBinding;
    }

    public void setIsBinding(String isBinding) {
        this.isBinding = isBinding;
    }

    public String getIsSigning() {
        return isSigning;
    }

    public void setIsSigning(String isSigning) {
        this.isSigning = isSigning;
    }

    public String getIsReserveing() {
        return isReserveing;
    }

    public void setIsReserveing(String isReserveing) {
        this.isReserveing = isReserveing;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public String getReserveCode() {
        return reserveCode;
    }

    public void setReserveCode(String reserveCode) {
        this.reserveCode = reserveCode;
    }


    public long getReserveConfigStart() {
        return reserveConfigStart;
    }

    public void setReserveConfigStart(long reserveConfigStart) {
        this.reserveConfigStart = reserveConfigStart;
    }

    public long getReserveConfigEnd() {
        return reserveConfigEnd;
    }

    public void setReserveConfigEnd(long reserveConfigEnd) {
        this.reserveConfigEnd = reserveConfigEnd;
    }

    public String getSumDuration() {
        return sumDuration;
    }

    public void setSumDuration(String sumDuration) {
        this.sumDuration = sumDuration;
    }

    public String getUseDuration() {
        return useDuration;
    }

    public void setUseDuration(String useDuration) {
        this.useDuration = useDuration;
    }

    public int getSurplusDuration() {
        return surplusDuration;
    }

    public void setSurplusDuration(int surplusDuration) {
        this.surplusDuration = surplusDuration;
    }

    public int getReserveType() {
        return reserveType;
    }

    public void setReserveType(int reserveType) {
        this.reserveType = reserveType;
    }

    public int getTreatmentType() {
        return treatmentType;
    }

    public void setTreatmentType(int treatmentType) {
        this.treatmentType = treatmentType;
    }
}
