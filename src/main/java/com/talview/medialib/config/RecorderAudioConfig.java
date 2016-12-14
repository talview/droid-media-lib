package com.talview.medialib.config;

public interface RecorderAudioConfig {
    /**
     * Sets the number of audio channels for recording.
     *
     * @param channels the number of audio channels. Usually it is either 1 (mono) or 2 (stereo).
     *                 Default value is 1 (mono)
     * @return a reference to itself for chaining functions.
     */
    RecorderAudioConfig setAudioChannels(int channels);

    /**
     * Set the sampling rate for the audio recording.
     * @param samplingRate the sampling rate for the audio recording.
     * @return a reference to itself for chaining functions.
     */
    Build<Configuration> setAudioSamplingRate(int samplingRate);
}
