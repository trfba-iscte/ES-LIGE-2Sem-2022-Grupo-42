package com.google.zxing.common.detector;


import com.google.zxing.ResultPoint;
import com.google.zxing.NotFoundException;

public class MonochromeRectangleDetectorProduct {
	/**
	* Attempts to locate a corner of the barcode by scanning up, down, left or right from a center point which should be within the barcode.
	* @param centerX  center's x component (horizontal)
	* @param deltaX  same as deltaY but change in x per step instead
	* @param left  minimum value of x
	* @param right  maximum value of x
	* @param centerY  center's y component (vertical)
	* @param deltaY  change in y per step. If scanning up this is negative; down, positive; left or right, 0
	* @param top  minimum value of y to search through (meaningless when di == 0)
	* @param bottom  maximum value of y
	* @param maxWhiteRun  maximum run of white pixels that can still be considered to be within the barcode
	* @return  a  {@link ResultPoint}  encapsulating the corner that was found
	* @throws NotFoundException  if such a point cannot be found
	*/
	public ResultPoint findCornerFromCenter(int centerX, int deltaX, int left, int right, int centerY, int deltaY,
			int top, int bottom, int maxWhiteRun, MonochromeRectangleDetector monochromeRectangleDetector)
			throws NotFoundException {
		int[] lastRange = null;
		for (int y = centerY, x = centerX; y < bottom && y >= top && x < right && x >= left; y += deltaY, x += deltaX) {
			int[] range;
			range = monochromeRectangleDetector.monochromeRefactor(deltaX, left, right, top, bottom, maxWhiteRun, y, x);
			if (range == null) {
				if (lastRange == null) {
					throw NotFoundException.getNotFoundInstance();
				}
				if (deltaX == 0) {
					int lastY = y - deltaY;
					return monochromeRefactor2(centerX, deltaY, lastRange, lastY);
				} else {
					int lastX = x - deltaX;
					return monochromeRefactor3(deltaX, centerY, lastRange, lastX);
				}
			}
			lastRange = range;
		}
		throw NotFoundException.getNotFoundInstance();
	}

	public ResultPoint monochromeRefactor3(int deltaX, int centerY, int[] lastRange, int lastX) {
		if (lastRange[0] < centerY) {
			if (lastRange[1] > centerY) {
				return new ResultPoint(lastX, lastRange[deltaX < 0 ? 0 : 1]);
			}
			return new ResultPoint(lastX, lastRange[0]);
		} else {
			return new ResultPoint(lastX, lastRange[1]);
		}
	}

	public ResultPoint monochromeRefactor2(int centerX, int deltaY, int[] lastRange, int lastY) {
		if (lastRange[0] < centerX) {
			if (lastRange[1] > centerX) {
				return new ResultPoint(lastRange[deltaY > 0 ? 0 : 1], lastY);
			}
			return new ResultPoint(lastRange[0], lastY);
		} else {
			return new ResultPoint(lastRange[1], lastY);
		}
	}
}