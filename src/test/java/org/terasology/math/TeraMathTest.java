/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.math;

import org.junit.Test;

import org.junit.Assert;

/**
 * @author Immortius
 */
public class TeraMathTest {

	private static final double MAX_DOUBLE_ERROR = 0.00001;

    @Test
    public void getEdgeRegion() {
        Region3i region = Region3i.createFromMinAndSize(new Vector3i(16,0,16), new Vector3i(16,128,16));
        Assert.assertEquals(Region3i.createFromMinMax(new Vector3i(16, 0, 16), new Vector3i(16, 127, 31)), TeraMath.getEdgeRegion(region, Side.LEFT));
    }

	// This is a slow yet simple implementation of a basic power function
	private int naivePow(int base, int exp) {
		if (base == 0) {
			return 0;
		}
		if (exp < 0) {
			base = 1 / base;
			exp = -exp;
		}
		int result = 1;
		while (exp-- > 0) {
			result *= base;
		}
		return result;
	}

	@Test
	public void powDouble() {
		for (int exp = -5; exp <= 5; exp++) {
			for (double base = -10.0; base <= 10.0; base += 0.2f) {
				checkError(Math.pow(base, exp) / TeraMath.pow(base, exp));
			}
		}
	}

	@Test
	public void powInt() {
		// Only from -2 because most negative exponents return a 0
		for (int exp = -2; exp <= 8; exp++) {
			for (int base = -8; base <= 8; base++) {
				int naive = naivePow(base, exp);
				Assert.assertEquals(TeraMath.pow(base, exp), naive);
				Assert.assertEquals(TeraMath.pow((long) base, exp), naive);
			}
		}
	}

	private void checkError(double error) {
		if (error < 0.0) {
			error = 1.0 / error;
		}
		error -= 1.0;
		Assert.assertTrue(error <= MAX_DOUBLE_ERROR);
	}
}
