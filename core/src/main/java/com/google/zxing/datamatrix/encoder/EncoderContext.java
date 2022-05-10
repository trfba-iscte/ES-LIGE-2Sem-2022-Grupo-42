/*
 * Copyright 2006-2007 Jeremias Maerki.
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

import com.google.zxing.Dimension;

import java.nio.charset.StandardCharsets;

final class EncoderContext {

  EncoderContextData data = new EncoderContextData();

EncoderContext(String msg) {
    //From this point on Strings are not Unicode anymore!
    byte[] msgBinary = msg.getBytes(StandardCharsets.ISO_8859_1);
    StringBuilder sb = new StringBuilder(msgBinary.length);
    for (int i = 0, c = msgBinary.length; i < c; i++) {
      char ch = (char) (msgBinary[i] & 0xff);
      if (ch == '?' && msg.charAt(i) != '?') {
        throw new IllegalArgumentException("Message contains characters outside ISO-8859-1 encoding.");
      }
      sb.append(ch);
    }
    this.data.msg = sb.toString(); //Not Unicode here!
    data.shape = SymbolShapeHint.FORCE_NONE;
    this.data.codewords = new StringBuilder(msg.length());
    data.newEncoding = -1;
  }

  public void setSymbolShape(SymbolShapeHint shape) {
    this.data.shape = shape;
  }

  public void setSizeConstraints(Dimension minSize, Dimension maxSize) {
    this.data.minSize = minSize;
    this.data.maxSize = maxSize;
  }

  public String getMessage() {
    return this.data.msg;
  }

  public void setSkipAtEnd(int count) {
    this.data.skipAtEnd = count;
  }

  public char getCurrentChar() {
    return data.msg.charAt(data.pos);
  }

  public char getCurrent() {
    return data.msg.charAt(data.pos);
  }

  public StringBuilder getCodewords() {
    return data.codewords;
  }

  public void writeCodewords(String codewords) {
    this.data.codewords.append(codewords);
  }

  public void writeCodeword(char codeword) {
    this.data.codewords.append(codeword);
  }

  public int getCodewordCount() {
    return this.data.codewords.length();
  }

  public int getNewEncoding() {
    return data.newEncoding;
  }

  public void signalEncoderChange(int encoding) {
    this.data.newEncoding = encoding;
  }

  public void resetEncoderSignal() {
    this.data.newEncoding = -1;
  }

  public boolean hasMoreCharacters() {
    return data.pos < getTotalMessageCharCount();
  }

  private int getTotalMessageCharCount() {
    return data.msg.length() - data.skipAtEnd;
  }

  public int getRemainingCharacters() {
    return getTotalMessageCharCount() - data.pos;
  }

  public SymbolInfo getSymbolInfo() {
    return data.symbolInfo;
  }

  public void updateSymbolInfo() {
    updateSymbolInfo(getCodewordCount());
  }

  public void updateSymbolInfo(int len) {
    if (this.data.symbolInfo == null || len > this.data.symbolInfo.getDataCapacity()) {
      this.data.symbolInfo = SymbolInfo.lookup(len, data.shape, data.minSize, data.maxSize, true);
    }
  }

  public void resetSymbolInfo() {
    this.data.symbolInfo = null;
  }

public void handleEODRefactoring() {
	if (hasMoreCharacters()) {
		writeCodeword(HighLevelEncoder.C40_UNLATCH);
	}
}

public void encodeMaximal(C40Encoder c40Encoder) {
	StringBuilder buffer = new StringBuilder();
	int lastCharSize = 0;
	int backtrackStartPosition = this.data.pos;
	int backtrackBufferLength = 0;
	while (hasMoreCharacters()) {
		char c = getCurrentChar();
		this.data.pos++;
		lastCharSize = c40Encoder.encodeChar(c, buffer);
		if (buffer.length() % 3 == 0) {
			backtrackStartPosition = this.data.pos;
			backtrackBufferLength = buffer.length();
		}
	}
	if (backtrackBufferLength != buffer.length()) {
		int unwritten = (buffer.length() / 3) * 2;
		int curCodewordCount = getCodewordCount() + unwritten + 1;
		updateSymbolInfo(curCodewordCount);
		int available = getSymbolInfo().getDataCapacity() - curCodewordCount;
		int rest = buffer.length() % 3;
		if ((rest == 2 && available != 2) || (rest == 1 && (lastCharSize > 3 || available != 1))) {
			buffer.setLength(backtrackBufferLength);
			this.data.pos = backtrackStartPosition;
		}
	}
	if (buffer.length() > 0) {
		writeCodeword(HighLevelEncoder.LATCH_TO_C40);
	}
	c40Encoder.handleEOD(this, buffer);
}

public int backtrackOneCharacter(StringBuilder buffer, StringBuilder removed, int lastCharSize, C40Encoder c40Encoder) {
	int count = buffer.length();
	buffer.delete(count - lastCharSize, count);
	this.data.pos--;
	char c = getCurrentChar();
	lastCharSize = c40Encoder.encodeChar(c, removed);
	resetSymbolInfo();
	return c40Encoder.encodeCharRefactoring7(lastCharSize);
}
}
