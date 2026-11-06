package com.example.myapplication.ui.map;

import com.example.myapplication.data.remote.dto.response.NearbyUserResponse;
import java.util.*;

public final class RadarData {
    public static double radius(String text) {
        String value = text == null ? "" : text.trim().replace(',', '.');
        double radius = value.isEmpty() ? 3 : Double.parseDouble(value);
        if (!Double.isFinite(radius) || radius < 1 || radius > 5) throw new IllegalArgumentException();
        return radius;
    }
    public static boolean coordinates(Double lat, Double lng) {
        return lat != null && lng != null && Double.isFinite(lat) && Double.isFinite(lng)
                && Math.abs(lat) <= 90 && Math.abs(lng) <= 180;
    }
    private static double distance(double lat, double lng, double otherLat, double otherLng) {
        double a = Math.pow(Math.sin(Math.toRadians(otherLat - lat) / 2), 2)
                + Math.cos(Math.toRadians(lat)) * Math.cos(Math.toRadians(otherLat))
                * Math.pow(Math.sin(Math.toRadians(otherLng - lng) / 2), 2);
        return 6371.0088 * 2 * Math.asin(Math.sqrt(Math.min(1, Math.max(0, a))));
    }
    public static List<NearbyUserResponse> filter(List<NearbyUserResponse> data, String self, double lat, double lng, double radius) {
        Map<String, NearbyUserResponse> unique = new LinkedHashMap<>();
        for (NearbyUserResponse user : data) {
            if (user == null || user.userId == null || user.userId.trim().isEmpty() || user.userId.equals(self)) continue;
            Double distance = user.distanceKm;
            if (distance == null && coordinates(user.latitude, user.longitude)) distance = distance(lat, lng, user.latitude, user.longitude);
            if (distance == null || !Double.isFinite(distance) || distance < 0 || distance > radius) continue;
            NearbyUserResponse old = unique.get(user.userId);
            if (old != null && old.distanceKm <= distance) continue;
            unique.put(user.userId, new NearbyUserResponse(user.userId, user.firstName, user.lastName, user.avatar,
                    user.university, user.majorName, user.statusTag, user.latitude, user.longitude, distance));
        }
        List<NearbyUserResponse> result = new ArrayList<>(unique.values());
        result.sort(Comparator.comparingDouble(user -> user.distanceKm)); return result;
    }
    private RadarData() { }
}
