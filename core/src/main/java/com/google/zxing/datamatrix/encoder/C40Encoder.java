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

class C40Encoder implements Encoder {

  @Override
  public int getEncodingMode() {
    return HighLevelEncoder.C40_ENCODATION;
  }

  void encodeMaximal(EncoderContext context) {
    context.encodeMaximal(this);
  }

  @Override
  public void encode(EncoderContext context) {
    //step C
    StringBuilder buffer = new StringBuilder();
    while (context.hasMoreCharacters()) {
      char c = context.getCurrentChar();
      context.data.pos++;

      int lastCharSize = encodeChar(c, buffer);

      int unwritten = (buffer.length() / 3) * 2;

      int curCodewordCount = context.getCodewordCount() + unwritten;
      context.updateSymbolInfo(curCodewordCount);
      int available = context.getSymbolInfo().getDataCapacity() - curCodewordCount;

      if (!context.hasMoreCharacters()) {
        //Avoid having a single C40 value in the last triplet
        StringBuilder removed = new StringBuilder();
        if ((buffer.length() % 3) == 2 && available != 2) {
          lastCharSize = context.backtrackOneCharacter(buffer, removed, lastCharSize, this);
        }
        while ((buffer.length() % 3) == 1 && (lastCharSize > 3 || available != 1)) {
          lastCharSize = context.backtrackOneCharacter(buffer, removed, lastCharSize, this);
        }
        break;
      }

      int count = buffer.length();
      if ((count % 3) == 0) {
        int newMode = HighLevelEncoder.lookAheadTest(context.getMessage(), context.data.pos, getEncodingMode());
        if (newMode != getEncodingMode()) {
          // Return to ASCII encodation, which will actually handle latch to new mode
          context.signalEncoderChange(HighLevelEncoder.ASCII_ENCODATION);
          break;
        }
      }
    }
    handleEOD(context, buffer);
  }

  static void writeNextTriplet(EncoderContext context, StringBuilder buffer) {
    context.writeCodewords(encodeToCodewords(buffer));
    buffer.delete(0, 3);
  }

  /**
   * Handle "end of data" situations
   *
   * @param context the encoder context
   * @param buffer  the buffer with the remaining encoded characters
   */
  public void handleEOD(EncoderContext context, StringBuilder buffer) {
    int unwritten = (buffer.length() / 3) * 2;
    int rest = buffer.length() % 3;

    int curCodewordCount = context.getCodewordCount() + unwritten;
    context.updateSymbolInfo(curCodewordCount);
    int available = context.getSymbolInfo().getDataCapacity() - curCodewordCount;

    if (rest == 2) {
      buffer.append('\0'); //Shift 1
      while (buffer.length() >= 3) {
        writeNextTriplet(context, buffer);
      }
      context.handleEODRefactoring();
    } else if (available == 1 && rest == 1) {
      while (buffer.length() >= 3) {
        writeNextTriplet(context, buffer);
      }
      context.handleEODRefactoring();
      // else no unlatch
      context.data.pos--;
    } else if (rest == 0) {
      while (buffer.length() >= 3) {
        writeNextTriplet(context, buffer);
      }
      if (available > 0 || context.hasMoreCharacters()) {
        context.writeCodeword(HighLevelEncoder.C40_UNLATCH);
      }
    } else {
      throw new IllegalStateException("Unexpected case. Please report!");
    }
    context.signalEncoderChange(HighLevelEncoder.ASCII_ENCODATION);
  }

public int encodeChar(char c, StringBuilder sb) {
    return encodeCharRefactoring12(c, sb);
  }

private int encodeCharRefactoring12(char c, StringBuilder sb) {
	if (c == ' ') {
      encodeCharRefactoring6(sb);
      return 1;
    }
    if (c >= '0' && c <= '9') {
      encodeCharRefactoring6(c, sb);
      return 1;
    }
    if (c >= 'A' && c <= 'Z') {
      encodeCharRefactoring5(c, sb);
      return 1;
    }
    if (c < ' ') {
      encodeCharRefactoring7(c, sb);
      return encodeCharRefactoring9();
    }
    if (c <= '/') {
      encodeCharRefactoring9(c, sb);
      return encodeCharRefactoring9();
    }
    if (c <= '@') {
      sb.append('\1'); //Shift 2 Set
      sb.append((char) (c - 58 + 15));
      return encodeCharRefactoring9();
    }
    if (c <= '_') {
      encodeCharRefactoring4(c, sb);
      return encodeCharRefactoring9();
    }
    if (c <= 127) {
      encodeCharRefactoring3(c, sb);
      return encodeCharRefactoring9();
    }
    return encodeCharRefactoring8(c, sb);
}

private int encodeCharRefactoring9() {
	return 2;
}

private void encodeCharRefactoring9(char c, StringBuilder sb) {
	sb.append('\1'); //Shift 2 Set
      sb.append((char) (c - 33));
}

private int encodeCharRefactoring8(char c, StringBuilder sb) {
	int len = encodeCharRefactoring1(c, sb);
    return encodeCharRefactoring7(len);
}

public int encodeCharRefactoring7(int len) {
	return len;
}

private void encodeCharRefactoring7(char c, StringBuilder sb) {
	sb.append('\0'); //Shift 1 Set
      sb.append(c);
}

private void encodeCharRefactoring6(StringBuilder sb) {
	sb.append('\3');
}

private void encodeCharRefactoring6(char c, StringBuilder sb) {
	sb.append((char) (c - 48 + 4));
}

private void encodeCharRefactoring5(char c, StringBuilder sb) {
	sb.append((char) (c - 65 + 14));
}

private void encodeCharRefactoring4(char c, StringBuilder sb) {
	sb.append('\1'); //Shift 2 Set
      sb.append((char) (c - 91 + 22));
}

private void encodeCharRefactoring3(char c, StringBuilder sb) {
	sb.append('\2'); //Shift 3 Set
      sb.append((char) (c - 96));
}

private int encodeCharRefactoring1(char c, StringBuilder sb) {
	int len = encodeCharRefactoring(c, sb);
	return encodeCharRefactoring7(len);
}

private int encodeCharRefactoring(char c, StringBuilder sb) {
	sb.append("\1\u001e"); //Shift 2, Upper Shift
    int len = 2;
    len += encodeChar((char) (c - 128), sb);
	return encodeCharRefactoring7(len);
}

  private static String encodeToCodewords(CharSequence sb) {
    int v = (1600 * sb.charAt(0)) + (40 * sb.charAt(1)) + sb.charAt(2) + 1;
    char cw1 = (char) (v / 256);
    char cw2 = (char) (v % 256);
    return new String(new char[] {cw1, cw2});
  }

}
