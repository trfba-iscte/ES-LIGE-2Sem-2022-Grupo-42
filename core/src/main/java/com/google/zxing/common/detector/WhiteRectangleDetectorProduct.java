package com.google.zxing.common.detector;


import com.google.zxing.common.BitMatrix;
import com.google.zxing.ResultPoint;

public class WhiteRectangleDetectorProduct {
	private final BitMatrix image;

	public WhiteRectangleDetectorProduct(BitMatrix image) {
		this.image = image;
	}

	public ResultPoint getBlackPointOnSegment(float aX, float aY, float bX, float bY) {
		int dist = MathUtils.round(MathUtils.distance(aX, aY, bX, bY));
		float xStep = (bX - aX) / dist;
		float yStep = (bY - aY) / dist;
		for (int i = 0; i < dist; i++) {
			int x = MathUtils.round(aX + i * xStep);
			int y = MathUtils.round(aY + i * yStep);
			if (image.get(x, y)) {
				return new ResultPoint(x, y);
			}
		}
		return null;
	}

	/**
	* Determines whether a segment contains a black point
	* @param a           min value of the scanned coordinate
	* @param b           max value of the scanned coordinate
	* @param fixed       value of fixed coordinate
	* @param horizontal  set to true if scan must be horizontal, false if vertical
	* @return  true if a black point has been found, else false.
	*/
	public boolean containsBlackPoint(int a, int b, int fixed, boolean horizontal) {
		if (horizontal) {
			for (int x = a; x <= b; x++) {
				if (image.get(x, fixed)) {
					return true;
				}
			}
		} else {
			for (int y = a; y <= b; y++) {
				if (image.get(fixed, y)) {
					return true;
				}
			}
		}
		return false;
	}
}