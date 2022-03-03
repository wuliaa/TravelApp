package com.example.travelapp.adapter;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MyItemDecoration extends RecyclerView.ItemDecoration {

    private final int spec;
    private final int lr;

    public MyItemDecoration(Context context, int dpValue,int dp2) {
        spec = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, context.getResources().getDisplayMetrics());
        lr = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp2, context.getResources().getDisplayMetrics());

    }

    @Override
    public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
    }


    @Override
    public void onDrawOver(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
    }


    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {

        outRect.set(lr, spec, lr, 0);
    }
}




