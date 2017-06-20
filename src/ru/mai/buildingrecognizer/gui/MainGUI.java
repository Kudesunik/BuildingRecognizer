package ru.mai.buildingrecognizer.gui;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import java.awt.GridBagLayout;

import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.image.BufferedImage;

import javax.swing.JButton;
import javax.swing.border.TitledBorder;

import ru.mai.buildingrecognizer.draw.DrawPanel;
import ru.mai.buildingrecognizer.handlers.MainHandler;

@SuppressWarnings("serial")
public class MainGUI extends JFrame {
	
	private JPanel contentPane;
	
	private JMenuBar menuBar;
	
	private JMenu fileMenu;
	
	private JMenuItem openMenuItem;
	private JMenuItem saveToBaseMenuItem;
	private JMenuItem saveResultMenuItem;
	private JMenuItem exitMenuItem;
	
	private DrawPanel panel1;
	private DrawPanel panel2;
	
	private JButton recognizeCompareButton;
	private JButton recognizeContourButton;
	
	private MainHandler mainHandler;
	private JMenuItem openBaseMenuItem;
	
	public MainGUI(MainHandler mainHandler) {
		
		super("Building recognizer");
		
		this.mainHandler = mainHandler;
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		setBounds(100, 100, 800, 600);
		
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		
		setContentPane(contentPane);
		
		GridBagLayout gbl_contentPane = new GridBagLayout();
		
		gbl_contentPane.columnWidths = new int[]{0, 0, 0};
		gbl_contentPane.rowHeights = new int[]{0, 0, 0, 0};
		gbl_contentPane.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		gbl_contentPane.rowWeights = new double[]{1.0, 0.0, 0.0, Double.MIN_VALUE};
		
		contentPane.setLayout(gbl_contentPane);
		
		initializeGUI();
		initializeListeners();
	}
	
	private void initializeGUI() {
		
		initializeMenu();
		
		panel1 = new DrawPanel();
		panel1.setBorder(new TitledBorder(null, "\u0412\u0445\u043E\u0434\u043D\u043E\u0435 \u0438\u0437\u043E\u0431\u0440\u0430\u0436\u0435\u043D\u0438\u0435", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_panel1 = new GridBagConstraints();
		gbc_panel1.fill = GridBagConstraints.BOTH;
		gbc_panel1.insets = new Insets(5, 5, 5, 5);
		gbc_panel1.gridx = 0;
		gbc_panel1.gridy = 0;
		contentPane.add(panel1, gbc_panel1);
		GridBagLayout gbl_panel1 = new GridBagLayout();
		gbl_panel1.columnWidths = new int[]{0, 0};
		gbl_panel1.rowHeights = new int[]{0, 0};
		gbl_panel1.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_panel1.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		panel1.setLayout(gbl_panel1);
		
		panel1.setFocusable(true);
		panel1.requestFocusInWindow();
		
		panel2 = new DrawPanel();
		panel2.setBorder(new TitledBorder(null, "\u0412\u044B\u0445\u043E\u0434\u043D\u043E\u0435 \u0438\u0437\u043E\u0431\u0440\u0430\u0436\u0435\u043D\u0438\u0435", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_panel2 = new GridBagConstraints();
		gbc_panel2.fill = GridBagConstraints.BOTH;
		gbc_panel2.insets = new Insets(5, 5, 5, 5);
		gbc_panel2.gridx = 1;
		gbc_panel2.gridy = 0;
		contentPane.add(panel2, gbc_panel2);
		GridBagLayout gbl_panel2 = new GridBagLayout();
		gbl_panel2.columnWidths = new int[]{0, 0};
		gbl_panel2.rowHeights = new int[]{0, 0};
		gbl_panel2.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_panel2.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		panel2.setLayout(gbl_panel2);
		
		recognizeCompareButton = new JButton("Распознать методом сравнения ключевых точек");
		recognizeCompareButton.setName("RecognizeByCompare");
		GridBagConstraints gbc_recognizeCompareButton = new GridBagConstraints();
		gbc_recognizeCompareButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_recognizeCompareButton.gridwidth = 2;
		gbc_recognizeCompareButton.insets = new Insets(5, 5, 5, 5);
		gbc_recognizeCompareButton.gridx = 0;
		gbc_recognizeCompareButton.gridy = 1;
		contentPane.add(recognizeCompareButton, gbc_recognizeCompareButton);
		
		recognizeContourButton = new JButton("Распознать методом поиска прямоугольников");
		recognizeContourButton.setName("RecognizeByLineDetection");
		GridBagConstraints gbc_recognizeContourButton = new GridBagConstraints();
		gbc_recognizeContourButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_recognizeContourButton.gridwidth = 2;
		gbc_recognizeContourButton.insets = new Insets(5, 5, 5, 5);
		gbc_recognizeContourButton.gridx = 0;
		gbc_recognizeContourButton.gridy = 2;
		contentPane.add(recognizeContourButton, gbc_recognizeContourButton);
	}
	
	private void initializeMenu() {
		
		menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		fileMenu = new JMenu("Меню");
		menuBar.add(fileMenu);
		
		openMenuItem = new JMenuItem("Открыть...");
		openMenuItem.setName("OpenMenuItem");
		fileMenu.add(openMenuItem);
		
		openBaseMenuItem = new JMenuItem("Открыть базу...");
		openBaseMenuItem.setName("OpenBaseMenuItem");
		fileMenu.add(openBaseMenuItem);
		
		saveToBaseMenuItem = new JMenuItem("Сохранить в базу");
		saveToBaseMenuItem.setName("SaveToBaseMenuItem");
		fileMenu.add(saveToBaseMenuItem);
		
		saveResultMenuItem = new JMenuItem("Сохранить результат");
		saveResultMenuItem.setName("SaveResultMenuItem");
		fileMenu.add(saveResultMenuItem);
		
		JSeparator separator = new JSeparator();
		fileMenu.add(separator);
		
		exitMenuItem = new JMenuItem("Выход");
		fileMenu.add(exitMenuItem);
	}
	
	private void initializeListeners() {
		
		mainHandler.getDrawHandler().getListener().addListeners(panel1);
		
		mainHandler.getMenuHandler().getListener().addListeners(openMenuItem);
		mainHandler.getMenuHandler().getListener().addListeners(openBaseMenuItem);
		mainHandler.getMenuHandler().getListener().addListeners(saveToBaseMenuItem);
		mainHandler.getMenuHandler().getListener().addListeners(saveResultMenuItem);
		
		mainHandler.getListener().addListeners(recognizeCompareButton);
		mainHandler.getListener().addListeners(recognizeContourButton);
	}
	
	public DrawPanel getDrawPanel() {
		return panel1;
	}
	
	public void setInputImage(BufferedImage image) {
		panel1.setImage(image);
	}
	
	public void setOutputImage(BufferedImage image) {
		panel2.setImage(image);
	}
}
