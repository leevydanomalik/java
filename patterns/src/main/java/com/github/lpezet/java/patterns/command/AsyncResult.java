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
/**
 * 
 */
package com.github.lpezet.java.patterns.command;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author luc
 *
 */
public class AsyncResult<T> implements IAsyncResult<T> {

	private static class ResultOrException<T> {
		private T mResult;
		private Exception mException;
		
		public ResultOrException(T pResult) {
			mResult = pResult;
		}
		public ResultOrException(Exception pException) {
			mException = pException;
		}
		public Exception getException() {
			return mException;
		}
		public T getResult() {
			return mResult;
		}
	}
	private Object mLock = new Object();
	private Callback<T> mCallback;
	private boolean mCalledback = false;
	private Future<ResultOrException<T>> mFutureResult;
	
	/**
	 * Executes command with custom callable.
	 * 
	 * @param pExecutorService Executor Service
	 * @param pImpl Implementation
	 */
	public AsyncResult(ExecutorService pExecutorService, final ICommand<T> pImpl) {
		mFutureResult = pExecutorService.submit(new Callable<ResultOrException<T>>() {
			@Override
			public ResultOrException<T> call() throws Exception {
				Exception oError = null;
				T oResults = null;
				try {
					oResults = pImpl.execute();
				} catch (Exception e) {
					oError = e;
				}
				synchronized (mLock) {
					if (mCallback != null && !mCalledback) {
						mCalledback = true;
						if (oError == null) mCallback.onResult(oResults);
						else mCallback.onException(oError);
					}
				}
				return oError == null ? new ResultOrException<T>(oResults) : new ResultOrException<T>(oError);		
			}
		});
	}

	@Override
	public void setCallback(Callback<T> pCallback) throws Exception {
		synchronized (mLock) {
			mCallback = pCallback;
			if (mFutureResult.isDone() && !mCalledback) {
				mCalledback = true;
				ResultOrException<T> oROE = mFutureResult.get();
				if (oROE.getException() != null) pCallback.onException(oROE.getException());
				else pCallback.onResult(oROE.getResult());
			}
		}
	}

	@Override
	public T get() throws Exception {
		ResultOrException<T> oROE = mFutureResult.get();
		if (oROE.getException() != null) throw oROE.getException();
		else return oROE.getResult();
	}
	
	@Override
	public T get(long pTimeout, TimeUnit pTimeUnit) throws Exception {
		ResultOrException<T> oROE = mFutureResult.get(pTimeout, pTimeUnit);
		if (oROE.getException() != null) throw oROE.getException();
		else return oROE.getResult();
	}

}
