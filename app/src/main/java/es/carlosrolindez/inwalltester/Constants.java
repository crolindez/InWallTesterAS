
package es.carlosrolindez.inwalltester;

/**
 * Defines several constants used between A2dpService and the UI.
 */
public interface Constants {

    // Message types sent from the BluetoothChatService Handler
/*    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;*/

    // Key names 
	String a2dpFilter = "es.carlosrolindez.InWallTester.A2dpService.FILTER";
	String NameFilter = "es.carlosrolindez.InWallTester.A2dpService.NAME";
	String LAUNCH_MAC = "es.carlosrolindez.InWallTester.A2dpService.MAC";

	String DEVICE_NAME = "es.carlosrolindez.InWallTester.A2dpService.DEVICE_NAME";
	String DEVICE_MAC = "es.carlosrolindez.InWallTester.A2dpService.DEVICE_MAC";
	String FTP_MODE = "es.carlosrolindez.InWallTester.A2dpService.FTP_MODE";
	
    // Intent request codes
	int REQUEST_ENABLE_BT = 1;
	

}
