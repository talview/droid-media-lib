package com.talview.media.video;

/**
 * Created by talview23 on 15/3/16.
 */
public class WidthHeight {
    private int width;
    private int height;

    public WidthHeight(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public boolean isGreaterThan(WidthHeight that) {
        return that.width * that.height > this.width * this.height;
    }

    public long getArea() {
        return this.width * this.height;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WidthHeight)) return false;

        WidthHeight that = (WidthHeight) o;

        if (width != that.width) return false;
        return height == that.height;

    }

    @Override
    public int hashCode() {
        int result = (int) (width ^ (width >>> 32));
        result = 31 * result + (int) (height ^ (height >>> 32));
        return result;
    }
}
