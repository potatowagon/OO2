package acamo;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import jsonstream.PlaneDataServer;
import messer.BasicAircraft;
import messer.*;
import senser.Senser;

public class Acamo extends Application implements Observer {
	private ActiveAircrafts activeAircrafts;
	private TableView<BasicAircraft> table = new TableView<BasicAircraft>();
	private ObservableList<BasicAircraft> aircraftList = FXCollections.observableArrayList();
	private ArrayList<String> fields;
	private ArrayList<String> cellValueName;
	private VBox selectedAircraftValues;
	private AircraftCursor selectedAircraftCursor = null;

	private double latitude = 48.7433425;
	private double longitude = 9.3201122;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) {
		String urlString;
		urlString = "https://public-api.adsbexchange.com/VirtualRadar/AircraftList.json";
		
		PlaneDataServer server = new PlaneDataServer(urlString, latitude, longitude, 50);
		new Thread(server).start();
		
		Senser senser = new Senser(server);
		new Thread(senser).start();
		
		Messer messer = new Messer();
		senser.addObserver(messer);
		new Thread(messer).start();
		
		activeAircrafts = new ActiveAircrafts();
		messer.addObserver(this);
		messer.addObserver(activeAircrafts);
		fields = BasicAircraft.getAttributesNames();
		cellValueName = BasicAircraft.getAttributesValues();
		
		setupTable();
		
		AddDoubleClickHandlerForSelectedAircraft();
		
		Scene scene = setupScene();
		stage.setScene(scene);
		stage.setTitle("Acamo");
		stage.sizeToScene();
		stage.setOnCloseRequest(e -> Platform.exit());
		stage.show();
	}

	// TODO: When messer updates Acamo (and activeAircrafts) the aircraftList
	// must be updated as well
	@Override
	public void update(Observable o, Object arg) {
		aircraftList.clear();
		aircraftList.addAll(activeAircrafts.values());
		if (selectedAircraftCursor != null) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					updateSelectedAircraftPane();
					table.requestFocus();
					table.getFocusModel().focus(selectedAircraftCursor.getRow());
				}
			});
		}
	}

	private Scene setupScene() {
		Pane mainRightBox = setupSelectedAircraftPane();
		Pane mainLeftBox = setupTablePane();
		Pane main = new HBox(mainLeftBox, mainRightBox);
		Scene scene = new Scene(main);
		return scene;
	}
	
	private void AddDoubleClickHandlerForSelectedAircraft(){
		table.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				if (event.isPrimaryButtonDown() && event.getClickCount() == 2) {

					BasicAircraft selectedAircraft = table.getSelectionModel().getSelectedItem();
					selectedAircraftCursor = new AircraftCursor(selectedAircraft.getIcao(),
							table.getSelectionModel().getFocusedIndex());
					displaySelectedAircraftValues(selectedAircraft);
					System.out.println(selectedAircraftCursor.getRow());
				}
			}
		});
	}

	private Pane setupTablePane() {
		Label label1 = new Label("Active Aircrafts");
		label1.setFont(new Font("Arial", 30));
		VBox tablePane = new VBox(label1, table);
		return tablePane;
	}

	private Pane setupSelectedAircraftPane() {
		VBox selectedAircraftHeader = new VBox();
		this.selectedAircraftValues = new VBox();

		fillSelectedAircraftPaneHeader(selectedAircraftHeader);
		Label label2 = new Label("Selected Aircraft");
		label2.setFont(new Font("Arial", 32));
		HBox selectedAircraftBox = new HBox(selectedAircraftHeader, selectedAircraftValues);
		VBox selectedAircraftPane = new VBox(label2, selectedAircraftBox);
		return selectedAircraftPane;
	}

	private void setupTable() {
		for (int i = 0; i < fields.size(); i++) {
			TableColumn<BasicAircraft, String> col = new TableColumn<BasicAircraft, String>(fields.get(i));
			col.setCellValueFactory(new PropertyValueFactory(cellValueName.get(i)));
			table.getColumns().add(col);
		}
		table.setItems(aircraftList);

		table.setEditable(false);
		table.autosize();
	}

	private void fillSelectedAircraftPaneHeader(Pane selectedAircraftHeader) {
		// fill selected aircraft header with labels
		for (int i = 0; i < fields.size(); i++) {
			selectedAircraftHeader.getChildren().add(new Label(" " + fields.get(i) + "   "));
		}
	}

	private void displaySelectedAircraftValues(BasicAircraft selectedAircraft) {
		selectedAircraftValues.getChildren().clear();

		for (int c = 0; c < fields.size(); c++) {
			String methodName = "get" + fields.get(c);
			try {
				Method method = BasicAircraft.class.getDeclaredMethod(methodName);
				String value = null;

				try {
					value = method.invoke(selectedAircraft).toString();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				selectedAircraftValues.getChildren().add(new Label(value));
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void updateSelectedAircraftPane() {
		boolean found = false;
		BasicAircraft newAircraft = null;
		// check if row still has same icao
		try {
			newAircraft = aircraftList.get(selectedAircraftCursor.getRow());
		} catch (IndexOutOfBoundsException e) {
			System.out.println("Server is slow");
		} finally {
			if (newAircraft != null) {
				if (newAircraft.getIcao().equals(selectedAircraftCursor.getIcao())) {
					displaySelectedAircraftValues(newAircraft);
				} else {
					// search for the aircraft that match the cursor's icao
					for (int i = 0; i < aircraftList.size(); i++) {
						newAircraft = aircraftList.get(i);
						if (newAircraft.getIcao().equals(selectedAircraftCursor.getIcao())) {
							displaySelectedAircraftValues(newAircraft);
							selectedAircraftCursor.setRow(i);
							found = true;
							break;
						}
					}

					if (!found) {
						selectedAircraftCursor = null;
						selectedAircraftValues.getChildren().clear();
					}
				}
			}
		}
	}
}
