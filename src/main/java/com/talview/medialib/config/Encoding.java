package com.talview.medialib.config;

public interface Encoding {
    /**
     * Set the video encoding and audio encoding bit rates
     * @param videoEncodingBitRate the video encoding bit rate
     * @param audioEncodingBitRate the audio encoding bit rate
     * @return a reference to itself for chaining functions.
     */
    RecorderVideoConfig setAudioVideoEncodingBitRates(int videoEncodingBitRate, int audioEncodingBitRate);
}
