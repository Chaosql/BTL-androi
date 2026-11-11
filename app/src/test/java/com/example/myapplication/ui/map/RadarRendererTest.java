package com.example.myapplication.ui.map;

import java.util.List;
import org.junit.Test;
import org.maplibre.geojson.Point;
import static org.junit.Assert.*;

public class RadarRendererTest {
    @Test public void scanCircleMatchesSelectedRadiusAndClosesItsBoundary() {
        List<Point> ring = RadarRenderer.circle(21.0285, 105.8542, 3).coordinates().get(0);
        assertEquals(49, ring.size());
        assertEquals(ring.get(0), ring.get(ring.size() - 1));
        Point center = Point.fromLngLat(105.8542, 21.0285);
        for (Point point : ring) assertEquals(3.0, distance(center, point), 0.00001);
    }
    @Test public void beamRotatesAroundLocationWithoutChangingScanRadius() {
        List<Point> first = RadarRenderer.sector(21.0285, 105.8542, 2, 0).coordinates().get(0);
        List<Point> turned = RadarRenderer.sector(21.0285, 105.8542, 2, 90).coordinates().get(0);
        Point center = Point.fromLngLat(105.8542, 21.0285);
        assertEquals(23, first.size());
        assertEquals(center, first.get(0)); assertEquals(center, first.get(first.size() - 1));
        assertEquals(center, turned.get(0)); assertNotEquals(first.get(1), turned.get(1));
        for (int i = 1; i < first.size() - 1; i++) {
            assertEquals(2.0, distance(center, first.get(i)), 0.00001);
            assertEquals(2.0, distance(center, turned.get(i)), 0.00001);
        }
    }
    private double distance(Point first, Point second) {
        double dLat = Math.toRadians(second.latitude() - first.latitude());
        double dLng = Math.toRadians(second.longitude() - first.longitude());
        double a = Math.pow(Math.sin(dLat / 2), 2) + Math.cos(Math.toRadians(first.latitude()))
                * Math.cos(Math.toRadians(second.latitude())) * Math.pow(Math.sin(dLng / 2), 2);
        return 6371.0 * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
