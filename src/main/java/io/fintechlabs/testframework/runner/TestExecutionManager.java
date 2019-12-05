package io.fintechlabs.testframework.runner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;

import org.springframework.security.core.Authentication;

import io.fintechlabs.testframework.security.AuthenticationFacade;
import io.fintechlabs.testframework.testmodule.TestFailureException;

/**
 * @author jricher
 *
 */
public class TestExecutionManager {


	private class BackgroundTask implements Callable {
		private String testId;
		private Callable myCallable;
		private Authentication savedAuthentication;

		public BackgroundTask(String testId, Callable callable) {
			this.testId = testId;
			this.myCallable = callable;
			// save the authentication context for use when we run it later
			savedAuthentication = authenticationFacade.getContextAuthentication();
		}

		@Override
		public Object call() throws TestFailureException {
			// restore the authentication context that was in place when this was created
			authenticationFacade.setLocalAuthentication(savedAuthentication);
			Object returnObj = null;
			try {
				returnObj = myCallable.call();
			} catch (TestFailureException e) {
				throw e;
			} catch (Exception e) {
				throw new TestFailureException(testId, e);
			}
			return returnObj;
		}
	}

	private String testId;

	private List<Future> futures = new ArrayList<>();

	private ExecutorCompletionService executorCompletionService;

	private AuthenticationFacade authenticationFacade;

	public TestExecutionManager(String testId, ExecutorCompletionService executorCompletionService, AuthenticationFacade authenticationFacade) {
		this.testId = testId;
		this.executorCompletionService = executorCompletionService;
		this.authenticationFacade = authenticationFacade;
	}

	/**
	 * @return the testId
	 */
	public String getTestId() {
		return testId;
	}

	/**
	 * @return the futures
	 */
	public List<Future> getFutures() {
		return futures;
	}

	@SuppressWarnings("unchecked")
	public void runInBackground(Callable callable) {
		futures.add(executorCompletionService.submit(new BackgroundTask(testId, callable)));
	}


}
