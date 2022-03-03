package com.example.travelapp.utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.LruCache;
import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.travelapp.MainActivity;
import com.example.travelapp.R;
import com.luck.picture.lib.PictureSelectionModel;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.xuexiang.xui.utils.DrawableUtils;
import com.xuexiang.xutil.XUtil;
import com.xuexiang.xutil.app.ActivityUtils;
import com.xuexiang.xutil.data.DateUtils;
import com.xuexiang.xutil.file.FileIOUtils;
import com.xuexiang.xutil.file.FileUtils;

import java.io.File;


public final class Utils {

    private Utils() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }



    /**
     * 同步首页状态,未启动就启动
     */
    public static void syncMainPageStatus() {
        if (!XUtil.getActivityLifecycleHelper().isActivityExist(MainActivity.class)) {
            ActivityUtils.startActivity(MainActivity.class);
        }
    }


    //==========图片选择===========//

    /**
     * 获取图片选择的配置
     *
     * @param fragment
     * @return
     */
    public static PictureSelectionModel getPictureSelector(Fragment fragment) {
        return PictureSelector.create(fragment)
                .openGallery(PictureMimeType.ofImage())
                .theme(R.style.XUIPictureStyle)
                .maxSelectNum(8)
                .minSelectNum(1)
                .selectionMode(PictureConfig.MULTIPLE)
                .previewImage(true)
                .isCamera(true)
                .enableCrop(false)
                .compress(true)
                .previewEggs(true);
    }

    public static PictureSelectionModel getPictureSelector(Activity activity) {
        return PictureSelector.create(activity)
                .openGallery(PictureMimeType.ofImage())
                .theme(R.style.XUIPictureStyle)
                .maxSelectNum(8)
                .minSelectNum(1)
                .selectionMode(PictureConfig.MULTIPLE)
                .previewImage(true)
                .isCamera(true)
                .enableCrop(false)
                .compress(true)
                .previewEggs(true);
    }

    //==========图片预览===========//

    /**
     * 大图预览
     *
     * @param fragment
     * @param url      图片资源
     * @param view     小图加载控件
     */
//    public static void previewPicture(Fragment fragment, String url, View view) {
//        if (fragment == null || StringUtils.isEmpty(url)) {
//            return;
//        }
//        Rect bounds = new Rect();
//        if (view != null) {
//            view.getGlobalVisibleRect(bounds);
//        }
//        PreviewBuilder.from(fragment)
//                .setImgs(ImageViewInfo.newInstance(url, bounds))
//                .setCurrentIndex(0)
//                .setSingleFling(true)
//                .setProgressColor(R.color.xui_config_color_main_theme)
//                .setType(PreviewBuilder.IndicatorType.Number)
//                .start();
//    }


    //==========拍照===========//

    public static final String JPEG = ".jpeg";

    /**
     * 处理拍照的回调
     *
     * @param data
     * @return
     */
    public static String handleOnPictureTaken(byte[] data) {
        return handleOnPictureTaken(data, JPEG);
    }

    /**
     * 处理拍照的回调
     *
     * @param data
     * @return
     */
    public static String handleOnPictureTaken(byte[] data, String fileSuffix) {
        String picPath = FileUtils.getDiskCacheDir() + "/images/" + DateUtils.getNowMills() + fileSuffix;
        boolean result = FileIOUtils.writeFileFromBytesByStream(picPath, data);
        return result ? picPath : "";
    }

    public static String getImageSavePath() {
        return FileUtils.getDiskCacheDir("images") + File.separator + DateUtils.getNowMills() + JPEG;
    }

    //==========截图===========//

    /**
     * 显示截图结果
     *
     * @param view
     */
//    public static void showCaptureBitmap(View view) {
//        final MaterialDialog dialog = new MaterialDialog.Builder(view.getContext())
//                .customView(R.layout.dialog_drawable_utils_createfromview, true)
//                .title("截图结果")
//                .build();
//        ImageView displayImageView = dialog.findViewById(R.id.createFromViewDisplay);
//        Bitmap createFromViewBitmap = DrawableUtils.createBitmapFromView(view);
//        displayImageView.setImageBitmap(createFromViewBitmap);
//
//        displayImageView.setOnClickListener(v -> dialog.dismiss());
//
//        dialog.show();
//    }

    /**
     * 显示截图结果
     */
//    public static void showCaptureBitmap(Context context, Bitmap bitmap) {
//        final MaterialDialog dialog = new MaterialDialog.Builder(context)
//                .customView(R.layout.dialog_drawable_utils_createfromview, true)
//                .title("截图结果")
//                .build();
//        ImageView displayImageView = dialog.findViewById(R.id.createFromViewDisplay);
//        displayImageView.setImageBitmap(bitmap);
//
//        displayImageView.setOnClickListener(v -> dialog.dismiss());
//
//        dialog.show();
//    }


    /**
     * 截图RecyclerView
     *
     * @param recyclerView
     * @return
     */
    public static Bitmap getRecyclerViewScreenSpot(RecyclerView recyclerView) {
        RecyclerView.Adapter adapter = recyclerView.getAdapter();
        Bitmap bigBitmap = null;
        if (adapter != null) {
            int size = adapter.getItemCount();
            int height = 0;
            Paint paint = new Paint();
            int iHeight = 0;
            final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
            final int cacheSize = maxMemory / 8;
            LruCache<String, Bitmap> bitmapCache = new LruCache<>(cacheSize);
            for (int i = 0; i < size; i++) {
                RecyclerView.ViewHolder holder = adapter.createViewHolder(recyclerView, adapter.getItemViewType(i));
                adapter.onBindViewHolder(holder, i);
                holder.itemView.measure(
                        View.MeasureSpec.makeMeasureSpec(recyclerView.getWidth(), View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                holder.itemView.layout(0, 0, holder.itemView.getMeasuredWidth(),
                        holder.itemView.getMeasuredHeight());
                holder.itemView.setDrawingCacheEnabled(true);
                holder.itemView.buildDrawingCache();
                Bitmap drawingCache = holder.itemView.getDrawingCache();
                if (drawingCache != null) {
                    bitmapCache.put(String.valueOf(i), drawingCache);
                }
                height += holder.itemView.getMeasuredHeight();
            }
            // 这个地方容易出现OOM，关键是要看截取RecyclerView的展开的宽高
            bigBitmap = DrawableUtils.createBitmapSafely(recyclerView.getMeasuredWidth(), height, Bitmap.Config.ARGB_8888, 1);
            if (bigBitmap == null) {
                return null;
            }
            Canvas canvas = new Canvas(bigBitmap);
            Drawable background = recyclerView.getBackground();
            //先画RecyclerView的背景色
            if (background instanceof ColorDrawable) {
                ColorDrawable lColorDrawable = (ColorDrawable) background;
                int color = lColorDrawable.getColor();
                canvas.drawColor(color);
            }
            for (int i = 0; i < size; i++) {
                Bitmap bitmap = bitmapCache.get(String.valueOf(i));
                canvas.drawBitmap(bitmap, 0f, iHeight, paint);
                iHeight += bitmap.getHeight();
                bitmap.recycle();
            }
            canvas.setBitmap(null);
        }
        return bigBitmap;
    }

}
