package ru.mai.buildingrecognizer.util;

import java.awt.Polygon;
import java.awt.image.BufferedImage;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.util.List;

import javax.imageio.ImageIO;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import ru.mai.buildingrecognizer.draw.BuildingPolygon;
import ru.mai.buildingrecognizer.entities.BuildingDatabase;
import ru.mai.buildingrecognizer.entities.BuildingImage;

public class Database {
	
	private final JSONParser jsonParser;

    public Database() {
    	jsonParser = new JSONParser();
    }
    
    public void parseDatabase(File file, BuildingDatabase buildingDatabase) {
    	
    	JSONObject jsonDatabase = null;
    	
    	try {
    		jsonDatabase = (JSONObject)jsonParser.parse(new FileReader(file));
        } catch (IOException | ParseException e) {
        	System.err.println("[Error] Database parsing failed: " + e.getLocalizedMessage());
            e.printStackTrace();
            return;
        }
    	
    	Long imageCounter = (Long)jsonDatabase.get("ImageCount");
    	
    	if(imageCounter == null) {
    		return;
    	}
    	
    	for(int i = 0; i < imageCounter; i++) {
    		
    		JSONObject image = (JSONObject)jsonDatabase.get("Image" + i);
    		
    		String imageName = (String)image.get("ImageName");
    		
    		BufferedImage bufferedImage = null;
    		
			try {
				bufferedImage = ImageIO.read(new File(file.getPath().replace(file.getName(), "") + imageName + ".png"));
			} catch(IOException e) {
				System.err.println("[Error] Database parsing failed: " + e.getLocalizedMessage());
	            e.printStackTrace();
	            return;
			}
    		
			BuildingImage buildingImage = new BuildingImage(imageName, bufferedImage);
    		
    		Long buildingCount = (Long)image.get("BuildingCount");
    		
    		for(int i2 = 0; i2 < buildingCount; i2++) {
    			
    			JSONObject building = (JSONObject)image.get("Building" + i2);
    			
    			BuildingPolygon buildingRectangle = new BuildingPolygon();
    			
    			Long pointsCount = (Long)building.get("PointsCount");
    			
    			for(int i3 = 0; i3 < pointsCount; i3++) {
    				
    				JSONObject point = (JSONObject)building.get("Point" + i3);
    				
    				buildingRectangle.addPoint(i3, Math.toIntExact(((Long)point.get("x"))), Math.toIntExact(((Long)point.get("y"))));
    			}
    			
    			buildingImage.addBuilding(buildingRectangle);
    		}
    		
    		buildingDatabase.addBuildingImage(buildingImage);
    	}
    }
    
    @SuppressWarnings("unchecked")
	public void writeDatabase(File file, BuildingDatabase buildingDatabase) {
    	
    	JSONObject jsonDatabaseObject = new JSONObject();
    	
    	List<BuildingImage> buildingImages = buildingDatabase.getBuildingImages();
    	
    	jsonDatabaseObject.put("ImageCount", buildingImages.size());
    	
    	for(int i = 0; i < buildingImages.size(); i++) {
    		
    		BuildingImage buildingImage = buildingImages.get(i);
    		
    		JSONObject buildingImageObject = new JSONObject();
    		
    		String imageName = buildingImage.getImageName();
    		
    		if((imageName == null) || imageName.isEmpty()) {
    			imageName = "Image" + i;
    		}
    		
    		buildingImageObject.put("ImageName", imageName);
    		
    		List<BuildingPolygon> buildingsList = buildingImage.getBuildings();
    		
    		buildingImageObject.put("BuildingCount", buildingsList.size());
    		
    		try {
    			ImageIO.write(buildingImage.getImage(), "png", new File(imageName + ".png"));
			} catch(IOException e) {
				System.err.println("[Error] Database writing failed: " + e.getLocalizedMessage());
	            e.printStackTrace();
	            return;
			}
    		
    		for(int bn = 0; bn < buildingsList.size(); bn++) {
    			
    			BuildingPolygon building = buildingsList.get(bn);
    			
    			JSONObject buildingObject = new JSONObject();
    			
    			Polygon polygon = building.getPolygon();
    			
    			buildingObject.put("PointsCount", polygon.npoints);
    			
    			for(int n = 0; n < polygon.npoints; n++) {
    				
    				JSONObject pointObject = new JSONObject();
    				
    				pointObject.put("x", polygon.xpoints[n]);
    				pointObject.put("y", polygon.ypoints[n]);
    				
    				buildingObject.put("Point" + n, pointObject);
    			}
    			
    			buildingImageObject.put("Building" + bn, buildingObject);
    		}
    		
    		jsonDatabaseObject.put("Image" + i, buildingImageObject);
    	}
    	
    	try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(jsonDatabaseObject.toJSONString());
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
