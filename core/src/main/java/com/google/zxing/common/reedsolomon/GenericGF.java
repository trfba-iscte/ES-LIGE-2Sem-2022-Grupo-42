/*
 * Copyright 2007 ZXing authors
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

package com.google.zxing.common.reedsolomon;

/**
 * <p>This class contains utility methods for performing mathematical operations over
 * the Galois Fields. Operations use a given primitive polynomial in calculations.</p>
 *
 * <p>Throughout this package, elements of the GF are represented as an {@code int}
 * for convenience and speed (but at the cost of memory).
 * </p>
 *
 * @author Sean Owen
 * @author David Olivier
 */
public final class GenericGF {

  private GenericGFProduct genericGFProduct;
public static final GenericGF AZTEC_DATA_12 = new GenericGF(0x1069, 4096, 1); // x^12 + x^6 + x^5 + x^3 + 1
  public static final GenericGF AZTEC_DATA_10 = new GenericGF(0x409, 1024, 1); // x^10 + x^3 + 1
  public static final GenericGF AZTEC_DATA_6 = new GenericGF(0x43, 64, 1); // x^6 + x + 1
  public static final GenericGF AZTEC_PARAM = new GenericGF(0x13, 16, 1); // x^4 + x + 1
  public static final GenericGF QR_CODE_FIELD_256 = new GenericGF(0x011D, 256, 0); // x^8 + x^4 + x^3 + x^2 + 1
  public static final GenericGF DATA_MATRIX_FIELD_256 = new GenericGF(0x012D, 256, 1); // x^8 + x^5 + x^3 + x^2 + 1
  public static final GenericGF AZTEC_DATA_8 = DATA_MATRIX_FIELD_256;
  public static final GenericGF MAXICODE_FIELD_64 = AZTEC_DATA_6;

  private final GenericGFPoly zero;
  private final GenericGFPoly one;
  private final int size;
  private final int primitive;
  private final int generatorBase;

  /**
   * Create a representation of GF(size) using the given primitive polynomial.
   *
   * @param primitive irreducible polynomial whose coefficients are represented by
   *  the bits of an int, where the least-significant bit represents the constant
   *  coefficient
   * @param size the size of the field
   * @param b the factor b in the generator polynomial can be 0- or 1-based
   *  (g(x) = (x+a^b)(x+a^(b+1))...(x+a^(b+2t-1))).
   *  In most cases it should be 1, but for QR code it is 0.
   */
  public GenericGF(int primitive, int size, int b) {
    this.genericGFProduct = new GenericGFProduct(size);
	this.primitive = primitive;
    this.size = size;
    this.generatorBase = b;

    int x = 1;
    for (int i = 0; i < size; i++) {
      genericGFProduct.getExpTable()[i] = x;
      x *= 2; // we're assuming the generator alpha is 2
      if (x >= size) {
        x ^= primitive;
        x &= size - 1;
      }
    }
    for (int i = 0; i < size - 1; i++) {
      genericGFProduct.getLogTable()[genericGFProduct.getExpTable()[i]] = i;
    }
    // logTable[0] == 0 but this should never be used
    zero = new GenericGFPoly(this, new int[]{0});
    one = new GenericGFPoly(this, new int[]{1});
  }

  GenericGFPoly getZero() {
    return zero;
  }

  GenericGFPoly getOne() {
    return one;
  }

  /**
   * @return the monomial representing coefficient * x^degree
   */
  GenericGFPoly buildMonomial(int degree, int coefficient) {
    if (degree < 0) {
      throw new IllegalArgumentException();
    }
    if (coefficient == 0) {
      return zero;
    }
    int[] coefficients = new int[degree + 1];
    coefficients[0] = coefficient;
    return new GenericGFPoly(this, coefficients);
  }

  /**
   * Implements both addition and subtraction -- they are the same in GF(size).
   *
   * @return sum/difference of a and b
   */
  static int addOrSubtract(int a, int b) {
    return a ^ b;
  }

  /**
   * @return 2 to the power of a in GF(size)
   */
  int exp(int a) {
    return genericGFProduct.exp(a);
  }

  /**
   * @return base 2 log of a in GF(size)
   */
  int log(int a) {
    return genericGFProduct.log(a);
  }

  /**
   * @return multiplicative inverse of a
   */
  int inverse(int a) {
    if (a == 0) {
      throw new ArithmeticException();
    }
    return genericGFProduct.getExpTable()[size - genericGFProduct.getLogTable()[a] - 1];
  }

  /**
   * @return product of a and b in GF(size)
   */
  int multiply(int a, int b) {
    if (a == 0 || b == 0) {
      return 0;
    }
    return genericGFProduct.getExpTable()[(genericGFProduct.getLogTable()[a] + genericGFProduct.getLogTable()[b]) % (size - 1)];
  }

  public int getSize() {
    return size;
  }

  public int getGeneratorBase() {
    return generatorBase;
  }

  @Override
  public String toString() {
    return "GF(0x" + Integer.toHexString(primitive) + ',' + size + ')';
  }

public GenericGFPoly[] runEuclideanAlgorithm(GenericGFPoly a, GenericGFPoly b, int R) throws ReedSolomonException {
	if (a.getDegree() < b.getDegree()) {
		GenericGFPoly temp = a;
		a = b;
		b = temp;
	}
	GenericGFPoly rLast = a;
	GenericGFPoly r = b;
	GenericGFPoly tLast = getZero();
	GenericGFPoly t = getOne();
	while (2 * r.getDegree() >= R) {
		GenericGFPoly rLastLast = rLast;
		GenericGFPoly tLastLast = tLast;
		rLast = r;
		tLast = t;
		if (rLast.isZero()) {
			throw new ReedSolomonException("r_{i-1} was zero");
		}
		r = rLastLast;
		GenericGFPoly q = getZero();
		int denominatorLeadingTerm = rLast.getCoefficient(rLast.getDegree());
		int dltInverse = inverse(denominatorLeadingTerm);
		while (r.getDegree() >= rLast.getDegree() && !r.isZero()) {
			int degreeDiff = r.getDegree() - rLast.getDegree();
			int scale = multiply(r.getCoefficient(r.getDegree()), dltInverse);
			q = q.addOrSubtract(buildMonomial(degreeDiff, scale));
			r = r.addOrSubtract(rLast.multiplyByMonomial(degreeDiff, scale));
		}
		t = q.multiply(tLast).addOrSubtract(tLastLast);
		if (r.getDegree() >= rLast.getDegree()) {
			throw new IllegalStateException(
					"Division algorithm failed to reduce polynomial? " + "r: " + r + ", rLast: " + rLast);
		}
	}
	int sigmaTildeAtZero = t.getCoefficient(0);
	if (sigmaTildeAtZero == 0) {
		throw new ReedSolomonException("sigmaTilde(0) was zero");
	}
	int inverse = inverse(sigmaTildeAtZero);
	GenericGFPoly sigma = t.multiply(inverse);
	GenericGFPoly omega = r.multiply(inverse);
	return new GenericGFPoly[] { sigma, omega };
}

public int[] findErrorMagnitudes(GenericGFPoly errorEvaluator, int[] errorLocations) {
	int s = errorLocations.length;
	int[] result = new int[s];
	for (int i = 0; i < s; i++) {
		int xiInverse = inverse(errorLocations[i]);
		int denominator = 1;
		for (int j = 0; j < s; j++) {
			if (i != j) {
				int term = multiply(errorLocations[j], xiInverse);
				int termPlus1 = (term & 0x1) == 0 ? term | 1 : term & ~1;
				denominator = multiply(denominator, termPlus1);
			}
		}
		result[i] = multiply(errorEvaluator.evaluateAt(xiInverse), inverse(denominator));
		if (getGeneratorBase() != 0) {
			result[i] = multiply(result[i], xiInverse);
		}
	}
	return result;
}

/**
 * <p>Decodes given set of received codewords, which include both data and error-correction codewords. Really, this means it uses Reed-Solomon to detect and correct errors, in-place, in the input.</p>
 * @param received  data and error-correction codewords
 * @param twoS  number of error-correction codewords available
 * @throws ReedSolomonException  if decoding fails for any reason
 */
public void decode(int[] received, int twoS) throws ReedSolomonException {
	GenericGFPoly poly = new GenericGFPoly(this, received);
	int[] syndromeCoefficients = new int[twoS];
	boolean noError = true;
	for (int i = 0; i < twoS; i++) {
		int eval = poly.evaluateAt(genericGFProduct.exp(i + getGeneratorBase()));
		syndromeCoefficients[syndromeCoefficients.length - 1 - i] = eval;
		if (eval != 0) {
			noError = false;
		}
	}
	if (noError) {
		return;
	}
	GenericGFPoly syndrome = new GenericGFPoly(this, syndromeCoefficients);
	GenericGFPoly[] sigmaOmega = runEuclideanAlgorithm(buildMonomial(twoS, 1), syndrome, twoS);
	GenericGFPoly sigma = sigmaOmega[0];
	GenericGFPoly omega = sigmaOmega[1];
	int[] errorLocations = sigma.findErrorLocations(this);
	int[] errorMagnitudes = findErrorMagnitudes(omega, errorLocations);
	for (int i = 0; i < errorLocations.length; i++) {
		int position = received.length - 1 - genericGFProduct.log(errorLocations[i]);
		if (position < 0) {
			throw new ReedSolomonException("Bad error location");
		}
		received[position] = GenericGF.addOrSubtract(received[position], errorMagnitudes[i]);
	}
}

}
