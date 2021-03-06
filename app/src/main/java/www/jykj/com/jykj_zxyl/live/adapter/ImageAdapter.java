 package www.jykj.com.jykj_zxyl.live.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

import orcameralib.util.BitmapUtil;
import www.jykj.com.jykj_zxyl.R;
import www.jykj.com.jykj_zxyl.activity.myself.Photo_Info;
import www.jykj.com.jykj_zxyl.application.JYKJApplication;
import yyz_exploit.Utils.StrUtils;


public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {
    public List<Photo_Info> datas = new ArrayList<>();
    private OnItemClickListener mOnItemClickListener;
    private JYKJApplication mApp;
    private boolean mCanClick = true;
    private Context mContext;
    private int width;
    private OnItemTakeClickListener mOnItemTakeClickListener;
    public ImageAdapter(List<Photo_Info> list, JYKJApplication app, Context context) {
        datas = new ArrayList<>();
        datas.addAll(list);
        mApp = app;
        this.mContext=context;
        Display defaultDisplay = ((Activity) mContext).getWindowManager().getDefaultDisplay();
         width = defaultDisplay.getWidth();
    }

    //创建新View，被LayoutManager所调用
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_identification_imageview, viewGroup, false);
        ViewHolder vh = new ViewHolder(view);
        return vh;
    }



    //将数据与界面进行绑定的操作
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        /**
         * 强制关闭复用，否则出现数据混乱
         */
        viewHolder.setIsRecyclable(false);
        String status = datas.get(position).getStatus();
        Log.e("TAG", "onBindViewHolder: "+status );
        if(!TextUtils.isEmpty(status)){
            if(status.equals("1")){
                if ("ADDPHOTO".equals(datas.get(position).getPhotoID())) {
                    //   viewHolder.mImageView.setBackgroundResource(R.mipmap.vpzz);
                    viewHolder.delete_img.setVisibility(View.GONE);
                  //  viewHolder.mImageView.setVisibility(View.VISIBLE);
                } else if (datas.get(position).getPhoto() != null) {
                    viewHolder.mImageView.setImageBitmap(BitmapUtil.stringtoBitmap(datas.get(position).getPhoto()));
                    Log.e("执行", "   onBindViewHolder    执行:" + position);
                    viewHolder.delete_img.setVisibility(View.VISIBLE);
                } else if (StrUtils.defaultStr(datas.get(position).getPhotoUrl()).length() > 0) {
                    Glide.with(viewHolder.mImageView.getContext()).load(datas.get(position).getPhotoUrl())
                            .apply(RequestOptions.placeholderOf(com.hyphenate.easeui.R.mipmap.docter_heard)
                                    .diskCacheStrategy(DiskCacheStrategy.ALL))
                            .into(viewHolder.mImageView);
                }

            } else if(status.equals("2")){
                Log.e("TAG", "onBindViewHolder: xxxxxxxxxxxxxx"+datas.get(position).getPhotoUrl() );
                viewHolder.delete_img.setVisibility(View.VISIBLE);
                Glide.with(viewHolder.mImageView.getContext()).load(datas.get(position).getPhotoUrl())
                        .apply(RequestOptions.placeholderOf(R.color.color_ffffff)
                                .diskCacheStrategy(DiskCacheStrategy.ALL))
                        .into(viewHolder.mImageView);

            }else if(status.equals("3")){
                viewHolder.delete_img.setVisibility(View.GONE);
                Glide.with(viewHolder.mImageView.getContext()).load(datas.get(position).getPhotoUrl())
                        .apply(RequestOptions.placeholderOf(R.color.color_ffffff)
                                .diskCacheStrategy(DiskCacheStrategy.ALL))
                        .into(viewHolder.mImageView);
            }
        }
        ViewGroup.LayoutParams layoutParams = viewHolder.mImageView.getLayoutParams();
        layoutParams.height=width/3;
        layoutParams.width=width/3;
        viewHolder.mImageView.setLayoutParams(layoutParams);
        if (mOnItemClickListener != null) {
            viewHolder.delete_img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mCanClick){
                        mOnItemClickListener.onClick(position);
                    }

                }
            });

            viewHolder.delete_img.setOnLongClickListener(new View.OnLongClickListener() {

                @Override
                public boolean onLongClick(View view) {
                    if (mCanClick){
                        mOnItemClickListener.onLongClick(position);
                    }
                    return false;
                }
            });
        }
        viewHolder.delete_img.setClickable(mCanClick);

        //拍照
        if(mOnItemTakeClickListener!=null){
            viewHolder.mImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mCanClick){
                        mOnItemTakeClickListener.onClick(position);
                    }
                }
            });
        }


    }

    //获取数据的数量
    @Override
    public int getItemCount() {
        return datas.size();
    }

    /**
     * 重置数据
     *
     * @param mPhotoInfos
     */
    public void setDate(List<Photo_Info> mPhotoInfos) {
        datas.clear();
        datas.addAll(mPhotoInfos);
    }


    //自定义的ViewHolder，持有每个Item的的所有界面元素

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView mImageView,delete_img,img_view;            //
        public RelativeLayout mRlContentRoot;
        public ViewHolder(View view) {
            super(view);
            mImageView = (ImageView) view.findViewById(R.id.iv_itempublishActivity_addPicture);
            delete_img = (ImageView) view.findViewById(R.id.iv_delete_btn);
            mRlContentRoot=view.findViewById(R.id.rl_content_root);
         //   img_view = (ImageView) view.findViewById(R.id.img_view);

        }
    }


    public interface OnItemClickListener {
        void onClick(int position);

        void onLongClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    public void setCanClick(boolean canClick) {
        this.mCanClick = canClick;
    }


    public interface OnItemTakeClickListener {
        void onClick(int position);

        void onLongClick(int position);
    }

    public void setOnItemTakeClickListener(OnItemTakeClickListener onItemTakeClickListener) {
        this.mOnItemTakeClickListener = onItemTakeClickListener;
    }

}