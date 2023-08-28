import sys
import os
import copy
import traceback
import time
import json
from datetime import datetime
from elasticsearch import helpers
from pyspark.sql.session import SparkSession

def stringToJson(str):
    return json.loads(str)

def extractField(json, str):
    if json==None:
        return None
    try:
        return json[str]
    except KeyError:
        return None

def extractSource(json, source_prefix="dbz_source_"):
    if json==None:
        return None

    json = extractField(json,"payload")
    source_ts_ms = ""
    try:
        source_ts_ms=json["source"]["ts_ms"]
    except KeyError:
        pass
    source_table=""
    try:
        source_table=json["source"]["schema"]
        source_table+="."
        source_table+=json["source"]["table"]
    except KeyError:
        pass
    source_op=""
    try:
        source_table=json["op"]
    except KeyError:
        pass
    json = extractField(json,"after")
    json[source_prefix+"ts_ms"]=source_ts_ms
    json[source_prefix+"table"]=source_table
    json[source_prefix+"op"]=source_op
    return json

def timestampConverter(json, key, type="milli_sec",format="%Y-%m-%d'T'%H:%M:%S.%f'Z'"):
    if json==None:
        return None

    k=0
    try:
        k=json[key]
    except KeyError:
        return json

    if type == "milli_sec":
        output=datetime.fromtimestamp(k/1000.0).strftime(format)
    elif type == "micro_sec":
        output=datetime.fromtimestamp(k/1000000.0).strftime(format)
    elif type == "days_epoch":
        output=datetime.fromtimestamp(k*24*60*60).strftime(format)

    json[key]=output
    return json

def selectNewFromList(json,old_list,new):
    if json==None:
        return None

    for i in old_list:
        try:
            json[new]=json[i]
            return json
        except KeyError:
            pass

    json[new]="No valid element in list"
    return json

def processESId(json):
    if json==None:
        return None

    str=""
    for key in json:
        str += str(key)+"-"+str(json[key])
        str += "_"
    return str[0:-1]

def processBatch(batch_df, id, pre_process, post_process):
    print("====> Batch %s" % str(id))
    # print(batch_df)
    print("====> MTrial %s" % str(batch_df.count()))
    # batch_df.show()
    # obj=batch_df.collect()
    # print("====> COLLECTED ",obj)
    # print("====> COLLECTED_TYPE ",type(obj))
    # for i in l:
    #     # pre_process(i)
    #     print(i)
    post_process([pre_process(i) for i in batch_df.collect()])
    # post_process(list(map(pre_process, batch_df.collect())))

def mmain(app_name, kafka_serv, i_topic, batch_interval, pre_process, post_process):
    # topic_names= topic_names.strip().replace(' ','').split(',')

    spark = SparkSession \
    .builder \
    .appName(app_name) \
    .config("spark.some.config.option", "some-value") \
    .getOrCreate()

    df = spark.readStream \
    .format("kafka") \
    .option("kafka.bootstrap.servers", kafka_serv) \
    .option("kafka.max.request.size", 1000000000) \
    .option("groupIdPrefix", app_name) \
    .option("subscribe", i_topic) \
    .option("startingOffsets", "earliest") \
    .load()

    df.selectExpr("CAST(key AS STRING)", "CAST(value AS STRING)", "topic") \
    .writeStream \
    .outputMode('append') \
    .trigger(processingTime=batch_interval) \
    .foreachBatch(lambda b,id: processBatch(b,id,pre_process,post_process)) \
    .start()
    # .awaitTermination()
