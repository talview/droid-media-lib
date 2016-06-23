package com.talview.media.config;

import com.talview.media.video.WidthHeight;

public class Configuration {
    private WidthHeight desiredVideoWidthHeight;
    private int whichCamera;
    private int recorderVideoOrientation;
    private int videoFrameRate;
    private int videoEncodingBitRate;
    private int audioEncodingBitRate;
    private int audioChannels;
    private int audioSamplingRate;
    private int displayOrientation;

    Configuration(BuildConfiguration builder) {
        this.desiredVideoWidthHeight = builder.getDesiredVideoWidthHeight();
        this.whichCamera = builder.getWhichCamera();
        this.recorderVideoOrientation = builder.getRecorderVideoOrientation();
        this.videoFrameRate = builder.getVideoFrameRate();
        this.videoEncodingBitRate = builder.getVideoEncodingBitRate();
        this.audioChannels = builder.getAudioChannels();
        this.audioEncodingBitRate = builder.getAudioEncodingBitRate();
        this.audioSamplingRate = builder.getAudioSamplingRate();
        this.displayOrientation = builder.getDisplayOrientation();
    }

    public WidthHeight getDesiredVideoWidthHeight() {
        return desiredVideoWidthHeight;
    }

    public int getWhichCamera() {
        return whichCamera;
    }

    public int getRecorderVideoOrientation() {
        return recorderVideoOrientation;
    }

    public int getVideoFrameRate() {
        return videoFrameRate;
    }

    public int getVideoEncodingBitRate() {
        return videoEncodingBitRate;
    }

    public int getAudioEncodingBitRate() {
        return audioEncodingBitRate;
    }

    public int getAudioChannels() {
        return audioChannels;
    }

    public int getAudioSamplingRate() {
        return audioSamplingRate;
    }

    public int getDisplayOrientation() {
        return displayOrientation;
    }
}
