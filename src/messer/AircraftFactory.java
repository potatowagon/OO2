package messer;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.*;

import senser.AircraftSentence;

/* Get the following datafields out of the JSON sentence using Regex and String methods
 * and return a BasicAircraft
 * 
 * For Lab3 replace usee JSO parsing instead
 * 
 * "Icao":"3C5467", matches; first part is Icao, second part is 3C5467
 * "Op":"Lufthansa matches; first part is Op, second part is Lufthansa
 * "Species":1, matches; first part is Species, second part is 1
 * "PosTime":1504179914003, matches; first part is PosTime, second part is 1504179914003
 * "Lat":49.1912, matches; first part is Lat, second part is 49.1912
 * "Long":9.3915, matches; first part is Long, second part is 9.3915
 * "Spd":420.7, matches; first part is Spd, second part is 420.7
 * "Trak":6.72, matches; first part is Trak, second part is 6.72
 * "GAlt":34135, matches; first part is GAlt, second part is 34135
 */

public class AircraftFactory {

	public BasicAircraft fromAircraftSentence(AircraftSentence sentence) {
		String icao = null; // 0
		String operator = null; // 1
		int species = 0; // 2
		Date posTime = null; // 3
		double longitude = 0; // 4
		double latitude = 0; // 5
		double speed = 0; // 6
		double trak = 0; // 7
		int altitude = 0; // 8

		// TODO: Your code goes here
		ArrayList<String> results = new ArrayList<String>();
		ArrayList<String> expression = new ArrayList<String>();
		expression.add("\"Icao\":\"[A-Z0-9]*\"");
		expression.add("\"Op\":\"[A-Za-z]*\"");
		expression.add("\"Species\":[0-9]");
		expression.add("\"PosTime\":[0-9]*");
		expression.add("\"Long\":[0-9.]*");
		expression.add("\"Lat\":[0-9.]*");
		expression.add("\"Spd\":[0-9.]*");
		expression.add("\"Trak\":[0-9.]*");
		expression.add("\"Alt\":[0-9.]*");

		// filter strings from sentence
		for (int i = 0; i < expression.size(); i++) {
			Pattern pattern = Pattern.compile(expression.get(i));
			Matcher matcher = pattern.matcher(sentence.toString());
			if (matcher.find()) {
				String result1 = matcher.group();
				result1 = result1.split(":")[1];
				results.add(result1);
			} else {
				results.add(null);
			}
		}

		// format filtered results 
		String icaoR = results.get(0);
		if (icaoR != null) {
			icao = icaoR.substring(1, icaoR.length() - 1);
		}
		if (results.get(1) != null) {
			operator = results.get(1).substring(1, (results.get(1).length() - 1));
		}
		if (results.get(2) != null) {
			species = Integer.parseInt(results.get(2));
		}
		if (results.get(3) != null) {
			posTime = new Date(Long.parseLong(results.get(3)));
		}
		if (results.get(4) != null) {
			longitude = Double.parseDouble(results.get(4));
		}
		if (results.get(5) != null) {
			latitude = Double.parseDouble(results.get(5));
		}
		if (results.get(6) != null) {
			speed = Double.parseDouble(results.get(6));
		}
		if (results.get(7) != null) {
			trak = Double.parseDouble(results.get(7));
		}
		if (results.get(8) != null) {
			altitude = Integer.parseInt(results.get(8));
		}
		BasicAircraft msg = new BasicAircraft(icao, operator, species, posTime, new Coordinate(latitude, longitude),
				speed, trak, altitude);

		return msg;
	}
}
