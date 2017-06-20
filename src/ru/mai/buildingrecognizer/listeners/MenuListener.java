package ru.mai.buildingrecognizer.listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JMenuItem;

import ru.mai.buildingrecognizer.handlers.MenuHandler;

public class MenuListener implements IListener, ActionListener {
	
	private final MenuHandler handler;
	
	public MenuListener(MenuHandler handler) {
		this.handler = handler;
	}
	
	@Override
	public void addListeners(JComponent component) {
		if(component instanceof JMenuItem) {
			((JMenuItem)component).addActionListener(this);
		}
		else {
			System.err.println("[Warning] Trying to add non menu item to menu listener");
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		JMenuItem menuItem = (JMenuItem)e.getSource();
		
		if(menuItem.getName().equals("OpenMenuItem")) {
			handler.loadInitialImage();
		}
		if(menuItem.getName().equals("OpenBaseMenuItem")) {
			handler.loadDatabase();
		}
		if(menuItem.getName().equals("SaveToBaseMenuItem")) {
			handler.saveToDatabase();
		}
		if(menuItem.getName().equals("SaveResultMenuItem")) {
			handler.saveResults();
		}
	}
}
