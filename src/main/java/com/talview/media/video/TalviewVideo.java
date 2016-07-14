package com.talview.media.video;

import android.content.Context;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

public interface TalviewVideo extends Closeable, SurfaceHolder.Callback, Camera.ErrorCallback {
    int FRONT_CAMERA = 42455;
    int REAR_CAMERA = 42456;

    /**
     * opens the configured camera(front/rear), if you are not sure if front camera exists,
     * call the {@code isFrontCameraAvailable} function.
     *
     * @throws IOException
     */
//    void openCamera() throws IOException;
    // first set camera preview.
    void setCameraPreviewSurface(SurfaceView cameraPreviewSurface);

    // set facedetection listener if needed
    void setFaceDetectionListener(Camera.FaceDetectionListener listener);

    // start preview first
    void startCameraPreview() throws IOException;

    // then start recording
    void startRecording(File outputFile) throws IOException;

//    void resumeRecoding(File outputFile) throws IOException;

    boolean isRecording();

    File stopRecording() throws IOException;

//    File pauseRecording();

    void prepareAndStartPlaying(
            final File fileToPlay, final MediaPlayerCallback mediaPlayerCallback, SurfaceHolder display);


    double getAudioAmplitude();

//    void releaseCamera();

//    void startPreviewWithFaceDetection() throws IOException;

    boolean checkForMic(Context context);

    long getFreeSpace(Context context);

    boolean checkForFrontFacingCamera(Context context);

    void destroy();
}
