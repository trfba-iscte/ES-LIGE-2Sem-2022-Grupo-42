/*
 * Copyright 2006 Jeremias Maerki.
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

package com.google.zxing.datamatrix.encoder;

import java.util.Arrays;

/**
 * Symbol Character Placement Program. Adapted from Annex M.1 in ISO/IEC 16022:2000(E).
 */
public class DefaultPlacement {

  private DefaultPlacementData1 data = new DefaultPlacementData1(new DefaultPlacementDataRefactoring1(new DefaultPlacementDataRefactoring()));

/**
   * Main constructor
   *
   * @param codewords the codewords to place
   * @param numcols   the number of columns
   * @param numrows   the number of rows
   */
  public DefaultPlacement(CharSequence codewords, int numcols, int numrows) {
    this.data.data.data.codewords = codewords;
    this.data.data.data.numcols = numcols;
    this.data.data.data.numrows = numrows;
    this.data.data.data.bits = new byte[numcols * numrows];
    Arrays.fill(this.data.data.data.bits, (byte) -1); //Initialize with "not set" value
  }

  final int getNumrows() {
    return data.data.data.numrows;
  }

  final int getNumcols() {
    return data.data.data.numcols;
  }

  final byte[] getBits() {
    return data.data.data.bits;
  }

  public final boolean getBit(int col, int row) {
    return data.data.data.bits[row * data.data.data.numcols + col] == 1;
  }

  private void setBit(int col, int row, boolean bit) {
    data.data.data.bits[row * data.data.data.numcols + col] = (byte) (bit ? 1 : 0);
  }

  private boolean noBit(int col, int row) {
    return data.data.data.bits[row * data.data.data.numcols + col] < 0;
  }

  public final void place() {
    int pos = 0;
    int row = 4;
    int col = 0;

    do {
      // repeatedly first check for one of the special corner cases, then...
      pos = placeRefactoring(pos, row, col);
      pos = placeRefactoring2(pos, row, col);
      pos = replaceRefactoring3(pos, row, col);
      pos = replaceRefactoring4(pos, row, col);
      // sweep upward diagonally, inserting successive characters...
      do {
        pos = replaceRefactoring5(pos, row, col);
        row -= 2;
        col += 2;
      } while (row >= 0 && (col < data.data.data.numcols));
      row++;
      col += 3;

      // and then sweep downward diagonally, inserting successive characters, ...
      do {
        pos = replaceRefactoring6(pos, row, col);
        row += 2;
        col -= 2;
      } while ((row < data.data.data.numrows) && (col >= 0));
      row += 3;
      col++;

      // ...until the entire array is scanned
    } while ((row < data.data.data.numrows) || (col < data.data.data.numcols));

    // Lastly, if the lower right-hand corner is untouched, fill in fixed pattern
    if (noBit(data.data.data.numcols - 1, data.data.data.numrows - 1)) {
      setBit(data.data.data.numcols - 1, data.data.data.numrows - 1, true);
      setBit(data.data.data.numcols - 2, data.data.data.numrows - 2, true);
    }
  }

private int replaceRefactoring6(int pos, int row, int col) {
	if ((row >= 0) && (col < data.data.data.numcols) && noBit(col, row)) {
	  utah(row, col, pos++);
	}
	return pos;
}

private int replaceRefactoring5(int pos, int row, int col) {
	if ((row < data.data.data.numrows) && (col >= 0) && noBit(col, row)) {
	  utah(row, col, pos++);
	}
	return pos;
}

private int replaceRefactoring4(int pos, int row, int col) {
	if ((row == data.data.data.numrows + 4) && (col == 2) && ((data.data.data.numcols % 8) == 0)) {
        corner4(pos++);
      }
	return pos;
}

private int replaceRefactoring3(int pos, int row, int col) {
	if ((row == data.data.data.numrows - 2) && (col == 0) && (data.data.data.numcols % 8 == 4)) {
        corner3(pos++);
      }
	return pos;
}

private int placeRefactoring2(int pos, int row, int col) {
	if ((row == data.data.data.numrows - 2) && (col == 0) && ((data.data.data.numcols % 4) != 0)) {
        corner2(pos++);
      }
	return pos;
}

private int placeRefactoring(int pos, int row, int col) {
	if ((row == data.data.data.numrows) && (col == 0)) {
        corner1(pos++);
      }
	return pos;
}

  private void module(int row, int col, int pos, int bit) {
    if (row < 0) {
      row += data.data.data.numrows;
      col += 4 - ((data.data.data.numrows + 4) % 8);
    }
    if (col < 0) {
      col += data.data.data.numcols;
      row += 4 - ((data.data.data.numcols + 4) % 8);
    }
    // Note the conversion:
    int v = data.data.data.codewords.charAt(pos);
    v &= 1 << (8 - bit);
    setBit(col, row, v != 0);
  }

  /**
   * Places the 8 bits of a utah-shaped symbol character in ECC200.
   *
   * @param row the row
   * @param col the column
   * @param pos character position
   */
  private void utah(int row, int col, int pos) {
    module(row - 2, col - 2, pos, 1);
    module(row - 2, col - 1, pos, 2);
    module(row - 1, col - 2, pos, 3);
    module(row - 1, col - 1, pos, 4);
    module(row - 1, col, pos, 5);
    module(row, col - 2, pos, 6);
    module(row, col - 1, pos, 7);
    module(row, col, pos, 8);
  }

  private void corner1(int pos) {
    module(data.data.data.numrows - 1, 0, pos, 1);
    module(data.data.data.numrows - 1, 1, pos, 2);
    module(data.data.data.numrows - 1, 2, pos, 3);
    module(0, data.data.data.numcols - 2, pos, 4);
    module(0, data.data.data.numcols - 1, pos, 5);
    module(1, data.data.data.numcols - 1, pos, 6);
    module(2, data.data.data.numcols - 1, pos, 7);
    module(3, data.data.data.numcols - 1, pos, 8);
  }

  private void corner2(int pos) {
    module(data.data.data.numrows - 3, 0, pos, 1);
    module(data.data.data.numrows - 2, 0, pos, 2);
    module(data.data.data.numrows - 1, 0, pos, 3);
    module(0, data.data.data.numcols - 4, pos, 4);
    module(0, data.data.data.numcols - 3, pos, 5);
    module(0, data.data.data.numcols - 2, pos, 6);
    module(0, data.data.data.numcols - 1, pos, 7);
    module(1, data.data.data.numcols - 1, pos, 8);
  }

  private void corner3(int pos) {
    module(data.data.data.numrows - 3, 0, pos, 1);
    module(data.data.data.numrows - 2, 0, pos, 2);
    module(data.data.data.numrows - 1, 0, pos, 3);
    module(0, data.data.data.numcols - 2, pos, 4);
    module(0, data.data.data.numcols - 1, pos, 5);
    module(1, data.data.data.numcols - 1, pos, 6);
    module(2, data.data.data.numcols - 1, pos, 7);
    module(3, data.data.data.numcols - 1, pos, 8);
  }

  private void corner4(int pos) {
    module(data.data.data.numrows - 1, 0, pos, 1);
    module(data.data.data.numrows - 1, data.data.data.numcols - 1, pos, 2);
    module(0, data.data.data.numcols - 3, pos, 3);
    module(0, data.data.data.numcols - 2, pos, 4);
    module(0, data.data.data.numcols - 1, pos, 5);
    module(1, data.data.data.numcols - 3, pos, 6);
    module(1, data.data.data.numcols - 2, pos, 7);
    module(1, data.data.data.numcols - 1, pos, 8);
  }

}
