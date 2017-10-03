package senser;

import java.util.ArrayList;

public class AircraftSentenceFactory
{
	public ArrayList<AircraftSentence> fromAircraftJson(String jsonAircraftList)
	{	
		//TODO: Get distinct aircrafts from the jsonAircraftList string
		// and store them in an ArrayList

		ArrayList<AircraftSentence> aircraftList = new ArrayList<AircraftSentence>();
		
		//remove end '}'
		jsonAircraftList = jsonAircraftList.substring(0, jsonAircraftList.length() - 1); 
		
		String[] seperated = jsonAircraftList.split("},");
		for(String p: seperated){
			p = p.substring(1, p.length());
			aircraftList.add(new AircraftSentence(p));
		}
		return aircraftList;
	}
}
