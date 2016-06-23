package com.talview.media.video;


public class MediaPlayerException extends Exception {
    public static final int ERROR_UNKNOWN = 1244;
    public static final int ERROR_SERVER_DIED = 234256;
    public static final int MEDIA_ERROR_IO = 1255;
    public static final int MEDIA_ERROR_MALFORMED = 1256;
    public static final int MEDIA_ERROR_UNSUPPORTED = 1257;
    public static final int MEDIA_ERROR_TIMED_OUT = 12578;
    public static final int MEDIA_ERROR_SYSTEM = 12679;

    public static final int ERROR_UNABLE_TO_SET_FILE = 126778;
    public static final int ERROR_UNABLE_TO_READ_FILE = 1265555;

    private int what;

    public MediaPlayerException(int what) {
        this.what = what;
    }

    public int getWhat() {
        return what;
    }
}
