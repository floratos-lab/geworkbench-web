package org.geworkbenchweb.plugins.ttest.results;

import java.awt.Color;

/**
 * Color palette generator based on GMT standards, see:
 * http://www.ruf.rice.edu/~ben/gmt.html
 * http://www.seismology.harvard.edu/%7Ebecker/igmt/data/schemes.pdf
 */
public class GMTColorPalette {

    public GMTColorPalette(ColorRange[] range) {
        this(range, range[0].minColor, range[range.length - 1].maxColor, Color.BLACK);
    }

    public GMTColorPalette(ColorRange[] range, Color smallColor, Color largeColor, Color NaNColor) {
        this.range = range;
        this.smallColor = smallColor;
        this.largeColor = largeColor;
        this.NaNColor = NaNColor;
    }

    public ColorRange[] getColorRange() {
        return range;
    }

    public Color getSmallColor() {
        return smallColor;
    }

    public Color getLargeColor() {
        return largeColor;
    }

    public Color getNaNColor() {
        return NaNColor;
    }

    public Color getColor(double val) {
        for (int i = 0; i < range.length; i++) {
            if (range[i].isInRange(val)) {
                return range[i].getColor(val);
            }
        }
        if (val < range[0].min) {
            return smallColor;
        }
        if (val >= range[range.length - 1].max) {
            return largeColor;
        }
        // assume NaN, probably never happens...
        return NaNColor;
    }

    public static GMTColorPalette getDefault(double min, double max) {
        return new GMTColorPalette(new ColorRange[] { new ColorRange(min, Color.RED, max, Color.BLUE) });
    }

    ColorRange[] range;

    Color smallColor = Color.CYAN, largeColor = Color.MAGENTA, NaNColor = Color.BLACK;

    public static class ColorRange {

        ColorRange(double min, Color minColor, double max, Color maxColor) {
            this.min = min;
            this.minColor = minColor;
            this.max = max;
            this.maxColor = maxColor;
        }

        boolean isInRange(double val) {
            return (min <= val && val < max);
        }

        Color getColor(double val) {
            float alpha = (float) (val / max);
            if (alpha < 0.1f)
                alpha = 0.1f;
            return new Color(linearInterp(min, minColor.getRed(), max, maxColor.getRed(), val) / 256f,
                    linearInterp(min, minColor.getGreen(), max, maxColor.getGreen(), val) / 256f,
                    linearInterp(min, minColor.getBlue(), max, maxColor.getBlue(), val) / 256f,
                    alpha);
        }

        public float linearInterp(double Xa, double Ya, double Xb, double Yb, double val) {
            if (val == Ya) {
                return (float) Xa;
            }
            return (float) (Ya + (Yb - Ya) * (val - Xa) / (Xb - Xa));
        }

        double min, max;

        Color minColor, maxColor;
    }
}