package com.talview.medialib.config;


public interface RecorderVideoConfig {
    /**
     * Choose which camera to use for recording.
     *
     * @param whichCamera which camera to use for recording, if this function is not set front
     *                    camera will be chosen for recording.
     * @return an instance of itself for chaining functions
     */
    RecorderVideoConfig whichCamera(int whichCamera);

    /**
     * set the orientation of the video being recorded.
     *
     * @param orientationInDegrees the orientation of the recorded video in degrees. Default value is
     *                             270 degrees (shows mirrored video).
     * @return a reference to itself for chaining functions.
     */
    RecorderVideoConfig videoOrientation(int orientationInDegrees);

    /**
     * set the orientation for the camera preview display. Default is 90.
     *
     * @param orientation the orientation of the recorded video in degrees. Default value is
     *                             90 degrees.
     * @return a reference to itself for chaining functions.
     */
    RecorderVideoConfig previewDisplayOrientation(int orientation);

    RecorderAudioConfig setVideoFrameRate(int frameRate);
}
