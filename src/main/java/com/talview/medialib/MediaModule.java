package com.talview.medialib;

import com.talview.medialib.config.Configuration;
import com.talview.medialib.video.TalviewVideo;
import com.talview.medialib.video.TalviewVideoImpl;

public class MediaModule {
    private Configuration config;

    public MediaModule(Configuration config) {
        this.config = config;
    }

    public TalviewVideo provideTalviewVideo() {
        return new TalviewVideoImpl(config);
    }
}
