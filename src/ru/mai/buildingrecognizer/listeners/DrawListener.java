package ru.mai.buildingrecognizer.listeners;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JComponent;

import ru.mai.buildingrecognizer.handlers.DrawHandler;

public class DrawListener implements IListener, MouseListener, KeyListener {
	
	private final DrawHandler handler;
	
	public DrawListener(DrawHandler handler) {
		this.handler = handler;
	}
	
	public void addListeners(JComponent component) {
		component.addMouseListener(this);
		component.addKeyListener(this);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		handler.addPoint(e.getX(), e.getY());
	}

	@Override
	public void mousePressed(MouseEvent e) {}

	@Override
	public void mouseReleased(MouseEvent e) {}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}

	@Override
	public void keyTyped(KeyEvent e) {}

	@Override
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_S) {
			handler.savePolygon();
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {}
}
