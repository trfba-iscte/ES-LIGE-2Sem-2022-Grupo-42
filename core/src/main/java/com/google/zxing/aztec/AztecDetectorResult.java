/*
 * Copyright 2010 ZXing authors
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

package com.google.zxing.aztec;

import com.google.zxing.ResultPoint;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.DetectorResult;

/**
 * <p>Extends {@link DetectorResult} with more information specific to the Aztec format,
 * like the number of layers and whether it's compact.</p>
 *
 * @author Sean Owen
 */
public final class AztecDetectorResult extends DetectorResult {

  private final boolean compact;
  private final int nbDatablocks;
  private final int nbLayers;

  public AztecDetectorResult(BitMatrix bits,
                             ResultPoint[] points,
                             boolean compact,
                             int nbDatablocks,
                             int nbLayers) {
    super(bits, points);
    this.compact = compact;
    this.nbDatablocks = nbDatablocks;
    this.nbLayers = nbLayers;
  }

  public int getNbLayers() {
    return nbLayers;
  }

  public int getNbDatablocks() {
    return nbDatablocks;
  }

  public boolean isCompact() {
    return compact;
  }

/**
 * Gets the array of bits from an Aztec Code matrix
 * @return  the array of bits
 */
public boolean[] extractBitsRefactorEnvy(BitMatrix matrix) {
	boolean compact = isCompact();
	int layers = getNbLayers();
	int baseMatrixSize = (compact ? 11 : 14) + layers * 4;
	int[] alignmentMap = new int[baseMatrixSize];
	boolean[] rawbits = new boolean[totalBitsInLayer(layers, compact)];
	if (compact) {
		for (int i = 0; i < alignmentMap.length; i++) {
			alignmentMap[i] = i;
		}
	} else {
		int matrixSize = baseMatrixSize + 1 + 2 * ((baseMatrixSize / 2 - 1) / 15);
		int origCenter = baseMatrixSize / 2;
		int center = matrixSize / 2;
		for (int i = 0; i < origCenter; i++) {
			int newOffset = i + i / 15;
			alignmentMap[origCenter - i - 1] = center - newOffset - 1;
			alignmentMap[origCenter + i] = center + newOffset + 1;
		}
	}
	for (int i = 0, rowOffset = 0; i < layers; i++) {
		int rowSize = (layers - i) * 4 + (compact ? 9 : 12);
		int low = i * 2;
		int high = baseMatrixSize - 1 - low;
		for (int j = 0; j < rowSize; j++) {
			int columnOffset = j * 2;
			for (int k = 0; k < 2; k++) {
				rawbits[rowOffset + columnOffset + k] = matrix.get(alignmentMap[low + k], alignmentMap[low + j]);
				rawbits[rowOffset + 2 * rowSize + columnOffset + k] = matrix.get(alignmentMap[low + j],
						alignmentMap[high - k]);
				rawbits[rowOffset + 4 * rowSize + columnOffset + k] = matrix.get(alignmentMap[high - k],
						alignmentMap[high - j]);
				rawbits[rowOffset + 6 * rowSize + columnOffset + k] = matrix.get(alignmentMap[high - j],
						alignmentMap[low + k]);
			}
		}
		rowOffset += rowSize * 8;
	}
	return rawbits;
}

private static int totalBitsInLayer(int layers, boolean compact) {
	return ((compact ? 88 : 112) + 16 * layers) * layers;
}

}
