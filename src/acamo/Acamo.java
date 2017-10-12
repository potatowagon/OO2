package acamo;

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
		Pane rightPane = new Pane();
		VBox left = new VBox(new Label("Active Aircrafts"), table);
		HBox main = new HBox(left, rightPane);

		// TODO: Add event handler for selected aircraft
		table.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				if (event.isPrimaryButtonDown() && event.getClickCount() == 2){
					System.out.println(table.getSelectionModel().getSelectedItem());
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
