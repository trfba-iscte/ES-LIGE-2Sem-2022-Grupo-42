package com.google.zxing.datamatrix.encoder;


public class SymbolInfoProductRefactoring2 {
	private SymbolInfoProductRefactoring symbolInfoProductRefactoring;

	public SymbolInfoProductRefactoring getSymbolInfoProductRefactoring() {
		return symbolInfoProductRefactoring;
	}

	public void setSymbolInfoProductRefactoring(SymbolInfoProductRefactoring symbolInfoProductRefactoring) {
		this.symbolInfoProductRefactoring = symbolInfoProductRefactoring;
	}

	public final int getSymbolDataWidth(int thisMatrixWidth) {
		return symbolInfoProductRefactoring.getHorizontalDataRegions() * thisMatrixWidth;
	}

	public final int getSymbolWidth(int thisMatrixWidth) {
		return getSymbolDataWidth(thisMatrixWidth) + (symbolInfoProductRefactoring.getHorizontalDataRegions() * 2);
	}
}