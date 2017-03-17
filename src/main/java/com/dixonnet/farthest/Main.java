package com.dixonnet.farthest;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.opencsv.CSVReader;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import java.io.*;
import java.text.MessageFormat;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        Main main = new Main();
        main.run(args);
    }

    private Point[] border;
    private Point[] points;
    private String[] descriptions;

    private void run(String[] args) throws IOException {
        loadBorder();
        loadPoints(args);
        solve();
    }

    private void loadBorder() {
        JsonElement je = new JsonParser().parse(new InputStreamReader(ClassLoader.getSystemClassLoader().getResourceAsStream("cb_2013_us_nation_500k.geojson")));
        JsonArray coords = je.getAsJsonObject().getAsJsonArray("features").get(0).getAsJsonObject().getAsJsonObject("geometry").getAsJsonArray("coordinates");
        JsonArray mainBorder = coords.get(467).getAsJsonArray();
        JsonArray exterior = mainBorder.get(0).getAsJsonArray();
        List<Point> answer = new ArrayList<>(exterior.size());
        for (JsonElement point : exterior) {
            JsonArray ptArr = point.getAsJsonArray();
            answer.add(new Point(ptArr.get(0).getAsDouble(), ptArr.get(1).getAsDouble()));
        }
        border = answer.toArray(new Point[0]);
    }

    private void loadPoints(String[] args) throws IOException {
        OptionParser parser = new OptionParser();
        OptionSpec<String> fSpec = parser.accepts("f", "CSV file containing data points").withRequiredArg().ofType(String.class).required();
        OptionSpec<Integer> latSpec = parser.accepts("lat", "Column containing latitude").withRequiredArg().ofType(Integer.class).required();
        OptionSpec<Integer> lonSpec = parser.accepts("lon", "Column containing longitude").withRequiredArg().ofType(Integer.class).required();
        OptionSpec<String> descSpec = parser.accepts("d", "Template for row description").withRequiredArg().ofType(String.class).required();

        OptionSet options;
        try {
            options = parser.parse(args);
        }
        catch (Exception e) {
            parser.printHelpOn(System.out);
            return;
        }

        String file = options.valueOf(fSpec);
        int lonCol = options.valueOf(lonSpec);
        int latCol = options.valueOf(latSpec);
        String descTemplate = options.valueOf(descSpec);

        CSVReader reader = new CSVReader(new FileReader(file));
        String[] line;
        List<Point> answer = new ArrayList<>();
        List<String> desc = new ArrayList<>();
        while ((line = reader.readNext()) != null) {
            try {
                Point pt = new Point(Double.parseDouble(line[lonCol]), Double.parseDouble(line[latCol]));
                if (isPointInBorder(pt)) {
                    answer.add(pt);
                    desc.add(MessageFormat.format(descTemplate, (Object[])line));
                }
            }
            catch (Exception e) {
                System.err.println("Skipping line: " + line[0]);
            }
        }
        points = answer.toArray(new Point[0]);
        descriptions = desc.toArray(new String[0]);
    }

    private boolean isPointInBorder(Point point) {
        int i;
        int j;
        boolean result = false;
        for (i = 0, j = border.length - 1; i < border.length; j = i++) {
            if ((border[i].getY() > point.getY()) != (border[j].getY() > point.getY()) &&
                    (point.getX() < (border[j].getX() - border[i].getX()) * (point.getY() - border[i].getY()) / (border[j].getY() - border[i].getY()) + border[i].getX())) {
                result = !result;
            }
        }
        return result;
    }

    private Rectangle bounds(Point[] points) {
        Rectangle answer = new Rectangle(Double.MAX_VALUE, Double.MAX_VALUE, 0, 0);
        for (Point p : points) {
            if (p.getX() < answer.getX()) {
                answer.setX(p.getX());
            }
            if (answer.getRight() < p.getX()) {
                answer.setWidth(p.getX() - answer.getX());
            }
            if (p.getY() < answer.getY()) {
                answer.setY(p.getY());
            }
            if (answer.getBottom() < p.getY()) {
                answer.setHeight(p.getY() - answer.getY());
            }
        }
        return answer;
    }

    private void solve() {
        Rectangle rect = bounds(border);
        double size = Math.min(rect.getWidth() / 2, rect.getHeight() / 2);
        Queue<Square> queue = new LinkedList<>();
        for (double x = rect.getLeft() ; x < rect.getRight(); x += size)
        {
            for (double y = rect.getTop(); y < rect.getBottom(); y += size)
            {
                queue.add(new Square(new Point(x, y), size));
            }
        }
        double best = 0;
        Square bestSquare = null;
        while (!queue.isEmpty())
        {
            Square square = queue.remove();
            if (square.getSide() < 1e-12)
            {
                continue;
            }
            if (!wantSquare(square))
            {
                continue;
            }
            double diameter = diameter(square);
            double centerDist = minDistance(square.getCenter());
            double min = Math.max(centerDist - diameter / 2, 0);
            double max = centerDist + diameter / 2;
            if (max < best)
            {
                continue;
            }
            if (best < min)
            {
                System.out.println(square + " has min distance " + min);
                best = min;
                bestSquare = square;
            }
            for (Square child : square.split())
            {
                queue.add(child);
            }
        }
        for (int i = 0 ; i < points.length ; ++i) {
            if (Math.abs(distance(bestSquare.getTopLeft(), points[i]) - best) < 10) {
                System.out.println(descriptions[i] + ": " + distance(bestSquare.getTopLeft(), points[i]));
            }
        }
    }

    private boolean wantSquare(Square square)
    {
        // We really want to check whether the square and polygon intersect, but it's enough to see if any corners are in the poly
        return isPointInBorder(square.getTopLeft()) || isPointInBorder(square.getTopRight())
                || isPointInBorder(square.getBottomLeft()) || isPointInBorder(square.getBottomRight());
    }

    private double diameter(Square square)
    {
        return distance(square.getTopLeft(), square.getBottomRight());
    }

    // Equirectangular
    private double Distance1(Point p1, Point p2)
    {
        //   return Math.Sqrt((p1.X - p2.X) * (p1.X - p2.X) + (p1.Y - p2.Y) * (p1.Y - p2.Y));
        double x = degreesToRadians((p2.getX() - p1.getX())) * Math.cos(degreesToRadians((p1.getY() + p2.getY()) / 2));
        double y = degreesToRadians((p2.getY() - p1.getY()));
        double R = 6371; // Earth radius in km
        return Math.sqrt(x * x + y * y) * R;
    }

    // Haversine
    private static double distance(Point p1, Point p2)
    {
        double R = 6371; // km
        double φ1 = degreesToRadians(p1.getY());
        double φ2 = degreesToRadians(p2.getY());
        double Δφ = degreesToRadians(p2.getY() - p1.getY());
        double Δλ = degreesToRadians(p1.getX() - p2.getX());

        double a = Math.sin(Δφ / 2) * Math.sin(Δφ / 2) +
                Math.cos(φ1) * Math.cos(φ2) *
                        Math.sin(Δλ / 2) * Math.sin(Δλ / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    private static double degreesToRadians(double d)
    {
        return Math.PI * d / 180.0;
    }

    private double minDistance(Point p) {
        double min = Double.MAX_VALUE;
        for (Point loc : points) {
            double d = distance(p, loc);
            if (d < min) {
                min = d;
            }
        }
        return min;
    }
}
