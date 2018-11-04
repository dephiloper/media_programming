package de.htw.mp.ui.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

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
    public Color getMeanColor(Path... imageFiles) {

        // no images? return BLACK
        if (imageFiles.length == 0) return Color.BLACK;

        // Load images
        BufferedImage[] images = loadImages(imageFiles);

        long[] meanColor = new long[3];
        int numPixels = 0;

        for (BufferedImage img : images) {
            numPixels += img.getHeight() * img.getWidth();

            long[] colorSum = getColorSum(img);
            for (int i = 0; i < 3; i++) {
                meanColor[i] += colorSum[i];
            }
        }

        for (int i = 0; i < 3; i++) {
            meanColor[i] = meanColor[i] / numPixels;
        }

        return Color.rgb((int) meanColor[0], (int) meanColor[1], (int) meanColor[2]);
    }

    /**
     * TODO Calculate the mean image of all given images. Or return NULL if there are no images.
     *
     * @param imageFiles
     * @return
     */
    public Image getMeanImage(Path... imageFiles) {

        // no images? return null
        if (imageFiles.length == 0) return null;

        BufferedImage[] images = loadImages(imageFiles);

        // assert Image Dimensions
        for (int i = 0; i < images.length - 1; i++) {
            assert (images[i].getWidth() == images[i + 1].getWidth());
            assert (images[i].getHeight() == images[i + 1].getHeight());
        }

        int numPixels = images[0].getWidth() * images[0].getHeight();
        int numBytes = numPixels * 3;

        int imageWidth = images[0].getWidth();
        int imageHeight = images[0].getHeight();

        long[] imageSum = new long[numBytes];

        for (BufferedImage image : images) {
            addImage(imageSum, image);
        }

        byte[] imageMean = new byte[numBytes];
        for (int i = 0; i < numBytes; i++) {
            imageMean[i] = (byte) (imageSum[i] / images.length);
        }

        return byteArrayToImage(imageMean, imageWidth, imageHeight);
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
        double[] distances = new double[database.length];

        switch (featureType) {
            case MeanColor:
                Color queryCol = query.getMeanColor();

                for (int i = 0; i < database.length; i++)
                    distances[i] = calculateDistance(database[i].getMeanColor(), queryCol);
                break;

            case MeanImage:
                Image queryImg = query.getMeanImage();

                for (int i = 0; i < database.length; i++)
                    distances[i] = calculateDistance(database[i].getMeanImage(), queryImg);
                break;
        }

        Map<Double, FeatureContainer> map = new TreeMap<>();
        for (int i = 0; i < distances.length; ++i) {
            map.put(distances[i], database[i]);
        }

        return new ArrayList<>(map.values());
    }

    /**
     * TODO Predict the category.
     * Make the prediction based on the sorted list of features (images or categories).
     * Investigate the first k best {@link FeatureContainer} and count which category occurs the most.
     *
     * @param sortedList
     * @return predicted category
     */
    @Override
    public String classify(List<FeatureContainer> sortedList, int k) {
        TreeMap<String, Integer> map = new TreeMap<>();

        for (int i = 0; i < k; i++) {
            String category = sortedList.get(i).getCategory();
            map.merge(category, 1, (a, b) -> a + b);
        }

        return map.firstKey();
    }

    /**
     * TODO Evaluate the overall performance of the system.
     * Predict for every query the category and compare the result against the real category
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

        AtomicInteger sum = new AtomicInteger(0);
        Arrays.stream(database).parallel().forEach(query -> {
            List<FeatureContainer> retrievedFeatures = retrieve(query, database, featureType);
            String result = classify(retrievedFeatures, k);
            sum.set(sum.intValue() + (result.equals(query.getCategory()) ? 1 : 0));
        });

        System.out.println((float) sum.intValue() / queries.length);
        return (float)sum.intValue() / queries.length;
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
                int index = (y * width + x) * 3;
                pw.setArgb(x, y, getArgb(data[index], data[index + 1], data[index + 2]));
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

    private void addImage(long[] imageSum, BufferedImage image) {
        int[] pixels = getPixels(image);

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int index = y * image.getWidth() + x;
                imageSum[index * 3] += getRed(pixels[index]);
                imageSum[index * 3 + 1] += getGreen(pixels[index]);
                imageSum[index * 3 + 2] += getBlue(pixels[index]);
            }
        }
    }

    private BufferedImage[] loadImages(Path[] paths) {
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

        long[] colorSum = new long[3];
        for (int pixel : pixels) {
            colorSum[0] += getRed(pixel);
            colorSum[1] += getGreen(pixel);
            colorSum[2] += getBlue(pixel);
        }

        return colorSum;
    }

    private int getUnsignedByte(byte b) {
        return b & 0xFF;
    }

    private double calculateDistance(Color c1, Color c2) {
        return Math.abs(c1.getRed() - c2.getRed()) +
                Math.abs(c1.getGreen() - c2.getGreen()) +
                Math.abs(c1.getBlue() - c2.getBlue());
    }

    private double calculateDistance(Image i1, Image i2) {
        BufferedImage b1 = SwingFXUtils.fromFXImage(i1, null);
        BufferedImage b2 = SwingFXUtils.fromFXImage(i2, null);

        int[] p1 = getPixels(b1);
        int[] p2 = getPixels(b2);

        double distance = 0;

        for (int i = 0; i < p1.length; i++) {
            distance += Math.abs(getRed(p1[i]) - getRed(p2[i])) +
                    Math.abs(getGreen(p1[i]) - getGreen(p2[i])) +
                    Math.abs(getBlue(p1[i]) - getBlue(p2[i]));
        }

        return distance;
    }

}