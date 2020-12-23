package www.jykj.com.jykj_zxyl.capitalpool.weiget;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.chad.library.adapter.base.BaseQuickAdapter;

import java.util.ArrayList;

import www.jykj.com.jykj_zxyl.R;
import www.jykj.com.jykj_zxyl.capitalpool.adapter.WithdrawTypeAdapter;

public class WithdrawTypePop extends PopupWindow implements View.OnClickListener {


    private final Activity mContext;
    private View mPopView;


    private RecyclerView mRecycleView;
    private WithdrawTypeAdapter typeAdapter;


    public WithdrawTypePop(Activity context) {
        super(context);
        mContext = context;
        init(context);
        setPopupWindow(context);
    }

    private void init(Activity context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        mPopView = inflater.inflate(R.layout.popup_withdraw_type, null);
        initView();

    }

    private void initData() {

    }

    private void initView() {
        ArrayList<String> mDatas = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            mDatas.add("xxx");
        }

        typeAdapter = new WithdrawTypeAdapter(R.layout.item_withdraw_type, mDatas);
        mRecycleView = mPopView.findViewById(R.id.type_recycleview);
        mPopView.findViewById(R.id.iv_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WindowManager.LayoutParams lp = mContext.getWindow().getAttributes();
                lp.alpha = 1f;
                mContext.getWindow().setAttributes(lp);
                dismiss();
            }
        });
        mRecycleView.setLayoutManager(new LinearLayoutManager(mContext));
        mRecycleView.setAdapter(typeAdapter);

        typeAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {

            }
        });


        LinearLayout addLayout = mPopView.findViewById(R.id.add_withdraw);
        addLayout.setOnClickListener(this);

    }


    /**
     * 设置窗口的相关属性
     */
    private void setPopupWindow(Activity activity) {
        this.setContentView(mPopView);// 设置View
        this.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);// 设置弹出窗口的宽
        this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);// 设置弹出窗口的高
        this.setFocusable(false);// 设置弹出窗口可
        this.setBackgroundDrawable(new ColorDrawable(0x00000000));// 设置背景透明
        this.setOutsideTouchable(false);
    }

    public void showPop(View view) {
        initData();

        WindowManager.LayoutParams lp = mContext.getWindow().getAttributes();
        lp.alpha = 0.5f;
        mContext.getWindow().setAttributes(lp);
        this.showAtLocation(view, Gravity.BOTTOM, 0, 0);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

        }

    }
/*
    public void setSeleteDev(DeviceInfoBean datas) {
//        this.mDatas.add(datas) ;
        seleteDevAdapter.addData(datas);
    }*/


    private onDevChoose myDevChoose;

    public void setOnDevChoose(onDevChoose listen) {
        this.myDevChoose = listen;
    }

    public interface onDevChoose {
        void onDevChoose(String mac, String name);
    }

}

