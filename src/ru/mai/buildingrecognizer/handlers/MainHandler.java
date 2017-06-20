package ru.mai.buildingrecognizer.handlers;

import java.awt.image.BufferedImage;

import ru.mai.buildingrecognizer.entities.BuildingDatabase;
import ru.mai.buildingrecognizer.entities.BuildingImage;

import ru.mai.buildingrecognizer.gui.MainGUI;

import ru.mai.buildingrecognizer.listeners.IListener;
import ru.mai.buildingrecognizer.listeners.MainListener;

import ru.mai.buildingrecognizer.recognition.KeypointsRecognizer;
import ru.mai.buildingrecognizer.recognition.LinesRecognizer;

import ru.mai.buildingrecognizer.util.Util;

public class MainHandler implements IHandler {
	
	private final MainGUI mainGUI;
	
	private final IHandler drawHandler;
	private final IHandler menuHandler;
	
	private final MainListener listener;
	
	private final KeypointsRecognizer keyPointsRecognizer;
	private final LinesRecognizer linesRecognizer;
	
	private BuildingImage bufferedRecognitionResult;
	
	public BuildingDatabase buildingDatabase;
	
	public MainHandler() {
		
		listener = new MainListener(this);
		
		drawHandler = new DrawHandler(this);
		menuHandler = new MenuHandler(this);
		
		mainGUI = new MainGUI(this);
		
		keyPointsRecognizer = new KeypointsRecognizer();
		linesRecognizer = new LinesRecognizer();
	}
	
	public MainGUI getMainGUI() {
		return mainGUI;
	}
	
	public IHandler getDrawHandler() {
		return drawHandler;
	}
	
	public IHandler getMenuHandler() {
		return menuHandler;
	}
	
	public void start() {
		mainGUI.setVisible(true);
	}
	
	public void recognizeByCompare() {
		
		BufferedImage image = mainGUI.getDrawPanel().getBuildingImage().getImage();
		
		keyPointsRecognizer.setBuildingDatabase(buildingDatabase);
		
		BuildingImage recognitionResult = keyPointsRecognizer.recognize(image);
		
		this.bufferedRecognitionResult = recognitionResult;
		
		Util.convertBuildingImageToBufferedImage(recognitionResult);
		
		mainGUI.setOutputImage(Util.convertBuildingImageToBufferedImage(recognitionResult));
	}
	
	public void recognizeByLineDetection() {
		
		BufferedImage image = mainGUI.getDrawPanel().getBuildingImage().getImage();
		
		BuildingImage recognitionResult = linesRecognizer.recognize(image);
		
		this.bufferedRecognitionResult = recognitionResult;
		
		mainGUI.setOutputImage(Util.convertBuildingImageToBufferedImage(recognitionResult));
	}
	
	public BuildingImage getBufferedRecognitionResult() {
		return bufferedRecognitionResult;
	}

	@Override
	public IListener getListener() {
		return listener;
	}
}
