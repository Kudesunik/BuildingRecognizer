package ru.mai.buildingrecognizer.recognition;

import org.opencv.calib3d.Calib3d;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.DMatch;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;

import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;

import static ru.mai.buildingrecognizer.util.Util.*;

import java.awt.Polygon;
import java.awt.image.BufferedImage;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ru.mai.buildingrecognizer.draw.BuildingPolygon;

import ru.mai.buildingrecognizer.entities.BuildingDatabase;
import ru.mai.buildingrecognizer.entities.BuildingImage;
import ru.mai.buildingrecognizer.entities.KeypointsImage;

public class KeypointsRecognizer implements IRecognizer {
	
	private final int ROI_REGION_LIMIT = 4;
	
	private final FeatureDetector featureDetector;
	private final DescriptorExtractor descriptorExtractor;
	private final DescriptorMatcher descriptorMatcher;
	
	private BuildingDatabase buildingDatabase;
	
	public KeypointsRecognizer() {
		featureDetector = FeatureDetector.create(FeatureDetector.SIFT);
		descriptorExtractor = DescriptorExtractor.create(DescriptorExtractor.SIFT);
		descriptorMatcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE);
	}
	
	public void setBuildingDatabase(BuildingDatabase buildingDatabase) {
		this.buildingDatabase = buildingDatabase;
	}
	
	@Override
	public BuildingImage recognize(BufferedImage initialImage) {
		
		List<BuildingImage> cachedDatabaseImages = new LinkedList<>();
		
		for(BuildingImage buildingImage : buildingDatabase.getBuildingImages()) {
			cachedDatabaseImages.add(buildingImage);
		}
		
		long startTime = System.nanoTime();
		
		Mat image = bufferedImageToMat(initialImage);
		
		Map<String, Mat> roi = getRegionsOfInterest(image);
		
		BuildingImage sceneImage = null;
		
		BuildingImage finalBuilding = new BuildingImage(null, initialImage);
		
		for(BuildingImage buildingImage : cachedDatabaseImages) {
			
			KeypointsImage databaseImage = getKeypointsImage(buildingImage);
			
			sceneImage = new BuildingImage(null, matToBufferedImage(databaseImage.image));
			
			for(String roiName : roi.keySet()) {
				
				KeypointsImage keypointsImage = getKeypointsImage(matToBufferedImage(roi.get(roiName)));
				
				List<Point> points = getMatches(databaseImage, keypointsImage);
				
				if((points == null) || points.isEmpty()) {
					continue;
				}
				
				BuildingPolygon sideCheckRectangle = new BuildingPolygon();
					
				double centerX = ((points.get(0).x + points.get(1).x + points.get(2).x + points.get(3).x) / 4);
				double centerY = ((points.get(0).y + points.get(1).y + points.get(2).y + points.get(3).y) / 4);
				
				for(int i = 0; i < 4; i++) {
					points.get(i).x -= (centerX - points.get(i).x) * (ROI_REGION_LIMIT - 1);
					points.get(i).y -= (centerY - points.get(i).y) * (ROI_REGION_LIMIT - 1);
				}
				
				int[] shift = getRegionShift(roiName, points);
				
				for(int i = 0; i < 4; i++) {
					points.get(i).x += shift[0];
					points.get(i).y += shift[1];
				}
				
				addPointsTo(sideCheckRectangle, points);
				
				sceneImage.addBuilding(sideCheckRectangle);
			}
			
			if(sceneImage.getBuildings().size() < 3) {
				sceneImage.removeBuildings();
				continue;
			}
			
			System.out.println("Regions found:  " + sceneImage.getBuildings().size());
			
			BuildingPolygon interpolatedPolygon = interpolateCoordinates(sceneImage.getBuildings());
			
			List<Point> list = new LinkedList<>();
			
			list.add(new Point(0, 0));
			list.add(new Point(initialImage.getWidth(), 0));
			list.add(new Point(initialImage.getWidth(), initialImage.getHeight()));
			list.add(new Point(0, initialImage.getHeight()));
			
			Mat homography = getHomography(list, interpolatedPolygon.getOpenCVPointsList());
			
			for(BuildingPolygon buildingPolygon : buildingImage.getBuildings()) {
				
				List<Point> transformedBuildingPointsList = transformPoints(homography, buildingPolygon.getOpenCVPointsList());
				
				BuildingPolygon transformedBuilding = new BuildingPolygon();
				
				for(int i = 0; i < transformedBuildingPointsList.size(); i++) {
					
					Point p = transformedBuildingPointsList.get(i);
					
					transformedBuilding.addPoint(i, Math.toIntExact(Math.round(p.x)), Math.toIntExact(Math.round(p.y)));
				}
				
				finalBuilding.addBuilding(transformedBuilding);
			}
		}
		
		System.out.println("Estimated time (ms): " + ((System.nanoTime() - startTime) / 1000000.0));
		
		return finalBuilding;
	}
	
	private BuildingPolygon interpolateCoordinates(List<BuildingPolygon> buildingAreaPolygons) {
		
		int areaNumber = buildingAreaPolygons.size();
		
		Polygon p = new Polygon(new int[4], new int[4], 4);
		
		BuildingPolygon result = new BuildingPolygon();
		
		for(BuildingPolygon bp : buildingAreaPolygons) {

			Polygon polygon = bp.getPolygon();
			
			for(int i = 0; i < 4; i++) {
				p.xpoints[i] += polygon.xpoints[i];
				p.ypoints[i] += polygon.ypoints[i];
			}
		}
		
		for(int i = 0; i < 4; i++) {
			p.xpoints[i] /= areaNumber;
			p.ypoints[i] /= areaNumber;
		}
		
		for(int i = 0; i < 4; i++) {
			result.addPoint(i, p.xpoints[i], p.ypoints[i]);
		}
		
		return result;
	}
	
	private List<Point> getMatches(KeypointsImage databaseImage, KeypointsImage sourceImage) {
		
		List<MatOfDMatch> matches = new LinkedList<MatOfDMatch>();
		
		descriptorMatcher.knnMatch(sourceImage.descriptors, databaseImage.descriptors, matches, 2);
		
		float nndrRatio = 0.75f;
		
		LinkedList<DMatch> goodMatchesList = new LinkedList<DMatch>();
		
		for(int i = 0; i < matches.size(); i++) {
			
			MatOfDMatch matofDMatch = matches.get(i);
			
			DMatch[] dmatcharray = matofDMatch.toArray();
			
			DMatch m1 = dmatcharray[0];
			DMatch m2 = dmatcharray[1];
			
			if(m1.distance <= (m2.distance * nndrRatio)) {
				goodMatchesList.addLast(m1);
			}
		}
		
		if(goodMatchesList.size() >= 7) {
			
			LinkedList<Point> objectPoints = new LinkedList<>();
			LinkedList<Point> scenePoints = new LinkedList<>();
			
			for(int i = 0; i < goodMatchesList.size(); i++) {
				objectPoints.addLast(sourceImage.keyPoints.get(goodMatchesList.get(i).queryIdx).pt);
				scenePoints.addLast(databaseImage.keyPoints.get(goodMatchesList.get(i).trainIdx).pt);
			}
			
			return transformScene(getHomography(scenePoints, objectPoints), sourceImage.image.cols(), sourceImage.image.rows());
		}
		
		return null;
	}
	
	List<Point> transformScene(Mat homography, int sceneWidth, int sceneHeight) {
		
		List<Point> pointList = new LinkedList<>();
		
		pointList.add(new Point(0, 0));
		pointList.add(new Point(sceneWidth, 0));
		pointList.add(new Point(sceneWidth, sceneHeight));
		pointList.add(new Point(0, sceneHeight));
		
		return transformPoints(homography, pointList);
	}
	
	private List<Point> transformPoints(Mat homography, List<Point> pointsList) {
		
		List<Point> result = new LinkedList<>();
		
		Mat objectCorners = new Mat(pointsList.size(), 1, CvType.CV_32FC2);
		Mat sceneCorners = new Mat(pointsList.size(), 1, CvType.CV_32FC2);
		
		for(int i = 0; i < pointsList.size(); i++) {
			objectCorners.put(i, 0, new double[]{pointsList.get(i).x, pointsList.get(i).y});
		}
		
		Core.perspectiveTransform(objectCorners, sceneCorners, homography);
		
		for(int i = 0; i < pointsList.size(); i++) {
			result.add(new Point(sceneCorners.get(i, 0)));
		}
		
		return result;
	}
	
	private Mat getHomography(List<Point> scenePoints, List<Point> objectPoints) {
		
		MatOfPoint2f objectMatOfPoint = new MatOfPoint2f();
		objectMatOfPoint.fromList(objectPoints);
		
		MatOfPoint2f sceneMatOfPoint = new MatOfPoint2f();
		sceneMatOfPoint.fromList(scenePoints);
		
		return Calib3d.findHomography(objectMatOfPoint, sceneMatOfPoint, Calib3d.RANSAC, 3);
	}
	
	private KeypointsImage getKeypointsImage(BufferedImage buildingImage) {
		return getKeypointsImage(new BuildingImage(null, buildingImage));
	}
	
	private KeypointsImage getKeypointsImage(BuildingImage buildingImage) {
		
		BufferedImage image = buildingImage.getImage();
		
		Mat matImage = bufferedImageToMat(image);
		
		//Mat grayscaledImage = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC1);
		
		//Imgproc.cvtColor(matImage, grayscaledImage, Imgproc.COLOR_BGR2GRAY);
		
		MatOfKeyPoint objectKeyPoints = new MatOfKeyPoint();
		MatOfKeyPoint objectDescriptors = new MatOfKeyPoint();
		
		featureDetector.detect(matImage, objectKeyPoints);
		descriptorExtractor.compute(matImage, objectKeyPoints, objectDescriptors);
		
		return new KeypointsImage(buildingImage.getImageName(), matImage, objectKeyPoints.toList(), objectDescriptors);
	}
	
	private void addPointsTo(BuildingPolygon buildingRectangle, List<Point> points) {
		
		for(int i = 0; i < points.size(); i++) {
			
			Point p = points.get(i);
			
			buildingRectangle.addPoint(i, Math.toIntExact(Math.round(p.x)), Math.toIntExact(Math.round(p.y)));
		}
	}
	
	private Map<String, Mat> getRegionsOfInterest(Mat image) {
		
		Map<String, Mat> map = new HashMap<>();
		
		int width = image.cols();
		int height = image.rows();
		
		int roiWidth = (width / ROI_REGION_LIMIT);
		int roiHeight = (height / ROI_REGION_LIMIT);
		
		map.put("Central", new Mat(image, new Rect((width / 2) - (roiWidth / 2), (height / 2) - (roiHeight / 2), roiWidth, roiHeight)));
		
		map.put("LeftTop", new Mat(image, new Rect(0, 0, roiWidth, roiHeight)));
		map.put("RightTop", new Mat(image, new Rect((width - roiWidth), 0, roiWidth, roiHeight)));
		
		map.put("LeftBottom", new Mat(image, new Rect(0, (height - roiHeight), roiWidth, roiHeight)));
		map.put("RightBottom", new Mat(image, new Rect((width - roiWidth), (height - roiHeight), roiWidth, roiHeight)));
		
		/**
		
		Imgproc.rectangle(image, new Point((width / 2) - (roiWidth / 2), (height / 2) - (roiHeight / 2)), new Point((width / 2) - (roiWidth / 2) + roiWidth, (height / 2) - (roiHeight / 2) + roiHeight), new Scalar(0, 255, 0), 2);
		Imgproc.rectangle(image, new Point(0, 0), new Point(roiWidth, roiHeight), new Scalar(0, 255, 0), 2);
		Imgproc.rectangle(image, new Point(0, (height - roiHeight)), new Point(roiWidth, height - roiHeight + roiHeight), new Scalar(0, 255, 0), 2);
		Imgproc.rectangle(image, new Point((width - roiWidth), 0), new Point((width - roiWidth) + roiWidth, 0 + roiHeight), new Scalar(0, 255, 0), 2);
		Imgproc.rectangle(image, new Point((width - roiWidth), (height - roiHeight)), new Point((width - roiWidth) + roiWidth, height - roiHeight + roiHeight), new Scalar(0, 255, 0), 2);
		
		try {
			ImageIO.write(matToBufferedImage(image), "png", new File("middle.png"));
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		*/
		
		return map;
	}
	
	private int[] getRegionShift(String roiName, List<Point> list) {
		
		int[] shift = new int[2];
		
		int centerX = 0;
		int centerY = 0;
		
		for(int i = 0; i < 4; i++) {
			centerX += list.get(i).x;
			centerY += list.get(i).y;
		}
		
		centerX /= 4;
		centerY /= 4;
		
		double multiplier = (1.0 - 1.0 / ROI_REGION_LIMIT);
		
		switch(roiName) {
			case "LeftTop":
				shift[0] = Math.toIntExact(Math.round((list.get(2).x - centerX) * multiplier));
				shift[1] = Math.toIntExact(Math.round((list.get(2).y - centerY) * multiplier));
				break;
			case "RightTop":
				shift[0] = Math.toIntExact(Math.round((list.get(3).x - centerX) * multiplier));
				shift[1] = Math.toIntExact(Math.round((list.get(3).y - centerY) * multiplier));
				break;
			case "LeftBottom":
				shift[0] = Math.toIntExact(Math.round((list.get(1).x - centerX) * multiplier));
				shift[1] = Math.toIntExact(Math.round((list.get(1).y - centerY) * multiplier));
				break;
			case "RightBottom":
				shift[0] = Math.toIntExact(Math.round((list.get(0).x - centerX) * multiplier));
				shift[1] = Math.toIntExact(Math.round((list.get(0).y - centerY) * multiplier));
				break;
				
		}
		
		return shift;
	}
	
	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}
}
