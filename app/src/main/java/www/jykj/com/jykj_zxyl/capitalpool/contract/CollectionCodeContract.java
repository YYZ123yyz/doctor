package www.jykj.com.jykj_zxyl.capitalpool.contract;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import www.jykj.com.jykj_zxyl.app_base.mvp.BasePresenter;
import www.jykj.com.jykj_zxyl.app_base.mvp.BaseView;

public class CollectionCodeContract {

    public interface View extends BaseView {

        void bindSucess();

        void showMsg(String msg);
    }

    public interface Presenter extends BasePresenter<View> {

        void bindCode(String params,MultipartBody.Part file);
    }
}
