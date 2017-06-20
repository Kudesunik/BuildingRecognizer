package ru.mai.buildingrecognizer.recognition;

import java.awt.image.BufferedImage;

import ru.mai.buildingrecognizer.entities.BuildingImage;

public interface IRecognizer {
	public BuildingImage recognize(BufferedImage initialImage);
}
