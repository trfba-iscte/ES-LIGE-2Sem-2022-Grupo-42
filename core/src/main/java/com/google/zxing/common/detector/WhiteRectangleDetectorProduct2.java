package com.google.zxing.common.detector;


import com.google.zxing.ResultPoint;
import com.google.zxing.NotFoundException;

public class WhiteRectangleDetectorProduct2 {
	private WhiteRectangleDetectorProduct whiteRectangleDetectorProduct;

	public WhiteRectangleDetectorProduct getWhiteRectangleDetectorProduct() {
		return whiteRectangleDetectorProduct;
	}

	public void setWhiteRectangleDetectorProduct(WhiteRectangleDetectorProduct whiteRectangleDetectorProduct) {
		this.whiteRectangleDetectorProduct = whiteRectangleDetectorProduct;
	}

	public ResultPoint[] refactorWhiteRectangleDetector(int left, int right, int up, int down,
			WhiteRectangleDetector whiteRectangleDetector) throws NotFoundException {
		int maxSize = right - left;
		ResultPoint z = refactorWhiteRectangleDetector2(left, down, maxSize);
		ResultPoint t = refactorWhiteRectangleDetector3(left, up, maxSize);
		ResultPoint x = refactorWhiteRectangleDetector4(right, up, maxSize);
		ResultPoint y = refactorWhiteRectangleDetector5(right, down, maxSize);
		return whiteRectangleDetector.centerEdges(y, z, x, t);
	}

	public ResultPoint refactorWhiteRectangleDetector5(int right, int down, int maxSize) throws NotFoundException {
		ResultPoint y = null;
		for (int i = 1; y == null && i < maxSize; i++) {
			y = whiteRectangleDetectorProduct.getBlackPointOnSegment(right, down - i, right - i, down);
		}
		if (y == null) {
			throw NotFoundException.getNotFoundInstance();
		}
		return y;
	}

	public ResultPoint refactorWhiteRectangleDetector4(int right, int up, int maxSize) throws NotFoundException {
		ResultPoint x = null;
		for (int i = 1; x == null && i < maxSize; i++) {
			x = whiteRectangleDetectorProduct.getBlackPointOnSegment(right, up + i, right - i, up);
		}
		if (x == null) {
			throw NotFoundException.getNotFoundInstance();
		}
		return x;
	}

	public ResultPoint refactorWhiteRectangleDetector3(int left, int up, int maxSize) throws NotFoundException {
		ResultPoint t = null;
		for (int i = 1; t == null && i < maxSize; i++) {
			t = whiteRectangleDetectorProduct.getBlackPointOnSegment(left, up + i, left + i, up);
		}
		if (t == null) {
			throw NotFoundException.getNotFoundInstance();
		}
		return t;
	}

	public ResultPoint refactorWhiteRectangleDetector2(int left, int down, int maxSize) throws NotFoundException {
		ResultPoint z = null;
		for (int i = 1; z == null && i < maxSize; i++) {
			z = whiteRectangleDetectorProduct.getBlackPointOnSegment(left, down - i, left + i, down);
		}
		if (z == null) {
			throw NotFoundException.getNotFoundInstance();
		}
		return z;
	}
}