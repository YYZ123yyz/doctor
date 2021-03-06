package www.jykj.com.jykj_zxyl.medicalrecord.popup;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.util.List;

import www.jykj.com.jykj_zxyl.R;
import www.jykj.com.jykj_zxyl.app_base.base_adapter.SecondaryListAdapter;
import www.jykj.com.jykj_zxyl.app_base.base_bean.DrugClassificationBean;
import www.jykj.com.jykj_zxyl.medicalrecord.adapter.DrugListAdapter;

/**
 * Description:药物类型弹框popup
 *
 * @author: qiuxinhai
 * @date: 2020-09-17 11:48
 */
public class MedicinalCategoryPopup2 extends PopupWindow {
    private Context myContext;
    private RecyclerView rvList;
    private LayoutInflater inflater ;
    private DrugListAdapter drugListAdapter;
    private View myMenuView;
    private TextView mTvallBtn;
    private OnClickListener onClickListener;

    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public MedicinalCategoryPopup2(Context context) {

        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        myMenuView = inflater.inflate(R.layout.popup_inspection, null);

        this.myContext = context;

        initWidget();
        setPopup();
        addListener();
    }

    /**
     * 初始化控件
     */
    private void initWidget() {
        rvList =  myMenuView.findViewById(R.id.rv_list);
        mTvallBtn=myMenuView.findViewById(R.id.tv_all_btn);
    }

    /**
     * 添加监听
     */
    private void addListener(){
        mTvallBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onClickListener!=null) {
                    onClickListener.onClickAll();
                    MedicinalCategoryPopup2.this.dismiss();
                }
            }
        });
    }
    /**
     * 设置popup的样式
     */
    private void setPopup() {
        // 设置AccessoryPopup的view
        this.setContentView(myMenuView);
        // 设置AccessoryPopup弹出窗体的宽度
        this.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
        this.setHeight(LinearLayout.LayoutParams.MATCH_PARENT);
        // 设置AccessoryPopup弹出窗体的高度

        // 设置AccessoryPopup弹出窗体可点击
        this.setFocusable(true);
        // 设置AccessoryPopup弹出窗体的动画效果
        this.setAnimationStyle(R.style.AnimTopMiddle);
        // 实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0x33000000);
        // 设置SelectPicPopupWindow弹出窗体的背景
        this.setBackgroundDrawable(dw);


    }


    /**
     * 设置数据
     *
     * @param datas 数据列表
     */
    public void setData(List<SecondaryListAdapter.DataTree<DrugClassificationBean,
            DrugClassificationBean.DrugTypeMedicineListBean>> datas) {
        if (isShowing()) {
            rvList.setLayoutManager(new LinearLayoutManager(myContext));
            drugListAdapter = new DrugListAdapter(myContext);
            drugListAdapter.setData(datas);

            rvList.setAdapter(drugListAdapter);

            drugListAdapter.setOnClickSubListener(new DrugListAdapter.OnClickSubListener() {
                @Override
                public void onClickChanged(DrugClassificationBean.DrugTypeMedicineListBean
                                                   drugTypeMedicineListBean) {
                    if (onClickListener!=null) {
                        onClickListener.onClickChanged(drugTypeMedicineListBean);
                        MedicinalCategoryPopup2.this.dismiss();
                    }
                }
            });
        }
    }

    /**
     * 显示弹窗界面
     *
     * @param anchor 根布局
     */
    public void show(View anchor) {

        if (Build.VERSION.SDK_INT >= 24) {
            Rect rect = new Rect();
            anchor.getGlobalVisibleRect(rect);
            int h = anchor.getResources().getDisplayMetrics().heightPixels - rect.bottom;
            setHeight(h);
        }
        super.showAsDropDown(anchor);
    }


    public interface OnClickListener{

        void onClickChanged(DrugClassificationBean.DrugTypeMedicineListBean
                                    drugTypeMedicineListBean);

        void onClickAll();
    }

}
