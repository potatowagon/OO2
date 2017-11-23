package uniacmapview;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import acamo.ActiveAircrafts;
import acamo.AircraftCursor;
import acmapview.AirplaneMarker;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import jsonstream.PlaneDataServer;
import messer.*;
import senser.Senser;

import com.lynden.gmapsfx.GoogleMapView;
import com.lynden.gmapsfx.MapComponentInitializedListener;
import com.lynden.gmapsfx.javascript.event.GMapMouseEvent;
import com.lynden.gmapsfx.javascript.event.UIEventType;
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

public class UniversalAcMapView extends Application implements Observer, MapComponentInitializedListener {
	private GoogleMapView mapComponent;
	private GoogleMap map;
	private VBox mapPane;
	private ArrayList<AirplaneMarker> aircraftMarkerGroup;
	private Marker mapCenter;
	private InfoWindow selectedAirplaneMarker;
	private Button submit;
	private TextField latInput;
	private TextField lonInput;

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
	private int searchRadius = 50;
	
	private final String URL = "https://public-api.adsbexchange.com/VirtualRadar/AircraftList.json";
	
	PlaneDataServer server;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) {

		server = new PlaneDataServer(URL, latitude, longitude, searchRadius);
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
			mapPane.getChildren().add(setUpUniversalInputBox());
			mapPane.setSpacing(10);
			submitButtonActivate();
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

		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				if (selectedAircraftCursor != null) {
					updateSelectedAircraftPane();
					//table.requestFocus();
					table.getFocusModel().focus(selectedAircraftCursor.getRow());
					//table.getChildren().get(selectedAircraftCursor.getRow()).setStyle("-fx-background-color:lightcoral");
					markSelectedAircraftOnMap();
				}
				updateMap();
			}
		});
	}

	private void markSelectedAircraftOnMap() {
		if (selectedAirplaneMarker != null) {
			selectedAirplaneMarker.close();
		}
		String icao = selectedAircraftCursor.getIcao();
		Coordinate coordinate = getBasicAircraftFromAircraftList(icao).getCoordinate();
		LatLong latLong = new LatLong(coordinate.getLatitude(), coordinate.getLongitude());
		InfoWindowOptions infoOptions = new InfoWindowOptions();
		infoOptions.content(icao).position(latLong);
		selectedAirplaneMarker = new InfoWindow(infoOptions);
		selectedAirplaneMarker.setPosition(latLong);
		selectedAirplaneMarker.open(map, null);
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
					markSelectedAircraftOnMap();
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

	private VBox setUpUniversalInputBox() {
		Label latLabel = new Label("Latitude");
		Label lonLabel = new Label("Longitude");
		latInput = new TextField("" + latitude);
		lonInput = new TextField("" + longitude);
		submit = new Button("Submit");
		VBox box = new VBox(latLabel, latInput, lonLabel, lonInput, submit);
		box.setSpacing(5);
		return box;
	}

	private void submitButtonActivate() {
		if (submit != null) {
			submit.setOnAction(new EventHandler<ActionEvent>() {

				@Override
				public void handle(ActionEvent e) {
					double newLat = Double.parseDouble(latInput.getText());
					double newLon = Double.parseDouble(lonInput.getText());
					if (newLat >= -90 && newLat <= 90 && newLon >= -180 && newLon <= 180) {
						longitude = newLon;
						latitude = newLat;
						LatLong latLong = new LatLong(latitude, longitude);
					    map.setCenter(latLong);
					    mapCenter.setPosition(latLong);
					    selectedAircraftCursor = null;
					    activeAircrafts.clear();
					    aircraftList.clear();
					    server.resetLocation(latitude, longitude, searchRadius);
					} else {
						Alert alert = new Alert(AlertType.INFORMATION);
						alert.setTitle("Dear Silly User");
						alert.setHeaderText(null);
						alert.setContentText("Latitude is between -90 and 90 \nLongitude is between -180 and 180");
						alert.showAndWait();
						latInput.setText("" + latitude);
						lonInput.setText("" + longitude);
					}
				}

			});
		}
		else {
			System.err.println("Submit Button not initialised, is null");
		}

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

	private BasicAircraft getBasicAircraftFromAircraftList(String icao) {
		for (int i = 0; i < aircraftList.size(); i++) {
			if (aircraftList.get(i).getIcao().equals(icao)) {
				return aircraftList.get(i);
			}
		}
		return null;
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
		mapCenter = new Marker(markerOptions);
		MarkerOptions markerOptions2 = new MarkerOptions();
		LatLong markerLatLong2 = new LatLong(latitude + 10, longitude + 10);
		markerOptions2.position(markerLatLong2).title("").visible(true).animation(Animation.DROP)
				.icon("https://raw.githubusercontent.com/potatowagon/OO2/master/icons/plane05.png");
		Marker myMarker2 = new Marker(markerOptions2);
		map.addMarker(mapCenter);
		// map.addMarker(myMarker2);
	}
}
