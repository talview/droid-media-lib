package com.talview.media;

import com.talview.media.config.Configuration;
import com.talview.media.video.TalviewVideo;

/**
 * A factory class for creating instances of talview video.
 */
public class TalviewMedia {
    public static TalviewVideo createFromConfig(Configuration config) {
        return new MediaModule(config).provideTalviewVideo();
    }

//    public static TalviewVideo createWithDefaultConfig() {
//        return new MediaModule(new ConfigurationBuilder)
//    }
}
