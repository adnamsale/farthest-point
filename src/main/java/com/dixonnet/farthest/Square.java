package com.dixonnet.farthest;

import java.util.ArrayList;
import java.util.List;

class Square {
    private Rectangle rect;

    Square(Point topLeft, double side) {
        rect = new Rectangle(topLeft.getX(), topLeft.getY(), side, side);
    }

    double getSide() {
        return rect.getWidth();
    }

    Point getTopLeft() {
        return rect.getTopLeft();
    }

    Point getTopRight() {
        return rect.getTopRight();
    }

    Point getBottomLeft() {
        return rect.getBottomLeft();
    }

    Point getBottomRight() {
        return rect.getBottomRight();
    }

    Point getCenter() {
        double side = rect.getWidth();
        return new Point(rect.getX() + side / 2, rect.getTop() + side / 2);
    }

    List<Square> split() {
        double newSide = getSide() / 2;
        List<Square> answer = new ArrayList<>();
        answer.add(new Square(getTopLeft(), newSide));
        answer.add(new Square(new Point(getTopLeft().getX() + newSide, getTopLeft().getY()), newSide));
        answer.add(new Square(new Point(getTopLeft().getX(), getTopLeft().getY() + newSide), newSide));
        answer.add(new Square(new Point(getTopLeft().getX() + newSide, getTopLeft().getY() + newSide), newSide));
        return answer;
    }

    @Override
    public String toString() {
        return getTopLeft() + "(" + getSide() + ")";
    }
}