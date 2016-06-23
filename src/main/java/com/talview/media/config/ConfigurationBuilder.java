package com.talview.media.config;

import com.talview.media.video.WidthHeight;

public class ConfigurationBuilder {

    public ConfigurationBuilder() {

    }

    public Encoding setVideoDimensions(int width, int height) {
        return new BuildConfiguration(new WidthHeight(width, height));
    }
}
