package io.mosip.kafka.connect.transforms;

import org.apache.kafka.common.config.ConfigException;
import org.apache.kafka.connect.data.Date;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.data.Time;
import org.apache.kafka.connect.data.Timestamp;
import org.apache.kafka.connect.source.SourceRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class StringToJsonTest {
    private final StringToJson<SourceRecord> xformValue = new StringToJson.Value<>();

    @AfterEach
    public void teardown() {
        xformValue.close();
    }

    @Test
    public void testSchemaless() {
        Map<String, String> config = new HashMap<>();
        config.put(StringToJson.INPUT_FIELD_CONFIG, "profile");

        xformValue.configure(config);

        Map<String, Object> original = new HashMap<>();
        // original.put("profile", "{\"nuller\":null,\"qwe\":123,\"double\":123.56778d,\"ert\":{\"asd\":\"123\",\"fgh\":[\"hello\",{\"hello1234\":true}]}}");
        original.put("profile", "{\"nuller\":null,\"qwe\":123,\"double\":123.56778d,\"ert\":{\"asd\":\"123\",\"fgh\":[\"hello\",\"namasthe\",{\"hello1234\":true}]}}");
        original.put("test12", 4567);
        original.put("test123", "other123");
        original.put("test1234", true);

        SourceRecord transformed = xformValue.apply(createRecordSchemaless(original));

        Map<String, Object> result123 = new HashMap<>();
        result123.put("hello1234",true);

        Map<String,Object> gotThis = ((Map<String,Object>)((Map<String,Object>)transformed.value()).get("profile"));

        assertNull( gotThis.get("nuller"));
        assertEquals(123, gotThis.get("qwe"));
        assertEquals(123.56778d, gotThis.get("double"));
        assertEquals("123", ((Map<String,Object>)gotThis.get("ert")).get("asd"));
        assertEquals("hello", ((List<Object>)((Map<String,Object>)gotThis.get("ert")).get("fgh")).get(0));
        assertEquals("namasthe", ((List<Object>)((Map<String,Object>)gotThis.get("ert")).get("fgh")).get(1));
        assertEquals(result123, ((List<Object>)((Map<String,Object>)gotThis.get("ert")).get("fgh")).get(2));
    }

    // Removing this test because its not ready
    // @Test
    public void testWithSchema() {
        Map<String, String> config = new HashMap<>();
        config.put(StringToJson.INPUT_FIELD_CONFIG, "profile");

        xformValue.configure(config);

        // ts field is a unix timestamp
        Schema structWithTimestampFieldSchema = SchemaBuilder.struct()
                .field("profile", Schema.STRING_SCHEMA)
                .field("test12", Schema.STRING_SCHEMA)
                .field("test123", Schema.STRING_SCHEMA)
                .build();
        Struct original = new Struct(structWithTimestampFieldSchema);
        original.put("profile", "{\"qwe\":123,\"double\":123.56778,\"ert\":{\"asd\":\"123\",\"fgh\":[\"hello\",{\"hello1234\":true}]}}");
        original.put("test12", "other12");
        original.put("test123", "other123");

        SourceRecord transformed = xformValue.apply(createRecordWithSchema(structWithTimestampFieldSchema, original));

        Schema expectedSchema = SchemaBuilder.struct()
                // .field("profile", Schema.STRING_SCHEMA)
                .field("test12", Schema.STRING_SCHEMA)
                .field("test123", Schema.STRING_SCHEMA)
                .build();
        Map<String, Object> result1 = new HashMap<>();
        Map<String, Object> result12 = new HashMap<>();
        Map<String, Object> result123 = new HashMap<>();
        result123.put("hello1234",true);
        Object[] array = {"hello",result123};
        result12.put("asd","123");
        result12.put("fgh",array);
        result1.put("qwe",123);
        result1.put("double",123.56778);
        result1.put("ert",result12);

        assertEquals(expectedSchema, transformed.valueSchema());
        assertEquals("other12", ((Struct) transformed.value()).get("test12"));
        assertEquals("other123", ((Struct) transformed.value()).get("test123"));
        assertEquals(result1, ((Struct) transformed.value()).get("profile"));
    }

    // Test null Value: Schemaless : timestamp -> string
    // Not working somehow
    @Test
    public void testSchemalessNullField() {
        Map<String, String> config = new HashMap<>();
        config.put(StringToJson.INPUT_FIELD_CONFIG, "profile");

        xformValue.configure(config);
        SourceRecord transformed = xformValue.apply(createRecordSchemaless(null));

        assertNull(transformed.valueSchema());
        assertNull(transformed.value());
    }

    private SourceRecord createRecordWithSchema(Schema schema, Object value) {
        return new SourceRecord(null, null, "topic", 0, schema, value);
    }

    private SourceRecord createRecordSchemaless(Object value) {
        return createRecordWithSchema(null, value);
    }
}
