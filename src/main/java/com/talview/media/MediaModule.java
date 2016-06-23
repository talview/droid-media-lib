package com.talview.media;

import com.talview.media.config.Configuration;
import com.talview.media.video.TalviewVideo;
import com.talview.media.video.TalviewVideoImpl;

public class MediaModule {
    private Configuration config;

    public MediaModule(Configuration config) {
        this.config = config;
    }

    public TalviewVideo provideTalviewVideo() {
        return new TalviewVideoImpl(config);
    }
}
