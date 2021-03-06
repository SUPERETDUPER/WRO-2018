/*
 * Copyright (c) [2018] [Jonathan McIntosh, Martin Staadecker, Ryan Zazo]
 */

package imagegenerator;

import org.jetbrains.annotations.NotNull;

import java.awt.*;

/**
 * A rectangle that has a color
 */
class Rectangle extends ColorRegion {

    @NotNull
    private final lejos.robotics.geometry.Rectangle mRectangle;

    Rectangle(Color color, float x1, float y1, float w, float h) {
        super(color);
        mRectangle = new lejos.robotics.geometry.Rectangle(x1, y1, w, h);
    }

    @Override
    public boolean contains(float x, float y) {
        return mRectangle.contains(x, y);
    }

    float getWidth() {
        return mRectangle.width;
    }

    float getHeight() {
        return mRectangle.height;
    }
}