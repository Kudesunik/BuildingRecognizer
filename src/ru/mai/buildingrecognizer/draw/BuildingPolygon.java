package ru.mai.buildingrecognizer.draw;

import java.awt.Point;
import java.awt.Polygon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BuildingPolygon {
	
	private final Map<Integer, Point> map;
	
	public BuildingPolygon() {
		map = new HashMap<>();
	}
	
	public void addPoint(int index, int x, int y) {
		map.put(index, new Point(x, y));
	}
	
	public BuildingPolygon getNormalizedRectangle(int x, int y) {
		
		BuildingPolygon normalized = new BuildingPolygon();
		
		int index = 0;
		
		for(Point p : map.values()) {
			
			index++;
			
			p.translate(-x, -y);
			
			normalized.addPoint(index, p.x, p.y);
			
			p.translate(x, y);
		}
		
		return normalized;
	}
	
	public List<org.opencv.core.Point> getOpenCVPointsList() {
		
		List<org.opencv.core.Point> result = new ArrayList<>();
		
		for(Point p : map.values()) {
			result.add(new org.opencv.core.Point(p.x, p.y));
		}
		
		return result;
	}
	
	public Polygon getPolygon() {
		
		Polygon polygon = new Polygon();
		
		for(Point p : map.values()) {
			polygon.addPoint(p.x, p.y);
		}
		
		return polygon;
	}
	
	public void setPolygon(Polygon polygon) {
		for(int i = 0; i < polygon.npoints; i++) {
			addPoint(i, polygon.xpoints[i], polygon.ypoints[i]);
		}
	}
}
