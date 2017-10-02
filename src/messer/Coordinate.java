package messer;

import java.text.DecimalFormat;

public class Coordinate {
	private double latitude;
	private double longitude;
	
	//TODO: Constructor, Getter/Setter and toString()
	public Coordinate(double lat, double lon){
		this.latitude = lat;
		this.longitude = lon;
	}
	
	public String toString() {
		DecimalFormat df = new DecimalFormat("#.00");
		String out = df.format(latitude) + "/" + df.format(longitude);
		return out;
	}
}