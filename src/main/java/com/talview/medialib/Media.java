package com.talview.medialib;

import com.talview.medialib.config.Configuration;
import com.talview.medialib.video.MediaModule;
import com.talview.medialib.video.Video;

/**
 * A factory class for creating instances of talview video.
 */
public class Media {
    public static Video createFromConfig(Configuration config) {
        return new MediaModule(config).provideTalviewVideo();
    }

//    public static TalviewVideo createWithDefaultConfig() {
//        return new MediaModule(new ConfigurationBuilder)
//    }
}
