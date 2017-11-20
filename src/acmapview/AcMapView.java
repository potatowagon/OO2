package acmapview;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import acamo.ActiveAircrafts;
import acamo.AircraftCursor;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
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
import messer.*;
import senser.Senser;

import com.lynden.gmapsfx.GoogleMapView;
import com.lynden.gmapsfx.MapComponentInitializedListener;
import com.lynden.gmapsfx.javascript.object.Animation;
import com.lynden.gmapsfx.javascript.object.GoogleMap;
import com.lynden.gmapsfx.javascript.object.InfoWindow;
import com.lynden.gmapsfx.javascript.object.InfoWindowOptions;
import com.lynden.gmapsfx.javascript.object.LatLong;
import com.lynden.gmapsfx.javascript.object.MapOptions;
import com.lynden.gmapsfx.javascript.object.MapTypeIdEnum;
import com.lynden.gmapsfx.javascript.object.Marker;
import com.lynden.gmapsfx.javascript.object.MarkerOptions;
import com.lynden.gmapsfx.util.MarkerImageFactory;

public class AcMapView extends Application implements Observer, MapComponentInitializedListener {
	private GoogleMapView mapComponent;
	private GoogleMap map;
	private Pane mapPane;
	private ArrayList<AirplaneMarker> aircraftMarkerGroup;

	// TODO: For Lab 5 copy your Acamo code here
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

		Platform.runLater(() -> {
			mapComponent = new GoogleMapView();
			mapComponent.addMapInializedListener(this);
			mapComponent.setPrefSize(400, 500);
			mapPane.getChildren().add(mapComponent);
		});
	}

	// TODO: When messer updates Acamo (and activeAircrafts) the aircraftList
	// must be updated as well
	@Override
	public void update(Observable o, Object arg) {
		aircraftList.clear();
		aircraftList.addAll(activeAircrafts.values());
		updateGUI();
	}

	private void updateGUI() {
		if (selectedAircraftCursor != null) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					updateSelectedAircraftPane();
					table.requestFocus();
					table.getFocusModel().focus(selectedAircraftCursor.getRow());
					updateMap();
				}
			});
		}
	}

	private void removeAllAircraftMarkers() {
		if (aircraftMarkerGroup != null) {
			for (int i = 0; i < aircraftMarkerGroup.size(); i++) {
				map.removeMarker(aircraftMarkerGroup.get(i));
			}
		}
	}

	private void updateMap() {
		removeAllAircraftMarkers();
		aircraftMarkerGroup = new ArrayList<AirplaneMarker>();
		for (int i = 0; i < aircraftList.size(); i++) {
			AirplaneMarker marker = new AirplaneMarker(aircraftList.get(i));
			aircraftMarkerGroup.add(marker);
			map.addMarker(marker);
		}
	}

	private Scene setupScene() {
		Pane mainRightBox = setupSelectedAircraftPane();
		Pane mainLeftBox = setupTablePane();
		mapPane = new VBox();
		SplitPane main = new SplitPane(mapPane, mainLeftBox, mainRightBox);
		main.setOrientation(Orientation.HORIZONTAL);
		Scene scene = new Scene(main);
		return scene;
	}

	private void AddDoubleClickHandlerForSelectedAircraft() {
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

	@Override
	public void mapInitialized() {
		// TODO Auto-generated method stub
		// Once the map has been loaded by the Webview, initialize the map
		// details.
		LatLong center = new LatLong(latitude, longitude);
		MapOptions options = new MapOptions();
		options.center(center).mapMarker(true).zoom(9).overviewMapControl(false).panControl(false).rotateControl(false)
				.scaleControl(false).streetViewControl(false).zoomControl(false).mapType(MapTypeIdEnum.ROADMAP);
		map = mapComponent.createMap(options);
		// Add a couple of markers to the map.
		MarkerOptions markerOptions = new MarkerOptions();
		LatLong markerLatLong = new LatLong(latitude, longitude);
		markerOptions.position(markerLatLong).title("My new Marker").animation(Animation.DROP).visible(true);
		Marker myMarker = new Marker(markerOptions);
		MarkerOptions markerOptions2 = new MarkerOptions();
		LatLong markerLatLong2 = new LatLong(latitude+10, longitude+10);
		markerOptions2.position(markerLatLong2).title("").visible(true).animation(Animation.DROP).icon("https://raw.githubusercontent.com/potatowagon/OO2/master/icons/plane05.png");
		Marker myMarker2 = new Marker(markerOptions2);
		map.addMarker(myMarker);
		map.addMarker(myMarker2);
	}

}
