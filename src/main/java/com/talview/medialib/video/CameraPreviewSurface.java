package com.talview.medialib.video;

import android.content.Context;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

@SuppressWarnings("deprecation")
public class CameraPreviewSurface extends SurfaceView {

    private SurfaceHolder surfaceHolder;

    public CameraPreviewSurface(Context context) {
        super(context);
        //Create a surfaceHolder with callbacks
        surfaceHolder = getHolder();
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        setMeasuredDimension(width, height);
    }
}

