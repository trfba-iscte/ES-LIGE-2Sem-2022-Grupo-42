package com.google.zxing.qrcode.encoder;

public class MinimalEncoderRefactoring {

	static boolean isAlphanumeric(char c) {
	    return Encoder.getAlphanumericCode(c) != -1;
	  }

}
