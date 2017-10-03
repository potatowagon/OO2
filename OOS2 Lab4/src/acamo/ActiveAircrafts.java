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
	private HashMap<K, V> activeAircrafts;    // store the active aircraft 

	public ActiveAircrafts () {
	}

	public synchronized void store(String icao, BasicAircraft ac) {
	}

	public synchronized void clear() {
	}

	public synchronized BasicAircraft retrieve(String icao) {
		return ac;
	}

	public synchronized ArrayList<BasicAircraft> values () {
	}

	public String toString () {
		return activeAircrafts.toString();
	}

	@Override
	// TODO: store arg in hashmap
	public void update(Observable o, Object arg) {
	}
}