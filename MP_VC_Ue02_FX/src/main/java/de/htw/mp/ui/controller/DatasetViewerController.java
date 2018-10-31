package de.htw.mp.ui.controller;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import de.htw.mp.model.FeatureContainer;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

/**
 * DatasetViewer: Categorizes and lists all image files in a directory.
 * The controller handles all events of the DatasetViewer.fxml view.
 * 
 * @author Nico Hezel
 */
public class DatasetViewerController extends DatasetViewerBase {

	/**
	 * TODO Compute the mean color of all given images. Or return PINK if there are no images.
	 * 
	 * @param imageFiles
	 * @return
	 */
	@Override
	public Color getMeanColor(Path ... imageFiles) {
		
		// no images? return PINK
		if(imageFiles.length == 0) return Color.PINK;
	
		return Color.PINK;
	}
	
	/**
	 * TODO Compute the mean image of all given images. Or return NULL if there are no images.
	 * 
	 * @param imageFiles
	 * @return
	 */
	@Override
	public Image getMeanImage(Path ... imageFiles) {
		
		// no images? return null
		if(imageFiles.length == 0) return null;		
		
		return null; 
	}
	
	/**
	 * TODO Sort the elements in the database based on the similarity to the search query.
	 * The similarity will be calculated between two features. Features are are stored in
	 * the FeatureContainer and the FeatureType specifies which feature should be used.
	 *  
	 * @param query
	 * @param database
	 * @param featureType
	 * @return sorted list of database elements
	 */
	@Override
	public List<FeatureContainer> retrieve(FeatureContainer query, FeatureContainer[] database, FeatureType featureType) {
		return Arrays.stream(database).collect(Collectors.toList());
	}
	
	/**
	 * TODO Predict the category.
	 * Make the prediction based on the sorted list of features (images or categories). 
	 * Investigate the first k best {@link FeatureContainer} and count which category occurs the most.
	 * 
	 * @param sortedList
	 * @param k
	 * @return predicted category
	 */
	@Override
	public String classify(List<FeatureContainer> sortedList, int k) {
		return sortedList.get(0).getCategory();
	}
	
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
	@Override
	public float evaluate(FeatureContainer[] queries, FeatureContainer[] database, FeatureType featureType, int k) {
		return 0;
	}		
}