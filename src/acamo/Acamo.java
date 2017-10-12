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

		// TODO: create activeAircrafts
		activeAircrafts = new ActiveAircrafts();

		// TODO: activeAircrafts and Acamo needs to observe messer
		messer.addObserver(this);
		messer.addObserver(activeAircrafts);
		fields = BasicAircraft.getAttributesNames();
		cellValueName = BasicAircraft.getAttributesValues();

		// TODO: Fill column header using the attribute names from BasicAircraft
		for (int i = 0; i < fields.size(); i++) {
			TableColumn<BasicAircraft, String> col = new TableColumn<BasicAircraft, String>(fields.get(i));
			col.setCellValueFactory(new PropertyValueFactory(cellValueName.get(i)));
			table.getColumns().add(col);
		}
		table.setItems(aircraftList);

		table.setEditable(false);
		table.autosize();

		// TODO: Create layout of table and pane for selected aircraft
		VBox selectedAircraftHeader = new VBox();
		VBox selectedAircraftValues = new VBox();

		// fill selected aircraft header with labels
		for (int i = 0; i < fields.size(); i++) {
			selectedAircraftHeader.getChildren().add(new Label(fields.get(i)));
		}

		Label label2 = new Label("Selected Aircraft");
		label2.setFont(new Font("Arial", 32));
		HBox selectedAircraftBox = new HBox(selectedAircraftHeader, selectedAircraftValues);
		VBox mainRightBox = new VBox(label2, selectedAircraftBox);

		Label label1 = new Label("Active Aircrafts");
		label1.setFont(new Font("Arial", 30));
		VBox mainLeftBox = new VBox(label1, table);

		HBox main = new HBox(mainLeftBox, mainRightBox);

		// TODO: Add event handler for selected aircraft
		table.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				if (event.isPrimaryButtonDown() && event.getClickCount() == 2) {
					
                    BasicAircraft selectedAircraft = table.getSelectionModel().getSelectedItem();
                    selectedAircraftValues.getChildren().clear();

					for (int c = 0; c < fields.size(); c++) {
						String methodName = "get" + fields.get(c);
						try {
							Method method = selectedAircraft.getClass().getDeclaredMethod(methodName);
							String value = null;
							
							try { 
								if(!(method.invoke(selectedAircraft) instanceof String)){
									value = method.invoke(selectedAircraft).toString();
								}
								else {
									value = (String)method.invoke(selectedAircraft);
								}
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
			}
		});

		Scene scene = new Scene(main);
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
	}
}
