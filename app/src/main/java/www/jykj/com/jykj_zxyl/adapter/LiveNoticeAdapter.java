package www.jykj.com.jykj_zxyl.adapter;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import entity.liveroom.PreLiveInfo;
import entity.liveroom.ProvideLiveBroadcastDetails;
import www.jykj.com.jykj_zxyl.R;
import www.jykj.com.jykj_zxyl.util.StrUtils;

import java.util.List;

public class LiveNoticeAdapter extends RecyclerView.Adapter<LiveNoticeAdapter.ViewHolder> {
    List<ProvideLiveBroadcastDetails> datas;
    OnHotliveItemClickListener myListener;

    public LiveNoticeAdapter(List<ProvideLiveBroadcastDetails> datas) {
        this.datas = datas;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.notice_live_items, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        ProvideLiveBroadcastDetails parinfo = datas.get(i);
        viewHolder.pre_live_catalog.setText("类目:" + StrUtils.defaulObjToStr(parinfo.getClassName()));
        viewHolder.pre_live_desc.setText(StrUtils.defaulObjToStr(parinfo.getAttrName()));
        viewHolder.pre_live_price.setText(StrUtils.defaulObjToStr(parinfo.getExtendBroadcastPriceShow()));
//        viewHolder.go_live.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                myListener.onClick(i,v);
//            }
//        });
        viewHolder.pre_live_title.setText(StrUtils.defaulObjToStr(parinfo.getBroadcastTitle()));
        viewHolder.pre_watch_num.setText(StrUtils.defaulObjToStr(parinfo.getExtendBroadcastFollowNum())+"人想看");
        if (StrUtils.defaulObjToStr(parinfo.getBroadcastCoverImgUrl()).length() > 0) {
            Glide.with(viewHolder.pre_live_btn.getContext()).load(parinfo.getBroadcastCoverImgUrl()).into(viewHolder.pre_live_btn);
        }
        viewHolder.ll_root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myListener.onClick(i, v);
            }
        });
        viewHolder.ll_root.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                myListener.onLongClick(i, v);
                return false;
            }
        });
        viewHolder.rl_collection_root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView pre_live_btn;
        private TextView pre_live_title;
        private TextView pre_live_desc;
        private TextView pre_live_catalog;
        private TextView pre_watch_num;
        private TextView pre_live_price;
        private TextView go_live;
        private LinearLayout ll_root;
        private LinearLayout rl_collection_root;

        public ViewHolder(View view) {
            super(view);
            pre_live_btn = view.findViewById(R.id.pre_live_btn);
            pre_live_title = view.findViewById(R.id.pre_live_title);
            pre_live_desc = view.findViewById(R.id.pre_live_desc);
            pre_live_catalog = view.findViewById(R.id.pre_live_catalog);
            pre_watch_num = view.findViewById(R.id.pre_watch_num);
            pre_live_price = view.findViewById(R.id.pre_live_price);
            go_live = view.findViewById(R.id.go_live);
            ll_root=view.findViewById(R.id.ll_root);
            rl_collection_root=view.findViewById(R.id.rl_collection_root);
        }

    }

    public OnHotliveItemClickListener getMyListener() {
        return myListener;
    }

    public void setMyListener(OnHotliveItemClickListener myListener) {
        this.myListener = myListener;
    }

    public interface OnHotliveItemClickListener {
        void onClick(int position, View view);

        void onLongClick(int position, View view);

    }

    public void setData(List<ProvideLiveBroadcastDetails> datas) {
        this.datas = datas;
    }
}
