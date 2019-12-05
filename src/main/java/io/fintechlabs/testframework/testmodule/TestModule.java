package io.fintechlabs.testframework.testmodule;

import java.time.Instant;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.JsonObject;

import io.fintechlabs.testframework.frontChannel.BrowserControl;
import io.fintechlabs.testframework.info.ImageService;
import io.fintechlabs.testframework.info.TestInfoService;
import io.fintechlabs.testframework.logging.TestInstanceEventLog;
import io.fintechlabs.testframework.runner.TestExecutionManager;

/**
 *
 * TestModule instances are assumed to have a constructor with the signature:
 *
 * String id, Map<String, String> owner, TestInstanceEventLog eventLog, BrowserControl browser, TestInfoService testInfo
 *
 * @author jricher
 *
 */
public interface TestModule {

	public static enum Status {
		CREATED, // test has been instantiated
		CONFIGURED, // configuration files have been sent and set up
		RUNNING, // test is executing
		WAITING, // test is waiting for external input
		INTERRUPTED, // test has been stopped before completion
		FINISHED, // test has completed
		UNKNOWN // test status is unknown, probably an error
	}

	public static enum Result {
		PASSED, // test has passed successfully
		FAILED, // test has failed
		WARNING, // test has warnings
		REVIEW, // test requires manual review
		UNKNOWN // test results not yet known, probably still running (see status)
	}

	/**
	 * *
	 * Method is called to pass configuration parameters
	 *
	 * @param config
	 *            A JSON object consisting of details that the testRunner
	 *            doesn't need to know about
	 * @param id
	 *            The id of this test
	 * @param baseUrl
	 *            The base of the URL that will need to be appended to any
	 *            URL construction.
	 */
	void configure(JsonObject config, String baseUrl);

	/**
	 * *
	 *
	 * @return The name of the test.
	 */
	String getName();

	/**
	 * @return the id of this test
	 */
	String getId();

	/**
	 * @return The current status of the test
	 */
	Status getStatus();

	/**
	 * Called by the TestRunner to start the test
	 */
	void start();

	/**
	 * Called by the test runner to stop the test
	 */
	void stop();

	/**
	 * Called when a the test runner calls a URL
	 *
	 * @param path
	 *            The path that was called
	 * @param req
	 *            The request that passed to the server
	 * @param res
	 *            A response that will be sent from the server
	 * @param session
	 *            Session details
	 * @param requestParts
	 *            elements from the request parsed out into a json object for use in condition classes
	 * @return A response (could be a ModelAndview, ResponseEntity, or other item)
	 */
	Object handleHttp(String path,
		HttpServletRequest req, HttpServletResponse res,
		HttpSession session,
		JsonObject requestParts);

	/**
	 * @return get the test results
	 */
	Result getResult();

	/**
	 * @return a map of runtime values exposed by the test itself, potentially useful for configuration
	 *         of external entities.
	 */
	Map<String, String> getExposedValues();

	/**
	 * @return the associated browser control module
	 */
	BrowserControl getBrowser();

	/**
	 * @return the associated execution manager
	 */
	TestExecutionManager getTestExecutionManager();

	/**
	 * @param path
	 * @param req
	 * @param res
	 * @param session
	 * @param requestParts
	 * @return
	 */
	Object handleHttpMtls(String path,
		HttpServletRequest req, HttpServletResponse res,
		HttpSession session,
		JsonObject requestParts);

	/**
	 *
	 * @return get the {'iss':,'sub'} owner of the test
	 */
	Map<String, String> getOwner();

	/**
	 * @return get the Instant marking when the test was created
	 */
	Instant getCreated();

	/**
	 * @return get the Instant marking when the test's status was last updated
	 */
	Instant getStatusUpdated();

	/**
	 * @param error the final error from this test while running
	 */
	void setFinalError(TestFailureException error);

	/**
	 * @return the final error from this test while running, possibly null
	 */
	TestFailureException getFinalError();

	/**
	 * Mark the test as failed and finished.
	 */
	void fireTestFailure();

	/**
	 * Mark the test as succeeded and finished.
	 */
	void fireTestSuccess();

	/**
	 * Mark the test as finished without setting a result.
	 */
	void fireTestFinished();

	/**
	 * Mark the test as finished as the result of filling a placeholder
	 */
	void fireTestPlaceholderFilled();

	/**
	 * Mark the test as having completed its setup.
	 */
	void fireSetupDone();

	/**
	 * Mark the test as requiring a manual review.
	 */
	void fireTestReviewNeeded();

	/**
	 * Pass along the appropriate runtime services and properties to allow the test to run. It cannot be used until this is completed.
	 */
	void setProperties(String id, Map<String, String> owner, TestInstanceEventLog wrappedEventLog, BrowserControl browser, TestInfoService testInfo, TestExecutionManager executionManager, ImageService imageService);

}
