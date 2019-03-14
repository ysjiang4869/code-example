package org.jys.common.utils;

/**
 * @author YueSong Jiang
 * @date 2019/3/14
 * @description <p> </p>
 */
public class CoordinateUtils {

    private static final double EARTH_RADIUS = 6_378_137;

    private static final double MAX_LATITUDE = 90;
    private static final double MIN_LATITUDE = -90;
    private static final double MAX_LONGITUDE = 180;
    private static final double MIN_LONGITUDE = -180;


    /**
     * know the fixed position ({@param latitude},{@param longitude})
     * and given the due east distance
     * get the given position longitude
     *
     * @param longitude longitude
     * @param latitude  latitude
     * @param distance  the distance of due east between current position and ({@param latitude},{@param longitude}
     * @return current position longitude
     */
    public static double getLongitude(double longitude, double latitude, double distance) {
        double radLatitude = rad(latitude);
        double b =
                Math.asin(Math.sqrt(Math.pow(Math.sin(Math.abs(distance) / 2 / EARTH_RADIUS), 2)
                        / Math.cos(radLatitude) / Math.cos(radLatitude))) * 2;
        double radLongitude2 = distance > 0 ? (rad(longitude) + b) : (rad(longitude) - b);
        double longitude2 = radLongitude2 * 180 / Math.PI;
        return Math.min(Math.max(longitude2, MIN_LONGITUDE), MAX_LONGITUDE);
    }

    /**
     * know the fixed position {@param latitude}
     * and given the due north distance
     * get the given position latitude
     *
     * @param latitude latitude
     * @param distance the distance of due north between current position and {@param latitude}
     * @return current position latitude
     */
    public static double getLatitude(double latitude, double distance) {
        double a = 2 * Math.asin(Math.sin(Math.abs(distance) / 2 / EARTH_RADIUS));
        double radLatitude1 = rad(latitude);
        double radLatitude2 = distance > 0 ? (radLatitude1 + a) : (radLatitude1 - a);
        double latitude2 = radLatitude2 * 180 / Math.PI;
        return Math.min(Math.max(latitude2, MIN_LATITUDE), MAX_LATITUDE);
    }

    public static double getDistance(double longitude1, double latitude1, double longitude2, double latitude2) {
        double radLatitude1 = rad(latitude1);
        double radLatitude2 = rad(latitude2);
        double a = radLatitude1 - radLatitude2;
        double b = rad(longitude1) - rad(longitude2);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
                + Math.cos(radLatitude1) * Math.cos(radLatitude2) * Math.pow(Math.sin(b / 2), 2)));
        return s * EARTH_RADIUS;
    }

    private static double rad(double d) {
        return d * Math.PI / 180.0;
    }

    public static void main(String[] args) {
        double latitude1 = 35;
        double longitude1 = 80;

        double latitude2 = 35.1;
        double longitude2 = 80;

        double s = getDistance(longitude1, latitude1, longitude2, latitude2);

        double backLatitude = getLatitude(latitude1, s);

        double backLongitude = getLongitude(longitude1, latitude1, 0);

        System.out.println(s);
        System.out.println(backLatitude);
        System.out.println(backLongitude);
    }
}
