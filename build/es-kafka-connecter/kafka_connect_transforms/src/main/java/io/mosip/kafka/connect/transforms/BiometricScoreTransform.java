package io.mosip.kafka.connect.transforms;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigException;
import org.apache.kafka.connect.connector.ConnectRecord;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.transforms.Transformation;

public abstract class BiometricScoreTransform<R extends ConnectRecord<R>> implements Transformation<R> {

	public static final String PURPOSE = "Apply all biometric scores related transformations";
	public static final String BIOMETRIC_SCORES_FIELDS = "biometricscores.fields.list";
	public static final String AGE_GROUPS_FIELD = "age.groups.list";
	public static final String OCCUPATION_GROUPS_FIELD = "occupation.groups.list";
	public static final String GENDER_GROUPS_FIELD = "gender.groups.list";
	public static final String TRANSFORM_FUNCTIONS_PROFILE = "biometricscores.listOfFunctions";

	private String[] biometricScoresFieldsList;
	private String[] ageGroupsList;
	private String[] occupationGroupList;
	private String[] genderGroupList;
	private String[] functionsListProfile;

	public static ConfigDef CONFIG_DEF = new ConfigDef()
			.define(BIOMETRIC_SCORES_FIELDS, ConfigDef.Type.STRING, "scores_json", ConfigDef.Importance.HIGH,
					"This is a list of biometric scores that have to be processed by this transform")
			.define(AGE_GROUPS_FIELD, ConfigDef.Type.STRING, "", ConfigDef.Importance.HIGH,
					"Give the age groups in which it has to be categorised")
			.define(OCCUPATION_GROUPS_FIELD, ConfigDef.Type.STRING, "", ConfigDef.Importance.HIGH,
					"Give the occupations in which data needs to be categorised")
			.define(GENDER_GROUPS_FIELD, ConfigDef.Type.STRING, "", ConfigDef.Importance.HIGH,
					"Give the genders in which data needs to be categorised")
			.define(TRANSFORM_FUNCTIONS_PROFILE, ConfigDef.Type.STRING, ConfigDef.Importance.HIGH,
					"List of functions to be applied on the profile");

	@Override
	public void configure(Map<String, ?> configs) {
		AbstractConfig absconf = new AbstractConfig(CONFIG_DEF, configs, false);
		String bioScoresField = absconf.getString(BIOMETRIC_SCORES_FIELDS);
		String ageGroupField = absconf.getString(AGE_GROUPS_FIELD);
		String occupationField = absconf.getString(OCCUPATION_GROUPS_FIELD);
		String genderField = absconf.getString(GENDER_GROUPS_FIELD);
		String listOfFunctionsProfile = absconf.getString(TRANSFORM_FUNCTIONS_PROFILE);
		biometricScoresFieldsList = bioScoresField.replaceAll("\\s+", "").split(",");
		ageGroupsList = ageGroupField.split(",");
		occupationGroupList = occupationField.split(",");
		genderGroupList = genderField.split(",");
		functionsListProfile = listOfFunctionsProfile.replaceAll("\\s+", "").split(",");

		// Base64 base64 = new Base64();

		if (bioScoresField.isEmpty() || ageGroupField.isEmpty() || occupationField.isEmpty() || genderField.isEmpty()
				|| listOfFunctionsProfile.isEmpty()) {
			throw new ConfigException("All the required fields are not set. Required Fields: " + BIOMETRIC_SCORES_FIELDS
					+ " ," + AGE_GROUPS_FIELD + " ," + OCCUPATION_GROUPS_FIELD + " ," + GENDER_GROUPS_FIELD + " ,"
					+ TRANSFORM_FUNCTIONS_PROFILE);
		}
	}

	@Override
	public ConfigDef config() {
		// return empty configdef
		return CONFIG_DEF;
	}

	@Override
	public void close() {
	}

	@Override
	public R apply(R record) {
		if (operatingValue(record) == null) {
			return record;
		} else if (operatingSchema(record) == null) {
			return applySchemaless(record);
		} else {
			// TODO: for now force only schemaless
			return record;
		}
	}

	protected abstract Schema operatingSchema(R record);

	protected abstract Object operatingValue(R record);

	protected abstract R newRecord(R record, Schema updatedSchema, Object updatedValue);

	public static class Key<R extends ConnectRecord<R>> extends BiometricScoreTransform<R> {
		@Override
		protected Schema operatingSchema(R record) {
			return record.keySchema();
		}

		@Override
		protected Object operatingValue(R record) {
			return record.key();
		}

		@Override
		protected R newRecord(R record, Schema updatedSchema, Object updatedValue) {
			return record.newRecord(record.topic(), record.kafkaPartition(), updatedSchema, updatedValue,
					record.valueSchema(), record.value(), record.timestamp());
		}
	}

	public static class Value<R extends ConnectRecord<R>> extends BiometricScoreTransform<R> {
		@Override
		protected Schema operatingSchema(R record) {
			return record.valueSchema();
		}

		@Override
		protected Object operatingValue(R record) {
			return record.value();
		}

		@Override
		protected R newRecord(R record, Schema updatedSchema, Object updatedValue) {
			return record.newRecord(record.topic(), record.kafkaPartition(), record.keySchema(), record.key(),
					updatedSchema, updatedValue, record.timestamp());
		}
	}

	private R applySchemaless(R record) {
		final Map<String, Object> value = Requirements.requireMap(operatingValue(record), PURPOSE);
		// final Map<String, Object> key = Requirements.requireMap(record.key(),
		// PURPOSE);

		Map<String, Object> updatedValueRoot = new HashMap<>(value);
		// Map<String, Object> updatedKeyRoot = new HashMap<>(key);

		for (int i = 0; i < biometricScoresFieldsList.length; i++) {
			Map<String, Object> updatedValue = updatedValueRoot;
			String[] profHierar = (biometricScoresFieldsList[i]).split("\\.");
			try {
				for (int j = 0; j < profHierar.length; j++) {
					updatedValue = (Map<String, Object>) updatedValue.get(profHierar[j]);
				}
			} catch (Exception e) {
				throw new ConfigException(
						"Improper profile fields list. Some of the given fields are not found. Given List: "
								+ biometricScoresFieldsList + "\n Exception details: " + e);
			}

			if (updatedValue != null) {
				for (String func : functionsListProfile) {

					switch (func) {
					case "processAgeGroup":
						processAgeGroup(updatedValue, ageGroupsList);
						break;
					case "processOccupation":
						processOccupation(updatedValue, occupationGroupList);
						break;
					case "processGender":
						processGender(updatedValue, genderGroupList);
						break;
					default:
						break;
					}
				}
			}
		}
		return newRecord(record, null, updatedValueRoot);
	}

	static void processAgeGroup(Map<String, Object> updatedValue, String[] agList) {
		if (updatedValue.get("ageGroup") == null) {
			return;
		}
		Object ageGroup = updatedValue.get("ageGroup");
		updatedValue.put("ageGroup", ageGroup);
	}

	static void processOccupation(Map<String, Object> updatedValue, String[] agList) {
		if (updatedValue.get("occupation") == null) {
			return;
		}
		Object ageGroup = updatedValue.get("occupation");
		updatedValue.put("occupation", ageGroup);
	}

	static void processGender(Map<String, Object> updatedValue, String[] agList) {
		if (updatedValue.get("gender") == null) {
			return;
		}
		Object ageGroup = updatedValue.get("gender");
		updatedValue.put("gender", ageGroup);
	}
}
