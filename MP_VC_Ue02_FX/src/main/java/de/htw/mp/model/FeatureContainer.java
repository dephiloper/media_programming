package de.htw.mp.model;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;

/**
 * Simple POJO (Plain old Java object) for holding precalculated results.
 * 
 * @author Nico Hezel
 */
public class FeatureContainer {

	protected String name;
	protected String category;
	protected Color meanColor;
	protected Image meanImage;
	
	public FeatureContainer(String name, String category, Color meanColor, Image meanImage) {
		this.name = name;
		this.category = category;
		this.meanColor = meanColor;
		this.meanImage = meanImage;
	}

	public String getName() {
		return name;
	}
	
	public String getCategory() {
		return category;
	}

	public Color getMeanColor() {
		return meanColor;
	}

	public Image getMeanImage() {
		return meanImage;
	}
	
	@Override
	public String toString() {
		return FeatureContainer.class.getSimpleName()+" for "+name;
	}
}
