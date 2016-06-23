package com.talview.media.config;

public interface RecorderAudioConfig {
    /**
     * Sets the number of audio channels for recording.
     *
     * @param channels the number of audio channels. Usually it is either 1 (mono) or 2 (stereo).
     *                 Default value is 1 (mono)
     * @return a reference to itself for chaining functions.
     */
    RecorderAudioConfig setAudioChannels(int channels);

    Build<Configuration> setAudioSamplingRate(int samplingRate);
}
