package de.htw.mp.ui.controller;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;

/**
 * Simple data set viewer. Categorizes and lists all image files in a directory.
 * The UI provides an image viewer and mean color calculation.
 * 
 * UI Controller The controller handles all events of the DatasetViewer.fxml view.
 * 
 * @author Nico Hezel
 */
public abstract class DatasetViewerBase {

	/**
	 * For each image category there exists a file list
	 */
	private Map<String, List<Path>> categoryToFileList = new HashMap<>();
	
	/**
	 * "Open Folder" Button
	 */
	@FXML
	protected Button openDirectoryBtn;

	/**
	 * Content of the left list
	 */
	@FXML
	protected ListView<String> categoryList;

	/**
	 * Content of the right list
	 */
	@FXML
	protected ListView<String> imageFileList;

	/**
	 * Image display on the bottom
	 */
	@FXML
	protected ImageView imagePane;	

	/**
	 * Panel in the middle
	 */
	@FXML
	protected Pane colorPane;

	/**
	 * Gets called once at program start
	 * 
	 * @throws URISyntaxException 
	 */
	@FXML
	public void initialize() throws URISyntaxException {

		// display an initial image
		URL res = getClass().getResource("/flower-385659_640.jpg");
		updateMeanColorAndImage(Paths.get(res.toURI()));
		
		Image image = new Image(res.toString());
		imagePane.setImage(image);
		
		registerEventHandler();
	};

	/**
	 * Register all event listeners
	 */
	private void registerEventHandler() {

		// call onOpenDirectoryClick if the openDirectoryBtn gets pressed
		openDirectoryBtn.setOnAction(this::onOpenDirectoryClick);

		// call onCategoryListChange if a item in the categoryList view gets selected
		categoryList.getSelectionModel().selectedItemProperty().addListener(this::onCategoryListChange);

		// call onImageFileListChange if a item in the imageFileList view gets selected
		imageFileList.getSelectionModel().selectedItemProperty().addListener(this::onImageFileListChange);
	}

	/**
	 * Analysis all images inside the selected category and paint their mean
	 * color in the color panel. Lists all image files of the category in the
	 * image file list view.
	 * 
	 * @param event
	 */
	private void onCategoryListChange(ObservableValue<? extends String> observable, String oldValue, String newValue) {		
		if (newValue != null && oldValue != newValue) {			
			List<Path> imageFiles = categoryToFileList.get(newValue);
			
			// Update the mean color pane and the mean image pane
			updateMeanColorAndImage(imageFiles.toArray(new Path[0]));

			// list all the image file names
			imageFileList.getItems().clear();
			for (Path file : imageFiles)
				imageFileList.getItems().add(file.getFileName().toString());
		}
	}

	/**
	 * Loads and displays the image from the selected image file.
	 * 
	 * @param event
	 */
	private void onImageFileListChange(ObservableValue<? extends String> observable, String oldValue, String newValue) {
		if (newValue != null && oldValue != newValue) {
			String categoryName = categoryList.getSelectionModel().getSelectedItem();
			Path[] imageFile = categoryToFileList.get(categoryName)
												 .stream()
												 .filter(file -> file.getFileName().toString().equals(newValue))
												 .toArray(Path[]::new);
				
			// Update the mean color pane and the mean image pane
			updateMeanColorAndImage(imageFile);
		}
	}
	
	/**
	 * Calculate a new mean color and mean image and displays them on the UI.
	 * 
	 * @param imageFiles
	 */
	private void updateMeanColorAndImage(Path ... imageFiles) {
		
		// calculate the mean color of the category
		Color meanColor = getMeanColor(imageFiles);
		colorPane.setBackground(new Background(new BackgroundFill(meanColor, CornerRadii.EMPTY, Insets.EMPTY)));

		// calculate the mean image of the category
		imagePane.setImage(getMeanImage(imageFiles));
	}

	/**
	 * Opens a dialog to select a data directory. All image files inside the
	 * directory will be filtered and categorized bases on their names. The
	 * resulting categories are listed in the category list view.
	 * 
	 * @param event
	 */
	private void onOpenDirectoryClick(ActionEvent event) {

		// open the directory chooser
		DirectoryChooser dirChooser = new DirectoryChooser();
		dirChooser.setInitialDirectory(new File("../MP_VC_Ue01_FX/dataset"));
		File dir = dirChooser.showDialog(null);
		
		// abort
		if(dir == null) return;

		// read all image files from the directory
		categoryToFileList.clear();
		try (DirectoryStream<Path> files = Files.newDirectoryStream(dir.toPath(), "*.{jpg,jpeg,png}")) {
			for (Path imageFile : files) {
				String name = imageFile.getFileName().toString().split("_")[0];
				List<Path> cat = categoryToFileList.getOrDefault(name, new ArrayList<Path>());
				cat.add(imageFile);
				categoryToFileList.putIfAbsent(name, cat);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
					
		// add an "All" category
		List<Path> all = categoryToFileList.values()
										   .stream()
										   .flatMap(files -> files.stream())
										   .collect(Collectors.toList());

		// list all category names
		resetAll();
		categoryList.getItems().add("All");
		categoryToFileList.keySet().stream().sorted().forEach(name -> categoryList.getItems().add(name));
		
		// now add the All category
		categoryToFileList.put("All", all);
	}
	
	/**
	 * Clears all lists and displays
	 */
	private void resetAll() {
		categoryList.getItems().clear();
		imageFileList.getItems().clear();
		imagePane.setImage(null);
		colorPane.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
	}
	
	
	/**
	 * Calculate the mean color of all given images. Or return PINK if there are no images.
	 * 
	 * @param imageFiles
	 * @return
	 */
	public abstract Color getMeanColor(Path ... imageFiles);
	
	/**
	 * Calculate the mean images of all given images. Or return NULL if there are no images.
	 * 
	 * @param imageFiles
	 * @return
	 */
	public abstract Image getMeanImage(Path ... imageFiles);
}
