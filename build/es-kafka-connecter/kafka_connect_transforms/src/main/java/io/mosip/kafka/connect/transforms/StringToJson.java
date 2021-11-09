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

import org.json.JSONObject;
import org.json.JSONArray;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.io.IOException;

import io.mosip.kafka.connect.transforms.SchemaUtil;
import static io.mosip.kafka.connect.transforms.Requirements.requireMap;
import static io.mosip.kafka.connect.transforms.Requirements.requireSinkRecord;
import static io.mosip.kafka.connect.transforms.Requirements.requireStruct;

public abstract class StringToJson<R extends ConnectRecord<R>> implements Transformation<R> {

    public static final String PURPOSE = "String to json converter";
    public static final String INPUT_FIELD_CONFIG = "input.field";

    private String configInputField;
    private Cache<Schema, Schema> schemaUpdateCache;

    public static ConfigDef CONFIG_DEF = new ConfigDef()
        .define(INPUT_FIELD_CONFIG, ConfigDef.Type.STRING, "", ConfigDef.Importance.HIGH, "The field that has to be converted to json/struct");

    @Override
    public void configure(Map<String, ?> configs) {
        AbstractConfig absconf = new AbstractConfig(CONFIG_DEF, configs, false);

        schemaUpdateCache = new SynchronizedCache<>(new LRUCache<Schema,Schema>(16));

        configInputField = absconf.getString(INPUT_FIELD_CONFIG);

        if(configInputField.isEmpty()){
            throw new ConfigException(INPUT_FIELD_CONFIG + " is not set.");
        }
    }

    @Override
    public ConfigDef config() {
        return CONFIG_DEF;
    }

    @Override
    public void close() {
        schemaUpdateCache = null;
    }

    @Override
    public R apply(R record) {
        if (operatingValue(record) == null) {
            return record;
        } else if (operatingSchema(record) == null) {
            return applySchemaless(record);
        } else {
            return applyWithSchema(record);
        }
    }

    protected abstract Schema operatingSchema(R record);

    protected abstract Object operatingValue(R record);

    protected abstract R newRecord(R record, Schema updatedSchema, Object updatedValue);

    public static class Key<R extends ConnectRecord<R>> extends StringToJson<R> {
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

    public static class Value<R extends ConnectRecord<R>> extends StringToJson<R> {
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
        final Map<String, Object> value = requireMap(operatingValue(record), PURPOSE);

        final Map<String, Object> updatedValue = new HashMap<>(value);

        if(value.get(configInputField) == null)
            updatedValue.put(configInputField,null);
        else if(!(value.get(configInputField) instanceof String))
            updatedValue.put(configInputField, "ERROR: THIS WAS NOT A STRING FIELD");
        else
            updatedValue.put(configInputField,returnSchemalessObject(new JSONObject((String)value.get(configInputField))));

        return newRecord(record, null, updatedValue);
    }

    private R applyWithSchema(R record) {
        final Struct value = requireStruct(operatingValue(record), PURPOSE);

        Schema updatedSchema = schemaUpdateCache.get(value.schema());
        if (updatedSchema == null) {
            updatedSchema = makeUpdatedSchema(value.schema());
            schemaUpdateCache.put(value.schema(), updatedSchema);
        }

        final Struct updatedValue = new Struct(updatedSchema);

        for (Field field : value.schema().fields()) {
            if(field.name().equals(configInputField)){
                if(field.schema()==Schema.STRING_SCHEMA || field.schema()==Schema.OPTIONAL_STRING_SCHEMA)
                    updatedValue.put(field.name(), returnSchemaObject(new JSONObject((String)value.get(field))));
                else
                    updatedValue.put(field.name(), "ERROR: THIS FIELD SCHEMA WAS NOT STRING");
            }
            else
                updatedValue.put(field.name(), value.get(field));
        }

        return newRecord(record, updatedSchema, updatedValue);
    }
    private Schema makeUpdatedSchema(Schema schema) {
        // this is not ready
        final SchemaBuilder builder = SchemaUtil.copySchemaBasics(schema, SchemaBuilder.struct());

        for (Field field : schema.fields()) {
            if(field.name().equals(configInputField))
                builder.field(field.name(), Schema.STRING_SCHEMA);
            else
                builder.field(field.name(), field.schema());
        }

        return builder.build();
    }

    static Object returnSchemalessObject(Object obj){
        if (obj instanceof JSONObject){
            Map<String, Object> returner = new HashMap<>();
            JSONObject json = (JSONObject)obj;
            Iterator<String> keys = json.keys();
            while(keys.hasNext()) {
                String key = keys.next();
                returner.put(key, returnSchemalessObject(json.get(key)));
            }
            return returner;
        }
        else if(obj instanceof JSONArray){
            JSONArray arr = (JSONArray)obj;
            List<Object> outArr = new ArrayList<>();
            for(int i=0;i<arr.length();i++){
                outArr.add(returnSchemalessObject(arr.get(i)));
            }
            return outArr;
        }
        else if(obj.equals(JSONObject.NULL)){
            return null;
        }
        // else if(){} // any other stuff here
        else{ // String, Integer, Long, Double, Number.. etc
            return obj;
        }
    }

    static Object returnSchemaObject(Object obj){
        // this is not ready
        return "WITH SCHEMA: NOT READY YET";
    }

}
