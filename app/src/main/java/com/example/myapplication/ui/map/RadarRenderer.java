package com.example.myapplication.ui.map;

import android.animation.ValueAnimator;
import android.graphics.Color;
import android.view.animation.LinearInterpolator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.maplibre.android.maps.Style;
import org.maplibre.android.style.layers.FillLayer;
import org.maplibre.android.style.layers.LineLayer;
import org.maplibre.android.style.layers.Property;
import org.maplibre.android.style.layers.PropertyFactory;
import org.maplibre.android.style.sources.GeoJsonSource;
import org.maplibre.geojson.Feature;
import org.maplibre.geojson.FeatureCollection;
import org.maplibre.geojson.LineString;
import org.maplibre.geojson.Point;
import org.maplibre.geojson.Polygon;

final class RadarRenderer {
    private Style style;
    private ValueAnimator sweep;
    private double latitude, longitude, radius;
    void setup(Style style) {
        stop(); this.style = style;
        if (style.getSource("radar-circle-source") == null) {
            style.addSource(new GeoJsonSource("radar-circle-source", FeatureCollection.fromFeatures(new Feature[0])));
            style.addSource(new GeoJsonSource("radar-circle-stroke-source", FeatureCollection.fromFeatures(new Feature[0])));
            style.addLayer(new FillLayer("radar-circle-fill-layer", "radar-circle-source").withProperties(
                    PropertyFactory.fillColor(Color.parseColor("#26000000")), PropertyFactory.visibility(Property.NONE)));
            style.addLayer(new LineLayer("radar-circle-stroke-layer", "radar-circle-stroke-source").withProperties(
                    PropertyFactory.lineColor(Color.parseColor("#4DFFFFFF")), PropertyFactory.lineWidth(2f), PropertyFactory.visibility(Property.NONE)));
        }
        if (style.getSource("radar-beam-source") == null) {
            style.addSource(new GeoJsonSource("radar-beam-source", FeatureCollection.fromFeatures(new Feature[0])));
            style.addLayer(new FillLayer("radar-beam-layer", "radar-beam-source").withProperties(
                    PropertyFactory.fillColor(Color.parseColor("#4DFFFFFF")), PropertyFactory.visibility(Property.NONE)));
        }
    }
    void start(double latitude, double longitude, double radius) {
        if (style == null || !style.isFullyLoaded() || !RadarData.coordinates(latitude, longitude)
                || !Double.isFinite(radius) || radius <= 0) return;
        this.latitude = latitude; this.longitude = longitude; this.radius = radius;
        Polygon circle = circle(latitude, longitude, radius);
        update("radar-circle-source", circle);
        GeoJsonSource outline = style.getSourceAs("radar-circle-stroke-source");
        if (outline != null) outline.setGeoJson(Feature.fromGeometry(LineString.fromLngLats(circle.coordinates().get(0))));
        visible(true);
        if (sweep == null) {
            sweep = ValueAnimator.ofFloat(0f, 360f);
            sweep.setDuration(2500); sweep.setRepeatCount(ValueAnimator.INFINITE);
            sweep.setInterpolator(new LinearInterpolator());
            sweep.addUpdateListener(value -> update("radar-beam-source", sector(this.latitude, this.longitude, this.radius,
                    ((Float) value.getAnimatedValue()).doubleValue())));
        }
        if (!sweep.isRunning()) sweep.start();
    }
    void stop() { if (sweep != null) sweep.cancel(); visible(false); }
    void destroy() {
        stop();
        if (sweep != null) { sweep.removeAllUpdateListeners(); sweep = null; }
        style = null;
    }
    private void visible(boolean show) {
        if (style == null || !style.isFullyLoaded()) return;
        for (String id : new String[]{"radar-circle-fill-layer", "radar-circle-stroke-layer", "radar-beam-layer"})
            if (style.getLayer(id) != null) style.getLayer(id).setProperties(PropertyFactory.visibility(show ? Property.VISIBLE : Property.NONE));
    }
    private void update(String id, Polygon polygon) {
        if (style == null || !style.isFullyLoaded()) return;
        GeoJsonSource source = style.getSourceAs(id);
        if (source != null) source.setGeoJson(Feature.fromGeometry(polygon));
    }
    static Polygon circle(double latitude, double longitude, double radius) {
        List<Point> points = new ArrayList<>();
        for (int i = 0; i < 48; i++) points.add(destination(latitude, longitude, radius, 360.0 * i / 48));
        points.add(points.get(0));
        return Polygon.fromLngLats(Collections.singletonList(points));
    }
    static Polygon sector(double latitude, double longitude, double radius, double angle) {
        List<Point> points = new ArrayList<>();
        Point center = Point.fromLngLat(longitude, latitude);
        points.add(center);
        for (int i = 0; i <= 20; i++) points.add(destination(latitude, longitude, radius, angle + 40.0 * i / 20));
        points.add(center);
        return Polygon.fromLngLats(Collections.singletonList(points));
    }
    private static Point destination(double latitude, double longitude, double radius, double angle) {
        double lat = Math.toRadians(latitude), lng = Math.toRadians(longitude);
        double distance = radius / 6371.0, bearing = Math.toRadians(angle);
        double nextLat = Math.asin(Math.sin(lat) * Math.cos(distance) + Math.cos(lat) * Math.sin(distance) * Math.cos(bearing));
        double nextLng = lng + Math.atan2(Math.sin(bearing) * Math.sin(distance) * Math.cos(lat),
                Math.cos(distance) - Math.sin(lat) * Math.sin(nextLat));
        return Point.fromLngLat(((Math.toDegrees(nextLng) + 540) % 360) - 180, Math.toDegrees(nextLat));
    }
}
