package ru.mai.buildingrecognizer.draw;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.image.BufferedImage;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import ru.mai.buildingrecognizer.entities.BuildingImage;

@SuppressWarnings("serial")
public class DrawPanel extends JPanel {

	private final List<BuildingPolygon> rectanglesList;
	
	private BuildingPolygon activeRectangle;
	
	private int currentPoint = 0;
	
	private Image realImage = null;
	private Image resizedImage = null;
	
	public DrawPanel() {
		rectanglesList = new ArrayList<>();
		activeRectangle = new BuildingPolygon();
	}
	
	public void addPoint(int x, int y) {
		
		currentPoint++;
		
		activeRectangle.addPoint(currentPoint, x, y);
		
		this.repaint();
	}
	
	public void saveBuildingRectangle() {
		
		rectanglesList.add(activeRectangle);
		
		currentPoint = 0;
		
		activeRectangle = new BuildingPolygon();
		
		this.repaint();
	}
	
	@Override
	public void paintComponent(Graphics g) {
		
		Graphics2D g2d = (Graphics2D)g;
		
		super.paintComponent(g2d);
		
		g2d.setColor(Color.GREEN);
		
		Stroke oldStroke = g2d.getStroke();
		
		g2d.setStroke(new BasicStroke(2));
		
		if(resizedImage != null) {
			
			int x = this.getWidth() / 2 - resizedImage.getWidth(null) / 2;
			int y = this.getHeight() / 2 - resizedImage.getHeight(null) / 2;
			
			g.drawImage(resizedImage, x, y, resizedImage.getWidth(null), resizedImage.getHeight(null), null);
		}
		
		if(activeRectangle != null) {
			
			Polygon activePolygon = activeRectangle.getPolygon();
			
			for(int i = 0; i < activePolygon.npoints; i++) {
				g.drawRect(activePolygon.xpoints[i] - 1, activePolygon.ypoints[i] - 1, 2, 2);
			}
		}
		
		for(BuildingPolygon r : rectanglesList) {
			g.drawPolygon(r.getPolygon());
		}
		
		g2d.setStroke(oldStroke);
	}

	public void setImage(BufferedImage image) {
		this.realImage = image; //Remove this line to get normal program work. Real image used to correct time measure and correct line recognition.
		this.resizedImage = resizeImage(image, this.getSize());
		this.repaint();
	}
	
	public List<BuildingPolygon> getTransformedRegions() {
		
		List<BuildingPolygon> regions = new ArrayList<>();
		
		int x = this.getWidth() / 2 - resizedImage.getWidth(null) / 2;
		int y = this.getHeight() / 2 - resizedImage.getHeight(null) / 2;
		
		for(BuildingPolygon r : rectanglesList) {
			
			BuildingPolygon nr = r.getNormalizedRectangle(x, y);
			
			regions.add(nr);
		}
		
		return regions;
	}
	
	public BuildingImage getBuildingImage() {
		
		if(resizedImage == null) {
			return null;
		}
		
		BufferedImage outputImage = new BufferedImage(realImage.getWidth(null), realImage.getHeight(null), BufferedImage.TYPE_INT_RGB);
		
		Graphics2D g2d = outputImage.createGraphics();
		
		g2d.drawImage(realImage, 0, 0, Color.BLACK, null);
		
		g2d.dispose();
		
		BuildingImage buildingImage = new BuildingImage(null, outputImage);
		
		for(BuildingPolygon br : getTransformedRegions()) {
			buildingImage.addBuilding(br);
		}
		
		return buildingImage;
	}
	
	private Image resizeImage(BufferedImage image, Dimension targetSize) {
		
		double ratio = (double)image.getWidth() / targetSize.getWidth();
		
		double heightRatio = (double)image.getHeight() / targetSize.getHeight();
		
		if(ratio < heightRatio) {
			ratio = image.getHeight() / targetSize.getHeight();
		}
		
		ratio += 0.1;
		
		return image.getScaledInstance((int)((double)image.getWidth() / ratio), (int)((double)image.getHeight() / ratio), Image.SCALE_SMOOTH);
	}
}
