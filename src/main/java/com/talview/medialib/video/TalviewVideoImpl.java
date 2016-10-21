package com.talview.medialib.video;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;

import com.talview.medialib.ManufacturerUtil;
import com.talview.medialib.config.Configuration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@SuppressWarnings("deprecation")
public class TalviewVideoImpl implements TalviewVideo {
    private Camera camera;
    private Configuration configuration;
    private int whichCamera;
    private SurfaceView cameraPreviewSurface;
    private Camera.FaceDetectionListener faceDetectionListener;
    private SurfaceHolder surfaceHolder;
    private MediaRecorder mediaRecorder;
    private File outputFile;
    private MediaPlayer player;
    private boolean isRecording = false;
    @SuppressWarnings("unused")
    private boolean isPlaying = false;
    private boolean isCameraOpen = false;
    private boolean isCameraUnlocked = true;
    private boolean isFaceDetection = false;
    private boolean faceDetectionRunning = false;
    private boolean cameraPreviewSurfaceCreated = false;
    private boolean previewStarted = false;
    @SuppressWarnings("FieldCanBeLocal")
    private Camera.Size mVideoSize, mPreviewSize;
    private boolean isSetVideoSizeSupported = false;
    private int initialFrameRate = 0;

    public TalviewVideoImpl(Configuration configuration) {
        this.configuration = configuration;
        this.whichCamera = configuration.getWhichCamera();
        initialFrameRate = configuration.getVideoFrameRate();
    }

    @Override
    public void setCameraPreviewSurface(SurfaceView cameraPreviewSurface) {
        if (this.cameraPreviewSurface != null) {
            return;
        }
        this.cameraPreviewSurface = cameraPreviewSurface;
        this.surfaceHolder = cameraPreviewSurface.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }


    private void openCamera() throws IOException {
        if (cameraPreviewSurface == null) {
            throw new NullPointerException("cameraPreviewSurface == null");
        }
        if (whichCamera == FRONT_CAMERA && isFrontCameraAvailable()) {
            openFrontCamera();
        } else {
            openRearCamera();
        }
        configureCamera();
        isCameraUnlocked = false;
    }

    @Override
    public boolean isPreviewStarted() {
        return previewStarted;
    }

    @Override
    public boolean isCameraOpen() {
        return isCameraOpen;
    }

    @Override
    public void startCameraPreview() throws IOException {
        if (camera != null) {
            if (isFaceDetection)
                stopFaceDetection();
            stopPreview();
            releaseCamera();
        }
        openCamera();
        if (isFaceDetection)
            setFaceDetectionListenerToCamera();
        if (cameraPreviewSurfaceCreated) {
            setPreviewToCamera();
            startPreview();
            if (isFaceDetection) {
                startFaceDetection();
            }
        } else {
            surfaceHolder.addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    // https://fabric.io/talview/android/apps/com.talview.candidate/issues/57ce7c300aeb16625bc7f5d5
                    if (!cameraPreviewSurfaceCreated)
                        cameraPreviewSurfaceCreated = true;
                    try {
                        startCameraPreview();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    holder.removeCallback(this);
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                }
            });
        }
    }

    @Override
    public boolean checkForMic(Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            return pm.hasSystemFeature(PackageManager.FEATURE_MICROPHONE);
        } catch (Exception ex) {
            return false;
        }
    }

    @Override
    public long getFreeSpace(Context context) {
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        stat.restat(Environment.getExternalStorageDirectory().getPath());
        return (long) stat.getBlockSize() * (long) stat.getAvailableBlocks();
    }

    @Override
    public boolean checkForFrontFacingCamera(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT);
    }

    @Override
    public void startRecording(File outputFile) throws IOException {
        if (cameraPreviewSurfaceCreated) {
            _startRecording(outputFile, 0);
        } else {
            this.outputFile = outputFile;
            surfaceHolder.addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    try {
                        _startRecording(TalviewVideoImpl.this.outputFile, 0);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    holder.removeCallback(this);
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {

                }
            });
        }
    }

    private void _startRecording(File outputFile, int retryLimiter) throws IOException {
        if (isFaceDetection)
            stopFaceDetection();
        if (!isCameraUnlocked) {
            if (camera != null) {
                try {
                    unlockCamera();
                } catch (RuntimeException cEx) {
                    releaseCamera();
                    openCamera();
                    unlockCamera();
                }
            } else {
                openCamera();
                unlockCamera();
            }
        }
        if (previewStarted)
            stopPreview();
        initializeRecorder();
        Log.v("TalviewVideo", "Video/Preview: Width = " + mVideoSize.width +
                " Height = " + mVideoSize.height);
        mediaRecorder.setOutputFile(outputFile.getAbsolutePath());
        // Apparently setPreviewDisplay must be called after setOutput file according
        // to https://developer.android.com/guide/topics/media/camera.html#capture-video
        makeSurfaceViewAspectRatioSameAsVideoAspectRatio();
        mediaRecorder.setPreviewDisplay(surfaceHolder.getSurface());
        mediaRecorder.prepare();
        try {
            // getting plenty of crashes on this line
            mediaRecorder.start();
            this.outputFile = outputFile;
            isRecording = true;
        } catch (RuntimeException rEx) {
            rEx.printStackTrace();
            releaseRecorder();
            if (configuration.getVideoFrameRate() < initialFrameRate + 40) {
                configuration.setVideoFrameRate(configuration.getVideoFrameRate() + 1);
                _startRecording(outputFile, retryLimiter);
            }
        } catch (Exception e) {
            e.printStackTrace();
            // catch exception and try to reset the recorder.
            releaseRecorder();
            if (retryLimiter > 3) {
                throw e;
            }
            _startRecording(outputFile, retryLimiter + 1);
        }
    }

    private void makeSurfaceViewAspectRatioSameAsVideoAspectRatio() {
        ViewGroup.LayoutParams params = cameraPreviewSurface.getLayoutParams();
        int previewSurfaceHeight = cameraPreviewSurface.getHeight();
        int previewSurfaceWidth = cameraPreviewSurface.getWidth();
        if (previewSurfaceHeight > previewSurfaceWidth) {
            previewSurfaceWidth = previewSurfaceHeight * mVideoSize.height / mVideoSize.width;
        } else {
            previewSurfaceHeight = previewSurfaceWidth * mVideoSize.width / mVideoSize.height;
        }
        params.height = previewSurfaceHeight;
        params.width = previewSurfaceWidth;
        cameraPreviewSurface.setLayoutParams(params);
    }

    private void lockCamera() {
        if (camera != null) {
            camera.lock();
            isCameraUnlocked = false;
        }
    }

    private void unlockCamera() {
        if (camera != null) {
            camera.unlock();
            isCameraUnlocked = true;
        }
    }

    @Override
    public boolean isRecording() {
        return isRecording;
    }

    @Override
    public File stopRecording() throws IOException {
        if (!isRecording)
            return null;
        try {
            mediaRecorder.stop();
        } catch (IllegalStateException iEX) {
            releaseRecorder();
            throw new IllegalStateException("Failed to stop recorder", iEX);
        } catch (RuntimeException rEx) {
            releaseRecorder();
            throw new IOException("no valid audio/video data has been received when stop() was called", rEx);
        }
        releaseRecorder();
        return this.outputFile;
    }

    private void releaseCamera() {
        if (camera != null) {
            try {
                camera.lock();
            } catch (Throwable ignored) {
            }
            camera.release();
            camera = null;
        }
        isCameraOpen = false;
    }

    private void configureMediaRecorder(MediaRecorder recorder) {
        if (recorder == null) throw new NullPointerException("Recorder is null");
        recorder.setCamera(camera);
        recorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        recorder.setOrientationHint(configuration.getRecorderVideoOrientation());

        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setVideoFrameRate(configuration.getVideoFrameRate());
        Log.v("TalviewVideo", "Video frame rate set = " + configuration.getVideoFrameRate());
        if (isSetVideoSizeSupported) {
            recorder.setVideoSize(mVideoSize.width, mVideoSize.height);
            Log.v("TalviewVideo", "recorder.setVideoSize() called");
        }
        recorder.setVideoEncodingBitRate(configuration.getVideoEncodingBitRate());
        recorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);

        recorder.setAudioEncodingBitRate(configuration.getAudioEncodingBitRate());
        recorder.setAudioChannels(configuration.getAudioChannels());
        recorder.setAudioSamplingRate(configuration.getAudioSamplingRate());
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
    }

    private void initializeRecorder() {
        if (camera == null) {
            throw new UnsupportedOperationException("Please start camera preview before recording");
        }
        if (mediaRecorder != null) {
            releaseRecorder();
        }
        mediaRecorder = new MediaRecorder();
        configureMediaRecorder(mediaRecorder);
        mediaRecorder.setOnErrorListener(new MediaRecorder.OnErrorListener() {
            @Override
            public void onError(MediaRecorder mr, int what, int extra) {
                Log.e("TalviewMediaRecorder", "what = " + what + " extra = " + extra);
                releaseRecorder();
                initializeRecorder();
                try {
                    startRecording(TalviewVideoImpl.this.outputFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        // Not setting the preview display here, but set it before starting the recording. We only
        // init and configure the recorder here.
//        mediaRecorder.setPreviewDisplay(cameraPreviewSurface.getHolder().getSurface());
    }

    @Override
    public void prepareAndStartPlaying(final File fileToPlay,
                                       final MediaPlayerCallback mediaPlayerCallback, final SurfaceHolder display) {
        if (fileToPlay == null || fileToPlay.length() <= 0) {
            mediaPlayerCallback.onError(new MediaPlayerException(MediaPlayerException.ERROR_UNABLE_TO_READ_FILE));
            return;
        }
        final MediaPlayer.OnPreparedListener onPreparedListener = new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
                isPlaying = true;
                mediaPlayerCallback.onStart();
            }
        };
        final MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.release();
                player = null;
                isPlaying = false;
                mediaPlayerCallback.onFinishedPlaying();
            }
        };
        MediaPlayer.OnErrorListener onErrorListener = new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                mp.release();
                try {
                    initializeMediaPlayer(fileToPlay.getAbsolutePath(), display,
                            onPreparedListener, this, onCompletionListener);
                    mp = player;
                } catch (IOException e) {
                    mediaPlayerCallback
                            .onError(new MediaPlayerException(MediaPlayerException
                                    .ERROR_UNABLE_TO_SET_FILE));
                    return false;
                }
                mp.prepareAsync();
                return true;
            }
        };
        try {
            initializeMediaPlayer(fileToPlay.getAbsolutePath(), display,
                    onPreparedListener, onErrorListener,
                    onCompletionListener);
        } catch (IOException e) {
            mediaPlayerCallback.onError(new MediaPlayerException(MediaPlayerException
                    .ERROR_UNABLE_TO_SET_FILE));
            return;
        }
        player.prepareAsync();
    }

    @Override
    public double getAudioAmplitude() {
        if (mediaRecorder != null)
            return mediaRecorder.getMaxAmplitude();
        else
            return 0;
    }

    private void initializeMediaPlayer(
            String filePath,
            SurfaceHolder display,
            MediaPlayer.OnPreparedListener onPreparedListener,
            MediaPlayer.OnErrorListener onErrorListener,
            MediaPlayer.OnCompletionListener onCompletionListener) throws IOException {
        if (player != null) {
            if (player.isPlaying()) {
                player.stop();
            }
            player.release();
            player = null;
        }
        player = new MediaPlayer();
        player.setOnPreparedListener(onPreparedListener);
        player.setOnErrorListener(onErrorListener);
        player.setOnCompletionListener(onCompletionListener);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setDisplay(display);
        player.setDataSource(filePath);
    }

    @Override
    @TargetApi(14)
    public void setFaceDetectionListener(Camera.FaceDetectionListener listener) {
        this.faceDetectionListener = listener;
        this.isFaceDetection = true;
    }

    private void openFrontCamera() throws IOException {
        camera = getFrontCamera();
        if (camera == null) {
            throw new IOException("Unable to open front camera");
        }
        isCameraOpen = true;
    }


    private void openRearCamera() throws IOException {
        camera = getRearCamera();
        if (camera == null) {
            throw new IOException("Unable to open rear camera");
        }
        isCameraOpen = true;
    }

    private Camera getRearCamera() {
        return Camera.open();
    }

    private Camera getFrontCamera() {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0, size = Camera.getNumberOfCameras(); i < size; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                try {
                    return Camera.open(i);
                } catch (RuntimeException rEx) {
                    return null;
                }
            }
        }
        return null;
    }

    private void configureCamera() {
        camera.setErrorCallback(this);
        Camera.Parameters camParams = camera.getParameters();
        camParams.set("contrast", 1);
        camParams.set("exposure", "auto");
        List<Camera.Size> videoSizes = camParams.getSupportedVideoSizes();
        if (videoSizes == null || videoSizes.isEmpty()) {
            Log.v("TalviewVideo", "getSupportedVideoSizes() is empty or null");
            isSetVideoSizeSupported = false;
            videoSizes = camParams.getSupportedPreviewSizes();
        } else {
            isSetVideoSizeSupported = true;
        }
        mVideoSize = chooseVideoSize(videoSizes);
        mPreviewSize = chooseOptimalSize(camParams.getSupportedPreviewSizes(), mVideoSize);
        if (ManufacturerUtil.isSamsungGalaxyS3()) {
            mPreviewSize.width = ManufacturerUtil.SAMSUNG_S3_PREVIEW_WIDTH;
            mPreviewSize.height = ManufacturerUtil.SAMSUNG_S3_PREVIEW_HEIGHT;
            camParams.setPreviewSize(ManufacturerUtil.SAMSUNG_S3_PREVIEW_WIDTH,
                    ManufacturerUtil.SAMSUNG_S3_PREVIEW_HEIGHT);
        } else {
            camParams.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
        }
        try {
            camera.setParameters(camParams);
        } catch (RuntimeException rEx) {
            Log.e("Camera", rEx.getMessage(), rEx);
            // todo: propagate it up the call stack.
        }
        camera.setDisplayOrientation(configuration.getDisplayOrientation());
    }

    private Camera.Size chooseOptimalSize(List<Camera.Size> choices, Camera.Size aspectRatio) {
        WidthHeight desiredWidthHeight = configuration.getDesiredVideoWidthHeight();
        int width = desiredWidthHeight.getWidth();
        int height = desiredWidthHeight.getHeight();
        // Collect the supported resolutions that are at least as big as the preview Surface
        List<Camera.Size> bigEnough = new ArrayList<>();
        int w = aspectRatio.width;
        int h = aspectRatio.height;
        for (Camera.Size option : choices) {
            if (option.height == width * h / w &&
                    option.width >= width && option.height >= height) {
                bigEnough.add(option);
            }
        }
        // Pick the smallest of those, assuming we found any
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else {
//            LOG(CameraFragment.class, "Couldn't find any suitable preview size");
            return aspectRatio;
        }
    }

    private int videoPreferredHeight() {
        return configuration.getDesiredVideoWidthHeight().getHeight();
    }

    private int videoPreferredAspect() {
        return Math.round(configuration.getDesiredVideoWidthHeight().getAspectRatio());
    }

    private Camera.Size chooseVideoSize(List<Camera.Size> choices) {
        Camera.Size backupSize = null;
        int preferredHeight = videoPreferredHeight();
        int preferredAspect = videoPreferredAspect();
        for (Camera.Size size : choices) {
            if (size.height <= preferredHeight) {
                if (size.width == size.height * preferredAspect)
                    return size;
                else
                    backupSize = size;
            }
        }
        if (backupSize != null) return backupSize;
//        LOG(CameraFragment.class, "Couldn't find any suitable video size");
        return choices.get(choices.size() - 1);
    }

    @Override
    public void close() throws IOException {
        stopRecording();
        releaseRecorder();
        releasePlayer();
        stopPreview();
        releaseCamera();
    }

    @Override
    public void destroy() {
        try {
            this.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.cameraPreviewSurface.getHolder().removeCallback(this);
    }

    private void releaseRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder.setPreviewDisplay(null);
            mediaRecorder = null;
            lockCamera();
            isRecording = false;
        }
    }

    private void releasePlayer() {
        if (player != null) {
            if (player.isPlaying()) {
                player.stop();
            }
            player.reset();
            player.release();
            player = null;
        }
    }

    @TargetApi(14)
    private void setFaceDetectionListenerToCamera() {
        if (camera != null && faceDetectionListener != null) {
            camera.setFaceDetectionListener(faceDetectionListener);
        }
    }

    @TargetApi(14)
    private void stopFaceDetection() {
        if (camera != null && faceDetectionListener != null && faceDetectionRunning) {
            try {
                camera.stopFaceDetection();
            } catch (RuntimeException rEx) {
                //todo: add crashlytics log here.
            }
            faceDetectionRunning = false;
        }
    }

    private void stopPreview() {
        if (camera != null) {
            try {
                camera.stopPreview();
                previewStarted = false;
            } catch (Exception e) {
                // ignore: tried to stop a non-existent preview
            }
        }
    }

    private void startPreview() {
        if (camera != null && !previewStarted) {
            camera.startPreview();
            previewStarted = true;
        }
    }

    @TargetApi(14)
    private void startFaceDetection() {
        if (faceDetectionListener != null & camera != null && !faceDetectionRunning) {
            try {
                camera.startFaceDetection();
                faceDetectionRunning = true;
            } catch (IllegalArgumentException iEx) {
                // face detection not supported.
                camera.setFaceDetectionListener(null);
                faceDetectionListener = null;
                faceDetectionRunning = false;
            }
        }
    }

    private void setPreviewToCamera() {
        try {
            camera.setPreviewDisplay(surfaceHolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        cameraPreviewSurfaceCreated = true;
        Log.v("cameraSurface", "Surface created");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        cameraPreviewSurfaceCreated = false;
//        surfaceHolder.removeCallback(this);
        if (camera != null) {
            stopPreview();
        }
        Log.v("cameraSurface", "Surface destroyed");
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (camera == null) {
            return;
        }
        if (holder.getSurface() == null) {
            return;
        }
        if (isRecording) {
            return;
        }
        if (holder.isCreating())
            return;
        // stop preview before making changes
        if (isFaceDetection)
            stopFaceDetection();
        stopPreview();
        setPreviewToCamera();
        startPreview();
        if (isFaceDetection)
            startFaceDetection();
        Log.v("cameraSurface", "Surface changed");
    }

    @Override
    public void onError(int error, Camera camera) {
        if (error == Camera.CAMERA_ERROR_SERVER_DIED) {
            try {
                releaseCamera();
                this.openCamera();
            } catch (IOException e) {
                e.printStackTrace();
                // TODO: Crashlytics log here.
            }
        }
    }

    private boolean isFrontCameraAvailable() {
        return cameraPreviewSurface.getContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT);
    }

    private static class CompareSizesByArea implements Comparator<Camera.Size> {
        @Override
        public int compare(Camera.Size lhs, Camera.Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.width * lhs.height -
                    (long) rhs.width * rhs.height);
        }
    }
}
