package com.talview.media.video;

/**
 * Created by talview23 on 22/3/16.
 */
public interface MediaPlayerCallback {
    void onError(MediaPlayerException e);

    void onFinishedPlaying();

    void onStart();
}
