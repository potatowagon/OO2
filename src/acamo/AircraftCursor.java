package acamo;

public class AircraftCursor {
	String icao;
	int row;
	
	public AircraftCursor(String icao, int row){
		this.icao = icao;
		this.row = row;
	}
	
	public String getIcao() {
		return icao;
	}
	public int getRow() {
		return row;
	}
	public void setIcao(String icao) {
		this.icao = icao;
	}
	public void setRow(int row) {
		this.row = row;
	}
}


