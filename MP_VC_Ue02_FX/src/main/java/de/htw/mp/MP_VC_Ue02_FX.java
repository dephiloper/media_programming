package de.htw.mp;

import java.net.URL;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class MP_VC_Ue02_FX extends Application {

	public static void main(String[] args) throws Exception {
		launch(args);
	}
	
	@Override
	public void start(Stage stage) throws Exception {
		URL res = getClass().getResource("/de/htw/mp/ui/view/DatasetViewer.fxml");
		Parent ui = new FXMLLoader(res).load();
		Scene scene = new Scene(ui);
		stage.setScene(scene);
		stage.setTitle("DatasetViewer - Your name here");
		stage.setOnCloseRequest((WindowEvent event) -> { Platform.exit(); });
		stage.show();
	}
}
