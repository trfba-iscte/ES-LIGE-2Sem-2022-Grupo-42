package com.google.zxing.common.reedsolomon;


public class GenericGFProduct {
	private final int[] expTable;
	private final int[] logTable;

	public GenericGFProduct(int size) {
		expTable = new int[size];
		logTable = new int[size];
	}

	public int[] getExpTable() {
		return expTable;
	}

	public int[] getLogTable() {
		return logTable;
	}

	/**
	* @return  2 to the power of a in GF(size)
	*/
	public int exp(int a) {
		return expTable[a];
	}

	/**
	* @return  base 2 log of a in GF(size)
	*/
	public int log(int a) {
		if (a == 0) {
			throw new IllegalArgumentException();
		}
		return logTable[a];
	}
}