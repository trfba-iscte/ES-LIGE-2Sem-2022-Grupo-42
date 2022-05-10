package com.google.zxing.common;

public class BitMatrixProduct {

	static void parseRefactor3(boolean[] bits, int bitsPos, int rowLength, BitMatrix matrix) {
		for (int i = 0; i < bitsPos; i++) {
	      if (bits[i]) {
	        matrix.set(i % rowLength, i / rowLength);
	      }
	    }
	}

	static int parseRefactor2(int bitsPos, int rowStartPos, int rowLength) {
		if (bitsPos > rowStartPos) {
	      if (rowLength == -1) {
	        rowLength = bitsPos - rowStartPos;
	      } else if (bitsPos - rowStartPos != rowLength) {
	        throw new IllegalArgumentException("row lengths do not match");
	      }
	    }
		return rowLength;
	}

	static void parseRefactor(boolean[][] image, int height, int width, BitMatrix bits) {
		for (int i = 0; i < height; i++) {
	      boolean[] imageI = image[i];
	      for (int j = 0; j < width; j++) {
	        if (imageI[j]) {
	          bits.set(j, i);
	        }
	      }
	    }
	}

	static int nRowsRefactor(int bitsPos, int rowStartPos, int rowLength, int nRows) {
		if (bitsPos > rowStartPos) {
			if (rowLength == -1) {
			} else if (bitsPos - rowStartPos != rowLength) {
				throw new IllegalArgumentException("row lengths do not match");
			}
			nRows++;
		}
		return nRows;
	}

	static int bitsPoss(String stringRepresentation, String setString, String unsetString, int bitsPos,
			int rowStartPos, int rowLength, int pos) throws IllegalArgumentException {
		if (stringRepresentation.charAt(pos) == '\n' || stringRepresentation.charAt(pos) == '\r') {
			if (bitsPos > rowStartPos) {
				if (rowLength == -1) {
				} else if (bitsPos - rowStartPos != rowLength) {
					throw new IllegalArgumentException("row lengths do not match");
				}
			}
		} else if (stringRepresentation.startsWith(setString, pos)) {
			bitsPos++;
		} else if (stringRepresentation.startsWith(unsetString, pos)) {
			bitsPos++;
		} else {
			throw new IllegalArgumentException("illegal character encountered: " + stringRepresentation.substring(pos));
		}
		return bitsPos;
	}

	static int bitsPos(String stringRepresentation, String setString, String unsetString, int bitsPos,
			int rowStartPos, int rowLength, int pos) throws IllegalArgumentException {
		if (stringRepresentation.charAt(pos) == '\n' || stringRepresentation.charAt(pos) == '\r') {
			if (bitsPos > rowStartPos) {
				if (rowLength == -1) {
				} else if (bitsPos - rowStartPos != rowLength) {
					throw new IllegalArgumentException("row lengths do not match");
				}
			}
		} else if (stringRepresentation.startsWith(setString, pos)) {
			bitsPos++;
		} else if (stringRepresentation.startsWith(unsetString, pos)) {
			bitsPos++;
		} else {
			throw new IllegalArgumentException("illegal character encountered: " + stringRepresentation.substring(pos));
		}
		return bitsPos;
	}

	static int nRows(String stringRepresentation, String setString, String unsetString)
			throws IllegalArgumentException {
		int bitsPos = 0;
		int rowStartPos = 0;
		int rowLength = -1;
		int nRows = 0;
		int pos = 0;
		while (pos < stringRepresentation.length()) {
			bitsPos = bitsPoss(stringRepresentation, setString, unsetString, bitsPos, rowStartPos, rowLength, pos);
			if (stringRepresentation.charAt(pos) == '\n' || stringRepresentation.charAt(pos) == '\r') {
				if (bitsPos > rowStartPos) {
					if (rowLength == -1) {
						rowLength = bitsPos - rowStartPos;
					} else if (bitsPos - rowStartPos != rowLength) {
						throw new IllegalArgumentException("row lengths do not match");
					}
					rowStartPos = bitsPos;
					nRows++;
				}
				pos++;
			} else if (stringRepresentation.startsWith(setString, pos)) {
				pos += setString.length();
			} else if (stringRepresentation.startsWith(unsetString, pos)) {
				pos += unsetString.length();
			} else {
				throw new IllegalArgumentException("illegal character encountered: " + stringRepresentation.substring(pos));
			}
		}
		nRows = nRowsRefactor(bitsPos, rowStartPos, rowLength, nRows);
		return nRows;
	}

	static boolean[] bits(String stringRepresentation, String setString, String unsetString)
			throws IllegalArgumentException {
		boolean[] bits = new boolean[stringRepresentation.length()];
		int bitsPos = 0;
		int rowStartPos = 0;
		int rowLength = -1;
		int pos = 0;
		while (pos < stringRepresentation.length()) {
			if (stringRepresentation.charAt(pos) == '\n' || stringRepresentation.charAt(pos) == '\r') {
				if (bitsPos > rowStartPos) {
					if (rowLength == -1) {
						rowLength = bitsPos - rowStartPos;
					} else if (bitsPos - rowStartPos != rowLength) {
						throw new IllegalArgumentException("row lengths do not match");
					}
					rowStartPos = bitsPos;
				}
				pos++;
			} else if (stringRepresentation.startsWith(setString, pos)) {
				pos += setString.length();
				bits[bitsPos] = true;
				bitsPos++;
			} else if (stringRepresentation.startsWith(unsetString, pos)) {
				pos += unsetString.length();
				bits[bitsPos] = false;
				bitsPos++;
			} else {
				throw new IllegalArgumentException("illegal character encountered: " + stringRepresentation.substring(pos));
			}
		}
		return bits;
	}

}
