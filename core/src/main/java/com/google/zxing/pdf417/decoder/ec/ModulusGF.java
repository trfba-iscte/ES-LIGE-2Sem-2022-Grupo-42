/*
 * Copyright 2012 ZXing authors
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

package com.google.zxing.pdf417.decoder.ec;

import com.google.zxing.ChecksumException;
import com.google.zxing.pdf417.PDF417Common;

/**
 * <p>A field based on powers of a generator integer, modulo some modulus.</p>
 *
 * @author Sean Owen
 * @see com.google.zxing.common.reedsolomon.GenericGF
 */
public final class ModulusGF {

  public static final ModulusGF PDF417_GF = new ModulusGF(PDF417Common.NUMBER_OF_CODEWORDS, 3);

  private final int[] expTable;
  private final int[] logTable;
  private final ModulusPoly zero;
  private final ModulusPoly one;
  private final int modulus;

  private ModulusGF(int modulus, int generator) {
    this.modulus = modulus;
    expTable = new int[modulus];
    logTable = new int[modulus];
    int x = 1;
    for (int i = 0; i < modulus; i++) {
      expTable[i] = x;
      x = (x * generator) % modulus;
    }
    for (int i = 0; i < modulus - 1; i++) {
      logTable[expTable[i]] = i;
    }
    // logTable[0] == 0 but this should never be used
    zero = new ModulusPoly(this, new int[]{0});
    one = new ModulusPoly(this, new int[]{1});
  }


  ModulusPoly getZero() {
    return zero;
  }

  ModulusPoly getOne() {
    return one;
  }

  ModulusPoly buildMonomial(int degree, int coefficient) {
    if (degree < 0) {
      throw new IllegalArgumentException();
    }
    if (coefficient == 0) {
      return zero;
    }
    int[] coefficients = new int[degree + 1];
    coefficients[0] = coefficient;
    return new ModulusPoly(this, coefficients);
  }

  int add(int a, int b) {
    return (a + b) % modulus;
  }

  int subtract(int a, int b) {
    return (modulus + a - b) % modulus;
  }

  int exp(int a) {
    return expTable[a];
  }

  int log(int a) {
    if (a == 0) {
      throw new IllegalArgumentException();
    }
    return logTable[a];
  }

  int inverse(int a) {
    if (a == 0) {
      throw new ArithmeticException();
    }
    return expTable[modulus - logTable[a] - 1];
  }

  int multiply(int a, int b) {
    if (a == 0 || b == 0) {
      return 0;
    }
    return expTable[(logTable[a] + logTable[b]) % (modulus - 1)];
  }

  int getSize() {
    return modulus;
  }


public ModulusPoly[] runEuclideanAlgorithm(ModulusPoly a, ModulusPoly b, int R) throws ChecksumException {
	if (a.getDegree() < b.getDegree()) {
		ModulusPoly temp = a;
		a = b;
		b = temp;
	}
	ModulusPoly rLast = a;
	ModulusPoly r = b;
	ModulusPoly tLast = getZero();
	ModulusPoly t = getOne();
	while (r.getDegree() >= R / 2) {
		ModulusPoly rLastLast = rLast;
		ModulusPoly tLastLast = tLast;
		rLast = r;
		tLast = t;
		if (rLast.isZero()) {
			throw ChecksumException.getChecksumInstance();
		}
		r = rLastLast;
		ModulusPoly q = getZero();
		int denominatorLeadingTerm = rLast.getCoefficient(rLast.getDegree());
		int dltInverse = inverse(denominatorLeadingTerm);
		while (r.getDegree() >= rLast.getDegree() && !r.isZero()) {
			int degreeDiff = r.getDegree() - rLast.getDegree();
			int scale = multiply(r.getCoefficient(r.getDegree()), dltInverse);
			q = q.add(buildMonomial(degreeDiff, scale));
			r = r.subtract(rLast.multiplyByMonomial(degreeDiff, scale));
		}
		t = q.multiply(tLast).subtract(tLastLast).negative();
	}
	int sigmaTildeAtZero = t.getCoefficient(0);
	if (sigmaTildeAtZero == 0) {
		throw ChecksumException.getChecksumInstance();
	}
	int inverse = inverse(sigmaTildeAtZero);
	ModulusPoly sigma = t.multiply(inverse);
	ModulusPoly omega = r.multiply(inverse);
	return new ModulusPoly[] { sigma, omega };
}


public int[] findErrorMagnitudes(ModulusPoly errorEvaluator, ModulusPoly errorLocator, int[] errorLocations) {
	int errorLocatorDegree = errorLocator.getDegree();
	if (errorLocatorDegree < 1) {
		return new int[0];
	}
	int[] formalDerivativeCoefficients = new int[errorLocatorDegree];
	for (int i = 1; i <= errorLocatorDegree; i++) {
		formalDerivativeCoefficients[errorLocatorDegree - i] = multiply(i, errorLocator.getCoefficient(i));
	}
	ModulusPoly formalDerivative = new ModulusPoly(this, formalDerivativeCoefficients);
	int s = errorLocations.length;
	int[] result = new int[s];
	for (int i = 0; i < s; i++) {
		int xiInverse = inverse(errorLocations[i]);
		int numerator = subtract(0, errorEvaluator.evaluateAt(xiInverse));
		int denominator = inverse(formalDerivative.evaluateAt(xiInverse));
		result[i] = multiply(numerator, denominator);
	}
	return result;
}


public int[] findErrorLocations(ModulusPoly errorLocator) throws ChecksumException {
	int numErrors = errorLocator.getDegree();
	int[] result = new int[numErrors];
	int e = 0;
	for (int i = 1; i < getSize() && e < numErrors; i++) {
		if (errorLocator.evaluateAt(i) == 0) {
			result[e] = inverse(i);
			e++;
		}
	}
	if (e != numErrors) {
		throw ChecksumException.getChecksumInstance();
	}
	return result;
}


/**
 * @param received  received codewords
 * @param numECCodewords  number of those codewords used for EC
 * @param erasures  location of erasures
 * @return  number of errors
 * @throws ChecksumException  if errors cannot be corrected, maybe because of too many errors
 */
public int decode(int[] received, int numECCodewords, int[] erasures) throws ChecksumException {
	ModulusPoly poly = new ModulusPoly(this, received);
	int[] S = new int[numECCodewords];
	boolean error = false;
	for (int i = numECCodewords; i > 0; i--) {
		int eval = poly.evaluateAt(exp(i));
		S[numECCodewords - i] = eval;
		if (eval != 0) {
			error = true;
		}
	}
	if (!error) {
		return 0;
	}
	ModulusPoly knownErrors = getOne();
	if (erasures != null) {
		for (int erasure : erasures) {
			int b = exp(received.length - 1 - erasure);
			ModulusPoly term = new ModulusPoly(this, new int[] { subtract(0, b), 1 });
			knownErrors = knownErrors.multiply(term);
		}
	}
	ModulusPoly syndrome = new ModulusPoly(this, S);
	ModulusPoly[] sigmaOmega = runEuclideanAlgorithm(buildMonomial(numECCodewords, 1), syndrome, numECCodewords);
	ModulusPoly sigma = sigmaOmega[0];
	ModulusPoly omega = sigmaOmega[1];
	int[] errorLocations = findErrorLocations(sigma);
	int[] errorMagnitudes = findErrorMagnitudes(omega, sigma, errorLocations);
	for (int i = 0; i < errorLocations.length; i++) {
		int position = received.length - 1 - log(errorLocations[i]);
		if (position < 0) {
			throw ChecksumException.getChecksumInstance();
		}
		received[position] = subtract(received[position], errorMagnitudes[i]);
	}
	return errorLocations.length;
}

}
