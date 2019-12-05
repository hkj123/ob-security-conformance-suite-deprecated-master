package io.fintechlabs.testframework.runner;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriUtils;

import com.google.common.base.Strings;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.gson.JsonObject;

import io.fintechlabs.testframework.condition.Condition.ConditionResult;
import io.fintechlabs.testframework.condition.ConditionError;
import io.fintechlabs.testframework.frontChannel.BrowserControl;
import io.fintechlabs.testframework.info.ImageService;
import io.fintechlabs.testframework.info.SavedConfigurationService;
import io.fintechlabs.testframework.info.TestInfoService;
import io.fintechlabs.testframework.info.TestPlanService;
import io.fintechlabs.testframework.logging.EventLog;
import io.fintechlabs.testframework.logging.TestInstanceEventLog;
import io.fintechlabs.testframework.security.AuthenticationFacade;
import io.fintechlabs.testframework.testmodule.DataUtils;
import io.fintechlabs.testframework.testmodule.PublishTestModule;
import io.fintechlabs.testframework.testmodule.TestFailureException;
import io.fintechlabs.testframework.testmodule.TestModule;

/**
 *
 * GET /runner/available: list of available tests
 * GET /runner/running: list of running tests
 * POST /runner: create test
 * GET /runner/id: get test status
 * POST /runner/id: start test
 * DELETE /runner/id: cancel test
 * GET /runner/browser/id: get front-channel external URLs
 * POST /runner/browser/id/visit: mark front-channel external URL as visited
 *
 * @author jricher
 *
 */
@Controller
public class TestRunner implements DataUtils {

	@Value("${fintechlabs.base_url:http://localhost:8080}")
	private String baseUrl;

	private static Logger logger = LoggerFactory.getLogger(TestRunner.class);

	@Autowired
	private TestRunnerSupport support;

	@Autowired
	private EventLog eventLog;

	@Autowired
	private TestInfoService testInfo;

	@Autowired
	private AuthenticationFacade authenticationFacade;

	@Autowired
	private TestPlanService planService;

	@Autowired
	private ImageService imageService;

	@Autowired
	private SavedConfigurationService savedConfigurationService;

	private Supplier<Map<String, TestModuleHolder>> testModuleSupplier = Suppliers.memoize(this::findTestModules);

	private ExecutorService executorService = Executors.newCachedThreadPool();
	private ExecutorCompletionService executorCompletionService = new ExecutorCompletionService(executorService);
	private FutureWatcher futureWatcher = new FutureWatcher();

	private class FutureWatcher implements Runnable {
		private boolean running = false;

		public void stop() {
			this.running = false;
		}

		@Override
		public void run() {
			running = true;
			while (running) {
				try {
					FutureTask future = (FutureTask) executorCompletionService.poll(1, TimeUnit.SECONDS);
					if (future != null && !future.isCancelled()) {
						future.get();
					}
				} catch (InterruptedException e) {
					// If we've been interrupted, then either it was on purpose, or something went very very wrong.
					logger.error("Background task was interrupted", e);
				} catch (ExecutionException e) {
					if (e.getCause().getClass().equals(TestFailureException.class)) {
						// This should always be the case for our BackgroundTasks
						TestFailureException testFailureException = (TestFailureException) e.getCause();

						String testId = testFailureException.getTestId();
						TestModule test = support.getRunningTestById(testId);
						if (test != null) {
							// We can't just throw it, the Exception Handler Annotation is only for HTTP requests
							conditionFailure(testFailureException);

							// there's an exception, stop the test
							test.stop();

							// Clean up other tasks for this test id
							TestExecutionManager executionManager = test.getTestExecutionManager();
							if (executionManager != null) {
								for (Future f : executionManager.getFutures()) {
									if (!f.isDone()) {
										f.cancel(true); // True allows the task to be interrupted.
									}
								}
							}

							// set the final exception flag only if this wasn't a normal condition error
							if (testFailureException.getCause() != null && !testFailureException.getCause().getClass().equals(ConditionError.class)) {
								test.setFinalError(testFailureException);
							}

							test.fireTestFailure();
						}
					} else {
						// TODO: Better handling if we get something we wern't expecting? But we don't have access to the test ID
						logger.error("Execution failure", e);
						//eventLog.log(testId, "TEST RUNNER", authenticationFacade.getPrincipal(), EventLog.ex(e));
					}

				}
			}
		}
	}

	public TestRunner() {
		executorService.submit(futureWatcher);
	}

	@RequestMapping(value = "/runner/available", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> getAvailableTests(Model m) {

		Set<Map<String, ?>> available = getTestModules().values().stream()
			.map(e -> args(
				"testName", e.a.testName(),
				"displayName", e.a.displayName(),
				"profile", e.a.profile(),
				"configurationFields", e.a.configurationFields(),
				"summary", e.a.summary()))
			.collect(Collectors.toSet());

		return new ResponseEntity<>(available, HttpStatus.OK);
	}

	@RequestMapping(value = "/runner", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, String>> createTest(@RequestParam("test") String testName, @RequestParam(name = "plan", required = false) String planId, @RequestBody(required = false) JsonObject testConfig, Model m) {
		final JsonObject config;

		String id = RandomStringUtils.randomAlphanumeric(10);

		if (!Strings.isNullOrEmpty(planId)) {
			if (testConfig != null) {
				// user should not supply a configuration when creating a test from a test plan
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}
			// if the test is part of a plan, the configuration comes from the plan
			config = planService.getModuleConfig(planId, testName);
			if (config == null) {
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			}
		} else {
			// we're starting an individual test module
			config = testConfig;
			if (config == null) {
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}

			// save this test config on the user's stack
			savedConfigurationService.saveTestConfigurationForCurrentUser(config, testName);

		}

		TestModule test = createTestModule(testName, id, config);

		if (test == null) {
			// return an error
			return new ResponseEntity<>(stringMap("error", "createTestModule failed"), HttpStatus.INTERNAL_SERVER_ERROR);
		}

		logger.info("Created: " + testName);

		// logger.info("Status of " + testName + ": " + test.getStatus());

		support.addRunningTest(id, test);

		String url;
		String alias = "";

		// see if an alias was passed in as part of the configuration and use it if available
		if (config.has("alias") && config.get("alias").isJsonPrimitive()) {
			try {
				alias = config.get("alias").getAsString();

				// create an alias for the test
				if (!createTestAlias(alias, id)) {
					// there was a failure in creating the test alias, return an error
					return new ResponseEntity<>(HttpStatus.CONFLICT);
				}
				url = baseUrl + TestDispatcher.TEST_PATH + "a/" + UriUtils.encodePathSegment(alias, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				// this should never happen, why is Java dumb
				e.printStackTrace();
				return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
			}

		} else {
			url = baseUrl + TestDispatcher.TEST_PATH + id;
		}

		String description = null;
		if (config.has("description") && config.get("description").isJsonPrimitive()) {
			description = config.get("description").getAsString();
		}

		// copy the summary from the test module
		String summary = getTestModules().get(testName).a.summary();

		// extract the `publish` field if available
		String publish = null;
		if (config.has("publish") && config.get("publish").isJsonPrimitive()) {
			publish = Strings.emptyToNull(config.get("publish").getAsString());
		}

		// record that this test was started
		testInfo.createTest(id, testName, url, config, alias, Instant.now(), planId, description, summary, publish);


		// log the test creation event in the event log
		eventLog.log(id, "TEST-RUNNER", test.getOwner(),
			args("msg", "Test instance " + id + " created",
				"result", ConditionResult.INFO,
				"baseUrl", url,
				"config", config,
				"alias", alias,
				"planId", planId,
				"description", description,
				"testName", testName));

		test.getTestExecutionManager().runInBackground(() -> {
			test.configure(config, url);

			/* automatically start all tests */
			if (test.getStatus() == TestModule.Status.CONFIGURED) {
				test.start();
			}
			return "done";
		});
		// logger.info("Status of " + testName + ": " + test.getId() + ": " + test.getStatus());

		Map<String, String> map = new HashMap<>();
		map.put("name", testName);
		map.put("id", test.getId());
		map.put("url", url);

		return new ResponseEntity<>(map, HttpStatus.CREATED);

	}

	/**
	 * @param alias
	 * @param id
	 * @return
	 */
	private boolean createTestAlias(String alias, String id) {
		// first see if the alias is already in use
		if (support.hasAlias(alias)) {
			// find the test that has the alias
			TestModule test = support.getRunningTestByAlias(alias);

			if (test != null) {
				// TODO: make the override configurable to allow for conflict of re-used aliases

				eventLog.log(test.getId(), "TEST-RUNNER", test.getOwner(), args("msg", "Stopping test due to alias conflict", "alias", alias, "new_test_id", id));

				test.stop(); // stop the currently-running test
			}
		}

		support.addAlias(alias, id);
		return true;
	}

	@RequestMapping(value = "/runner/{id}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> startTest(@PathVariable("id") String testId) {
		TestModule test = support.getRunningTestById(testId);
		if (test != null) {
			Map<String, Object> map = createTestStatusMap(test);

			//logger.info("Status of " + test.getName() + ": " + test.getId() + ": " + test.getStatus());

			test.getTestExecutionManager().runInBackground(() -> {
				test.start();
				return "started";
			});

			//logger.info("Status of " + test.getName() + ": " + test.getId() + ": " + test.getStatus());

			return ResponseEntity.ok().body(map);

		} else {
			return ResponseEntity.notFound().build();
		}

	}

	@GetMapping(value = "/runner/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, Object>> getTestStatus(@PathVariable("id") String testId, Model m) {
		//logger.info("Getting status of " + testId);

		TestModule test = support.getRunningTestById(testId);
		if (test != null) {
			Map<String, Object> map = createTestStatusMap(test);

			return ResponseEntity.ok().body(map);

		} else {
			return ResponseEntity.notFound().build();
		}
	}

	@DeleteMapping(value = "/runner/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> cancelTest(@PathVariable("id") String testId) {
		// logger.info("Canceling " + testId);

		TestModule test = support.getRunningTestById(testId);
		if (test != null) {

			// stop the test
			test.getTestExecutionManager().runInBackground(() -> {
				eventLog.log(test.getId(), "TEST-RUNNER", test.getOwner(), args("msg", "Stopping test from external request"));
				test.stop();
				return "stopped";
			});

			// return its immediate status
			Map<String, Object> map = createTestStatusMap(test);

			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@RequestMapping(value = "/runner/running", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Set<String>> getAllRunningTestIds(Model m) {
		Set<String> testIds = support.getAllRunningTestIds();

		return new ResponseEntity<>(testIds, HttpStatus.OK);
	}

	@RequestMapping(value = "/runner/browser/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, Object>> getBrowserStatus(@PathVariable("id") String testId, Model m) {
		// logger.info("Getting status of " + testId);

		TestModule test = support.getRunningTestById(testId);
		if (test != null) {
			BrowserControl browser = test.getBrowser();
			if (browser != null) {
				Map<String, Object> map = new HashMap<>();
				map.put("id", testId);
				map.put("urls", browser.getUrls());
				map.put("visited", browser.getVisited());
				map.put("runners", browser.getWebRunners());

				return new ResponseEntity<>(map, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
			}
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@RequestMapping(value = "/runner/browser/{id}/visit", method = RequestMethod.POST)
	public ResponseEntity<String> visitBrowserUrl(@PathVariable("id") String testId, @RequestParam("url") String url, Model m) {
		TestModule test = support.getRunningTestById(testId);
		if (test != null) {
			BrowserControl browser = test.getBrowser();
			if (browser != null) {
				browser.urlVisited(url);

				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			} else {
				return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
			}

		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	private TestModule createTestModule(String testName, String id, JsonObject config) {

		TestModuleHolder holder = getTestModules().get(testName);

		if (holder == null) {
			logger.warn("Couldn't find a test module for " + testName);
			return null;
		}

		try {

			Class<? extends TestModule> testModuleClass = holder.c;

			@SuppressWarnings("unchecked")
			Map<String, String> owner = authenticationFacade.getPrincipal();

			TestInstanceEventLog wrappedEventLog = new TestInstanceEventLog(id, owner, eventLog);

			TestExecutionManager executionManager = new TestExecutionManager(id, executorCompletionService, authenticationFacade);
			BrowserControl browser = new BrowserControl(config, id, wrappedEventLog, executionManager, imageService);

			// call the constructor
			TestModule module = testModuleClass.getDeclaredConstructor()
				.newInstance();

			// pass in all the components for this test module to execute
			module.setProperties(id, owner, wrappedEventLog, browser, testInfo, executionManager, imageService);

			return module;

		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {

			logger.warn("Couldn't create test module", e);

			return null;
		}

	}

	// get the test modules from the memoized copy, filling it if necessary
	private Map<String, TestModuleHolder> getTestModules() {
		return testModuleSupplier.get();
	}

	// this is used to load all the test modules into the memoized copy used above
	// we memoize this because reflection is slow
	private Map<String, TestModuleHolder> findTestModules() {

		Map<String, TestModuleHolder> testModules = new HashMap<>();

		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
		scanner.addIncludeFilter(new AnnotationTypeFilter(PublishTestModule.class));
		for (BeanDefinition bd : scanner.findCandidateComponents("io.fintechlabs")) {
			try {
				@SuppressWarnings("unchecked")
				Class<? extends TestModule> c = (Class<? extends TestModule>) Class.forName(bd.getBeanClassName());
				PublishTestModule a = c.getDeclaredAnnotation(PublishTestModule.class);

				testModules.put(a.testName(), new TestModuleHolder(c, a));

			} catch (ClassNotFoundException e) {
				logger.error("Couldn't load test module definition: " + bd.getBeanClassName());
			}
		}

		return testModules;
	}

	private class TestModuleHolder {
		public Class<? extends TestModule> c;
		public PublishTestModule a;

		public TestModuleHolder(Class<? extends TestModule> c, PublishTestModule a) {
			this.c = c;
			this.a = a;
		}
	}

	private Map<String, Object> createTestStatusMap(TestModule test) {
		Map<String, Object> map = new HashMap<>();
		map.put("name", test.getName());
		map.put("id", test.getId());
		map.put("exposed", test.getExposedValues());
		map.put("owner", test.getOwner());
		map.put("created", test.getCreated().toString());
		map.put("updated", test.getStatusUpdated().toString());
		map.put("error", ex(test.getFinalError()));

		BrowserControl browser = test.getBrowser();
		if (browser != null) {
			Map<String, Object> bmap = new HashMap<>();
			bmap.put("urls", browser.getUrls());
			bmap.put("visited", browser.getVisited());
			bmap.put("runners", browser.getWebRunners());
			map.put("browser", bmap);
		}
		return map;
	}

	// handle errors thrown by running tests
	@ExceptionHandler(TestFailureException.class)
	public ResponseEntity<Object> conditionFailure(TestFailureException error) {
		try {
			TestModule test = support.getRunningTestById(error.getTestId());
			if (test != null) {
				logger.error("Caught an error in TestRunner while running the test, stopping the test: " + error.getMessage());
				test.stop();
				eventLog.log(test.getId(), "TEST-RUNNER", test.getOwner(), ex(error));

				// Any form of exception from a test counts as a failure
				test.fireTestFailure();

				if (!(error.getCause() != null && error.getCause().getClass().equals(ConditionError.class))) {
					// if the root error isn't a ConditionError, set this so the UI can display the underlying error in detail
					// ConditionError will get handled by the logging system, no need to display with stacktrace
					test.setFinalError(error);
				}
			} else {
				logger.error("Caught an error from a test, but the test isn't running: " + error.getMessage());
			}
		} catch (Exception e) {
			logger.error("Something terrible happened when handling an error, I give up", e);
		}

		JsonObject obj = new JsonObject();
		obj.addProperty("error", error.getMessage());
		obj.addProperty("cause", error.getCause() != null ? error.getCause().getMessage() : null);
		obj.addProperty("testId", error.getTestId());
		return new ResponseEntity<>(obj, HttpStatus.INTERNAL_SERVER_ERROR);
	}

}
