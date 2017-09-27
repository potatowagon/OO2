package senser;

import java.util.ArrayList;

public class AircraftSentenceFactory
{
	public ArrayList<AircraftSentence> fromAircraftJson(String jsonAircraftList)
	{	
		//TODO: Get distinct aircrafts from the jsonAircraftList string
		// and store them in an ArrayList
		ArrayList<AircraftSentence> aircraftList = new ArrayList<AircraftSentence>();
		
		String[] seperated = jsonAircraftList.split("/{(.*?)}/g");
		for(String p: seperated){
			p = p.substring(1, p.length() - 2);
			aircraftList.add(new AircraftSentence(p));
		}
		return aircraftList;
	}
}
