package ru.mai.buildingrecognizer.recognition;

import java.awt.image.BufferedImage;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.Test;

import ru.mai.buildingrecognizer.entities.BuildingImage;
import ru.mai.buildingrecognizer.util.Util;

public class LinesRecognizerTest {
	
	private final LinesRecognizer linesRecognizer;
	
	public LinesRecognizerTest() throws IOException {
		linesRecognizer = new LinesRecognizer();
	}
	
	@Test
	public void test() throws Exception {
		
		BufferedImage image = ImageIO.read(new File("e:/Projects/Institute/BuildingRecognizer/resources/keypoints/input/database/LineRecognizer/Image0.png"));
		
		BuildingImage resultBuildingImage = linesRecognizer.recognize(image);
		
		BufferedImage bi = Util.convertBuildingImageToBufferedImage(resultBuildingImage);
		
		ImageIO.write(bi, "png", new File("LineRecognizerResult.png"));
	}
}
