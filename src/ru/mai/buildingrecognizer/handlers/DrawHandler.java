package ru.mai.buildingrecognizer.handlers;

import ru.mai.buildingrecognizer.listeners.DrawListener;
import ru.mai.buildingrecognizer.listeners.IListener;

public class DrawHandler implements IHandler {
	
	private final MainHandler handler;
	
	private final DrawListener listener;
	
	public DrawHandler(MainHandler handler) {
		this.handler = handler;
		this.listener = new DrawListener(this);
	}
	
	@Override
	public IListener getListener() {
		return listener;
	}
	
	public void addPoint(int x, int y) {
		handler.getMainGUI().getDrawPanel().addPoint(x, y);
	}

	public void savePolygon() {
		handler.getMainGUI().getDrawPanel().saveBuildingRectangle();
	}
}
