package com.talview.medialib.video;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

public interface Video extends Closeable, SurfaceHolder.Callback, Camera.ErrorCallback {
    int FRONT_CAMERA = 42455;
    int REAR_CAMERA = 42456;

    // first set camera preview.
    void setCameraPreviewSurface(SurfaceView cameraPreviewSurface);

    // set facedetection listener if needed
    @TargetApi(14)
    void setFaceDetectionListener(Camera.FaceDetectionListener listener);

    // start preview first
    void startCameraPreview() throws IOException;

    boolean isPreviewStarted();

    boolean isCameraOpen();

    // then start recording
    void startRecording(File outputFile) throws IOException;

    boolean isRecording();

    /**
     * Stops the media recorder, if recorder is currently recording, no matter if the action suceeds
     * or fails the media recorder is released in the process, hence perform this action in a background
     * thread with callbacks top not block your UI.
     *
     * @return The recorded file.
     * @throws IOException           if no valid data was written into the file
     * @throws IllegalStateException if called before start() was called.
     */
    File stopRecording() throws IOException;

    /**
     * Note: This feature is in beta
     * Use this method to asynchronously play an mp4 video on the surface attached to the SurfaceHolder.
     * @param fileToPlay The mp4 video file to play
     * @param mediaPlayerCallback A callback instance to let you know when the player started finished etc.
     * @param display The holder of the surface on which to render the video.
     */
    void prepareAndStartPlaying(
            final File fileToPlay, final MediaPlayerCallback mediaPlayerCallback, SurfaceHolder display);


    double getAudioAmplitude();

    boolean checkForMic(Context context);

    long getFreeSpace(Context context);

    boolean checkForFrontFacingCamera(Context context);

    void destroy();
}
