package com.example.travelapp.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.travelapp.R;
import com.example.travelapp.bean.ImageViewInfo;
import com.example.travelapp.bean.NineGridInfo;
import com.xuexiang.xui.adapter.recyclerview.XRecyclerAdapter;
import com.xuexiang.xui.widget.imageview.nine.ItemImageClickListener;
import com.xuexiang.xui.widget.imageview.nine.NineGridImageView;
import com.xuexiang.xui.widget.imageview.nine.NineGridImageViewAdapter;
import com.xuexiang.xui.widget.imageview.preview.PreviewBuilder;
import com.xuexiang.xui.widget.imageview.preview.loader.GlideMediaLoader;

import java.util.List;

/**
 * 九宫格适配器
 *
 */
public class NineGridRecycleAdapter extends XRecyclerAdapter<NineGridInfo, NineGridRecycleAdapter.NineGridHolder> {

    @NonNull
    @Override
    protected NineGridHolder getViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == NineGridImageView.STYLE_GRID) {
            return new NineGridHolder(inflateView(parent, R.layout.adapter_item_nine_grid_grid_style));
        } else {
            return new NineGridHolder(inflateView(parent, R.layout.adapter_item_nine_grid_fill_style));
        }
    }

    @Override
    protected void bindData(@NonNull NineGridHolder holder, int position, NineGridInfo item) {
        holder.bind(item);
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).getShowType();
    }


    public static class NineGridHolder extends RecyclerView.ViewHolder {
        private NineGridImageView<ImageViewInfo> mNglContent;

        public NineGridHolder(View itemView) {
            super(itemView);
            mNglContent = itemView.findViewById(R.id.ngl_images);
            /**
             * 图片加载
             *
             * @param context
             * @param imageView
             * @param imageViewInfo 图片信息
             */
            NineGridImageViewAdapter<ImageViewInfo> mAdapter = new NineGridImageViewAdapter<ImageViewInfo>() {
                /**
                 * 图片加载
                 *
                 * @param context
                 * @param imageView
                 * @param imageViewInfo 图片信息
                 */
                @Override
                protected void onDisplayImage(Context context, ImageView imageView, ImageViewInfo imageViewInfo) {
                    Glide.with(imageView.getContext())
                            .load(imageViewInfo.getUrl())
                            .placeholder(R.drawable.ic_place_holder)
                            .error(R.drawable.ic_photo_error)
                            .apply(GlideMediaLoader.getRequestOptions())
                            .into(imageView);
                }

                @Override
                protected ImageView generateImageView(Context context) {
                    return super.generateImageView(context);
                }
            };
            mNglContent.setAdapter(mAdapter);
            mNglContent.setItemImageClickListener((imageView, index, list) -> {
                computeBoundsBackward(list);//组成数据
                PreviewBuilder.from((Activity) imageView.getContext())
                        .setImgs(list)
                        .setCurrentIndex(index)
                        .setProgressColor(R.color.xui_config_color_main_theme)
                        .setType(PreviewBuilder.IndicatorType.Dot)
                        .start();//启动
            });
        }

        /**
         * 查找信息
         * @param list 图片集合
         */
        private void computeBoundsBackward(List<ImageViewInfo> list) {
            for (int i = 0;i < mNglContent.getChildCount(); i++) {
                View itemView = mNglContent.getChildAt(i);
                Rect bounds = new Rect();
                if (itemView != null) {
                    ImageView thumbView = (ImageView) itemView;
                    thumbView.getGlobalVisibleRect(bounds);
                }
                list.get(i).setBounds(bounds);
                list.get(i).setUrl(list.get(i).getUrl());
            }

        }

        public void bind(NineGridInfo gridInfo) {
            mNglContent.setImagesData(gridInfo.getImgUrlList(), gridInfo.getSpanType());
        }
    }
}
