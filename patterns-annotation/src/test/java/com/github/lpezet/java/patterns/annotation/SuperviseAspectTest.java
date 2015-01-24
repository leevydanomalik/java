/**
 * 
 */
package com.github.lpezet.java.patterns.annotation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Luc Pezet
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class SuperviseAspectTest {

	@Autowired
	private ISuperviseMe mTest;
	
	@Test
	public void doIt() throws Exception {
		assertEquals(0, mTest.timesExecuted());
		assertTrue( doSomething() );
		assertEquals(1, mTest.timesExecuted());
		setTimeToWait(300);
		assertFalse( doSomething() );
		assertEquals(2, mTest.timesExecuted());
	}
	
	private void setTimeToWait(int pTime) {
		mTest.timeToWaitInMillis(pTime);;
	}

	private boolean doSomething() {
		try {
			mTest.doSomething();
			return true;
		} catch (Throwable t) {}
		return false;
	}
}