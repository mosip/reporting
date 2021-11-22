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

import org.json.JSONObject;
import org.json.JSONException;
import org.json.JSONArray;

import java.util.Arrays;
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

    private String[] profileFieldsList;
    private String[] ageGroupsList;
    private String[] channelGroupList;
    private String ageGroupsOuputField;

    public static ConfigDef CONFIG_DEF = new ConfigDef()
        .define(PROFILES_FIELDS, ConfigDef.Type.STRING, "profile", ConfigDef.Importance.HIGH, "This is a list of profiles that have to be processed by this transform")
        .define(AGE_GROUPS_FIELD, ConfigDef.Type.STRING, "", ConfigDef.Importance.HIGH, "Give the age groups in which it has to be categorised")
        .define(AGE_GROUPS_OUPUT_FIELD, ConfigDef.Type.STRING, "", ConfigDef.Importance.HIGH, "the ouput field in which agegroup has to be put (w.r.t to the profile fields)")
        .define(CHANNEL_GROUPS_FIELD, ConfigDef.Type.STRING, "", ConfigDef.Importance.HIGH, "Give the categories in which channel needs to be categorised, Code Hardcoded accoring to Both phone email, only phone, only email, None");

    @Override
    public void configure(Map<String, ?> configs) {
        AbstractConfig absconf = new AbstractConfig(CONFIG_DEF, configs, false);
        String prFL = absconf.getString(PROFILES_FIELDS);
        String agl = absconf.getString(AGE_GROUPS_FIELD);
        String cgl = absconf.getString(CHANNEL_GROUPS_FIELD);

        ageGroupsOuputField = absconf.getString(AGE_GROUPS_OUPUT_FIELD);

        profileFieldsList = prFL.replaceAll("\\s+","").split(",");
        ageGroupsList = agl.split(",");
        channelGroupList = cgl.split(",");


        if(prFL.isEmpty() || agl.isEmpty() || ageGroupsOuputField.isEmpty() || cgl.isEmpty()){
            throw new ConfigException("All the required fields are not set. Required Fields: " + PROFILES_FIELDS + " ," + AGE_GROUPS_FIELD + " ," + AGE_GROUPS_OUPUT_FIELD + " ," + CHANNEL_GROUPS_FIELD);
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

        Map<String, Object> updatedValueRoot = new HashMap<>(value);

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
                processBiometricList(updatedValue);
                processLocationList(updatedValue);
                processAgeGroup(updatedValue,ageGroupsList,ageGroupsOuputField);
                processChannel(updatedValue, channelGroupList);
                processProcessName(updatedValue);
                // rest of the transform funcs
            }
        }

        return newRecord(record, null, updatedValueRoot);
    }

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
                    qualScoreSum+=(int)each.get("qualityScore");
                    attemptsSum+=Integer.parseInt((String)m.get("attempts"));
                    count++;
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
            m.put("attempts",attemptsSum/count);
            m.put("qualityScore", qualScoreSum/count);

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
    static void processAgeGroup(Map<String, Object> updatedValue, String[] agList, String agOut){
        if(updatedValue.get("date") == null || updatedValue.get("yearOfBirth") == null){
            return;
        }
        int age = Integer.parseInt(((String)updatedValue.get("date")).split("-")[0])-(int)updatedValue.get("yearOfBirth");
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

}
