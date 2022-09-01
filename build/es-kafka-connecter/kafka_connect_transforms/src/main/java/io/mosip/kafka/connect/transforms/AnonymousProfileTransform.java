package io.mosip.kafka.connect.transforms;

import org.apache.kafka.common.cache.Cache;
import org.apache.kafka.common.cache.LRUCache;
import org.apache.kafka.common.cache.SynchronizedCache;
import org.apache.kafka.connect.connector.ConnectRecord;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.transforms.Transformation;
import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigException;
import org.apache.kafka.connect.errors.DataException;
import org.apache.kafka.connect.data.Field;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Struct;
import org.apache.commons.codec.binary.Base64;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;

import org.json.JSONObject;
import org.json.JSONException;
import org.json.JSONArray;

import java.util.Arrays;
// import java.util.Base64;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.io.IOException;

public abstract class AnonymousProfileTransform<R extends ConnectRecord<R>> implements Transformation<R> {

    public static final String PURPOSE = "Apply all anonymous profile related transformations";
    public static final String PROFILES_FIELDS = "profiles.fields.list";
    public static final String AGE_GROUPS_FIELD = "age.groups.list";
    public static final String AGE_GROUPS_OUPUT_FIELD = "age.groups.output.field";
    public static final String CHANNEL_GROUPS_FIELD = "channel.groups.list";
    public static final String TOPIC_NAME_FIELD = "kafka.topic.name";
    public static final String ES_URL_FIELD = "es.url";
    public static final String TRANSFORM_FUNCTIONS_PROFILE = "profile.listOfFunctions";
    public static final String TRANSFORM_FUNCTIONS = "listOfFunctions";

    private String[] profileFieldsList;
    private String[] ageGroupsList;
    private String[] channelGroupList;
    private String[] functionsList;
    private String[] functionsListProfile;
    private String ageGroupsOuputField;
    private String elasticSearchURL;
    private String esTopicName;
    // private Base64 base64;

    public static ConfigDef CONFIG_DEF = new ConfigDef()
        .define(PROFILES_FIELDS, ConfigDef.Type.STRING, "profile", ConfigDef.Importance.HIGH, "This is a list of profiles that have to be processed by this transform")
        .define(AGE_GROUPS_FIELD, ConfigDef.Type.STRING, "", ConfigDef.Importance.HIGH, "Give the age groups in which it has to be categorised")
        .define(AGE_GROUPS_OUPUT_FIELD, ConfigDef.Type.STRING, "", ConfigDef.Importance.HIGH, "the ouput field in which agegroup has to be put (w.r.t to the profile fields)")
        .define(CHANNEL_GROUPS_FIELD, ConfigDef.Type.STRING, "", ConfigDef.Importance.HIGH, "Give the categories in which channel needs to be categorised, Code Hardcoded accoring to Both phone email, only phone, only email, None")
        .define(TOPIC_NAME_FIELD, ConfigDef.Type.STRING, "", ConfigDef.Importance.HIGH, "Current Transform's topic name")
        .define(ES_URL_FIELD, ConfigDef.Type.STRING, "", ConfigDef.Importance.HIGH, "Elasticseach url")
        .define(TRANSFORM_FUNCTIONS_PROFILE, ConfigDef.Type.STRING, ConfigDef.Importance.HIGH, "List of functions to be applied on the profile")
        .define(TRANSFORM_FUNCTIONS, ConfigDef.Type.STRING, ConfigDef.Importance.HIGH, "List of functions to be applied on the record outside profile");

    @Override
    public void configure(Map<String, ?> configs) {
        AbstractConfig absconf = new AbstractConfig(CONFIG_DEF, configs, false);
        String prFL = absconf.getString(PROFILES_FIELDS);
        String agl = absconf.getString(AGE_GROUPS_FIELD);
        String cgl = absconf.getString(CHANNEL_GROUPS_FIELD);
        String esUrl = absconf.getString(ES_URL_FIELD);
        String kafkaTopicName = absconf.getString(TOPIC_NAME_FIELD);
        String listOfFunctionsProfile = absconf.getString(TRANSFORM_FUNCTIONS_PROFILE);
        String listOfFunctions = absconf.getString(TRANSFORM_FUNCTIONS);

        ageGroupsOuputField = absconf.getString(AGE_GROUPS_OUPUT_FIELD);

        elasticSearchURL = esUrl;
        esTopicName = kafkaTopicName;

        profileFieldsList = prFL.replaceAll("\\s+","").split(",");
        ageGroupsList = agl.split(",");
        channelGroupList = cgl.split(",");
        functionsList = listOfFunctions.replaceAll("\\s+","").split(",");
        functionsListProfile = listOfFunctionsProfile.replaceAll("\\s+","").split(",");

        // Base64 base64 = new Base64();

        if(prFL.isEmpty() || agl.isEmpty() || ageGroupsOuputField.isEmpty() || cgl.isEmpty() || esUrl.isEmpty() || kafkaTopicName.isEmpty()){
            throw new ConfigException("All the required fields are not set. Required Fields: " + PROFILES_FIELDS + " ," + AGE_GROUPS_FIELD + " ," + AGE_GROUPS_OUPUT_FIELD + 
            " ," + CHANNEL_GROUPS_FIELD + " ,"+ TOPIC_NAME_FIELD + " ," + ES_URL_FIELD);
        }

        esPutMapping(esUrl,kafkaTopicName);
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

    public static class Key<R extends ConnectRecord<R>> extends AnonymousProfileTransform<R> {
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
            return record.newRecord(record.topic(), record.kafkaPartition(), updatedSchema, updatedValue, record.valueSchema(), record.value(), record.timestamp());
        }
    }

    public static class Value<R extends ConnectRecord<R>> extends AnonymousProfileTransform<R> {
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
            return record.newRecord(record.topic(), record.kafkaPartition(), record.keySchema(), record.key(), updatedSchema, updatedValue, record.timestamp());
        }
    }

    private R applySchemaless(R record) {
        final Map<String, Object> value = Requirements.requireMap(operatingValue(record), PURPOSE);
        // final Map<String, Object> key = Requirements.requireMap(record.key(), PURPOSE);

        Map<String, Object> updatedValueRoot = new HashMap<>(value);
        // Map<String, Object> updatedKeyRoot = new HashMap<>(key);
        String date = (String)Requirements.getNestedField(updatedValueRoot,"profile.date");
        for(int i=0; i<profileFieldsList.length ; i++){
            Map<String, Object> updatedValue = updatedValueRoot;
            String[] profHierar = (profileFieldsList[i]).split("\\.");
            try{
                for(int j=0; j<profHierar.length ; j++){
                    updatedValue = (Map<String, Object>)updatedValue.get(profHierar[j]);
                }
            }
            catch(Exception e){
                throw new ConfigException("Improper profile fields list. Some of the given fields are not found. Given List: " + profileFieldsList + "\n Exception details: " + e);
            }
            
            if(updatedValue != null){
                for(String func : functionsListProfile){
        
                    switch (func) {
                        case "processBiometricList": processBiometricList(updatedValue); break;
                        case "processAgeGroup": processAgeGroup(updatedValue,ageGroupsList,ageGroupsOuputField,date); break;
                        case "processChannel": processChannel(updatedValue, channelGroupList); break;
                        case "processProcessName": processProcessName(updatedValue); break;
                        case "processAssister": processAssister(updatedValue); break;
                        case "processLocationList": processLocationList(updatedValue); break;     
                        case "processUpdateProfile": processUpdateProfile(updatedValue, elasticSearchURL, esTopicName); break;               
                        default: break;
                    }
                    
                // rest of the transform funcs
                }    
            }
        }
        // call non profile related funcs here

        for(String func : functionsList){
            switch (func) {
                case "processRegistrationCenter": processRegistrationCenter(updatedValueRoot); break;
                default: break;
            }
        }

        // updatedValueRoot = processSchemasForSchemaLess(updatedValueRoot);


        // record.newRecord(record.topic(), record.kafkaPartition(), null, updatedKey, null, updatedValueRoot, record.timestamp());
        
        return newRecord(record, null, updatedValueRoot);
    }

    // static String extractId(Map<String, Object> updatedKey){
    //     return updatedKey.get('payload')
    // } 

    static void processBiometricList(Map<String, Object> updatedValue){
        if( updatedValue.get("biometricInfo") == null ){
            return;
        }

        List<Object> arr = (List<Object>)updatedValue.get("biometricInfo");

        Map<String, Object> ret = new HashMap<>();

        for(int i=0; i<arr.size();){
            // todo: check type before casting
            Map<String, Object> m = new HashMap<>((Map<String, Object>)arr.get(i));
            m.remove("subType");
            String mtype = (String)m.get("type");
            float qualScoreSum=0,attemptsSum=0;
            int count=0;
            int j;
            for(j=i;j<arr.size();j++){
                Map<String, Object> each = (Map<String, Object>)arr.get(j);
                if(((String)each.get("type")).equals(mtype)) {
                    try{
                        int q =(int)each.get("qualityScore");
                        int a =Integer.parseInt((String)each.get("attempts"));
                        count++;

                        qualScoreSum += q;
                        attemptsSum += a;
                    }
                    catch(Exception e){
                        System.out.println(">>>>>>> " + e);
                    }
                    arr.remove(j--);
                }
                else{
                }
            }

            if(m.get("digitalId")!=null){
                // System.out.println("=========> DIGITALID PROBLEM " + m.get("digitalId"));
                try{
                    m.put("digitalId", StringToJson.returnSchemalessObject(new JSONObject((String)m.get("digitalId"))));
                    ((Map<String, Object>)m.get("digitalId")).remove("dateTime");
                }
                catch(JSONException je){
                    try{
                        String str = (String)m.get("digitalId");
                        str = new String(new Base64(true).decode(str));
                        m.put("digitalId", StringToJson.returnSchemalessObject(new JSONObject(str)));
                        ((Map<String, Object>)m.get("digitalId")).remove("dateTime");
                    }
                    catch(Exception e){
                      e.printStackTrace();
                    }
                }
            }


            m.put("attempts",count == 0 ? 0 : attemptsSum/count);
            m.put("qualityScore", count == 0 ? 0 : qualScoreSum/count);

            ret.put(mtype,m);

            i=0;
        }

        updatedValue.put("biometricInfo",ret);
    }

    static void processLocationList(Map<String, Object> updatedValue){
        if( updatedValue.get("location") == null ){
            return;
        }

        List<Object> arr = (List<Object>)updatedValue.get("location");

        Map<String, Object> ret = new HashMap<>();

        for(int i=0;i<arr.size();i++){
            ret.put("hierarchy"+(i+1),arr.get(i));
        }
        updatedValue.put("location",ret);
    }
    
    static void processAssister(Map<String, Object> updatedValue){
        if( updatedValue.get("assisted") == null ){
            return;
        }

        List<Object> arr = (List<Object>)updatedValue.get("assisted"); 

        Map<String, Object> ret = new HashMap<>();
        
        if(arr.size() == 0) return;
        if(arr.size() >= 1) ret.put("Operator",(String)arr.get(0));
        if(arr.size() >= 2) ret.put("Supervisor",(String)arr.get(1));
        
        updatedValue.put("registrationOfficers",ret);
    }

    static void processAgeGroup(Map<String, Object> updatedValue, String[] agList, String agOut, String date){
        if(date == null || updatedValue.get("yearOfBirth") == null){
            return;
        }
        Object yob = updatedValue.get("yearOfBirth");
        int yearOfBirth;
        if(yob instanceof Integer){
            yearOfBirth = (int)yob;
        } else if(yob instanceof String){
            yearOfBirth = Integer.parseInt((String)yob);
        } else{
            return;
        }

        int age = Integer.parseInt(date.split("-")[0])-yearOfBirth;
        int i;
        for(i=0;i<agList.length-1;i++){
            String[] ag = agList[i].trim().split("-");
            if(age>=Integer.parseInt(ag[0]) && age<Integer.parseInt(ag[1])){
                break;
            }
        }
        updatedValue.put(agOut,agList[i].trim());
    }
    
    static void processChannel(Map<String, Object> updatedValue, String[] agList){

        // agList expected in the form Both phone email, only phone, only email, None
        if(updatedValue.get("channel") == null){
            updatedValue.put("channel",agList[agList.length-1].trim());
            return;
        }
        List<Object> channelList = (List<Object>)updatedValue.get("channel");

        boolean hasPhone = false;
        boolean hasEmail = false;
        int agListIndex;

        for(Object c : channelList){

            if(c == null) continue;

            String channelTxt = (String)c;

            if(channelTxt.toLowerCase().equals("phone")){
                hasPhone = true;
            }
            else if(channelTxt.toLowerCase().equals("email")) {
                hasEmail = true;
            }
        }

        if(hasPhone && hasEmail) agListIndex = 0;
        else if(hasPhone) agListIndex = 1;
        else if(hasEmail) agListIndex = 2;
        else agListIndex = agList.length - 1;

        updatedValue.put("channel",agList[agListIndex].trim());
    }

    static void processProcessName(Map<String, Object> updatedValue){
        if( updatedValue.get("processName") == null ){
            return;
        }

        String pName = (String)updatedValue.get("processName");
        pName = pName.toLowerCase();
        String ret = pName.substring(0,1).toUpperCase() + pName.substring(1);

        updatedValue.put("processName",ret);
    }

    static void processUpdateProfile(Map<String, Object> updatedValue, String esUrl, String topicName){
        if(updatedValue.get("oldProfile") == null && updatedValue.get("newProfile") == null){
            return;
        }

        if(updatedValue.get("newProfile") == null) return;

        Base64 base64 = new Base64(); 

        JSONObject newProfile = new JSONObject((HashMap<String,Object>)updatedValue.get("newProfile"));
        String newProfileString = newProfile.toString();
        String newUpdateID = new String(base64.encode(newProfileString.getBytes()));
        updatedValue.put("updateId",newUpdateID);

        if(updatedValue.get("oldProfile") == null) return;

        JSONObject oldProfile = new JSONObject((HashMap<String,Object>)updatedValue.get("oldProfile"));
        String oldProfileString = oldProfile.toString();
        String oldUpdateID = new String(base64.encode(oldProfileString.getBytes()));


        String query = "{\"query\": {\"term\": {\"profile.updateId\":\"" + oldUpdateID +"\"}},\"size\": 1}";
        JSONObject responseJson;

        CloseableHttpClient hClient= HttpClients.createDefault();
        HttpGet hGet = new HttpGet(esUrl+"/"+topicName+"/_search");
        
        hGet.setHeader("Content-type", "application/json");
        hGet.setEntity(new StringEntity(query));

        
        String deleteId;
        try(CloseableHttpResponse hResponse = hClient.execute(hGet)){
            HttpEntity entity = hResponse.getEntity();
            String jsonString = EntityUtils.toString(entity);
            responseJson = new JSONObject(jsonString);
            deleteId = responseJson.getJSONObject("hits").getJSONArray("hits").getJSONObject(0).getString("_id");
            if(hResponse.getCode()!=200){
                System.out.println(">>>>>>> Unsuccessful while getting hits : " + jsonString);
                return;
            }
        }
        catch(Exception e){
            System.out.println(">>>>>>> In Exception: Unsuccessful while getting hits : "+e);
            return;
        }

        HttpDelete hDelete = new HttpDelete(esUrl+"/"+topicName+"/_doc/"+deleteId);

        try(CloseableHttpResponse hResponse = hClient.execute(hDelete)){
            HttpEntity entity = hResponse.getEntity();
            String jsonString = EntityUtils.toString(entity);
            if(hResponse.getCode()!=200){
                System.out.println(">>>>>>> Unsuccessful while deleting record: " + jsonString);
            }
        }
        catch(Exception e){
            System.out.println(">>>>>>> In Exception: Unsuccessful while deleting record : "+e);
        }
       
        return;
    }

    static void processRegistrationCenter(Map<String,Object> updateValue){
        if(updateValue.get("temp_latitude")==null || updateValue.get("temp_longitude")==null){
            return;
        }
        try{
            Double.parseDouble((String)updateValue.get("temp_latitude"));
            Double.parseDouble((String)updateValue.get("temp_longitude"));
        } catch(NumberFormatException nfe) {
            System.out.println("Couldn't parse Latitude/Longitude as Numbers, while Processing Reg Center Geo Location" + nfe);
            try{ updateValue.remove("temp_latitude"); updateValue.remove("temp_longitude"); } catch(Exception ignored){}
            return;
        }

        String ret = "";

        ret+=(String)updateValue.get("temp_latitude");
        ret+=",";
        ret+=(String)updateValue.get("temp_longitude");
        try{
            updateValue.remove("temp_latitude");
            updateValue.remove("temp_longitude");
        }
        catch(Exception e){
            throw new DataException("Geolocation processing: Something wrong with latitude and longitude");
        }
        updateValue.put("registrationCenterGeoLocation",ret);
    }
    // static Map<String,Object> processSchemasForSchemaLess(Map<String,Object> updateValue){
    //     returnValue = new HashMap<String, Object>();
    //     returnValue.put("payload",updateValue);
    //
    //     Map<String, Object> mSchema = new HashMap<String, Object>();
    //     mSchema.put("type","geo_point");
    //     mSchema.put("field","registrationCenterGeoLocation");
    //     returnValue.put("schema",mSchema);
    //
    //     return returnValue;
    // }

    static void esPutMapping(String esUrl, String topicName){
        String sRequest= "{\"mappings\": {\"properties\": {\"registrationCenterGeoLocation\": {\"type\": \"geo_point\"}, \"profile\": {\"properties\": {\"updateId\": {\"type\": \"keyword\"}}}}}}";

        CloseableHttpClient hClient= HttpClients.createDefault();
        HttpPut hPut = new HttpPut(esUrl+"/"+topicName+"/");
        hPut.setHeader("Content-type", "application/json");
        hPut.setEntity(new StringEntity(sRequest));
        try(CloseableHttpResponse hResponse = hClient.execute(hPut)){
            HttpEntity entity = hResponse.getEntity();
            String jsonString = EntityUtils.toString(entity);
            if(hResponse.getCode()!=200){
                System.out.println(">>>>>>>Unsuccessful while putting mapping : " + jsonString);
            }
        }
        catch(Exception e){
            System.out.println(">>>>>>>In Exception: Unsuccessful while putting mapping : "+e);
        }
    }

}
