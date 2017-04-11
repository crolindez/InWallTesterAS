package es.carlosrolindez.inwalltester;

/**
 * Created by Carlos on 08/04/2017.
 */

public class DeviceFilter {
    private static final String inWallFootprint = "00:0D:18";
    private static final String inWall2Footprint = "5C:0E:23";
    private static final String iSelectFootprint = "00:08:F4";
    private static final String selectBtFootprint = "8C:DE:52";
    private static final String inWallWiFiFootprint = "12:05:12";

    public static boolean filterAddress(String address) {
        return (address.substring(0,8).equals(inWallFootprint) ||  address.substring(0,8).equals(inWall2Footprint));
    }

    public static boolean filterName(String name) {
        return (name.length() == 11) && (name.substring(0, 7).equals("KINGBT-"));
    }

    public static int musicTrack(int numTrack) {
        return R.raw.inwall_sample;
    }

}
