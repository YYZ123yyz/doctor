package www.jykj.com.jykj_zxyl.app_base.base_dialog;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;


import java.util.Objects;

import www.jykj.com.jykj_zxyl.app_base.R;


/**
 * Description:
 *
 * @author: qiuxinhai
 * @date: 2020-09-02 11:16
 */
public class CommonConfirmDialog extends Dialog {
    private Context mContext;
    private View mRootView;
    private TextView mTvContentMsg;
    private TextView mTvIknowBtn;
    private TextView mTvCancelBtn;
    private TextView mTvConfirmBtn;
    private OnClickListener onClickListener;
    private boolean isShowCancelBtn;



    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public CommonConfirmDialog(@NonNull Context context) {
        super(context, R.style.DialogTheme);
        setCanceledOnTouchOutside(true);//禁止点击空白区域消失
        Objects.requireNonNull(this.getWindow()).setDimAmount(0f);//核心代码 解决了无法去除遮罩
        init(context);
        addListener();
    }

    /**
     * 初始化页面
     * @param context 上下文
     */
    private void init(Context context){
        mContext = context;
        mRootView = LayoutInflater.from(context).inflate(R.layout.dialog_common_confirm, null);
        setContentView(mRootView);
        initView(mRootView);
        Window window = getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height=WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(params);
        window.setGravity(Gravity.CENTER);
    }


    private void initView(View view){
        mTvContentMsg = view.findViewById(R.id.tv_content_msg);
        mTvIknowBtn= view.findViewById(R.id.tv_iknow_btn);
        mTvCancelBtn=view.findViewById(R.id.tv_cancel_btn);
        mTvConfirmBtn=view.findViewById(R.id.tv_confirm_btn);
    }

    public void setContentText(String content){
        mTvContentMsg.setText(content);
    }

    public void setShowCancelBtn(boolean showCancelBtn) {
        isShowCancelBtn = showCancelBtn;
        if(isShowing()){
            if (isShowCancelBtn) {
                mTvCancelBtn.setVisibility(View.VISIBLE);
            }else{
                mTvCancelBtn.setVisibility(View.GONE);
            }
        }
    }
    private void addListener(){
        mTvIknowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               CommonConfirmDialog.this.dismiss();
                if (onClickListener!=null) {
                    onClickListener.onConfirm();
                }
            }
        });
        mTvCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommonConfirmDialog.this.dismiss();

            }
        });
        mTvConfirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommonConfirmDialog.this.dismiss();
                if (onClickListener!=null) {
                    onClickListener.onConfirm();
                }
            }
        });
    }

    public interface OnClickListener{

        void onConfirm();
    }
}
