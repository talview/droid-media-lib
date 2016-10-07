package com.talview.medialib.config;

import com.talview.medialib.video.WidthHeight;

public class ConfigurationBuilder {

    public ConfigurationBuilder() {

    }

    public Encoding setVideoDimensions(int width, int height) {
        return new BuildConfiguration(new WidthHeight(width, height));
    }
}
