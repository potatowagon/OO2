package messer;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;

public class BasicAircraft {
	private String icao;
	private String operator;
	private Integer species;
	private Date posTime;
	private Coordinate coordinate;
	private Double speed;
	private Double trak;
	private Integer altitude;
	
	//TODO: Create constructor

	public BasicAircraft(String icao, String operator, Integer species, Date posTime, Coordinate co, Double speed, Double trak, Integer altitude) {
		this.icao = icao;
		this.operator = operator;
		this.species = species;
		this.posTime = posTime;
		this.coordinate = co;
		this.speed = speed;
		this.trak = trak;
		this.altitude = altitude;
	}

	//TODO: Create relevant getter methods
	
	//TODO: Lab 4-6 return attribute names and values for table
	public static ArrayList<String> getAttributesNames()
	{
		ArrayList<String> attributeNames = new ArrayList<String>();
		attributeNames.add("Icao");
		attributeNames.add("Operator");
		attributeNames.add("Species");
		attributeNames.add("PosTime");
		attributeNames.add("Coordinate");
		attributeNames.add("Speed");
		attributeNames.add("Trak");
		attributeNames.add("Altitude");
		return attributeNames;
	}

	public static ArrayList<String> getAttributesValues(BasicAircraft ac)
	{
		ArrayList<String> attributeValues = new ArrayList<String>();
		attributeValues.add(ac.icao);
		attributeValues.add(ac.operator);
		attributeValues.add(Integer.toString(ac.species));
		attributeValues.add(ac.posTime.toString());
		attributeValues.add(ac.coordinate.toString());
		attributeValues.add(Double.toString(ac.speed));
		attributeValues.add(Double.toString(ac.trak));
		attributeValues.add(Double.toString(ac.altitude));
		return attributeValues;
	}

	//TODO: Overwrite toString() method to print fields

	@Override
	public String toString() {
		String out = "BasicAircraft [icao=" + this.icao + ", operator=" + this.operator + ", species=" + this.species + ", posTime=" + this.posTime.toString() + ", coordinate=" + this.coordinate.toString() + ", speed=" + this.speed + ", trak=" + this.trak + ", altitude=" + this.altitude + "]";
		return out;  
	}
}
