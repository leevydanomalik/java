/**
 * The MIT License
 * Copyright (c) 2014 Luc Pezet
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.github.lpezet.java.patterns.loadbalancer;

import java.security.SecureRandom;

/**
 * @author Luc Pezet
 *
 */
public class RandomLoadBalancer<T> extends SimpleListLoadBalancer<T> {

	private static interface IRandomizer {
		double next();
	}

	private static class SecureRandomizer implements IRandomizer {
		private SecureRandom mSecureRandom;

		public SecureRandomizer(SecureRandom pRandom) {
			mSecureRandom = pRandom;
		}

		@Override
		public double next() {
			return mSecureRandom.nextDouble();
		}
	}

	private static class MathRandomizer implements IRandomizer {

		@Override
		public double next() {
			return Math.random();
		}
	}

	private IRandomizer mRandomizer = null;

	public RandomLoadBalancer(SecureRandom pRandom) {
		mRandomizer = new SecureRandomizer(pRandom);
	}

	public RandomLoadBalancer() {
		mRandomizer = new MathRandomizer();
	}

	@Override
	protected T pickResource() {
		synchronized (mLock) {
			int oSize = getResources().size();
			int i = (int) Math.round(mRandomizer.next() * oSize);
			return getResources().get(i % oSize);
		}
	}

}
