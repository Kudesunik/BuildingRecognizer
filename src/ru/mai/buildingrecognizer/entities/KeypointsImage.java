package ru.mai.buildingrecognizer.entities;

import java.util.List;

import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;

public class KeypointsImage {
	
	public final String imageName;
	
	public final Mat image;
	
	public final List<KeyPoint> keyPoints;
	public final Mat descriptors;
	
	public KeypointsImage(String imageName, Mat image, List<KeyPoint> keyPoints, Mat descriptors) {
		this.imageName = imageName;
		this.image = image;
		this.keyPoints = keyPoints;
		this.descriptors = descriptors;
	}
}
