package com.google.zxing.datamatrix.encoder;

import com.google.zxing.Dimension;

public class EncoderContextData {
	public String msg;
	public SymbolShapeHint shape;
	public Dimension minSize;
	public Dimension maxSize;
	public StringBuilder codewords;
	public int pos;
	public int newEncoding;
	public SymbolInfo symbolInfo;
	public int skipAtEnd;

	public EncoderContextData() {
	}
}