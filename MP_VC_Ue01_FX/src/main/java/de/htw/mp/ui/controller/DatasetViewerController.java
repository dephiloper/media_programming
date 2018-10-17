package de.htw.mp.ui.controller;

import java.nio.file.Path;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;

/**
 * DatasetViewer: Categorizes and lists all image files in a directory.
 * The controller handles all events of the DatasetViewer.fxml view.
 *  * 
 * @author Nico Hezel
 */
public class DatasetViewerController extends DatasetViewerBase {
	
	/**
	 * TODO Calculate the mean color of all given images. Or return PINK if there are no images.
	 * 
	 * @param imageFiles
	 * @return
	 */
	public Color getMeanColor(Path ... imageFiles) {
		
		// no images? return PINK
		if(imageFiles.length == 0) return Color.PINK;
	
		return Color.PINK;
	}
	
	/**
	 * TODO Calculate the mean image of all given images. Or return NULL if there are no images.
	 * 
	 * @param imageFiles
	 * @return
	 */
	public Image getMeanImage(Path ... imageFiles) {
		
		// no images? return null
		if(imageFiles.length == 0) return null;			
		
		return null; 
	}
}
