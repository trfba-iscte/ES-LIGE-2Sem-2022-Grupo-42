package com.google.zxing.common.detector;


import com.google.zxing.ResultPoint;
import com.google.zxing.NotFoundException;

public class WhiteRectangleDetectorProduct3 {
	private WhiteRectangleDetectorProduct2 whiteRectangleDetectorProduct2 = new WhiteRectangleDetectorProduct2();
	private final int height;
	private final int leftInit;
	private final int rightInit;
	private final int downInit;
	private final int upInit;

	public WhiteRectangleDetectorProduct3(int getHeight, int x, int halfsize, int y) {
		height = getHeight;
		leftInit = x - halfsize;
		rightInit = x + halfsize;
		upInit = y - halfsize;
		downInit = y + halfsize;
	}

	public WhiteRectangleDetectorProduct2 getWhiteRectangleDetectorProduct2() {
		return whiteRectangleDetectorProduct2;
	}

	public int getHeight() {
		return height;
	}

	public int getLeftInit() {
		return leftInit;
	}

	public int getRightInit() {
		return rightInit;
	}

	public int getDownInit() {
		return downInit;
	}

	public int getUpInit() {
		return upInit;
	}

	public ResultPoint[] refactorWhiteRectangleDetector6(int left, int right, int up, int down, boolean sizeExceeded,
			WhiteRectangleDetector whiteRectangleDetector) throws NotFoundException {
		if (!sizeExceeded) {
			return whiteRectangleDetectorProduct2.refactorWhiteRectangleDetector(left, right, up, down,
					whiteRectangleDetector);
		} else {
			throw NotFoundException.getNotFoundInstance();
		}
	}

	/**
	* <p> Detects a candidate barcode-like rectangular region within an image. It starts around the center of the image, increases the size of the candidate region until it finds a white rectangular region. </p>
	* @return   {@link ResultPoint} [] describing the corners of the rectangular region. The first and last points are opposed on the diagonal, as are the second and third. The first point will be the topmost point and the last, the bottommost. The second point will be leftmost and the third, the rightmost
	* @throws NotFoundException  if no Data Matrix Code can be found
	*/
	public ResultPoint[] detect(int thisWidth, WhiteRectangleDetector whiteRectangleDetector) throws NotFoundException {
		int left = leftInit;
		int right = rightInit;
		int up = upInit;
		int down = downInit;
		boolean sizeExceeded = false;
		boolean aBlackPointFoundOnBorder = true;
		boolean atLeastOneBlackPointFoundOnRight = false;
		boolean atLeastOneBlackPointFoundOnBottom = false;
		boolean atLeastOneBlackPointFoundOnLeft = false;
		boolean atLeastOneBlackPointFoundOnTop = false;
		while (aBlackPointFoundOnBorder) {
			aBlackPointFoundOnBorder = false;
			boolean rightBorderNotWhite = true;
			while ((rightBorderNotWhite || !atLeastOneBlackPointFoundOnRight) && right < thisWidth) {
				rightBorderNotWhite = whiteRectangleDetectorProduct2.getWhiteRectangleDetectorProduct()
						.containsBlackPoint(up, down, right, false);
				if (rightBorderNotWhite) {
					right++;
					aBlackPointFoundOnBorder = true;
					atLeastOneBlackPointFoundOnRight = true;
				} else if (!atLeastOneBlackPointFoundOnRight) {
					right++;
				}
			}
			if (right >= thisWidth) {
				sizeExceeded = true;
				break;
			}
			boolean bottomBorderNotWhite = true;
			while ((bottomBorderNotWhite || !atLeastOneBlackPointFoundOnBottom) && down < height) {
				bottomBorderNotWhite = whiteRectangleDetectorProduct2.getWhiteRectangleDetectorProduct()
						.containsBlackPoint(left, right, down, true);
				if (bottomBorderNotWhite) {
					down++;
					aBlackPointFoundOnBorder = true;
					atLeastOneBlackPointFoundOnBottom = true;
				} else if (!atLeastOneBlackPointFoundOnBottom) {
					down++;
				}
			}
			if (down >= height) {
				sizeExceeded = true;
				break;
			}
			boolean leftBorderNotWhite = true;
			while ((leftBorderNotWhite || !atLeastOneBlackPointFoundOnLeft) && left >= 0) {
				leftBorderNotWhite = whiteRectangleDetectorProduct2.getWhiteRectangleDetectorProduct()
						.containsBlackPoint(up, down, left, false);
				if (leftBorderNotWhite) {
					left--;
					aBlackPointFoundOnBorder = true;
					atLeastOneBlackPointFoundOnLeft = true;
				} else if (!atLeastOneBlackPointFoundOnLeft) {
					left--;
				}
			}
			if (left < 0) {
				sizeExceeded = true;
				break;
			}
			boolean topBorderNotWhite = true;
			while ((topBorderNotWhite || !atLeastOneBlackPointFoundOnTop) && up >= 0) {
				topBorderNotWhite = whiteRectangleDetectorProduct2.getWhiteRectangleDetectorProduct()
						.containsBlackPoint(left, right, up, true);
				if (topBorderNotWhite) {
					up--;
					aBlackPointFoundOnBorder = true;
					atLeastOneBlackPointFoundOnTop = true;
				} else if (!atLeastOneBlackPointFoundOnTop) {
					up--;
				}
			}
			if (up < 0) {
				sizeExceeded = true;
				break;
			}
		}
		return refactorWhiteRectangleDetector6(left, right, up, down, sizeExceeded, whiteRectangleDetector);
	}
}