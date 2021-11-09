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

        assertEquals(transformed.value().toString(),"{test1234=true, test12=4567, test123=other123, profile={biometricInfo={IRIS={qualityScore=80.0, type=IRIS, digitalId={deviceSubType=Double, model=IRIS01, type=Iris, make=MOSIP, serialNo=3456789012, deviceProviderId=MOSIP.PROXY.SBI, deviceProvider=MOSIP}, attempts=1.0}, FINGER={qualityScore=85.5, type=FINGER, digitalId={deviceSubType=Slap, model=SLAP01, type=Finger, make=MOSIP, serialNo=1234567890, deviceProviderId=MOSIP.PROXY.SBI, deviceProvider=MOSIP}, attempts=1.0}, FACE={qualityScore=80.0, type=FACE, digitalId={deviceSubType=Full face, model=FACE01, type=Face, make=MOSIP, serialNo=2345678901, deviceProviderId=MOSIP.PROXY.SBI, deviceProvider=MOSIP}, attempts=3.0}}, location={hierarchy2=14050, hierarchy1=Sidi Allal Tazi}}}");
    }

    @Test
    public void testSchemalessWithJWTDigitalId() {
        Map<String, String> config = new HashMap<>();
        xformValue.configure(config);
        config.put(StringToJson.INPUT_FIELD_CONFIG, "profile");
        parentTransform.configure(config);

        Map<String, Object> original = new HashMap<>();
        original.put("profile", "{\"biometricInfo\":[{\"type\":\"FINGER\",\"subType\":\"Left LittleFinger\",\"qualityScore\":81,\"attempts\":\"1\",\"digitalId\":\"{\\\"serialNo\\\":\\\"1234567890\\\",\\\"make\\\":\\\"MOSIP\\\",\\\"model\\\":\\\"SLAP01\\\",\\\"type\\\":\\\"Finger\\\",\\\"deviceSubType\\\":\\\"Slap\\\",\\\"deviceProviderId\\\":\\\"MOSIP.PROXY.SBI\\\",\\\"deviceProvider\\\":\\\"MOSIP\\\",\\\"dateTime\\\":\\\"2021-10-21T17:16:13Z\\\"}\"},{\"type\":\"FINGER\",\"subType\":\"Right Thumb\",\"qualityScore\":82,\"attempts\":\"1\",\"digitalId\":\"eyJ4NWMiOlsiTUlJRjZqQ0NBOUtnQXdJQkFnSUJCVEFOQmdrcWhraUc5dzBCQVFzRkFEQlRNUXN3Q1FZRFZRUUdFd0pKVGpFTU1Bb0dBMVVFQ0F3RFMyRnlNUXd3Q2dZRFZRUUhEQU5DYkhJeEREQUtCZ05WQkFvTUEwUlFNakVNTUFvR0ExVUVDd3dEUkZBeU1Rd3dDZ1lEVlFRRERBTkVVREl3SGhjTk1qRXdPVEV6TVRBeE9EQXpXaGNOTWpFeE1ERXpNVEF4T0RBeldqQldNUXN3Q1FZRFZRUUdFd0pKVGpFTU1Bb0dBMVVFQ0F3RFMyRnlNUXd3Q2dZRFZRUUhEQU5DYkhJeERUQUxCZ05WQkFvTUJFWkJRMFV4RFRBTEJnTlZCQXNNQkVaQlEwVXhEVEFMQmdOVkJBTU1CRVpCUTBVd2dnSWlNQTBHQ1NxR1NJYjNEUUVCQVFVQUE0SUNEd0F3Z2dJS0FvSUNBUUNoQTk2am1tNFd2T1orTWc4aFJhay9Id2pIaFNSZjBOaTBBRkd1Wkx2UE5hWmNZTVoxMHZyRThBWWZGalRlbTBmck1Idy9YVWZWcDNUNVRzbytWMVRFVTEzUkd1TGJteE41TTc3a1hPUnp4aTlPeFkzcGp3QVMzZFVzN3ZOcUFWSy8vZHYzL1c4aHQzZ3ZzOFB4ZXNlUTBTemh2SjJWUUdHQm5iYnpvekhqTEZDMDRlM3NmT2J1bVJra2VJNjZUSzArMHpSa2FONldicnM3WFFPdDlNRGc2NnhFN0w1U3ZJTkZaZzZOLzd2ZFZwTFUxRVpKcEpwdWJvZUJWUjFOS0tJR1gveWMxYkxrZ1hBSFkvc2ZNeWNpYUpJcmFaSHRhaHdxekhaU0NMdEVtdnIrcTk1TFBEekpGNXRHTHlVU3FUeE1zWEw5dTQ4Vlp3dC9EU1lNd2ZkamE5SU9NWGNBVklNWjFmeVRGYTlLTGFGK3B2QVZ1VkFJQ3NCZnpHSGZZNElWdHZTU1VCWnRSNnFWdnJLaVRCZTVuNGxhMFdGelVNSDdZa0FHR3o1ZkhQalpWc3FwanNpNVZ1U1R5cFg0OUJPRnNTbCtSQVdSeU45YzRqQVAvQ0cxVFUyYmFvUDhxYVY4dWpLUG4rZU5yRkRNOTl5cEtDTmlOL2ZGeWd3V2xvOHJVdklKOTg4YU1VM0MvcU1OcHRKMUNidDhmUSt3aER5WkNXaXFuQzJCNzA1NmxkVWJwQktmR1lnY0k4am5ob25xa0tVanFjcEEvNGRLTkw3TnI5ayswNDdjUXNMeUFXczVuRzhBVXd3M2p0KzNGMWVFSjBXdWpWeDFMbFBha1JHekR5eG55TVpuU0xXaG1RSDhKejJWTldsTTA4S3doWlVTdktZT3VpbXMzUUlEQVFBQm80SEZNSUhDTUFrR0ExVWRFd1FDTUFBd0VRWUpZSVpJQVliNFFnRUJCQVFEQWdXZ01ETUdDV0NHU0FHRytFSUJEUVFtRmlSUGNHVnVVMU5NSUVkbGJtVnlZWFJsWkNCRGJHbGxiblFnUTJWeWRHbG1hV05oZEdVd0hRWURWUjBPQkJZRUZHeGVQSlNmc1dQUW84bHdXcStOT2o4dUZQWTVNQjhHQTFVZEl3UVlNQmFBRkpmYTZXQWtieXFJYjVvOGpNWHpIT1hoQy85U01BNEdBMVVkRHdFQi93UUVBd0lGNERBZEJnTlZIU1VFRmpBVUJnZ3JCZ0VGQlFjREFnWUlLd1lCQlFVSEF3UXdEUVlKS29aSWh2Y05BUUVMQlFBRGdnSUJBS1BEK3RYenBTNnpsWlowUFBOTkltb3ZvZVZSZFEzNVBaL0F5OEUyUUJudzN2L0tOR3ZGVmFuZGRENTFaeW5XQkk1TDAybXNUMmVYWnVYRVBtNnRJdXRqRWdWYlBKSVc3VFVxbDk0RXBnS3lKMmNzYkVVdWduRklQUUxLYlFWeEhSZVVHYWtmSjYwZXZTY1d4Qjd3VkgxbXVzWjR4bE1pK1RmTmd4M2lqUHNZa1pDOXZhdFUxajc3RWdaR1pDNy8wYzNoQXpacDVGTGVnUEtDUE81UlgveW5CTTZ1U1dKQnFCUVhaRG1lcVlsMDVsdVRGbk1DR01SbzUzMWFLWjhnOWRRdXVzT3dvYmV3V1VEUEJtTy9kNXNjUjY0T0JHengwQUhnR0JyK01oZldlcDdWMWliRDRsK011YkltODhaQjlJaHFnNnYvSEVmM3hmSlptdUF1NVRJWlg2TERIUm45NWlKTm9nRTZwWXgvZU44L1U4bGxlcFduQWJBRGVZT0YyQjNSRVdiZWpHcFQ1NjlkZXVsTExybEwrNGQxNDVFV0diRHY2N2d2MzNyVnR6a05Hc3dkVlZEenhvdVNCWHBjQ2lWeFpobGZEZ2ZiZGNQRlJHNWUxQkdMM3pqOWFxQnpOVUYvZ2VhR1Q4M3RuK0VteWdRRjVrbGdQcThSbGJJeml0TGZBcExlbTgwZkxHM0RqbmdWbWFhSVJDc3cwVVg4YmhONzFKOWpoZzJncUZDbGVSUDBTdFE0RC8yeitZdzhrYkpBMFcrRjdIZnZJU29qSU5aV0Via3JjOFNLUlc5NTdSNXl0a0tLV1hZUGZMdDloTGNzWVFWREtUS2Q0WVl3SUwrdWhKQjYzdlZPZFlCRXN3Ui9wclB2amRJUjI4dmZvc1B2Rk83UFB6KzQiXSwiYWxnIjoiUlMyNTYiLCJ0eXAiOiJKV1QifQ.eyJzZXJpYWxObyI6IjEyMzQ1Njc4OTAiLCJtYWtlIjoiTU9TSVAiLCJtb2RlbCI6IlNMQVAwMSIsInR5cGUiOiJGaW5nZXIiLCJkZXZpY2VTdWJUeXBlIjoiU2xhcCIsImRldmljZVByb3ZpZGVySWQiOiJNT1NJUC5QUk9YWS5TQkkiLCJkZXZpY2VQcm92aWRlciI6Ik1PU0lQIiwiZGF0ZVRpbWUiOiIyMDIxLTA5LTIxVDA4OjI5OjI1WiJ9.YJtf-B7Ic8_YoMPNySif7kKeEN_v5ORYZfj87q7WCo8g8SMJ2QJ_FhWNDHmrfOoP1E9ou8okrnDq4HzZxw2Y58nBmExNSRoNpHVV0uoPGy_8YeBv1sStdTUev8N5JYGir1DAte0Ywjik-uyP4RFZmcMF8IQvA7yO5UZzS4UQ0Ud7RbkUoxQlvURuP6VR1XKDJouvBJf0Ktd-EmgQztOz5jKj0Od2EF3IErBvLhQy2c5JlkNKXp0UmIPTVHnzWcc8AKqkALHX3zqz7Xm8pJLo0LcMYIhUEfewf96LslMWgl_TYtUnxO41G78uBMYmMPTtXjakKt-jbgjmIHBJHmDZfW0eiwvXGrI_ahOKMYi2H3km6NE2TkNhqo2SKTNwBzq94DGvTinOVk4OVGKPz2SW9VSm0npdR3tASRkFsAlst_WYr5RAl7PsP34Ihtpi-VrEzmlv6zgLfaynhhOw-X_phmfwKCsQ_nXHc53Oh1duhjBUM--yvCgBt2Di79CBU3NLuGQWZ2yr5wNilagVADsd6gCbL0JHvjszS9Tm9a4HQyLsKQTzNqS4QuyVs4uXtKkpLv1rdEChYS2d9wzke0j6JaEUyl44_SFeMr8DmtcB-Nl8lhC5tWxVT-7Q8oSRGQZgHNDFB_A_c7zGCUE1ktvkAHabujPwwl7Tm7xx5M2kxAM\"}],\"location\":[\"Sidi Allal Tazi\",\"14050\"]}");
        original.put("test12", 4567);
        original.put("test123", "other123");
        original.put("test1234", true);

        SourceRecord transformed = xformValue.apply(parentTransform.apply(createRecordSchemaless(original)));

        // System.out.println(transformed.value());
        assertEquals(transformed.value().toString(),"{test1234=true, test12=4567, test123=other123, profile={biometricInfo={FINGER={qualityScore=81.5, type=FINGER, digitalId={deviceSubType=Slap, model=SLAP01, type=Finger, make=MOSIP, serialNo=1234567890, deviceProviderId=MOSIP.PROXY.SBI, deviceProvider=MOSIP}, attempts=1.0}}, location={hierarchy2=14050, hierarchy1=Sidi Allal Tazi}}}");
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
