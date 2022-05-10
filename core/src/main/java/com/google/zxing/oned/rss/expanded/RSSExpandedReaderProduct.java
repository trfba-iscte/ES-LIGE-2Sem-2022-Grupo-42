package com.google.zxing.oned.rss.expanded;


import com.google.zxing.NotFoundException;

public class RSSExpandedReaderProduct {
	public int RSSExpandedReader1(float value, int count) throws NotFoundException {
		if (count < 1) {
			if (value < 0.3f) {
				throw NotFoundException.getNotFoundInstance();
			}
			count = 1;
		} else if (count > 8) {
			if (value > 8.7f) {
				throw NotFoundException.getNotFoundInstance();
			}
			count = 8;
		}
		return count;
	}

	public int[] RSSExpandedReader5(int[] counters, float elementWidth, int[] evenCounts) throws NotFoundException {
		for (int i = 0; i < counters.length; i++) {
			float value = 1.0f * counters[i] / elementWidth;
			int count = (int) (value + 0.5f);
			count = RSSExpandedReader1(value, count);
			int offset = i / 2;
			if ((i & 0x01) == 0) {
			} else {
				evenCounts[offset] = count;
			}
		}
		return evenCounts;
	}

	public int[] RSSExpandedReader4(int[] counters, float elementWidth, int[] oddCounts, int i)
			throws NotFoundException {
		float value = 1.0f * counters[i] / elementWidth;
		int count = (int) (value + 0.5f);
		count = RSSExpandedReader1(value, count);
		int offset = i / 2;
		if ((i & 0x01) == 0) {
			oddCounts[offset] = count;
		} else {
		}
		return oddCounts;
	}
}