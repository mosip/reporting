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

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import java.lang.Thread;

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
        config.put(StringToJson.INPUT_FIELD_CONFIG, "profile");
        parentTransform.configure(config);
        config = new HashMap<>();
        config.put(AnonymousProfileTransform.PROFILES_FIELDS, "profile");
        config.put(AnonymousProfileTransform.AGE_GROUPS_FIELD, "1-18,18-24,24-50,50-100,default");
        config.put(AnonymousProfileTransform.AGE_GROUPS_OUPUT_FIELD, "ageGroup");
        config.put(AnonymousProfileTransform.CHANNEL_GROUPS_FIELD, "Both phone and email,only phone,only email,None");
        config.put(AnonymousProfileTransform.TOPIC_NAME_FIELD, "some-test-index");
        config.put(AnonymousProfileTransform.ES_URL_FIELD, "http://localhost:9200");
        config.put(AnonymousProfileTransform.TRANSFORM_FUNCTIONS, "processRegistrationCenter");
        config.put(AnonymousProfileTransform.TRANSFORM_FUNCTIONS_PROFILE, "processBiometricList,processAgeGroup,processChannel,processProcessName,processAssister,processLocationList,processUpdateProfile");
        
        xformValue.configure(config);

        Map<String, Object> original = new HashMap<>();
        original.put("profile", "{\"assisted\":[\"10060\",\"10060\"] ,\"biometricInfo\":[{\"type\":\"FINGER\",\"subType\":\"Left LittleFinger\",\"qualityScore\":81,\"attempts\":\"1\",\"digitalId\":\"{\\\"serialNo\\\":\\\"1234567890\\\",\\\"make\\\":\\\"MOSIP\\\",\\\"model\\\":\\\"SLAP01\\\",\\\"type\\\":\\\"Finger\\\",\\\"deviceSubType\\\":\\\"Slap\\\",\\\"deviceProviderId\\\":\\\"MOSIP.PROXY.SBI\\\",\\\"deviceProvider\\\":\\\"MOSIP\\\",\\\"dateTime\\\":\\\"2021-10-21T17:16:13Z\\\"}\"},{\"type\":\"FINGER\",\"subType\":\"Right Thumb\",\"qualityScore\":82,\"attempts\":\"1\",\"digitalId\":\"{\\\"serialNo\\\":\\\"1234567890\\\",\\\"make\\\":\\\"MOSIP\\\",\\\"model\\\":\\\"SLAP01\\\",\\\"type\\\":\\\"Finger\\\",\\\"deviceSubType\\\":\\\"Slap\\\",\\\"deviceProviderId\\\":\\\"MOSIP.PROXY.SBI\\\",\\\"deviceProvider\\\":\\\"MOSIP\\\",\\\"dateTime\\\":\\\"2021-10-21T17:16:19Z\\\"}\"},{\"type\":\"FINGER\",\"subType\":\"Right IndexFinger\",\"qualityScore\":83,\"attempts\":\"1\",\"digitalId\":\"{\\\"serialNo\\\":\\\"1234567890\\\",\\\"make\\\":\\\"MOSIP\\\",\\\"model\\\":\\\"SLAP01\\\",\\\"type\\\":\\\"Finger\\\",\\\"deviceSubType\\\":\\\"Slap\\\",\\\"deviceProviderId\\\":\\\"MOSIP.PROXY.SBI\\\",\\\"deviceProvider\\\":\\\"MOSIP\\\",\\\"dateTime\\\":\\\"2021-10-21T17:16:16Z\\\"}\"},{\"type\":\"FINGER\",\"subType\":\"Right LittleFinger\",\"qualityScore\":84,\"attempts\":\"1\",\"digitalId\":\"{\\\"serialNo\\\":\\\"1234567890\\\",\\\"make\\\":\\\"MOSIP\\\",\\\"model\\\":\\\"SLAP01\\\",\\\"type\\\":\\\"Finger\\\",\\\"deviceSubType\\\":\\\"Slap\\\",\\\"deviceProviderId\\\":\\\"MOSIP.PROXY.SBI\\\",\\\"deviceProvider\\\":\\\"MOSIP\\\",\\\"dateTime\\\":\\\"2021-10-21T17:16:16Z\\\"}\"},{\"type\":\"IRIS\",\"subType\":\"Right\",\"qualityScore\":80,\"attempts\":\"1\",\"digitalId\":\"{\\\"serialNo\\\":\\\"3456789012\\\",\\\"make\\\":\\\"MOSIP\\\",\\\"model\\\":\\\"IRIS01\\\",\\\"type\\\":\\\"Iris\\\",\\\"deviceSubType\\\":\\\"Double\\\",\\\"deviceProviderId\\\":\\\"MOSIP.PROXY.SBI\\\",\\\"deviceProvider\\\":\\\"MOSIP\\\",\\\"dateTime\\\":\\\"2021-10-21T17:16:22Z\\\"}\"},{\"type\":\"FINGER\",\"subType\":\"Left RingFinger\",\"qualityScore\":85,\"attempts\":\"1\",\"digitalId\":\"{\\\"serialNo\\\":\\\"1234567890\\\",\\\"make\\\":\\\"MOSIP\\\",\\\"model\\\":\\\"SLAP01\\\",\\\"type\\\":\\\"Finger\\\",\\\"deviceSubType\\\":\\\"Slap\\\",\\\"deviceProviderId\\\":\\\"MOSIP.PROXY.SBI\\\",\\\"deviceProvider\\\":\\\"MOSIP\\\",\\\"dateTime\\\":\\\"2021-10-21T17:16:13Z\\\"}\"},{\"type\":\"FACE\",\"subType\":null,\"qualityScore\":80,\"attempts\":\"3\",\"digitalId\":\"{\\\"serialNo\\\":\\\"2345678901\\\",\\\"make\\\":\\\"MOSIP\\\",\\\"model\\\":\\\"FACE01\\\",\\\"type\\\":\\\"Face\\\",\\\"deviceSubType\\\":\\\"Full face\\\",\\\"deviceProviderId\\\":\\\"MOSIP.PROXY.SBI\\\",\\\"deviceProvider\\\":\\\"MOSIP\\\",\\\"dateTime\\\":\\\"2021-10-21T17:16:31Z\\\"}\"},{\"type\":\"FINGER\",\"subType\":\"Right RingFinger\",\"qualityScore\":86,\"attempts\":\"1\",\"digitalId\":\"{\\\"serialNo\\\":\\\"1234567890\\\",\\\"make\\\":\\\"MOSIP\\\",\\\"model\\\":\\\"SLAP01\\\",\\\"type\\\":\\\"Finger\\\",\\\"deviceSubType\\\":\\\"Slap\\\",\\\"deviceProviderId\\\":\\\"MOSIP.PROXY.SBI\\\",\\\"deviceProvider\\\":\\\"MOSIP\\\",\\\"dateTime\\\":\\\"2021-10-21T17:16:16Z\\\"}\"},{\"type\":\"IRIS\",\"subType\":\"Left\",\"qualityScore\":80,\"attempts\":\"1\",\"digitalId\":\"{\\\"serialNo\\\":\\\"3456789012\\\",\\\"make\\\":\\\"MOSIP\\\",\\\"model\\\":\\\"IRIS01\\\",\\\"type\\\":\\\"Iris\\\",\\\"deviceSubType\\\":\\\"Double\\\",\\\"deviceProviderId\\\":\\\"MOSIP.PROXY.SBI\\\",\\\"deviceProvider\\\":\\\"MOSIP\\\",\\\"dateTime\\\":\\\"2021-10-21T17:16:22Z\\\"}\"},{\"type\":\"FINGER\",\"subType\":\"Left Thumb\",\"qualityScore\":87,\"attempts\":\"1\",\"digitalId\":\"{\\\"serialNo\\\":\\\"1234567890\\\",\\\"make\\\":\\\"MOSIP\\\",\\\"model\\\":\\\"SLAP01\\\",\\\"type\\\":\\\"Finger\\\",\\\"deviceSubType\\\":\\\"Slap\\\",\\\"deviceProviderId\\\":\\\"MOSIP.PROXY.SBI\\\",\\\"deviceProvider\\\":\\\"MOSIP\\\",\\\"dateTime\\\":\\\"2021-10-21T17:16:19Z\\\"}\"},{\"type\":\"FINGER\",\"subType\":\"Left IndexFinger\",\"qualityScore\":88,\"attempts\":\"1\",\"digitalId\":\"{\\\"serialNo\\\":\\\"1234567890\\\",\\\"make\\\":\\\"MOSIP\\\",\\\"model\\\":\\\"SLAP01\\\",\\\"type\\\":\\\"Finger\\\",\\\"deviceSubType\\\":\\\"Slap\\\",\\\"deviceProviderId\\\":\\\"MOSIP.PROXY.SBI\\\",\\\"deviceProvider\\\":\\\"MOSIP\\\",\\\"dateTime\\\":\\\"2021-10-21T17:16:13Z\\\"}\"},{\"type\":\"FINGER\",\"subType\":\"Right MiddleFinger\",\"qualityScore\":89,\"attempts\":\"1\",\"digitalId\":\"{\\\"serialNo\\\":\\\"1234567890\\\",\\\"make\\\":\\\"MOSIP\\\",\\\"model\\\":\\\"SLAP01\\\",\\\"type\\\":\\\"Finger\\\",\\\"deviceSubType\\\":\\\"Slap\\\",\\\"deviceProviderId\\\":\\\"MOSIP.PROXY.SBI\\\",\\\"deviceProvider\\\":\\\"MOSIP\\\",\\\"dateTime\\\":\\\"2021-10-21T17:16:16Z\\\"}\"},{\"type\":\"FINGER\",\"subType\":\"Left MiddleFinger\",\"qualityScore\":90,\"attempts\":\"1\",\"digitalId\":\"{\\\"serialNo\\\":\\\"1234567890\\\",\\\"make\\\":\\\"MOSIP\\\",\\\"model\\\":\\\"SLAP01\\\",\\\"type\\\":\\\"Finger\\\",\\\"deviceSubType\\\":\\\"Slap\\\",\\\"deviceProviderId\\\":\\\"MOSIP.PROXY.SBI\\\",\\\"deviceProvider\\\":\\\"MOSIP\\\",\\\"dateTime\\\":\\\"2021-10-21T17:16:13Z\\\"}\"}],\"location\":[\"Sidi Allal Tazi\",\"14050\"],\"date\":\"2021-09-22T11:20:30.796464Z\",\"yearOfBirth\": 1960,\"channel\":[null,null],\"processName\":\"NEW\"}");
        original.put("temp_latitude","34.6");
        original.put("temp_longitude","-34.6");
        original.put("test12", 4567);
        original.put("test123", "other123");
        original.put("test1234", true);

        SourceRecord transformed = xformValue.apply(parentTransform.apply(createRecordSchemaless(original, "some-test-index")));

        // System.out.println(transformed.value());
        assertEquals("{test1234=true, registrationCenterGeoLocation=34.6,-34.6, test12=4567, test123=other123, profile={date=2021-09-22T11:20:30.796464Z, assisted=[10060, 10060], biometricInfo={IRIS={qualityScore=80.0, type=IRIS, digitalId={deviceSubType=Double, model=IRIS01, type=Iris, make=MOSIP, serialNo=3456789012, deviceProviderId=MOSIP.PROXY.SBI, deviceProvider=MOSIP}, attempts=1.0}, FINGER={qualityScore=85.5, type=FINGER, digitalId={deviceSubType=Slap, model=SLAP01, type=Finger, make=MOSIP, serialNo=1234567890, deviceProviderId=MOSIP.PROXY.SBI, deviceProvider=MOSIP}, attempts=1.0}, FACE={qualityScore=80.0, type=FACE, digitalId={deviceSubType=Full face, model=FACE01, type=Face, make=MOSIP, serialNo=2345678901, deviceProviderId=MOSIP.PROXY.SBI, deviceProvider=MOSIP}, attempts=3.0}}, processName=New, channel=None, location={hierarchy2=14050, hierarchy1=Sidi Allal Tazi}, ageGroup=50-100, registrationOfficers={Operator=10060, Supervisor=10060}, yearOfBirth=1960}}",transformed.value().toString());
    }

    @Test
    public void testSchemalessWithJWTDigitalId() {
      Map<String, String> config = new HashMap<>();
      config.put(StringToJson.INPUT_FIELD_CONFIG, "profile");
      parentTransform.configure(config);
      config = new HashMap<>();
      config.put(AnonymousProfileTransform.PROFILES_FIELDS, "profile.oldProfile,profile.newProfile");
      config.put(AnonymousProfileTransform.AGE_GROUPS_FIELD, "1-18,18-24,24-50,50-100,default");
      config.put(AnonymousProfileTransform.AGE_GROUPS_OUPUT_FIELD, "ageGroup");
      config.put(AnonymousProfileTransform.CHANNEL_GROUPS_FIELD, "Both phone and email,only phone,only email,None");
      config.put(AnonymousProfileTransform.TOPIC_NAME_FIELD, "some-test-index2");
      config.put(AnonymousProfileTransform.ES_URL_FIELD, "http://localhost:9200");
      config.put(AnonymousProfileTransform.TRANSFORM_FUNCTIONS, "processRegistrationCenter");
      config.put(AnonymousProfileTransform.TRANSFORM_FUNCTIONS_PROFILE, "processBiometricList,processAgeGroup,processChannel,processProcessName,processAssister,processLocationList");

      xformValue.configure(config);

        Map<String, Object> original = new HashMap<>();
        
        // original.put("profile", "{\"oldProfile\" : {\"preferredLanguage\" : \"eng\",\"biometricInfo\" : null,\"gender\" : \"Male\",\"documents\" : [\"DOC001\",\"DOC001\",\"passport\"],\"channel\" : [\"PHONE\",\"EMAIL\"],\"verified\" : null,\"location\" : [\"QRHS\",\"14022\"],\"exceptions\" : null,\"yearOfBirth\" : \"1995\"}, \"newProfile\" : {\"preferredLanguage\" : \"hin\",\"biometricInfo\" : null,\"gender\" : \"Male\",\"documents\" : [\"DOC001\",\"DOC001\",\"passport\"],\"channel\" : [\"PHONE\",\"EMAIL\"],\"verified\" : [ ],\"location\" : [\"QRHS\",\"14022\"],\"exceptions\" : null,\"yearOfBirth\" : \"1995\"}");
        
        original.put("profile", "{\"date\":\"2021-09-22\", \"oldProfile\": {\"assisted\":[\"10060\",\"10060\"],\"biometricInfo\":[{\"type\":\"FINGER\",\"subType\":\"Left LittleFinger\",\"qualityScore\":81,\"attempts\":\"1\",\"digitalId\":\"{\\\"serialNo\\\":\\\"1234567890\\\",\\\"make\\\":\\\"MOSIP\\\",\\\"model\\\":\\\"SLAP01\\\",\\\"type\\\":\\\"Finger\\\",\\\"deviceSubType\\\":\\\"Slap\\\",\\\"deviceProviderId\\\":\\\"MOSIP.PROXY.SBI\\\",\\\"deviceProvider\\\":\\\"MOSIP\\\",\\\"dateTime\\\":\\\"2021-10-21T17:16:13Z\\\"}\"},{\"type\":\"FINGER\",\"subType\":\"Right Thumb\",\"qualityScore\":82,\"attempts\":\"1\",\"digitalId\":\"Fake Data\"}],\"location\":[\"Sidi Allal Tazi\",\"14050\"],\"yearOfBirth\": 1960,\"channel\":[\"phone\"],\"processName\":\"Update\"}, \"newProfile\": {\"biometricInfo\":[{\"type\":\"FINGER\",\"subType\":\"Left LittleFinger\",\"qualityScore\":81,\"attempts\":\"1\",\"digitalId\":\"{\\\"serialNo\\\":\\\"1234567890\\\",\\\"make\\\":\\\"MOSIP\\\",\\\"model\\\":\\\"SLAP01\\\",\\\"type\\\":\\\"Finger\\\",\\\"deviceSubType\\\":\\\"Slap\\\",\\\"deviceProviderId\\\":\\\"MOSIP.PROXY.SBI\\\",\\\"deviceProvider\\\":\\\"MOSIP\\\",\\\"dateTime\\\":\\\"2021-10-21T17:16:13Z\\\"}\"}],\"location\":[\"Sidi Allal Tazi\",\"14050\"],\"yearOfBirth\": 1960,\"channel\":[\"email\"],\"processName\":\"NEW\"}}}");
        original.put("test12", 4567);
        original.put("test123", "other123");
        original.put("test1234", true);

        SourceRecord transformed = xformValue.apply(parentTransform.apply(createRecordSchemaless(original, "some-test-index2")));

        // System.out.println(transformed.value());
        assertEquals("{test1234=true, test12=4567, test123=other123, profile={date=2021-09-22, oldProfile={assisted=[10060, 10060], biometricInfo={FINGER={qualityScore=81.5, type=FINGER, digitalId={deviceSubType=Slap, model=SLAP01, type=Finger, make=MOSIP, serialNo=1234567890, deviceProviderId=MOSIP.PROXY.SBI, deviceProvider=MOSIP}, attempts=1.0}}, processName=Update, channel=only phone, location={hierarchy2=14050, hierarchy1=Sidi Allal Tazi}, ageGroup=50-100, registrationOfficers={Operator=10060, Supervisor=10060}, yearOfBirth=1960}, newProfile={biometricInfo={FINGER={qualityScore=81.0, type=FINGER, digitalId={deviceSubType=Slap, model=SLAP01, type=Finger, make=MOSIP, serialNo=1234567890, deviceProviderId=MOSIP.PROXY.SBI, deviceProvider=MOSIP}, attempts=1.0}}, processName=New, channel=only email, location={hierarchy2=14050, hierarchy1=Sidi Allal Tazi}, ageGroup=50-100, yearOfBirth=1960}}}",transformed.value().toString());
        

    }

    @Test
    public void testSchemalessWithJWTDigitalIdUpdate() {
      Map<String, String> config = new HashMap<>();
      config.put(StringToJson.INPUT_FIELD_CONFIG, "profile");
      parentTransform.configure(config);
      config = new HashMap<>();
      config.put(AnonymousProfileTransform.PROFILES_FIELDS, "profile,profile.oldProfile,profile.newProfile");
      config.put(AnonymousProfileTransform.AGE_GROUPS_FIELD, "1-18,18-24,24-50,50-100,default");
      config.put(AnonymousProfileTransform.AGE_GROUPS_OUPUT_FIELD, "ageGroup");
      config.put(AnonymousProfileTransform.CHANNEL_GROUPS_FIELD, "Both phone and email,only phone,only email,None");
      config.put(AnonymousProfileTransform.TOPIC_NAME_FIELD, "some-test-index3");
      config.put(AnonymousProfileTransform.ES_URL_FIELD, "http://localhost:9200");
      config.put(AnonymousProfileTransform.TRANSFORM_FUNCTIONS, "processRegistrationCenter");
    //   config.put(AnonymousProfileTransform.TRANSFORM_FUNCTIONS_PROFILE, "processBiometricList,processAgeGroup,processChannel,processProcessName,processAssister,processLocationList,processUpdateProfile");
      config.put(AnonymousProfileTransform.TRANSFORM_FUNCTIONS_PROFILE, "processUpdateProfile,processBiometricList");

      xformValue.configure(config);

        Map<String, Object> original = new HashMap<>();
        
        // original.put("profile", "{\"oldProfile\" : {\"preferredLanguage\" : \"eng\",\"biometricInfo\" : null,\"gender\" : \"Male\",\"documents\" : [\"DOC001\",\"DOC001\",\"passport\"],\"channel\" : [\"PHONE\",\"EMAIL\"],\"verified\" : null,\"location\" : [\"QRHS\",\"14022\"],\"exceptions\" : null,\"yearOfBirth\" : \"1995\"}, \"newProfile\" : {\"preferredLanguage\" : \"hin\",\"biometricInfo\" : null,\"gender\" : \"Male\",\"documents\" : [\"DOC001\",\"DOC001\",\"passport\"],\"channel\" : [\"PHONE\",\"EMAIL\"],\"verified\" : [ ],\"location\" : [\"QRHS\",\"14022\"],\"exceptions\" : null,\"yearOfBirth\" : \"1995\"}");
        
        original.put("profile", "{\"oldProfile\": null, \"newProfile\": {\"biometricInfo\":[{\"type\":\"FINGER\",\"subType\":\"Left LittleFinger\",\"qualityScore\":81,\"attempts\":\"1\",\"digitalId\":\"{\\\"serialNo\\\":\\\"1234567890\\\",\\\"make\\\":\\\"MOSIP\\\",\\\"model\\\":\\\"SLAP01\\\",\\\"type\\\":\\\"Finger\\\",\\\"deviceSubType\\\":\\\"Slap\\\",\\\"deviceProviderId\\\":\\\"MOSIP.PROXY.SBI\\\",\\\"deviceProvider\\\":\\\"MOSIP\\\",\\\"dateTime\\\":\\\"2021-10-21T17:16:13Z\\\"}\"}],\"location\":[\"Sidi Allal Tazi\",\"14050\"],\"date\":\"2021-09-22T11:20:30.796464Z\",\"yearOfBirth\": 1960,\"channel\":[\"email\"],\"processName\":\"NEW\"}}}");
        original.put("test12", 4567);
        original.put("test123", "other123");
        original.put("test1234", true);

        SourceRecord transformed = xformValue.apply(parentTransform.apply(createRecordSchemaless(original, "some-test-index3")));
        
        CloseableHttpClient hClient= HttpClients.createDefault();
        HttpPut hPut = new HttpPut( "http://localhost:9200/some-test-index3/_doc/1");
        
        
        hPut.setHeader("Content-type", "application/json");
        hPut.setEntity(new StringEntity(new JSONObject((HashMap<String,Object>)transformed.value()).toString()));
       
        try(CloseableHttpResponse hResponse = hClient.execute(hPut)){

        }
        catch(Exception e){
            System.out.println("In Exception: Test unable to make the call "+e);
        }

        System.out.println("##### Check entry in "+"some-test-index3");
        try{Thread.sleep(10*1000);}
        catch(Exception e){}

        Map<String, Object> updateOriginal = new HashMap<>();
        updateOriginal.put("profile", "{\"newProfile\": {\"assisted\":[\"10060\",\"10060\"],\"biometricInfo\":[{\"type\":\"FINGER\",\"subType\":\"Left LittleFinger\",\"qualityScore\":81,\"attempts\":\"1\",\"digitalId\":\"{\\\"serialNo\\\":\\\"1234567890\\\",\\\"make\\\":\\\"MOSIP\\\",\\\"model\\\":\\\"SLAP01\\\",\\\"type\\\":\\\"Finger\\\",\\\"deviceSubType\\\":\\\"Slap\\\",\\\"deviceProviderId\\\":\\\"MOSIP.PROXY.SBI\\\",\\\"deviceProvider\\\":\\\"MOSIP\\\",\\\"dateTime\\\":\\\"2021-10-21T17:16:13Z\\\"}\"},{\"type\":\"FINGER\",\"subType\":\"Right Thumb\",\"qualityScore\":82,\"attempts\":\"1\",\"digitalId\":\"Fake Data\"}],\"location\":[\"Sidi Allal Tazi\",\"14050\"],\"date\":\"2021-09-22T11:20:30.796464Z\",\"yearOfBirth\": 1960,\"channel\":[\"phone\"],\"processName\":\"Update\"}, \"oldProfile\": {\"biometricInfo\":[{\"type\":\"FINGER\",\"subType\":\"Left LittleFinger\",\"qualityScore\":81,\"attempts\":\"1\",\"digitalId\":\"{\\\"serialNo\\\":\\\"1234567890\\\",\\\"make\\\":\\\"MOSIP\\\",\\\"model\\\":\\\"SLAP01\\\",\\\"type\\\":\\\"Finger\\\",\\\"deviceSubType\\\":\\\"Slap\\\",\\\"deviceProviderId\\\":\\\"MOSIP.PROXY.SBI\\\",\\\"deviceProvider\\\":\\\"MOSIP\\\",\\\"dateTime\\\":\\\"2021-10-21T17:16:13Z\\\"}\"}],\"location\":[\"Sidi Allal Tazi\",\"14050\"],\"date\":\"2021-09-22T11:20:30.796464Z\",\"yearOfBirth\": 1960,\"channel\":[\"email\"],\"processName\":\"NEW\"}}}");

        SourceRecord updateTransformed = xformValue.apply(parentTransform.apply(createRecordSchemaless(updateOriginal, "some-test-index3")));

        // System.out.println(updateTransformed.value());
        assertEquals("{profile={oldProfile={date=2021-09-22T11:20:30.796464Z, biometricInfo={FINGER={qualityScore=81.0, type=FINGER, digitalId={deviceSubType=Slap, model=SLAP01, type=Finger, make=MOSIP, serialNo=1234567890, deviceProviderId=MOSIP.PROXY.SBI, deviceProvider=MOSIP}, attempts=1.0}}, processName=NEW, channel=[email], location=[Sidi Allal Tazi, 14050], yearOfBirth=1960}, updateId=eyJkYXRlIjoiMjAyMS0wOS0yMlQxMToyMDozMC43OTY0NjRaIiwiYXNzaXN0ZWQiOlsiMTAwNjAiLCIxMDA2MCJdLCJiaW9tZXRyaWNJbmZvIjpbeyJxdWFsaXR5U2NvcmUiOjgxLCJzdWJUeXBlIjoiTGVmdCBMaXR0bGVGaW5nZXIiLCJ0eXBlIjoiRklOR0VSIiwiZGlnaXRhbElkIjoie1wic2VyaWFsTm9cIjpcIjEyMzQ1Njc4OTBcIixcIm1ha2VcIjpcIk1PU0lQXCIsXCJtb2RlbFwiOlwiU0xBUDAxXCIsXCJ0eXBlXCI6XCJGaW5nZXJcIixcImRldmljZVN1YlR5cGVcIjpcIlNsYXBcIixcImRldmljZVByb3ZpZGVySWRcIjpcIk1PU0lQLlBST1hZLlNCSVwiLFwiZGV2aWNlUHJvdmlkZXJcIjpcIk1PU0lQXCIsXCJkYXRlVGltZVwiOlwiMjAyMS0xMC0yMVQxNzoxNjoxM1pcIn0iLCJhdHRlbXB0cyI6IjEifSx7InF1YWxpdHlTY29yZSI6ODIsInN1YlR5cGUiOiJSaWdodCBUaHVtYiIsInR5cGUiOiJGSU5HRVIiLCJkaWdpdGFsSWQiOiJGYWtlIERhdGEiLCJhdHRlbXB0cyI6IjEifV0sInByb2Nlc3NOYW1lIjoiVXBkYXRlIiwiY2hhbm5lbCI6WyJwaG9uZSJdLCJsb2NhdGlvbiI6WyJTaWRpIEFsbGFsIFRhemkiLCIxNDA1MCJdLCJ5ZWFyT2ZCaXJ0aCI6MTk2MH0=, newProfile={date=2021-09-22T11:20:30.796464Z, assisted=[10060, 10060], biometricInfo={FINGER={qualityScore=81.5, type=FINGER, digitalId={deviceSubType=Slap, model=SLAP01, type=Finger, make=MOSIP, serialNo=1234567890, deviceProviderId=MOSIP.PROXY.SBI, deviceProvider=MOSIP}, attempts=1.0}}, processName=Update, channel=[phone], location=[Sidi Allal Tazi, 14050], yearOfBirth=1960}}}",updateTransformed.value().toString());
    }

    private void testSchemalessNullValueConversion() {
        Map<String, String> config = new HashMap<>();
        xformValue.configure(config);
        SourceRecord transformed = xformValue.apply(createRecordSchemaless(null,null));

        assertNull(transformed.valueSchema());
        assertNull(transformed.value());
    }

    private SourceRecord createRecordWithSchema(Schema schema, Object value, String topic) {
        return new SourceRecord(null, null, topic, 0, schema, value);
    }

    private SourceRecord createRecordSchemaless(Object value, String topic) {
        return createRecordWithSchema(null, value, topic);
    }
}
