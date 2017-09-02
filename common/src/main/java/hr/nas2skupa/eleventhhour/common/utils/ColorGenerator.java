package hr.nas2skupa.eleventhhour.common.utils;

import android.graphics.Color;
import android.support.annotation.ColorInt;

import java.util.Random;

/**
 * Created by nas2skupa on 01/03/2017.
 */
public class ColorGenerator {

    private static final Random random = new Random(System.currentTimeMillis());

    /***
     * Generates color with hue value based on key hash
     * @param key Object: the object to use for hashing
     * @param s float: the saturation component of the returned argb color [0...1].
     * @param v float: the value component of the returned argb color [0...1].
     * @param a float: the alpha component of the returned argb color [0...1].
     * @return int the resulting argb color
     */
    public static int getHsvColor(Object key, float s, float v, float a) {
        float[] hsv = {Math.abs(key.hashCode()) % 360, s, v};
        return Color.HSVToColor((int) (a * 255), hsv);
    }

    /***
     * Generates color with random hue value
     * @param s float: the saturation component of the returned argb color [0...1].
     * @param v float: the value component of the returned argb color [0...1].
     * @param a float: the alpha component of the returned argb color [0...1].
     * @return int the resulting argb color
     */
    public static int getRandomHsvColor(float s, float v, float a) {
        float[] hsv = {random.nextInt(360), s, v};
        return Color.HSVToColor((int) (a * 255), hsv);
    }

    public static int getPaletteColor(Object key, @ColorInt int[] palette) {
        return palette[Math.abs(key.hashCode()) % palette.length];
    }

    public static int getRandomPaletteColor(@ColorInt int[] palette) {
        return palette[random.nextInt(palette.length)];
    }
}