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

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.io.IOException;

import io.mosip.kafka.connect.transforms.SchemaUtil;
import static io.mosip.kafka.connect.transforms.Requirements.requireMap;
import static io.mosip.kafka.connect.transforms.Requirements.requireSinkRecord;
import static io.mosip.kafka.connect.transforms.Requirements.requireStruct;

public abstract class TimestampSelector<R extends ConnectRecord<R>> implements Transformation<R> {

    private class Config{
        String[] tsOrder;
        String outputField;

        Config(String[] tso, String outField){
            this.tsOrder = tso;
            this.outputField = outField;
        }

        // Object make(Object input){
        //
        // }
    }

    public static final String PURPOSE = "select timestamp in order";
    public static final String TS_ORDER_CONFIG = "ts.order";
    public static final String OUTPUT_FIELD_CONFIG = "output.field";

    private Config config;
    private Cache<Schema, Schema> schemaUpdateCache;

    public static ConfigDef CONFIG_DEF = new ConfigDef()
        .define(TS_ORDER_CONFIG, ConfigDef.Type.STRING, "", ConfigDef.Importance.HIGH, "The order of the timestamp fields to select from.")
        .define(OUTPUT_FIELD_CONFIG, ConfigDef.Type.STRING, "@ts_generated", ConfigDef.Importance.HIGH, "Name of the resultant/ouptut timestamp field.");

    @Override
    public void configure(Map<String, ?> configs) {
        AbstractConfig absconf = new AbstractConfig(CONFIG_DEF, configs, false);

        schemaUpdateCache = new SynchronizedCache<>(new LRUCache<Schema,Schema>(16));

        String tsOrderBulk = absconf.getString(TS_ORDER_CONFIG);
        String outputField = absconf.getString(OUTPUT_FIELD_CONFIG);

        if(tsOrderBulk.isEmpty()){
            throw new ConfigException("One of required transform config fields not set. Required field in tranforms: " + TS_ORDER_CONFIG + ". Optional Fields: " + OUTPUT_FIELD_CONFIG);
        }

        String[] tsOrder = tsOrderBulk.replaceAll("\\s+","").split(",");

        if(tsOrder.length == 0){
            throw new ConfigException("Number of fields in timestamp order are zero.");
        }

        config = new Config(tsOrder,outputField);
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

    public static class Key<R extends ConnectRecord<R>> extends TimestampSelector<R> {
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

    public static class Value<R extends ConnectRecord<R>> extends TimestampSelector<R> {
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

        Object ret=null;
        for(String field : config.tsOrder){
            ret = Requirements.getNestedField(value,field);
            if(ret!=null){ break; }
        }
        if(ret==null){
            throw new DataException("None of the fields mentioned in timestamp order have a valid value.");
        }

        updatedValue.put(config.outputField, ret);

        return newRecord(record, null, updatedValue);
    }

    private R applyWithSchema(R record) {
        final Struct value = requireStruct(operatingValue(record), PURPOSE);

        Object ret=null;
        Object outSchema=null;
        for(String field : config.tsOrder){
            Object tmp = Requirements.getNestedField(value,field);
            ret = ((Object[])tmp)[0]; outSchema = ((Object[])tmp)[1];
            if(ret!=null)if(!ret.equals("")) break;
        }
        if(ret==null){
            throw new DataException("None of the fields mentioned in timestamp order have a valid value.");
        }

        Schema updatedSchema = schemaUpdateCache.get(value.schema());
        if (updatedSchema == null) {
            // Hardcoding to string schema here .. which might not be correct in all cases
            updatedSchema = makeUpdatedSchema(value.schema(), config.outputField, (Schema)outSchema);
            schemaUpdateCache.put(value.schema(), updatedSchema);
        }

        final Struct updatedValue = new Struct(updatedSchema);

        for (Field field : value.schema().fields()) {
            updatedValue.put(field.name(), value.get(field));
        }

        updatedValue.put(config.outputField, ret);

        return newRecord(record, updatedSchema, updatedValue);
    }
    static Schema makeUpdatedSchema(Schema schema, String outField, Schema outSchema) {
        final SchemaBuilder builder = SchemaUtil.copySchemaBasics(schema, SchemaBuilder.struct());

        for (Field field : schema.fields()) {
            builder.field(field.name(), field.schema());
        }

        builder.field(outField, outSchema);

        return builder.build();
    }


}
