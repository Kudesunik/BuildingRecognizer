package ru.mai.buildingrecognizer.listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;

import ru.mai.buildingrecognizer.handlers.MainHandler;

	public class MainListener implements IListener, ActionListener {
	
	private final MainHandler handler;
	
	public MainListener(MainHandler handler) {
		this.handler = handler;
	}
	
	@Override
	public void addListeners(JComponent component) {
		((JButton)component).addActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		Object source = e.getSource();
		
		if(source instanceof JButton) {
			
			JButton button = (JButton) source;
			
			if(button.getName().equals("RecognizeByCompare")) {
				handler.recognizeByCompare();
			}
			
			if(button.getName().equals("RecognizeByLineDetection")) {
				handler.recognizeByLineDetection();
			}
		}
	}
}
