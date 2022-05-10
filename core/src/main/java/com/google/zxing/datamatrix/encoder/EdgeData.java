package com.google.zxing.datamatrix.encoder;

import com.google.zxing.datamatrix.encoder.MinimalEncoder.Mode;

public class EdgeData {
	public Mode mode;
	public int fromPosition;
	public int characterLength;
	public int cachedTotalSize;

	public EdgeData() {
	}
}