package com.talview.media.video;

import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.talview.media.config.Configuration;

import java.io.File;
import java.io.IOException;
import java.util.List;

@SuppressWarnings("deprecation")
public class TalviewVideoImpl implements TalviewVideo {
    private Camera camera;
    private Configuration configuration;
    private int whichCamera;
    private SurfaceView cameraPreviewSurface;
    private Camera.FaceDetectionListener faceDetectionListener;
    private SurfaceHolder surfaceHolder;
    private WidthHeight videoWidthHeight;
    private MediaRecorder mediaRecorder;
    private File outputFile;
    private MediaPlayer player;
    private boolean isRecording = false;
    private boolean isPlaying = false;
    private boolean isCameraOpen = false;
    private boolean isCameraUnlocked = true;

    public TalviewVideoImpl(Configuration configuration) {
        this.configuration = configuration;
        this.whichCamera = configuration.getWhichCamera();
    }

    @Override
    public void setCameraPreviewSurface(SurfaceView cameraPreviewSurface) {
        this.cameraPreviewSurface = cameraPreviewSurface;
        this.surfaceHolder = cameraPreviewSurface.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void openCamera() throws IOException {
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
    public void startPreviewWithFaceDetection() {
        if (surfaceHolder.getSurface() == null) {
            return;
        }
        setFaceDetectionListenerToCamera();
        stopFaceDetection();
        stopPreview();
        setPreviewToCamera();
        startPreview();
        startFaceDetection();
    }

    @Override
    public void startRecording(File outputFile) throws IOException {
        initializeRecorder();
        camera.unlock();
        isCameraUnlocked = true;
        mediaRecorder.setOutputFile(outputFile.getAbsolutePath());
        mediaRecorder.prepare();
        mediaRecorder.start();
        isRecording = true;
        this.outputFile = outputFile;
    }

    @Override
    public void resumeRecoding(File outputFile) throws IOException {
        initializeRecorder();
        mediaRecorder.setOutputFile(outputFile.getAbsolutePath());
        mediaRecorder.prepare();
        mediaRecorder.start();
        isRecording = true;
        this.outputFile = outputFile;
    }

    @Override
    public File stopRecording() {
        if (!isRecording)
            return null;
        mediaRecorder.stop();
        isRecording = false;
        mediaRecorder.release();
//        mediaRecorder.setPreviewDisplay(null);
        mediaRecorder = null;
        return this.outputFile;
    }

    @Override
    public void releaseCamera() {
        if (camera != null) {
            camera.release();
            camera = null;
        }
        isCameraOpen = false;
    }

    @Override
    public File pauseRecording() {
        mediaRecorder.stop();
        isRecording = false;
        return this.outputFile;
    }

    private void initializeRecorder() {
        if (camera == null) {
            throw new UnsupportedOperationException("Please call open camera before trying to do any recording");
        }
        if (mediaRecorder != null) {
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
        }
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setCamera(camera);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setOrientationHint(configuration.getRecorderVideoOrientation());
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setVideoSize(videoWidthHeight.getWidth(), videoWidthHeight.getHeight());
        mediaRecorder.setVideoFrameRate(configuration.getVideoFrameRate());
        mediaRecorder.setVideoEncodingBitRate(configuration.getVideoEncodingBitRate());
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mediaRecorder.setAudioEncodingBitRate(configuration.getAudioEncodingBitRate());
        mediaRecorder.setAudioChannels(configuration.getAudioChannels());
        mediaRecorder.setAudioSamplingRate(configuration.getAudioSamplingRate());
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        mediaRecorder.setPreviewDisplay(cameraPreviewSurface.getHolder().getSurface());
    }

    @Override
    public void prepareAndStartPlaying(final File fileToPlay,
                                       final MediaPlayerCallback mediaPlayerCallback, final SurfaceHolder display) {
        if (fileToPlay.length() <= 0) {
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
                mp = null;
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
    /*public void setSoundMeter(SoundMeter soundMeter) {
        soundMeter.setMediaRecorder(mediaRecorder);
        soundMeter.start();
    }*/

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
    public void setFaceDetectionListener(Camera.FaceDetectionListener listener) {
        this.faceDetectionListener = listener;
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
                return Camera.open(i);
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
        if (videoSizes != null && !videoSizes.isEmpty()) {
            videoWidthHeight = getOptimalPreviewSize(videoSizes);
        } else {
            videoWidthHeight = getOptimalPreviewSize(camParams.getSupportedPreviewSizes());
        }
        camParams.setPreviewSize(videoWidthHeight.getWidth(), videoWidthHeight.getHeight());
        camera.setParameters(camParams);
        camera.setDisplayOrientation(configuration.getDisplayOrientation());
    }

    @Override
    public void close() throws IOException {
        releaseCamera();
        releaseRecorder();
        releasePlayer();
    }

    public void releaseRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }

    public void releasePlayer() {
        if (player != null) {
            if (player.isPlaying()) {
                player.stop();
            }
            player.reset();
            player.release();
            player = null;
        }
    }

    private WidthHeight getOptimalPreviewSize(List<Camera.Size> sizes) {
        long desired = configuration.getDesiredVideoWidthHeight().getArea();
        WidthHeight selected = new WidthHeight(sizes.get(0).width, sizes.get(0).height);
        for (int i = 0, length = sizes.size(); i < length; i++) {
            WidthHeight widthHeight = new WidthHeight(sizes.get(i).width, sizes.get(i).height);
            if (Math.abs(widthHeight.getArea() - desired) < Math.abs(selected.getArea() - desired)) {
                selected = widthHeight;
            }
        }
        if (selected.getArea() != 0)
            return selected;
        else
            throw new RuntimeException("Failed to calculate optimal preview size");
    }

    private void setFaceDetectionListenerToCamera() {
        if (camera != null && faceDetectionListener != null) {
            camera.setFaceDetectionListener(faceDetectionListener);
        }
    }

    private void stopFaceDetection() {
        if (camera != null && faceDetectionListener != null) {
            camera.stopFaceDetection();
        }
    }

    private void stopPreview() {
        if (camera != null) {
            try {
                camera.stopPreview();
            } catch (Exception e) {
                // ignore: tried to stop a non-existent preview
            }
        }
    }

    private void startPreview() {
        if (camera != null) {
            camera.startPreview();
        }
    }

    private void startFaceDetection() {
        stopFaceDetection();
        if (faceDetectionListener != null & camera != null) {
            camera.startFaceDetection();
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
        if (camera == null) {
            return;
        }
        //surface has been created
        setFaceDetectionListenerToCamera();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (camera == null) {
            return;
        }
        surfaceHolder.removeCallback(this);
        stopFaceDetection();
        stopPreview();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (camera == null) {
            return;
        }
        if (holder.getSurface() == null) {
            return;
        }
        // stop preview before making changes
        stopFaceDetection();
        stopPreview();
        setPreviewToCamera();
        startPreview();
        startFaceDetection();
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

    public boolean isFrontCameraAvailable() {
        return cameraPreviewSurface.getContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT);
    }
}
