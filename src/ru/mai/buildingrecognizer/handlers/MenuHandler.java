package ru.mai.buildingrecognizer.handlers;

import java.awt.image.BufferedImage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import javax.imageio.ImageIO;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.opencv.core.Point;

import ru.mai.buildingrecognizer.draw.BuildingPolygon;
import ru.mai.buildingrecognizer.entities.BuildingDatabase;
import ru.mai.buildingrecognizer.entities.BuildingImage;
import ru.mai.buildingrecognizer.listeners.IListener;
import ru.mai.buildingrecognizer.listeners.MenuListener;
import ru.mai.buildingrecognizer.util.Database;

public class MenuHandler implements IHandler {
	
	private final MainHandler handler;
	private final IListener listener;
	
	private final JFileChooser imageFileChooser;
	private final JFileChooser baseFileChooser;
	private final JFileChooser resultFileChooser;
	
	private final Database database;
	private final BuildingDatabase buildingDatabase;
	
	public MenuHandler(MainHandler handler) {
		
		this.handler = handler;
		
		this.database = new Database();
		
		this.buildingDatabase = new BuildingDatabase(database);
		
		this.listener = new MenuListener(this);
		
		imageFileChooser = new JFileChooser();
		imageFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		imageFileChooser.setFileFilter(new FileNameExtensionFilter("Файлы изображений (*.png, *.jpg)", "png", "jpg"));
		imageFileChooser.setCurrentDirectory(new File("./"));
		
		baseFileChooser = new JFileChooser();
		baseFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		baseFileChooser.setFileFilter(new FileNameExtensionFilter("Файлы базы данных (*.rb)", "rb"));
		baseFileChooser.setCurrentDirectory(new File("./"));
		
		resultFileChooser = new JFileChooser();
		resultFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		resultFileChooser.setFileFilter(new FileNameExtensionFilter("Файлы базы данных (*.txt)", "txt"));
		resultFileChooser.setCurrentDirectory(new File("./"));
	}
	
	public void loadInitialImage() {
		
		int result = imageFileChooser.showSaveDialog(handler.getMainGUI());
		
		if(result == JFileChooser.APPROVE_OPTION) {
			
            File file = imageFileChooser.getSelectedFile();
            
            try {
            	
				BufferedImage image = ImageIO.read(file);
				
				handler.getMainGUI().setInputImage(image);
				
			} catch(IOException e) {
				System.out.println("[Error] Image load failed: " + e.getLocalizedMessage());
				e.printStackTrace();
			}
        }
	}
	
	public void loadDatabase() {
		
		int result = baseFileChooser.showSaveDialog(handler.getMainGUI());
		
		if(result == JFileChooser.APPROVE_OPTION) {
			
			File file = baseFileChooser.getSelectedFile();
			
			buildingDatabase.loadBuildingImages(file);
			
			handler.buildingDatabase = buildingDatabase;
		}
	}
	
	public void saveToDatabase() {
		
		int result = baseFileChooser.showSaveDialog(handler.getMainGUI());
		
		if(result == JFileChooser.APPROVE_OPTION) {
			
			File file = baseFileChooser.getSelectedFile();
			
			BuildingDatabase buildingDatabase = new BuildingDatabase(database);
			
			buildingDatabase.loadBuildingImages(file);
			
			buildingDatabase.addBuildingImage(handler.getMainGUI().getDrawPanel().getBuildingImage());
			
			buildingDatabase.saveBuildingImages(file);
		}
	}
	
	public void saveResults() {
		
		int result = resultFileChooser.showSaveDialog(handler.getMainGUI());
		
		if(result != JFileChooser.APPROVE_OPTION) {
			return;
		}
		
		File file = resultFileChooser.getSelectedFile();
		
		BuildingImage buildingImage = handler.getBufferedRecognitionResult();
		
		try {
			
			PrintWriter writer = new PrintWriter(file);
			
			int bn = 0;
			
			for(BuildingPolygon bp : buildingImage.getBuildings()) {
				
				writer.println("№ здания: " + (++bn));
				
				int pn = 0;
				
				for(Point p : bp.getOpenCVPointsList()) {
					
					StringBuilder buildingCoordinates = new StringBuilder();
					
					buildingCoordinates.append("Координата угла № ");
					buildingCoordinates.append(++pn);
					buildingCoordinates.append(": ");
					buildingCoordinates.append(p);
					buildingCoordinates.append("; ");
					
					writer.println(buildingCoordinates.toString());
				}
			}
			
			writer.flush();
			writer.close();
			
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public IListener getListener() {
		return listener;
	}
}
