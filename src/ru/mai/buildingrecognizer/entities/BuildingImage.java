package ru.mai.buildingrecognizer.entities;

import java.awt.image.BufferedImage;

import java.util.ArrayList;
import java.util.List;

import ru.mai.buildingrecognizer.draw.BuildingPolygon;

public class BuildingImage {
	
	private final String imageName;
	private final BufferedImage image;
	private final List<BuildingPolygon> buildings;
	
	public BuildingImage(String imageName, BufferedImage image) {
		this.imageName = imageName;
		this.image = image;
		this.buildings = new ArrayList<>();
	}
	
	public String getImageName() {
		return imageName;
	}
	
	public BufferedImage getImage() {
		return image;
	}
	
	public void addBuilding(BuildingPolygon building) {
		buildings.add(building);
	}
	
	public List<BuildingPolygon> getBuildings() {
		return buildings;
	}
	
	public void removeBuildings() {
		buildings.clear();
	}
}
