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
import java.util.Iterator;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AnonymousProfileTest {

    private final StringToJson<SourceRecord> parentTransform = new StringToJson.Value<>();
    private final AnonymousProfileTransform<SourceRecord> xformValue = new AnonymousProfileTransform.Value<>();

    @AfterEach
    public void teardown() {
        xformValue.close();
    }

    @Test
    public void testSchemaless() {
        Map<String, String> config = new HashMap<>();
        xformValue.configure(config);
        config.put(StringToJson.INPUT_FIELD_CONFIG, "profile");
        parentTransform.configure(config);

        Map<String, Object> original = new HashMap<>();
        original.put("profile", "{\"biometricInfo\":[{\"type\":\"FINGER\",\"subType\":\"Left LittleFinger\",\"qualityScore\":81,\"attempts\":\"1\",\"digitalId\":\"{\\\"serialNo\\\":\\\"1234567890\\\",\\\"make\\\":\\\"MOSIP\\\",\\\"model\\\":\\\"SLAP01\\\",\\\"type\\\":\\\"Finger\\\",\\\"deviceSubType\\\":\\\"Slap\\\",\\\"deviceProviderId\\\":\\\"MOSIP.PROXY.SBI\\\",\\\"deviceProvider\\\":\\\"MOSIP\\\",\\\"dateTime\\\":\\\"2021-10-21T17:16:13Z\\\"}\"},{\"type\":\"FINGER\",\"subType\":\"Right Thumb\",\"qualityScore\":82,\"attempts\":\"1\",\"digitalId\":\"{\\\"serialNo\\\":\\\"1234567890\\\",\\\"make\\\":\\\"MOSIP\\\",\\\"model\\\":\\\"SLAP01\\\",\\\"type\\\":\\\"Finger\\\",\\\"deviceSubType\\\":\\\"Slap\\\",\\\"deviceProviderId\\\":\\\"MOSIP.PROXY.SBI\\\",\\\"deviceProvider\\\":\\\"MOSIP\\\",\\\"dateTime\\\":\\\"2021-10-21T17:16:19Z\\\"}\"},{\"type\":\"FINGER\",\"subType\":\"Right IndexFinger\",\"qualityScore\":83,\"attempts\":\"1\",\"digitalId\":\"{\\\"serialNo\\\":\\\"1234567890\\\",\\\"make\\\":\\\"MOSIP\\\",\\\"model\\\":\\\"SLAP01\\\",\\\"type\\\":\\\"Finger\\\",\\\"deviceSubType\\\":\\\"Slap\\\",\\\"deviceProviderId\\\":\\\"MOSIP.PROXY.SBI\\\",\\\"deviceProvider\\\":\\\"MOSIP\\\",\\\"dateTime\\\":\\\"2021-10-21T17:16:16Z\\\"}\"},{\"type\":\"FINGER\",\"subType\":\"Right LittleFinger\",\"qualityScore\":84,\"attempts\":\"1\",\"digitalId\":\"{\\\"serialNo\\\":\\\"1234567890\\\",\\\"make\\\":\\\"MOSIP\\\",\\\"model\\\":\\\"SLAP01\\\",\\\"type\\\":\\\"Finger\\\",\\\"deviceSubType\\\":\\\"Slap\\\",\\\"deviceProviderId\\\":\\\"MOSIP.PROXY.SBI\\\",\\\"deviceProvider\\\":\\\"MOSIP\\\",\\\"dateTime\\\":\\\"2021-10-21T17:16:16Z\\\"}\"},{\"type\":\"IRIS\",\"subType\":\"Right\",\"qualityScore\":80,\"attempts\":\"1\",\"digitalId\":\"{\\\"serialNo\\\":\\\"3456789012\\\",\\\"make\\\":\\\"MOSIP\\\",\\\"model\\\":\\\"IRIS01\\\",\\\"type\\\":\\\"Iris\\\",\\\"deviceSubType\\\":\\\"Double\\\",\\\"deviceProviderId\\\":\\\"MOSIP.PROXY.SBI\\\",\\\"deviceProvider\\\":\\\"MOSIP\\\",\\\"dateTime\\\":\\\"2021-10-21T17:16:22Z\\\"}\"},{\"type\":\"FINGER\",\"subType\":\"Left RingFinger\",\"qualityScore\":85,\"attempts\":\"1\",\"digitalId\":\"{\\\"serialNo\\\":\\\"1234567890\\\",\\\"make\\\":\\\"MOSIP\\\",\\\"model\\\":\\\"SLAP01\\\",\\\"type\\\":\\\"Finger\\\",\\\"deviceSubType\\\":\\\"Slap\\\",\\\"deviceProviderId\\\":\\\"MOSIP.PROXY.SBI\\\",\\\"deviceProvider\\\":\\\"MOSIP\\\",\\\"dateTime\\\":\\\"2021-10-21T17:16:13Z\\\"}\"},{\"type\":\"FACE\",\"subType\":null,\"qualityScore\":80,\"attempts\":\"3\",\"digitalId\":\"{\\\"serialNo\\\":\\\"2345678901\\\",\\\"make\\\":\\\"MOSIP\\\",\\\"model\\\":\\\"FACE01\\\",\\\"type\\\":\\\"Face\\\",\\\"deviceSubType\\\":\\\"Full face\\\",\\\"deviceProviderId\\\":\\\"MOSIP.PROXY.SBI\\\",\\\"deviceProvider\\\":\\\"MOSIP\\\",\\\"dateTime\\\":\\\"2021-10-21T17:16:31Z\\\"}\"},{\"type\":\"FINGER\",\"subType\":\"Right RingFinger\",\"qualityScore\":86,\"attempts\":\"1\",\"digitalId\":\"{\\\"serialNo\\\":\\\"1234567890\\\",\\\"make\\\":\\\"MOSIP\\\",\\\"model\\\":\\\"SLAP01\\\",\\\"type\\\":\\\"Finger\\\",\\\"deviceSubType\\\":\\\"Slap\\\",\\\"deviceProviderId\\\":\\\"MOSIP.PROXY.SBI\\\",\\\"deviceProvider\\\":\\\"MOSIP\\\",\\\"dateTime\\\":\\\"2021-10-21T17:16:16Z\\\"}\"},{\"type\":\"IRIS\",\"subType\":\"Left\",\"qualityScore\":80,\"attempts\":\"1\",\"digitalId\":\"{\\\"serialNo\\\":\\\"3456789012\\\",\\\"make\\\":\\\"MOSIP\\\",\\\"model\\\":\\\"IRIS01\\\",\\\"type\\\":\\\"Iris\\\",\\\"deviceSubType\\\":\\\"Double\\\",\\\"deviceProviderId\\\":\\\"MOSIP.PROXY.SBI\\\",\\\"deviceProvider\\\":\\\"MOSIP\\\",\\\"dateTime\\\":\\\"2021-10-21T17:16:22Z\\\"}\"},{\"type\":\"FINGER\",\"subType\":\"Left Thumb\",\"qualityScore\":87,\"attempts\":\"1\",\"digitalId\":\"{\\\"serialNo\\\":\\\"1234567890\\\",\\\"make\\\":\\\"MOSIP\\\",\\\"model\\\":\\\"SLAP01\\\",\\\"type\\\":\\\"Finger\\\",\\\"deviceSubType\\\":\\\"Slap\\\",\\\"deviceProviderId\\\":\\\"MOSIP.PROXY.SBI\\\",\\\"deviceProvider\\\":\\\"MOSIP\\\",\\\"dateTime\\\":\\\"2021-10-21T17:16:19Z\\\"}\"},{\"type\":\"FINGER\",\"subType\":\"Left IndexFinger\",\"qualityScore\":88,\"attempts\":\"1\",\"digitalId\":\"{\\\"serialNo\\\":\\\"1234567890\\\",\\\"make\\\":\\\"MOSIP\\\",\\\"model\\\":\\\"SLAP01\\\",\\\"type\\\":\\\"Finger\\\",\\\"deviceSubType\\\":\\\"Slap\\\",\\\"deviceProviderId\\\":\\\"MOSIP.PROXY.SBI\\\",\\\"deviceProvider\\\":\\\"MOSIP\\\",\\\"dateTime\\\":\\\"2021-10-21T17:16:13Z\\\"}\"},{\"type\":\"FINGER\",\"subType\":\"Right MiddleFinger\",\"qualityScore\":89,\"attempts\":\"1\",\"digitalId\":\"{\\\"serialNo\\\":\\\"1234567890\\\",\\\"make\\\":\\\"MOSIP\\\",\\\"model\\\":\\\"SLAP01\\\",\\\"type\\\":\\\"Finger\\\",\\\"deviceSubType\\\":\\\"Slap\\\",\\\"deviceProviderId\\\":\\\"MOSIP.PROXY.SBI\\\",\\\"deviceProvider\\\":\\\"MOSIP\\\",\\\"dateTime\\\":\\\"2021-10-21T17:16:16Z\\\"}\"},{\"type\":\"FINGER\",\"subType\":\"Left MiddleFinger\",\"qualityScore\":90,\"attempts\":\"1\",\"digitalId\":\"{\\\"serialNo\\\":\\\"1234567890\\\",\\\"make\\\":\\\"MOSIP\\\",\\\"model\\\":\\\"SLAP01\\\",\\\"type\\\":\\\"Finger\\\",\\\"deviceSubType\\\":\\\"Slap\\\",\\\"deviceProviderId\\\":\\\"MOSIP.PROXY.SBI\\\",\\\"deviceProvider\\\":\\\"MOSIP\\\",\\\"dateTime\\\":\\\"2021-10-21T17:16:13Z\\\"}\"}],\"location\":[\"Sidi Allal Tazi\",\"14050\"]}");
        original.put("test12", 4567);
        original.put("test123", "other123");
        original.put("test1234", true);

        SourceRecord transformed = xformValue.apply(parentTransform.apply(createRecordSchemaless(original)));

        System.out.println(transformed.value());

    }

    private void testSchemalessNullValueConversion() {
        Map<String, String> config = new HashMap<>();
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
