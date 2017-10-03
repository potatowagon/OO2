package messer;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

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
		String icao = null; 
		String operator = null; 
		int species = 0; 
		Date posTime = null; 
		double longitude = 0; 
		double latitude = 0; 
		double speed = 0;
		double trak = 0; 
		int altitude = 0; 

		// TODO: Your code goes here
		JSONObject jsonObj = new JSONObject(sentence.toString());
		try {
			icao = (String) jsonObj.get("Icao");
		} catch (JSONException e) {
			icao = null;
		}
		try {
			operator = (String) jsonObj.get("Op");
		} catch (JSONException e) {
			operator = null;
		}
		try {
			species = (int) jsonObj.get("Species");
		} catch (JSONException e) {
			species = 0;
		}
		try {
			long longTime = (long) jsonObj.get("PosTime");
			posTime = new Date(longTime);
		} catch (JSONException e) {
			posTime = null;
		}
		try {
			speed = jsonObj.getDouble("Spd");
		} catch (JSONException e) {
			speed = 0;
		}
		try {
			longitude = jsonObj.getDouble("Long");
		} catch (JSONException e) {
			longitude = 0;
		}
		try {
			latitude = jsonObj.getDouble("Lat");
		} catch (JSONException e) {
			latitude = 0;
		}
		try {
			trak = jsonObj.getDouble("Trak");
		} catch (JSONException e) {
			trak = 0;
		}
		try {
			altitude = jsonObj.getInt("Alt");
		} catch (JSONException e) {
			altitude = 0;
		}

		BasicAircraft msg = new BasicAircraft(icao, operator, species, posTime, new Coordinate(latitude, longitude),
				speed, trak, altitude);

		return msg;
	}
}
