package acmapview;

import com.lynden.gmapsfx.javascript.object.Animation;
import com.lynden.gmapsfx.javascript.object.LatLong;
import com.lynden.gmapsfx.javascript.object.Marker;
import com.lynden.gmapsfx.javascript.object.MarkerOptions;

import messer.BasicAircraft;

public class AirplaneMarker extends Marker {
	BasicAircraft basicAircraft;
	MarkerOptions markerOptions;
	String iconPath;

	public AirplaneMarker(BasicAircraft ba) {
		super(new MarkerOptions());
		this.basicAircraft = ba;
		this.iconPath = setIconPath(ba.getTrak());
		this.markerOptions = new MarkerOptions();
		markerOptions.position(new LatLong(ba.getCoordinate().getLatitude(), ba.getCoordinate().getLongitude()))
				     .icon(this.iconPath)
				     .title("aircraft icon")
				     .visible(true)
				     .animation(Animation.NULL);
		super.setOptions(markerOptions);
	}

	private String setIconPath(double heading) {
		int iconNum = (int) (heading / 360 * 24);
		if (iconNum < 10) {
			return "https://raw.githubusercontent.com/potatowagon/OO2/master/icons/plane0" + iconNum + ".png";
			//return "../../icons/plane0" + iconNum + ".png";
			//return "file:///C:/Users/Sherry%20Wong/Desktop/he/OO2/OOS2Lab456/icons/plane03.png";
		}
		else {
			return "https://raw.githubusercontent.com/potatowagon/OO2/master/icons/plane" + iconNum + ".png";
			//return "../../icons/plane" + iconNum + ".png";
			//return "file:///C:/Users/Sherry%20Wong/Desktop/he/OO2/OOS2Lab456/icons/plane03.png";
		}
	}
}
