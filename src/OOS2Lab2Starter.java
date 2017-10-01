import jsonstream.*;
import messer.Messer;
import senser.Senser;

public class OOS2Lab2Starter
{
    private static double latitude = 48.7433425;
    private static double longitude = 9.3201122;

	public static void main(String[] args)
	{
		String urlString;
		urlString = "https://public-api.adsbexchange.com/VirtualRadar/AircraftList.json";
		
		PlaneDataServer server = new PlaneDataServer(urlString, latitude, longitude, 50);
		Senser senser = new Senser(server);
		new Thread(server).start();
		new Thread(senser).start();
		
		Messer messer = new Messer();
		senser.addObserver(messer);
		new Thread(messer).start();
	}
}