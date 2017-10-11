package acamo;

import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import java.util.ArrayList;
import messer.*;

//TODO: create hash map and complete all operations
public class ActiveAircrafts implements Observer
{
	private static boolean lab4 = true;
	private HashMap<String, BasicAircraft> activeAircrafts;    // store the active aircraft 

	public ActiveAircrafts () {
		this.activeAircrafts = new HashMap<String, BasicAircraft>();
	}

	public synchronized void store(String icao, BasicAircraft ac) {
		this.activeAircrafts.put(icao, ac);
	}

	public synchronized void clear() {
		this.activeAircrafts.clear();
	}

	public synchronized BasicAircraft retrieve(String icao) {
		return this.activeAircrafts.get(icao);
	}

	public synchronized ArrayList<BasicAircraft> values() {
		return (ArrayList<BasicAircraft>) this.activeAircrafts.values();
	}

	public String toString () {
		return activeAircrafts.toString();
	}

	@Override
	// TODO: store arg in hashmap
	public void update(Observable o, Object arg) {
		this.store(((BasicAircraft)arg).getIcao(), (BasicAircraft)arg);
	}
}