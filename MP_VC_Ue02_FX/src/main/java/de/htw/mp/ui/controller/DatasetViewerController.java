package de.htw.mp.ui.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import javax.imageio.ImageIO;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import de.htw.mp.model.FeatureContainer;

/**
 * DatasetViewer: Categorizes and lists all image files in a directory.
 * The controller handles all events of the DatasetViewer.fxml view.
 * 
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

		// no images? return BLACK
		if(imageFiles.length == 0) return Color.BLACK;

		// Load images
		BufferedImage[] images = load_images(imageFiles);

		long[] mean_color = new long[3];
		int num_pixels = 0;

		for (BufferedImage img : images) {
			num_pixels += img.getHeight()*img.getWidth();

			long[] color_sum = getColorSum(img);
			for (int i = 0; i < 3; i++) {
				mean_color[i] += color_sum[i];
			}
		}

		for (int i = 0; i < 3; i++) {
			mean_color[i] = mean_color[i] / num_pixels;
		}

		return Color.rgb((int)mean_color[0], (int)mean_color[1], (int)mean_color[2]);
	}

	private BufferedImage[] load_images(Path[] paths) {
		BufferedImage[] images = new BufferedImage[paths.length];
		int i = 0;
		for (Path p : paths) {
			try {
				images[i] = ImageIO.read(new File(p.toString()));
			} catch (IOException e) {
				e.printStackTrace();
			}
			i++;
		}

		return images;
	}

	private long[] getColorSum(BufferedImage image) {
		int[] pixels = getPixels(image);

		long[] color_sum = new long[3];
		for (int i = 0; i < pixels.length; i++) {
			color_sum[0] += getRed(pixels[i]);
			color_sum[1] += getGreen(pixels[i]);
			color_sum[2] += getBlue(pixels[i]);
		}

		return color_sum;
	}

	private int getUnsignedByte(byte b) {
		return b & 0xFF;
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

		BufferedImage[] images = load_images(imageFiles);

		// assert Image Dimensions
		for (int i = 0; i < images.length-1; i++) {
			assert(images[i].getWidth() == images[i+1].getWidth());
			assert(images[i].getHeight() == images[i+1].getHeight());
		}

		int num_pixels = images[0].getWidth() * images[0].getHeight();
		int num_bytes = num_pixels * 3;

		int image_width = images[0].getWidth();
		int image_height = images[0].getHeight();

		long[] image_sum = new long[num_bytes];

		for (BufferedImage image : images) {
			addImage(image_sum, image);
		}

		byte[] image_mean = new byte[num_bytes];
		for (int i = 0; i < num_bytes; i++) {
			image_mean[i] = (byte)(image_sum[i] / images.length);
		}

		Image img = byteArrayToImage(image_mean, image_width, image_height);

		return img;
	}

	private int getArgb(byte r, byte g, byte b) {
		int color = getUnsignedByte(b);
		color |= getUnsignedByte(g) << 8;
		color |= getUnsignedByte(r) << 16;
		color |= 0xFF << 24;
		return color;
	}

	private WritableImage byteArrayToImage(byte[] data, int width, int height) {
		WritableImage img = new WritableImage(width, height);
		PixelWriter pw = img.getPixelWriter();
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int index = (y*width + x)*3;
				pw.setArgb(x, y, getArgb(data[index], data[index+1], data[index+2]));
			}
		}

		return img;
	}

	private int getRed(int rgb) {
		return (rgb >> 16) & 0x000000FF;
	}

	private int getGreen(int rgb) {
		return (rgb >> 8) & 0x000000FF;
	}

	private int getBlue(int rgb) {
		return rgb & 0x000000FF;
	}

	private int[] getPixels(BufferedImage image) {
		return image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
	}

	private void addImage(long[] image_sum, BufferedImage image) {
		int[] pixels = getPixels(image);

		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				int index = y*image.getWidth()+x;
				image_sum[index*3+0] += getRed(pixels[index]);
				image_sum[index*3+1] += getGreen(pixels[index]);
				image_sum[index*3+2] += getBlue(pixels[index]);
			}
		}
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