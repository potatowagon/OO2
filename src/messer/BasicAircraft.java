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
	public String getIcao(){
		return this.icao;
	}
	
	public String getOperator() {
		return this.operator;
	}
	
	public int getSpecies() {
		return this.species;
	}
	
	public String getPosTime() {
		return posTime.toString();
	}

	public String getCoordinate() {
		return coordinate.toString();
	}

	public Double getSpeed() {
		return speed;
	}

	public Double getTrak() {
		return trak;
	}

	public Integer getAltitude() {
		return altitude;
	}

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

	public static ArrayList<String> getAttributesValues()
	{
		ArrayList<String> attributeValues = new ArrayList<String>();
		
		attributeValues.add("icao");
		attributeValues.add("operator");
		attributeValues.add("species");
		attributeValues.add("posTime");
		attributeValues.add("coordinate");
		attributeValues.add("speed");
		attributeValues.add("trak");
		attributeValues.add("altitude");
		return attributeValues;
	}

	//TODO: Overwrite toString() method to print fields

	@Override
	public String toString() {
		String out = "BasicAircraft [icao=" + this.icao + ", operator=" + this.operator + ", species=" + this.species + ", posTime=" + this.posTime.toString() + ", coordinate=" + this.coordinate.toString() + ", speed=" + this.speed + ", trak=" + this.trak + ", altitude=" + this.altitude + "]";
		return out;  
	}
}
