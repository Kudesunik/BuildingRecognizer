package ru.mai.buildingrecognizer.recognition;

import java.awt.Polygon;
import java.awt.image.BufferedImage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.LineSegmentDetector;

import ru.mai.buildingrecognizer.draw.BuildingPolygon;
import ru.mai.buildingrecognizer.entities.BuildingImage;
import ru.mai.buildingrecognizer.entities.Line;

import static ru.mai.buildingrecognizer.util.RecognitionMath.*;
import static ru.mai.buildingrecognizer.util.Util.*;

public class LinesRecognizer implements IRecognizer {
	
	private final LineSegmentDetector lineSegmentDetector;
	
	private final int perpendicularLineDistance = 10;
	private final int perpendicularLineLength = 30;
	private final int perpendicularLineMinAngle = 85;
	private final int perpendicularLineMaxAngle = 95;
	private final boolean checkBorder = true;
	
	public LinesRecognizer() {
		lineSegmentDetector = Imgproc.createLineSegmentDetector();
	}
	
	Mat grayscaledImage;
	
	@Override
	public BuildingImage recognize(BufferedImage initialImage) {
		
		long startTime = System.nanoTime();
		
		Mat image = bufferedImageToMat(initialImage);
		
		grayscaledImage = new Mat(image.height(), image.width(), CvType.CV_8UC1);
		
		Imgproc.cvtColor(image, grayscaledImage, Imgproc.COLOR_BGR2GRAY);
		
		Mat linesMat = new Mat();
		
		lineSegmentDetector.detect(grayscaledImage, linesMat);
		
		lineSegmentDetector.drawSegments(grayscaledImage, linesMat);
		
		List<Line> lines = getLinesList(linesMat);
		
		BuildingImage buildingImage = new BuildingImage(null, initialImage);
		
		for(int i = 0; i < 100; i++) {
			
			BuildingPolygon buildingPolygon = new BuildingPolygon();
			
			Polygon building = findNextBuilding(lines, i, 0);
			
			if(building != null) {
				buildingPolygon.setPolygon(building);
			}
			
			buildingImage.addBuilding(buildingPolygon);
		}
		
		System.out.println("Estimated time (ms): " + ((System.nanoTime() - startTime) / 1000000.0));
		
		return buildingImage;
	}
	
	public Polygon findNextBuilding(List<Line> lines, int i, int depth) {
		
		if(depth > 100) {
			return null;
		}
		
		Polygon buildingPolygon = new Polygon();
		
		Line longestLine = getLongestLine(lines);
		
		if(longestLine == null) {
			return null;
		}
		
		continueLine(lines, longestLine);
		
		Imgproc.line(grayscaledImage, longestLine.p1, longestLine.p2, new Scalar(0, 212, 0), 2);
		
		lines.remove(longestLine);
		
		Line perpendicularLine = getNearestPerpendicularLine(lines, longestLine, perpendicularLineDistance);
		
		if(perpendicularLine == null) {
			return findNextBuilding(lines, i, ++depth);
		}
		
		Map<Double, Point[]> distanceMap = new HashMap<>();
		
		distanceMap.put(getPointsDistance(longestLine.p1, perpendicularLine.p1), new Point[]{longestLine.p1, perpendicularLine.p1});
		distanceMap.put(getPointsDistance(longestLine.p1, perpendicularLine.p2), new Point[]{longestLine.p1, perpendicularLine.p2});
		distanceMap.put(getPointsDistance(longestLine.p2, perpendicularLine.p1), new Point[]{longestLine.p2, perpendicularLine.p1});
		distanceMap.put(getPointsDistance(longestLine.p2, perpendicularLine.p2), new Point[]{longestLine.p2, perpendicularLine.p2});
		
		Double minimumDistance = Collections.min(distanceMap.keySet());
		
		Point[] nodePoints = distanceMap.get(minimumDistance);
		
		Point longPoint = longestLine.p1;
		Point perpendicularPoint = perpendicularLine.p1;
		
		if(nodePoints[0].equals(longPoint)) {
			longPoint = longestLine.p2;
		}
		
		if(nodePoints[1].equals(perpendicularPoint)) {
			perpendicularPoint = perpendicularLine.p2;
		}
		
		Point nodePoint = getIntersectionPoint(longestLine, perpendicularLine);
		
		buildingPolygon.addPoint(StrictMath.toIntExact(Math.round(longPoint.x)), StrictMath.toIntExact(Math.round(longPoint.y)));
		buildingPolygon.addPoint(StrictMath.toIntExact(Math.round(nodePoint.x)), StrictMath.toIntExact(Math.round(nodePoint.y)));
		buildingPolygon.addPoint(StrictMath.toIntExact(Math.round(perpendicularPoint.x)), StrictMath.toIntExact(Math.round(perpendicularPoint.y)));
		
		Point lastPoint = findBuildingPointByOther(nodePoint, longPoint, perpendicularPoint);
		
		buildingPolygon.addPoint(StrictMath.toIntExact(Math.round(lastPoint.x)), StrictMath.toIntExact(Math.round(lastPoint.y)));
		
		if(!isBuildingBoundExists(lines, buildingPolygon) && checkBorder) {
			return findNextBuilding(lines, i, ++depth);
		}
		
		Polygon extendedPolygon = getExtendedPolygon(buildingPolygon);
		
		removeAllLinesAtPolygonArea(lines, extendedPolygon);
		
		return buildingPolygon;
	}
	
	private List<Line> getLinesList(Mat linesMat) {
		
		List<Line> linesList = new ArrayList<>();
		
		for(int i = 0; i < linesMat.rows(); i++) {
			
			double[] vector = linesMat.get(i, 0);
			
			Point p1 = new Point();
			Point p2 = new Point();
			
			p1.x = vector[0];
			p1.y = vector[1];
			p2.x = vector[2];
			p2.y = vector[3];
			
			linesList.add(new Line(p1, p2));
		}
		
		return linesList;
	}
	
	private Point findBuildingPointByOther(Point nodePoint, Point sidePoint1, Point sidePoint2) {
		
		Point resultPoint = new Point();
		
		resultPoint.x = (sidePoint1.x - nodePoint.x) + sidePoint2.x;
		resultPoint.y = (sidePoint1.y - nodePoint.y) + sidePoint2.y;
		
		return resultPoint;
	}
	
	private Line getLongestLine(List<Line> lines) {
		
		double length = 0;
		
		Line line = null;
		
		for(Line l : lines) {
			double bufferLength = getLineLength(l);
			if(bufferLength > length) {
				length = bufferLength;
				line = l;
			}
		}
		
		return line;
	}
	
	private Line getNearestPerpendicularLine(List<Line> lines, Line line, int maxDistance) {
		
		Line result = null;
		
		for(Iterator<Line> linesIterator = lines.iterator(); linesIterator.hasNext();) {
			
			Line l = linesIterator.next();
			
			if((getPointsDistance(line.p1, l.p1) < maxDistance) || (getPointsDistance(line.p1, l.p2) < maxDistance) || (getPointsDistance(line.p2, l.p1) < maxDistance) || (getPointsDistance(line.p2, l.p2) < maxDistance)) {
				if((getAngle(line, l) >= perpendicularLineMinAngle) && (getAngle(line, l) <= perpendicularLineMaxAngle) && (getLineLength(l) > perpendicularLineLength)) {
					if(result == null || (getLineLength(result) < getLineLength(l))) {
						result = l;
						linesIterator.remove();
					}
				}
			}
		}
		
		return result;
	}
	
	private Line continueLine(List<Line> lines, Line line) {
		
		Line lineToRemove = null;
		
		boolean flag = false;
		
		for(Line l : lines) {
			
			Point closestBaseLinePoint = null;
			Point furtherContinueLinePoint = null;
			
			if((getPointsDistance(line.p1, l.p1) < 10) && (getPointsDistance(line.p2, l.p1) < getPointsDistance(line.p2, l.p2))) {
				closestBaseLinePoint = line.p1;
				furtherContinueLinePoint = l.p2;
			}
			
			if((getPointsDistance(line.p2, l.p1) < 10) && (getPointsDistance(line.p1, l.p1) < getPointsDistance(line.p1, l.p2))) {
				closestBaseLinePoint = line.p2;
				furtherContinueLinePoint = l.p2;
			}
			
			if((getPointsDistance(line.p1, l.p2) < 10) && (getPointsDistance(line.p2, l.p2) < getPointsDistance(line.p2, l.p1))) {
				closestBaseLinePoint = line.p1;
				furtherContinueLinePoint = l.p1;
			}
			
			if((getPointsDistance(line.p2, l.p2) < 10) && (getPointsDistance(line.p1, l.p2) < getPointsDistance(line.p1, l.p1))) {
				closestBaseLinePoint = line.p2;
				furtherContinueLinePoint = l.p1;
			}
			
			if((closestBaseLinePoint != null) && (furtherContinueLinePoint != null)) {
				
				if(((getAngle(line, l) >= 0) && (getAngle(line, l) < 4)) || ((getAngle(line, l) > 176) && (getAngle(line, l) <= 180))) {
					
					closestBaseLinePoint.x = furtherContinueLinePoint.x;
					closestBaseLinePoint.y = furtherContinueLinePoint.y;
					
					lineToRemove = l;
					
					flag = true;
					
					break;
				}
			}
		}
		
		lines.remove(lineToRemove);
		
		if(flag) {
			continueLine(lines, line);
		}
		
		return line;
	}
	
	private Polygon getExtendedPolygon(Polygon polygon) {
		
		Polygon clonedPolygon = new Polygon(polygon.xpoints.clone(), polygon.ypoints.clone(), polygon.npoints);
		
		double centerX = 0;
		double centerY = 0;
		
		for(int i = 0; i < clonedPolygon.npoints; i++) {
			centerX += clonedPolygon.xpoints[i];
			centerY += clonedPolygon.ypoints[i];
		}
		
		centerX /= clonedPolygon.npoints;
		centerY /= clonedPolygon.npoints;
		
		for(int i = 0; i < clonedPolygon.npoints; i++) {
			clonedPolygon.xpoints[i] += (clonedPolygon.xpoints[i] - centerX) / 5;
			clonedPolygon.ypoints[i] += (clonedPolygon.ypoints[i] - centerY) / 5;
		}
		
		return clonedPolygon;
	}
	
	private boolean isBuildingBoundExists(List<Line> lines, Polygon polygon) {
		
		Polygon clonedPolygon = new Polygon(polygon.xpoints.clone(), polygon.ypoints.clone(), polygon.npoints);
		
		double lineCentralPointX = (clonedPolygon.xpoints[2] + clonedPolygon.xpoints[3]) / 2.0;
		double lineCentralPointY = (clonedPolygon.ypoints[2] + clonedPolygon.ypoints[3]) / 2.0;
		
		double lineSizePointX1 = (clonedPolygon.xpoints[3] + clonedPolygon.xpoints[0]) / 2.0;
		double lineSizePointY1 = (clonedPolygon.ypoints[3] + clonedPolygon.ypoints[0]) / 2.0;
		
		double lineSizePointX2 = (clonedPolygon.xpoints[2] + clonedPolygon.xpoints[1]) / 2.0;
		double lineSizePointY2 = (clonedPolygon.ypoints[2] + clonedPolygon.ypoints[1]) / 2.0;
		
		double[] polygonCenter = getPolygonMassCenter(clonedPolygon);
		
		for(int i = 0; i < polygon.npoints; i++) {
			clonedPolygon.xpoints[i] += (lineCentralPointX - polygonCenter[0]);
			clonedPolygon.ypoints[i] += (lineCentralPointY - polygonCenter[1]);
		}
		
		clonedPolygon.xpoints[0] -= (polygon.xpoints[0] - lineSizePointX1) / 2.0;
		clonedPolygon.ypoints[0] -= (polygon.ypoints[0] - lineSizePointY1) / 2.0;
		
		clonedPolygon.xpoints[3] -= (polygon.xpoints[3] - lineSizePointX1) / 2.0;
		clonedPolygon.ypoints[3] -= (polygon.ypoints[3] - lineSizePointY1) / 2.0;
		
		clonedPolygon.xpoints[2] -= (polygon.xpoints[2] - lineSizePointX2) / 2.0;
		clonedPolygon.ypoints[2] -= (polygon.ypoints[2] - lineSizePointY2) / 2.0;
		
		clonedPolygon.xpoints[1] -= (polygon.xpoints[1] - lineSizePointX2) / 2.0;
		clonedPolygon.ypoints[1] -= (polygon.ypoints[1] - lineSizePointY2) / 2.0;
		
		double totalLength = 0;
		
		for(Line l : lines) {
			
			if(clonedPolygon.contains(l.p1.x, l.p1.y) && clonedPolygon.contains(l.p2.x, l.p2.y)) {
				totalLength += getLineLength(l);
			}
		}
		
		if((totalLength * 2.0) > getLineLength(new Line(new Point(clonedPolygon.xpoints[0], clonedPolygon.ypoints[0]), new Point(clonedPolygon.xpoints[1], clonedPolygon.ypoints[1])))) {
			return true;
		}
		
		return false;
	}
	
	private void removeAllLinesAtPolygonArea(List<Line> lines, Polygon polygon) {
		
		for(Iterator<Line> linesIterator = lines.iterator(); linesIterator.hasNext();) {
			
			Line l = linesIterator.next();
			
			if(polygon.contains(l.p1.x, l.p1.y) || polygon.contains(l.p2.x, l.p2.y)) {
				linesIterator.remove();
			}
		}
	}
	
	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}
}
