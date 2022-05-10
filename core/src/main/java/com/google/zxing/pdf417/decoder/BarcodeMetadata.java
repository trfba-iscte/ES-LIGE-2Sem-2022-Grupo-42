/*
 * Copyright 2013 ZXing authors
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

package com.google.zxing.pdf417.decoder;

/**
 * @author Guenther Grau
 */
final class BarcodeMetadata {

  private final int columnCount;
  private final int errorCorrectionLevel;
  private final int rowCountUpperPart;
  private final int rowCountLowerPart;
  private final int rowCount;

  BarcodeMetadata(int columnCount, int rowCountUpperPart, int rowCountLowerPart, int errorCorrectionLevel) {
    this.columnCount = columnCount;
    this.errorCorrectionLevel = errorCorrectionLevel;
    this.rowCountUpperPart = rowCountUpperPart;
    this.rowCountLowerPart = rowCountLowerPart;
    this.rowCount = rowCountUpperPart + rowCountLowerPart;
  }

  int getColumnCount() {
    return columnCount;
  }

  int getErrorCorrectionLevel() {
    return errorCorrectionLevel;
  }

  int getRowCount() {
    return rowCount;
  }

  int getRowCountUpperPart() {
    return rowCountUpperPart;
  }

  int getRowCountLowerPart() {
    return rowCountLowerPart;
  }

public void removeIncorrectCodewordsRefactoring(Codeword[] codewords, int codewordRow, int rowIndicatorValue,
		int codewordRowNumber) {
	switch (codewordRowNumber % 3) {
	case 0:
		if (rowIndicatorValue * 3 + 1 != getRowCountUpperPart()) {
			codewords[codewordRow] = null;
		}
		break;
	case 1:
		if (rowIndicatorValue / 3 != getErrorCorrectionLevel() || rowIndicatorValue % 3 != getRowCountLowerPart()) {
			codewords[codewordRow] = null;
		}
		break;
	case 2:
		if (rowIndicatorValue + 1 != getColumnCount()) {
			codewords[codewordRow] = null;
		}
		break;
	}
}

public void removeIncorrectCodewords(Codeword[] codewords, boolean isLeft) {
	for (int codewordRow = 0; codewordRow < codewords.length; codewordRow++) {
		Codeword codeword = codewords[codewordRow];
		if (codewords[codewordRow] == null) {
			continue;
		}
		int rowIndicatorValue = codeword.getValue() % 30;
		int codewordRowNumber = codeword.getRowNumber();
		if (codewordRowNumber > getRowCount()) {
			codewords[codewordRow] = null;
			continue;
		}
		if (!isLeft) {
			codewordRowNumber += 2;
		}
		removeIncorrectCodewordsRefactoring(codewords, codewordRow, rowIndicatorValue, codewordRowNumber);
	}
}

}
