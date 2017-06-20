package ru.mai.buildingrecognizer.entities;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ru.mai.buildingrecognizer.util.Database;

public class BuildingDatabase {
	
	private final List<BuildingImage> buildingImages;
	
	private final Database database;
	
	public BuildingDatabase(Database database) {
		this.database = database;
		buildingImages = new ArrayList<>();
	}
	
	public List<BuildingImage> getBuildingImages() {
		return buildingImages;
	}
	
	public void loadBuildingImages(File file) {
		database.parseDatabase(file, this);
	}
	
	public void addBuildingImage(BuildingImage image) {
		buildingImages.add(image);
	}
	
	public void saveBuildingImages(File file) {
		database.writeDatabase(file, this);
	}
}
