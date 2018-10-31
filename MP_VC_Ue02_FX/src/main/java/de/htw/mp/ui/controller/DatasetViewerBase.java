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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.htw.mp.model.FeatureContainer;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
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
	 * Type of features
	 */
	public static enum FeatureType { 
		MeanColor, MeanImage;
		
		public static FeatureType get(String value) {
			return FeatureType.valueOf(value.replaceAll(" ", ""));
		}
	};

	/**
	 * For each image category exists a file list
	 */
	private Map<String, Path[]> categoryToFileList = new HashMap<>();
	
	/**
	 * Map from category name to the feature container of a category
	 */
	private Map<String, FeatureContainer> categoryFeatures = new HashMap<>();
	
	/**
	 * Map from filename to feature container for the image
	 */
	private Map<String, FeatureContainer> imageFeatures = new HashMap<>();
	
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
	 * Which type of feature was selected
	 */
	@FXML
	protected ToggleGroup featureGroup;
	
	/**
	 * Which database was selected
	 */
	@FXML
	protected ToggleGroup databaseGroup;
	
	/**
	 * Search result ranking
	 */
	@FXML
	protected ListView<String> rankList;

	/**
	 * How many neighbours should be used
	 */
	@FXML
	protected TextField kNearestNeighbours;
	
	/**
	 * Name of the predicted category
	 */
	@FXML
	protected TextField predictionResult;
	
	/**
	 * "Evaluation" Button
	 */
	@FXML
	protected Button evaluationBtn;
	
	/**
	 * Show the overall correct rate of the system
	 */
	@FXML
	protected Label correctRateLabel;
	

	/**
	 * Gets called once at program start
	 * 
	 * @throws URISyntaxException 
	 */
	@FXML
	public void initialize() throws URISyntaxException {

		// display an initial image
		URL res = getClass().getResource("/flower-385659_640.jpg");
		Path file = Paths.get(res.toURI());
		updateMeanColorAndImage(getMeanColor(file), getMeanImage(file));
		
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
		
		// call onEvaluationClick if the evaluationBtn gets pressed
		evaluationBtn.setOnAction(this::onEvaluationClick);
		
		// call onCategoryListChange if a item in the categoryList view gets selected
		categoryList.getSelectionModel().selectedItemProperty().addListener(this::onCategoryListChange);

		// call onImageFileListChange if a item in the imageFileList view gets selected
		imageFileList.getSelectionModel().selectedItemProperty().addListener(this::onImageFileListChange);
	
		// register clicks events
		imageFileList.setOnMouseClicked(this::onImageFileClick);		
	}

	/**
	 * If a double click is registered a search will be triggered.
	 * 
	 * @param click
	 */
	private void onImageFileClick(MouseEvent click) {
		if (click.getClickCount() == 2) {

			// get the query
			String filename = imageFileList.getSelectionModel().getSelectedItem();
			FeatureContainer query = imageFeatures.get(filename);

			// get the database
			String dbName = ((RadioButton) databaseGroup.getSelectedToggle()).getText();
			Map<String, FeatureContainer> database = ("All Images".equalsIgnoreCase(dbName)) ? imageFeatures : categoryFeatures;

			// sort the elements
			FeatureType featureType = FeatureType.get(((RadioButton) featureGroup.getSelectedToggle()).getText());
			List<FeatureContainer> result = retrieve(query, database.values().toArray(new FeatureContainer[0]), featureType);

			// list all search results
			rankList.getItems().clear();
			for (FeatureContainer element : result)
				rankList.getItems().add(element.getName());

			// make a prediction
			int kNN = Integer.parseInt(kNearestNeighbours.getText());
			String prediction = classify(result, kNN);
			predictionResult.setText(prediction);
		}
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
			updateMeanColorAndImage(this.categoryFeatures.get(newValue));

			// list all the image file names
			imageFileList.getItems().clear();
			for (Path file : categoryToFileList.get(newValue))
				imageFileList.getItems().add(file.getFileName().toString());
		}
	}

	/**
	 * Loads and displays the image from the selected image file.
	 * 
	 * @param event
	 */
	private void onImageFileListChange(ObservableValue<? extends String> observable, String oldValue, String newValue) {
		if (newValue != null && oldValue != newValue)
			updateMeanColorAndImage(this.imageFeatures.get(newValue));
	}
	
	/**
	 * Display the mean color and mean image
	 * 
	 * @param imageFiles
	 */
	private void updateMeanColorAndImage(FeatureContainer feature) {
		updateMeanColorAndImage(feature.getMeanColor(), feature.getMeanImage());
	}
	
	/**
	 * Display the mean color and mean image
	 * 
	 * @param meanColor
	 * @param meanImage
	 */
	private void updateMeanColorAndImage(Color meanColor, Image meanImage) {
		colorPane.setBackground(new Background(new BackgroundFill(meanColor, CornerRadii.EMPTY, Insets.EMPTY)));
		imagePane.setImage(meanImage);
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
		dirChooser.setInitialDirectory(new File("dataset"));
		File dir = dirChooser.showDialog(null);
		
		// abort
		if(dir == null) return;

		// read all image files from the directory
		try (DirectoryStream<Path> files = Files.newDirectoryStream(dir.toPath(), "*.{jpg,jpeg,png}")) {
			Map<String, List<Path>> categories = new HashMap<>();
			for (Path imageFile : files) {
				String name = imageFile.getFileName().toString().split("_")[0];
				List<Path> cat = categories.getOrDefault(name, new ArrayList<Path>());
				cat.add(imageFile);
				categories.putIfAbsent(name, cat);
			}
			
			// copy over
			categoryToFileList.clear();
			categories.forEach((key, value) -> { categoryToFileList.put(key, value.toArray(new Path[0])); });
			
		} catch (IOException e) {
			e.printStackTrace();
		}
					
		// create an "All" category
		Path[] all = categoryToFileList.values()
							   		   .stream()
							   		   .flatMap(files -> Arrays.stream(files))
							   		   .toArray(Path[]::new);
		
		// list all category names
		resetAll();
		categoryList.getItems().add("All");
		categoryToFileList.keySet().stream().sorted().forEach(name -> categoryList.getItems().add(name));
		
		// now add the All category
		categoryToFileList.put("All", all);
		
		// calculate all the mean colors and mean images for all files
		precalculateFeatures(categoryToFileList);
	}
	
	/**
	 * Calculate the overall correct rate of the system.
	 * 
	 * @param event
	 */
	private void onEvaluationClick(ActionEvent event) {
		
		// get the database
		String dbName = ((RadioButton) databaseGroup.getSelectedToggle()).getText();
		Map<String, FeatureContainer> database = ("All Images".equalsIgnoreCase(dbName)) ? imageFeatures : categoryFeatures;

		// evaluate the system
		int kNN = Integer.parseInt(kNearestNeighbours.getText());
		FeatureType featureType = FeatureType.get(((RadioButton) featureGroup.getSelectedToggle()).getText());
		float correctRate = evaluate(imageFeatures.values().toArray(new FeatureContainer[0]), database.values().toArray(new FeatureContainer[0]), featureType, kNN);
		
		correctRateLabel.setText("Correct Rate: "+(int)(correctRate*100)+"%");
	}
	
	/**
	 * Compute all images and categories in advance and store them.
	 * 
	 * @param categoryToFileList
	 */
	private void precalculateFeatures(Map<String, Path[]> categories) {
		
		categoryFeatures.clear();
		categories.forEach((categoryName, categoryFiles) -> {
			if("All".equalsIgnoreCase(categoryName)) return;
			
			String name = categoryName;
			Color meanColor = getMeanColor(categoryFiles);
			Image meanImage = getMeanImage(categoryFiles);
			FeatureContainer feature = new FeatureContainer(name, categoryName, meanColor, meanImage);
			categoryFeatures.put(name, feature);
		});
		
		imageFeatures.clear();
		categories.forEach((categoryName, categoryFiles) -> {
			if("All".equalsIgnoreCase(categoryName)) return;
			
			for (Path imageFile : categoryFiles) {	
				String name = imageFile.getFileName().toString();
				Color meanColor = getMeanColor(imageFile);
				Image meanImage = getMeanImage(imageFile);
				FeatureContainer feature = new FeatureContainer(name, categoryName, meanColor, meanImage);
				imageFeatures.put(name, feature);
			}			
		});
	}

	/**
	 * Clears all lists and displays
	 */
	private void resetAll() {
		categoryList.getItems().clear();
		imageFileList.getItems().clear();
		rankList.getItems().clear();
		imagePane.setImage(null);
		colorPane.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
		predictionResult.setText("");
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
	
	/**
	 * Sort the elements in the database based on the similarity to the search query.
	 * The similarity will be calculated between two features. Features are are stored in
	 * the FeatureContainer and the FeatureType specifies which feature should be used.
	 *  
	 * @param query
	 * @param database
	 * @param featureType
	 * @return sorted list of database elements
	 */
	public abstract List<FeatureContainer> retrieve(FeatureContainer query, FeatureContainer[] database, FeatureType featureType);

	/**
	 * Predict the category.
	 * Make the prediction based on the sorted list of features (images or categories). 
	 * 
	 * @param sortedList
	 * @param k
	 * @return predicted category
	 */
	public abstract String classify(List<FeatureContainer> sortedList, int k);
	
	/**
	 * TODO Evaluate the overall performance of the system. 
	 * Predict for every query the a category and compare the result against the real category
	 * {@link FeatureContainer#getCategory()}. Count how many time the system was correct and
	 * return an overall correct rate in percent. Reuse the {@link #retrieve(FeatureContainer, FeatureContainer[], FeatureType)}
	 * and {@link #classify(List, int)} methods for this task.
	 * 
	 * @param queries
	 * @param database
	 * @param featureType
	 * @param k nearest neighbors
	 * @return overall correct rate
	 */	
	public abstract float evaluate(FeatureContainer[] queries, FeatureContainer[] database, FeatureType featureType, int k) ;
}
