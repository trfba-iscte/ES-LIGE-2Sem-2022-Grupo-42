/*
 * Copyright 2009 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.zxing.common.detector;

import com.google.zxing.NotFoundException;
import com.google.zxing.ResultPoint;
import com.google.zxing.common.BitMatrix;

/**
 * <p>A somewhat generic detector that looks for a barcode-like rectangular region within an image.
 * It looks within a mostly white region of an image for a region of black and white, but mostly
 * black. It returns the four corners of the region, as best it can determine.</p>
 *
 * @author Sean Owen
 * @deprecated without replacement since 3.3.0
 */
@Deprecated
public final class MonochromeRectangleDetector {

  private MonochromeRectangleDetectorProduct monochromeRectangleDetectorProduct = new MonochromeRectangleDetectorProduct();

private static final int MAX_MODULES = 32;

  private final BitMatrix image;

  public MonochromeRectangleDetector(BitMatrix image) {
    this.image = image;
  }

  /**
   * <p>Detects a rectangular region of black and white -- mostly black -- with a region of mostly
   * white, in an image.</p>
   *
   * @return {@link ResultPoint}[] describing the corners of the rectangular region. The first and
   *  last points are opposed on the diagonal, as are the second and third. The first point will be
   *  the topmost point and the last, the bottommost. The second point will be leftmost and the
   *  third, the rightmost
   * @throws NotFoundException if no Data Matrix Code can be found
   */
  public ResultPoint[] detect() throws NotFoundException {
    int height = image.getHeight();
    int width = image.getWidth();
    int halfHeight = height / 2;
    int halfWidth = width / 2;
    int deltaY = Math.max(1, height / (MAX_MODULES * 8));
    int deltaX = Math.max(1, width / (MAX_MODULES * 8));

    int top = 0;
    int bottom = height;
    int left = 0;
    int right = width;
    ResultPoint pointA = monochromeRectangleDetectorProduct.findCornerFromCenter(halfWidth, 0, left, right,
        halfHeight, -deltaY, top, bottom, halfWidth / 2, this);
    top = (int) pointA.getY() - 1;
    ResultPoint pointB = monochromeRectangleDetectorProduct.findCornerFromCenter(halfWidth, -deltaX, left, right,
        halfHeight, 0, top, bottom, halfHeight / 2, this);
    left = (int) pointB.getX() - 1;
    ResultPoint pointC = monochromeRectangleDetectorProduct.findCornerFromCenter(halfWidth, deltaX, left, right,
        halfHeight, 0, top, bottom, halfHeight / 2, this);
    right = (int) pointC.getX() + 1;
    ResultPoint pointD = monochromeRectangleDetectorProduct.findCornerFromCenter(halfWidth, 0, left, right,
        halfHeight, deltaY, top, bottom, halfWidth / 2, this);
    bottom = (int) pointD.getY() + 1;

    // Go try to find point A again with better information -- might have been off at first.
    pointA = monochromeRectangleDetectorProduct.findCornerFromCenter(halfWidth, 0, left, right,
        halfHeight, -deltaY, top, bottom, halfWidth / 4, this);

    return new ResultPoint[] { pointA, pointB, pointC, pointD };
  }

  public int[] monochromeRefactor(int deltaX, int left, int right, int top, int bottom, int maxWhiteRun, int y, int x) {
	int[] range;
	if (deltaX == 0) {
        // horizontal slices, up and down
        range = blackWhiteRange(y, maxWhiteRun, left, right, true);
      } else {
        // vertical slices, left and right
        range = blackWhiteRange(x, maxWhiteRun, top, bottom, false);
      }
	return range;
}

  /**
   * Computes the start and end of a region of pixels, either horizontally or vertically, that could
   * be part of a Data Matrix barcode.
   *
   * @param fixedDimension if scanning horizontally, this is the row (the fixed vertical location)
   *  where we are scanning. If scanning vertically it's the column, the fixed horizontal location
   * @param maxWhiteRun largest run of white pixels that can still be considered part of the
   *  barcode region
   * @param minDim minimum pixel location, horizontally or vertically, to consider
   * @param maxDim maximum pixel location, horizontally or vertically, to consider
   * @param horizontal if true, we're scanning left-right, instead of up-down
   * @return int[] with start and end of found range, or null if no such range is found
   *  (e.g. only white was found)
   */
  private int[] blackWhiteRange(int fixedDimension, int maxWhiteRun, int minDim, int maxDim, boolean horizontal) {

    int center = (minDim + maxDim) / 2;

    // Scan left/up first
    int start = center;
    start = blackWhiteRangeRefactor(fixedDimension, maxWhiteRun, minDim, horizontal, start);
    start++;

    // Then try right/down
    int end = center;
    end = blackWhiteRangeRefactor2(fixedDimension, maxWhiteRun, maxDim, horizontal, end);
    end--;

    return end > start ? new int[]{start, end} : null;
  }

private int blackWhiteRangeRefactor2(int fixedDimension, int maxWhiteRun, int maxDim, boolean horizontal, int end) {
	while (end < maxDim) {
      if (horizontal ? image.get(end, fixedDimension) : image.get(fixedDimension, end)) {
        end++;
      } else {
        int whiteRunStart = end;
        do {
          end++;
        } while (end < maxDim && !(horizontal ? image.get(end, fixedDimension) :
            image.get(fixedDimension, end)));
        int whiteRunSize = end - whiteRunStart;
        if (end >= maxDim || whiteRunSize > maxWhiteRun) {
          end = whiteRunStart;
          break;
        }
      }
    }
	return end;
}

private int blackWhiteRangeRefactor(int fixedDimension, int maxWhiteRun, int minDim, boolean horizontal, int start) {
	while (start >= minDim) {
      if (horizontal ? image.get(start, fixedDimension) : image.get(fixedDimension, start)) {
        start--;
      } else {
        int whiteRunStart = start;
        do {
          start--;
        } while (start >= minDim && !(horizontal ? image.get(start, fixedDimension) :
            image.get(fixedDimension, start)));
        int whiteRunSize = whiteRunStart - start;
        if (start < minDim || whiteRunSize > maxWhiteRun) {
          start = whiteRunStart;
          break;
        }
      }
    }
	return start;
}

}