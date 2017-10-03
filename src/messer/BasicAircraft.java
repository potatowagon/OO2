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
		return null;
	}

	public static ArrayList<Object> getAttributesValues(BasicAircraft ac)
	{
		return null;
	}

	//TODO: Overwrite toString() method to print fields

	@Override
	public String toString() {
		String out = "BasicAircraft [icao=" + this.icao + ", operator=" + this.operator + ", species=" + this.species + ", posTime=" + this.posTime.toString() + ", coordinate=" + this.coordinate.toString() + ", speed=" + this.speed + ", trak=" + this.trak + ", altitude=" + this.altitude + "]";
		return out;  
	}
}
