package ru.mai.buildingrecognizer.recognition;

import java.awt.image.BufferedImage;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.Test;

import ru.mai.buildingrecognizer.entities.BuildingDatabase;
import ru.mai.buildingrecognizer.entities.BuildingImage;
import ru.mai.buildingrecognizer.util.Database;
import ru.mai.buildingrecognizer.util.Util;

public class KeypointsRecognizerTest {
	
	private final KeypointsRecognizer keypointsRecognizer;
	
	public KeypointsRecognizerTest() throws IOException {
		
		Database database = new Database();
		
		BuildingDatabase buildingDatabase = new BuildingDatabase(database);
		
		buildingDatabase.loadBuildingImages(new File("db.rb"));
		
		keypointsRecognizer = new KeypointsRecognizer();
		
		keypointsRecognizer.setBuildingDatabase(buildingDatabase);
	}
	
	@Test
	public void test() throws Exception {
		
		BufferedImage image = ImageIO.read(new File("resources/keypoints/input/example.png"));
		
		BuildingImage resultBuildingImage = keypointsRecognizer.recognize(image);
		
		BufferedImage bi = Util.convertBuildingImageToBufferedImage(resultBuildingImage);
		
		ImageIO.write(bi, "png", new File("KeypointsResult.png"));
	}
}
