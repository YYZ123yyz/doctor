package www.jykj.com.jykj_zxyl.appointment.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import www.jykj.com.jykj_zxyl.R;
import www.jykj.com.jykj_zxyl.app_base.base_bean.BaseReasonBean;

/**
 * Description:
 *
 * @author: qiuxinhai
 * @date: 2020-08-29 18:32
 */
public class AppointTypeAdapter extends RecyclerView.Adapter<AppointTypeAdapter.ViewHolder>{
    private Context context;
    private List<BaseReasonBean> list;
    public AppointTypeAdapter(Context mContext, List<BaseReasonBean> list){
        this.context=mContext;
        this.list=list;
    }
    @NonNull
    @Override
    public AppointTypeAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dialog_appoint_time, parent, false);
        ViewHolder vh = new ViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull AppointTypeAdapter.ViewHolder holder, int position) {
        holder.tvDateTime.setText(list.get(position).getAttrName());
        if (list.get(position).isChoosed()) {
            holder.tvDateTime.setTextColor(ContextCompat.getColor(context,R.color.color_000000));
        }else{
            holder.tvDateTime.setTextColor(ContextCompat.getColor(context,R.color.color_666666));
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvDateTime;
        public ViewHolder(View view) {
            super(view);
            tvDateTime=view.findViewById(R.id.tv_time);
        }
    }

}

