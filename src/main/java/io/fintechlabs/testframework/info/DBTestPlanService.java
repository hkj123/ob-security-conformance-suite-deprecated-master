package io.fintechlabs.testframework.info;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;

import io.fintechlabs.testframework.CollapsingGsonHttpMessageConverter;
import io.fintechlabs.testframework.security.AuthenticationFacade;

/**
 * @author jricher
 *
 */
@Service
public class DBTestPlanService implements TestPlanService {

	public static final String COLLECTION = "TEST_PLAN";

	private static Logger logger = LoggerFactory.getLogger(DBTestInfoService.class);

	@Value("${fintechlabs.version}")
	private String version;

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private AuthenticationFacade authenticationFacade;

	private Gson gson = CollapsingGsonHttpMessageConverter.getDbObjectCollapsingGson();

	/**
	 * @param planId
	 * @param testName
	 * @param id
	 */
	@Override
	public void updateTestPlanWithModule(String planId, String testName, String id) {

		Criteria criteria = new Criteria();
		criteria.and("_id").is(planId);
		criteria.and("modules.testModule").is(testName);

		Query query = new Query(criteria);

		Update update = new Update();
		update.push("modules.$.instances", id);

		mongoTemplate.updateFirst(query, update, COLLECTION);


	}

	/* (non-Javadoc)
	 * @see io.fintechlabs.testframework.info.TestPlanService#createTestPlan(java.lang.String, java.lang.String, com.google.gson.JsonObject, java.util.Map, io.fintechlabs.testframework.plan.TestPlan)
	 */
	@Override
	public void createTestPlan(String id, String planName, JsonObject config, String description, String[] testModules, String summary, String publish) {

		ImmutableMap<String, String> owner = authenticationFacade.getPrincipal();

		BasicDBObjectBuilder documentBuilder = BasicDBObjectBuilder.start()
			.add("_id", id)
			.add("planName", planName)
			.add("config", config)
			.add("started", Instant.now().toString())
			.add("owner", owner)
			.add("description", description) // for the specific instance
			.add("version", version)
			.add("summary", summary) // from the plan definition
			.add("publish", publish);

		List<DBObject> moduleStructure = new ArrayList<>();

		for (String module : testModules) {
			BasicDBObjectBuilder moduleBuilder = BasicDBObjectBuilder.start()
				.add("testModule", module)
				.add("instances", Collections.emptyList());

			moduleStructure.add(moduleBuilder.get());
		}

		documentBuilder.add("modules", moduleStructure);


		mongoTemplate.insert(documentBuilder.get(), COLLECTION);
	}

	/* (non-Javadoc)
	 * @see io.fintechlabs.testframework.info.TestPlanService#getTestPlan(java.lang.String)
	 */
	@Override
	public Map getTestPlan(String id) {
		Criteria criteria = new Criteria();
		criteria.and("_id").is(id);

		if (!authenticationFacade.isAdmin()) {
			criteria.and("owner").is(authenticationFacade.getPrincipal());
		}

		Query query = new Query(criteria);

		DBObject testPlan = mongoTemplate.getCollection(COLLECTION).findOne(query.getQueryObject());

		if (testPlan == null) {
			return null;
		} else {
			return testPlan.toMap();
		}
	}

	@Override
	public JsonObject getModuleConfig(String planId, String moduleName) {
		Map testPlan = getTestPlan(planId);

		BasicDBList modules = (BasicDBList) testPlan.get("modules");

		boolean found = false;

		for (Object o : modules)
		{
			BasicDBObject module = (BasicDBObject) o;
			if (module.containsValue(moduleName)) {
				found = true;
			}
		}

		if (!found) {
			// the user has asked to create a module that isn't part of the plan
			return null;
		}

		DBObject dbConfig = (DBObject) testPlan.get("config");

		String json = gson.toJson(dbConfig);

		JsonObject config = new JsonParser().parse(json).getAsJsonObject();

		if (config.has("override")) {
			JsonObject override = config.getAsJsonObject("override");
			config.remove("override");
			if (override.has(moduleName)) {
				// Move all the overridden elements up into the configuration
				JsonObject overrides = override.getAsJsonObject(moduleName);
				for (Map.Entry<String, JsonElement> entry : overrides.entrySet()) {
					config.add(entry.getKey(), entry.getValue());
				}
			}
		}

		return config;
	}

	/* (non-Javadoc)
	 * @see io.fintechlabs.testframework.info.TestPlanService#getAllPlansForCurrentUser()
	 */
	@Override
	public List<Map> getAllPlansForCurrentUser() {

		Criteria criteria = new Criteria();

		if (!authenticationFacade.isAdmin()) {
			criteria.and("owner").is(authenticationFacade.getPrincipal());
		}

		Query query = new Query(criteria);

		List<DBObject> results = mongoTemplate.getCollection(COLLECTION).find(query.getQueryObject()).toArray();

		return results.stream().map(DBObject::toMap).collect(Collectors.toList());

	}


	/* (non-Javadoc)
	 * @see io.fintechlabs.testframework.info.TestPlanService#getPublicPlans()
	 */
	@Override
	public List<Map> getPublicPlans() {

		Criteria criteria = new Criteria();

		criteria.and("publish").in("summary", "everything");

		Query query = new Query(criteria);

		query.fields()
			.include("_id")
			.include("planName")
			.include("description")
			.include("started")
			.include("modules");

		List<DBObject> results = mongoTemplate.getCollection(COLLECTION).find(query.getQueryObject(), query.getFieldsObject()).toArray();

		return results.stream().map(DBObject::toMap).collect(Collectors.toList());

	}

}
