package es.carlosrolindez.inwalltester;



public class DeviceFilter {
    public static final String TAG = "ISELECT";
    private static final String inWallFootprint = "00:0D:18";
    private static final String inWall2Footprint = "5C:0E:23";
    private static final String iSelectFootprint = "00:08:F4";
    private static final String selectBtFootprint = "8C:DE:52";
    private static final String inWallWiFiFootprint = "12:05:12";

    public static boolean filterAddress(String address) {
        return (address.substring(0,8).equals(iSelectFootprint));
    }

    public static boolean filterName(String name) {
        return (name.length() == 9) && (name.substring(0, 4).equals("BTC-"));
    }

    public static int musicTrack(int numTrack) {
  /*      if (numTrack==0)*/ return R.raw.iselect;
  //      else return R.raw.iselect_right;
    }
}
