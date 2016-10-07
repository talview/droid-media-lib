package com.talview.medialib;

import com.talview.medialib.config.Configuration;
import com.talview.medialib.video.TalviewVideo;

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
