package org.openbw.tsbw.micro;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.openbw.tsbw.unit.MobileUnit;

import bwapi.Position;
import bwapi.Unit;

public class MicroMath {

	public static double[] getAverages(Collection<? extends MobileUnit> units) {
		
		double[] avg = new double[5];
		for (MobileUnit unit : units) {
			avg[0] += unit.getX();
			avg[1] += unit.getY();
			avg[2] += unit.width();
			avg[3] += unit.height();
			avg[4] += unit.getHitPoints();
		}
		avg[0] = avg[0] / units.size();
		avg[1] = avg[1] / units.size();
		avg[2] = avg[2] / units.size();
		avg[3] = avg[3] / units.size();
		avg[4] = avg[4] / units.size();
		
		return avg;
	}

	public static boolean intersect(Position borderP1, Position borderP2, Position unitOriginal, Position unitTarget) {
		
		double dx1 = borderP1.getX() == borderP2.getX() ? 0.001 : borderP1.getX() - borderP2.getX(); // this handles division by 0 case
		double m1 = (borderP1.getY() - borderP2.getY()) / dx1;
		double c1 = borderP2.getY() - m1*borderP2.getX();
		
		double dx2 = unitOriginal.getX() == unitTarget.getX() ? 0.001 : unitOriginal.getX() - unitTarget.getX(); // this handles division by 0 case
		double m2 = (unitOriginal.getY() - unitTarget.getY()) / dx2;
		double c2 = unitTarget.getY() - m2*unitTarget.getX();
		
		double x = (c1 - c2) / (m2 - m1);
		//double y = m1 * x + c1;
		
		if ((borderP1.getX() <= x && x <= borderP2.getX() || borderP1.getX() >= x && x >= borderP2.getX())
				&& (unitOriginal.getX() <= x && x <= unitTarget.getX() || unitOriginal.getX() >= x && x >= unitTarget.getX())) {
			
			return true;
		} else {
			return false;
		}
		
	}
	
	public static List<Position> calculateIntersection(double m, double c, double p, double q, double r) {
		
		double a = m*m + 1;
		double b = 2*(m*c - m*q - p);
		double c1 = q*q - r*r + p*p - 2*c*q + c*c;
		
		double term = b*b - 4*a*c1;
		
		List<Position> list = new ArrayList<Position>();
		
		if (term >= 0) {
			double x1 = (-b + Math.sqrt(term)) / (2*a);
			double y1 = m*x1 + c;
			double x2 = (-b - Math.sqrt(term)) / (2*a);
			double y2 = m*x2 + c;
			
			list.add(new Position((int)x1, (int)y1));
			list.add(new Position((int)x2, (int)y2));
		}
		return list;
	}
	
	public static int e2eDistanceX(Unit unit1, Unit unit2) {

		int left = unit2.getLeft() - 1;
		int right = unit2.getRight() + 1;

		// compute x distance
		int xDist = unit1.getLeft() - right;
		if (xDist < 0) {
			xDist = left - unit1.getRight();
			if (xDist < 0)
				xDist = 0;
		}

		return xDist;
	}
	
	public static int e2eDistanceY(Unit unit1, Unit unit2) {

		int top = unit2.getTop() - 1;
		int bottom = unit2.getBottom() + 1;

		// compute y distance
		int yDist = unit1.getTop() - bottom;
		if (yDist < 0) {
			yDist = top - unit1.getBottom();
			if (yDist < 0)
				yDist = 0;
		}
		return yDist;
	}
}
