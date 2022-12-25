package com.amit.yoganet.utils;

import static com.amit.yoganet.constants.Constant.BASE32;
import static com.amit.yoganet.constants.Constant.BITS;

public class MapUtils {


    public static double[] decodeHash(String geohash) {
        boolean isEven = true;
        double[] lat = new double[2];
        double[] lon = new double[2];
        lat[0] = -90.0;
        lat[1] = 90.0;
        lon[0] = -180.0;
        lon[1] = 180.0;

        for (int i = 0; i < geohash.length(); i++) {
            char c = geohash.charAt(i);
            int cd = BASE32.indexOf(c);
            for (int j = 0; j < 5; j++) {
                int mask = BITS[j];
                if (isEven) {
                    refineInterval(lon, cd, mask);
                } else {
                    refineInterval(lat, cd, mask);
                }
                isEven = !isEven;
            }
        }
        double resultLat = (lat[0] + lat[1]) / 2;
        double resultLon = (lon[0] + lon[1]) / 2;
        double[] location = new double[2];
        location[0] = resultLat;
        location[1] = resultLon;

        return location;
    }

    private static void refineInterval(double[] interval, int cd, int mask) {
        if ((cd & mask) != 0)
            interval[0] = (interval[0] + interval[1]) / 2;
        else
            interval[1] = (interval[0] + interval[1]) / 2;
    }

    public static double distance(double myLat, double myLon, double hisLat, double hisLon) {
        double theta = myLon - hisLon;
        double dist = Math.sin(deg2rad(myLat)) * Math.sin(deg2rad(hisLat)) + Math.cos(deg2rad(myLat)) * Math.cos(deg2rad(hisLat)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        dist = dist * 1.609344;
        return (dist);
    }

    private static double rad2deg(double dist) {
        return (dist * 180.0 / Math.PI);
    }

    private static double deg2rad(double myLat) {
        return (myLat * Math.PI / 180.0);
    }
}
