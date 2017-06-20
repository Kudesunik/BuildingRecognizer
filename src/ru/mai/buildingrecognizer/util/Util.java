package ru.mai.buildingrecognizer.util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import java.util.List;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

import ru.mai.buildingrecognizer.draw.BuildingPolygon;
import ru.mai.buildingrecognizer.entities.BuildingImage;

public class Util {
	
	public static Mat bufferedImageToMat(BufferedImage image) {
		
		Mat result = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3);
		
		byte[] data = new byte[image.getWidth() * image.getHeight() * (int)result.elemSize()];
		
		int[] dataBuffer = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
		
		for (int i = 0; i < dataBuffer.length; i++) {
			data[i * 3] = (byte)((dataBuffer[i]));
			data[i * 3 + 1] = (byte)((dataBuffer[i]));
			data[i * 3 + 2] = (byte)((dataBuffer[i]));
		}
		
		result.put(0, 0, data);
		
		return result;
	}
	
	public static BufferedImage matToBufferedImage(Mat image) {
		
		int cols = image.cols();
		int rows = image.rows();
		
		int elemSize = (int) image.elemSize();
		
		byte[] data = new byte[cols * rows * elemSize];
		
		int type;
		
		image.get(0, 0, data);
		
		switch(image.channels()) {
		case 1:
			type = BufferedImage.TYPE_BYTE_GRAY;
			break;
		case 3:
			type = BufferedImage.TYPE_3BYTE_BGR;
			
			byte b;
			
			for(int i = 0; i < data.length; i = i + 3) {
				b = data[i];
				data[i] = data[i + 2];
				data[i + 2] = b;
			}
			
			break;
		default:
			return null;
		}
		
		BufferedImage resultImage = new BufferedImage(cols, rows, type);
		
		resultImage.getRaster().setDataElements(0, 0, cols, rows, data);
		
		return resultImage;
	}
	
	public static BufferedImage convertBuildingImageToBufferedImage(BuildingImage image) {
		
		BufferedImage resultImage = image.getImage();
		
		BufferedImage colorImage = new BufferedImage(resultImage.getWidth(), resultImage.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
		
		Graphics2D g2d = (Graphics2D) colorImage.createGraphics();
		
		g2d.drawImage(resultImage, 0, 0, null);
		
		g2d.setColor(Color.GREEN);
		
		g2d.setStroke(new BasicStroke(2));
		
		List<BuildingPolygon> buildingRectangles = image.getBuildings();
		
		for(BuildingPolygon rectangle : buildingRectangles) {
			g2d.drawPolygon(rectangle.getPolygon());
		}
		
		g2d.dispose();
		
		return colorImage;
	}
}
