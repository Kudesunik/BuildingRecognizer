package ru.mai.buildingrecognizer;

import java.nio.file.Paths;

import org.opencv.core.Core;

import ru.mai.buildingrecognizer.handlers.MainHandler;

public class BuildingRecognizer {
	
	public static void main(String[] args) {
		
		System.setProperty("java.library.path", System.getProperty("java.library.path") + ";" + Paths.get("").toAbsolutePath().toString());
		
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		(new MainHandler()).start();
	}
}
