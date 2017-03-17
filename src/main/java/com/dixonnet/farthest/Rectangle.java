package com.dixonnet.farthest;

class Rectangle {
    private double left;
    private double top;
    private double width;
    private double height;

    Rectangle(double left, double top, double width, double height) {
        this.left = left;
        this.top = top;
        this.width = width;
        this.height = height;
    }

    double getX() {
        return left;
    }

    double getLeft() {
        return left;
    }

    void setX(double value) {
        left = value;
    }

    double getRight() {
        return left + width;
    }

    void setWidth(double value) {
        width = value;
    }

    double getY() {
        return top;
    }

    double getTop() {
        return top;
    }

    void setY(double value) {
        top = value;
    }

    double getBottom() {
        return top + height;
    }

    void setHeight(double value) {
        height = value;
    }

    double getWidth() {
        return width;
    }

    double getHeight() {
        return height;
    }

    Point getTopLeft() {
        return new Point(left, top);
    }

    Point getTopRight() {
        return new Point(left + width, top);
    }

    Point getBottomLeft() {
        return new Point(left, top + height);
    }

    Point getBottomRight() {
        return new Point(left + width, top + height);
    }
}


