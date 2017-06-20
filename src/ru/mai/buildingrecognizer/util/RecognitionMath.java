package ru.mai.buildingrecognizer.util;

import java.awt.Polygon;

import org.opencv.core.Point;

import ru.mai.buildingrecognizer.entities.Line;

public class RecognitionMath {
	
	public static double[] getPolygonMassCenter(Polygon polygon) {
		
		double x = 0;
		double y = 0;
		
		for(int i = 0; i < polygon.npoints; i++) {
			x += polygon.xpoints[i];
			y += polygon.ypoints[i];
		}
		
		y /= polygon.npoints;
		x /= polygon.npoints;
		
		return new double[]{x, y};
	}

	public static double getPointsDistance(Point p1, Point p2) {
		return Math.sqrt(Math.pow((p1.x - p2.x), 2) + Math.pow((p1.y - p2.y), 2));
	}
	
	public static double getAngle(Line l1, Line l2) {
		
		double vx = l1.p2.x - l1.p1.x;
		double vy = l1.p2.y - l1.p1.y;
		
		double ux = l2.p2.x - l2.p1.x;
		double uy = l2.p2.y - l2.p1.y;
		
		double num = (vx * ux + vy * uy);
		
		double den = (Math.sqrt(Math.pow(vx, 2) + Math.pow(vy, 2)) * (Math.sqrt(Math.pow(ux, 2) + Math.pow(uy, 2))));
		
		double cos = num / den;
		
		return StrictMath.toDegrees(Math.acos(cos));
	}
	
	public static double getLineLength(Line line) {
		return Math.sqrt(Math.pow((line.p1.x - line.p2.x), 2) + Math.pow((line.p1.y - line.p2.y), 2));
	}
	
	public static Point getIntersectionPoint(Line line1, Line line2) {
		
		double x1 = line1.p1.x;
		double x2 = line1.p2.x;
		double x3 = line2.p1.x;
		double x4 = line2.p2.x;
		double y1 = line1.p1.y;
		double y2 = line1.p2.y;
		double y3 = line2.p1.y;
		double y4 = line2.p2.y;
		
		double d = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
		
		if(Math.abs(d) < 0.0001) {
			return null;
		}
		
		double xi = ((x3 - x4) * (x1 * y2 - y1 * x2) - (x1 - x2) * (x3 * y4 - y3 * x4)) / d;
		double yi = ((y3 - y4) * (x1 * y2 - y1 * x2) - (y1 - y2) * (x3 * y4 - y3 * x4)) / d;
		
		return new Point(xi, yi);
	}
}
