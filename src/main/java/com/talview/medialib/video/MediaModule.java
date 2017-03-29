package com.talview.medialib.video;

import com.talview.medialib.config.Configuration;

public class MediaModule {
    private Configuration config;

    public MediaModule(Configuration config) {
        this.config = config;
    }

    public Video provideTalviewVideo() {
        return new VideoImpl(config);
    }
}
