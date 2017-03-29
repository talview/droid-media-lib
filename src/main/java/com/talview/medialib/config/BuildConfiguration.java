package com.talview.medialib.config;

import com.talview.medialib.video.Video;
import com.talview.medialib.video.WidthHeight;

/**
 * A class that is used a builder for the configuration class.
 */
class BuildConfiguration implements Encoding, Build<Configuration>, RecorderVideoConfig,
        RecorderAudioConfig {
    private static final int DEFAULT_VIDEO_FRAME_RATE = 8;
    private static final int DEFAULT_CAMERA = Video.FRONT_CAMERA;
    private static final int DEFAULT_RECORDED_VIDEO_ORIENTATION = 270;
    private static final int DEFAULT_DISPLAY_ORIENTATION = 90;

    private WidthHeight desiredVideoWidthHeight;
    private int whichCamera = DEFAULT_CAMERA;
    private int recorderVideoOrientation = DEFAULT_RECORDED_VIDEO_ORIENTATION;
    private int videoFrameRate = DEFAULT_VIDEO_FRAME_RATE;
    private int videoEncodingBitRate;
    private int audioEncodingBitRate;
    private int audioChannels;
    private int audioSamplingRate;
    private int displayOrientation = DEFAULT_DISPLAY_ORIENTATION;

    BuildConfiguration(WidthHeight videoDimensions) {
        this.desiredVideoWidthHeight = videoDimensions;
    }

    @Override
    public RecorderVideoConfig setAudioVideoEncodingBitRates(int videoEncodingBitRate, int audioEncodingBitRate) {
        this.videoEncodingBitRate = videoEncodingBitRate;
        this.audioEncodingBitRate = audioEncodingBitRate;
        return this;
    }

    int getDisplayOrientation() {
        return displayOrientation;
    }

    int getWhichCamera() {
        return whichCamera;
    }

    int getRecorderVideoOrientation() {
        return recorderVideoOrientation;
    }

    int getVideoFrameRate() {
        return videoFrameRate;
    }

    int getVideoEncodingBitRate() {
        return videoEncodingBitRate;
    }

    int getAudioEncodingBitRate() {
        return audioEncodingBitRate;
    }

    int getAudioChannels() {
        return audioChannels;
    }

    int getAudioSamplingRate() {
        return audioSamplingRate;
    }

    WidthHeight getDesiredVideoWidthHeight() {
        return desiredVideoWidthHeight;
    }

    @Override
    public Configuration build() {
        return new Configuration(this);
    }

    @Override
    public RecorderVideoConfig whichCamera(int whichCamera) {
        this.whichCamera = whichCamera;
        return this;
    }

    @Override
    public RecorderVideoConfig videoOrientation(int orientationInDegrees) {
        this.recorderVideoOrientation = orientationInDegrees;
        return this;
    }

    @Override
    public RecorderVideoConfig previewDisplayOrientation(int orientation) {
        this.displayOrientation = orientation;
        return this;
    }

    @Override
    public RecorderAudioConfig setVideoFrameRate(int frameRate) {
        this.videoFrameRate = frameRate;
        return this;
    }


    @Override
    public RecorderAudioConfig setAudioChannels(int channels) {
        this.audioChannels = channels;
        return this;
    }

    @Override
    public Build<Configuration> setAudioSamplingRate(int samplingRate) {
        this.audioSamplingRate = samplingRate;
        return this;
    }
}
