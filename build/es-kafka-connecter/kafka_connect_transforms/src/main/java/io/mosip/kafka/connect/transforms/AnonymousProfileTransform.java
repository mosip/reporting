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

    public static ConfigDef CONFIG_DEF = new ConfigDef();

    @Override
    public void configure(Map<String, ?> configs) {
        // nothing to configure
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

        Map<String, Object> updatedValue = new HashMap<>(value);

        processBiometricList(updatedValue);
        processLocationList(updatedValue);
        // rest of the transform funcs

        return newRecord(record, null, updatedValue);
    }

    void processBiometricList(Map<String, Object> updatedValue){
        if(updatedValue.get("profile") == null){
            return;
        }
        else if( ((Map<String,Object>)updatedValue.get("profile")).get("biometricInfo") == null ){
            return;
        }

        List<Object> arr = (List<Object>)((Map<String,Object>)updatedValue.get("profile")).get("biometricInfo");

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

        ((Map<String,Object>)updatedValue.get("profile")).put("biometricInfo",ret);
    }

    void processLocationList(Map<String, Object> updatedValue){
        if(updatedValue.get("profile") == null){
            return;
        }
        else if( ((Map<String,Object>)updatedValue.get("profile")).get("location") == null ){
            return;
        }

        List<Object> arr = (List<Object>)((Map<String,Object>)updatedValue.get("profile")).get("location");

        Map<String, Object> ret = new HashMap<>();

        for(int i=0;i<arr.size();i++){
            ret.put("hierarchy"+(i+1),arr.get(i));
        }
        ((Map<String,Object>)updatedValue.get("profile")).put("location",ret);
    }

}
