import sys
import os
import time
from pyspark.sql.session import SparkSession
from elasticsearch import Elasticsearch
from elasticsearch import helpers
import base

logs_dir='/my_spark_logs'
file_name = '.'.join(os.path.basename(sys.argv[0]).split('.')[0:-1])
kafka_serv, topic_prefix, es_server_url, batch_interval_time, es_index_prefix = sys.argv[1:]
# log_file = open(os.path.join(logs_dir,file_name+'.log'),'a+')
topic_names = [topic_prefix+".prereg.reg_available_slot",topic_prefix+".audit.app_audit_log"]
# topic_names = [topic_prefix+".regprc.anonymous_profile"]
es_server = Elasticsearch(es_server_url)

def put_to_es(row_list):
    actions1 = [
        {
            "_index": es_index_prefix+j['topic'],
            "_id": j['key'],
            "_op_type": "delete",
        }
        for j in row_list if j['value']==None
    ]
    helpers.bulk(es_server, actions1)
    actions2 = [
        {
            "_index": es_index_prefix+j['topic'],
            "_id": j['key'],
            "_source": j['value']
        }
        for j in row_list if j['value']!=None
    ]
    helpers.bulk(es_server, actions2)

def reg_available_slot_process_row(row):
    print("====> Row")
    json = {"key": str(row[0]), "value": str(row[1]), "topic": str(row[2])}
    #
    # json["key"] = base.stringToJson(json["key"])
    # json["key"] = base.extractField(json["key"],"payload")
    # json["key"] = base.processESId(json["key"])
    #
    # json["value"] = base.stringToJson(json["value"])
    # json["value"] = base.extractSource(json["value"],source_prefix="dbz_source_")
    # json["value"] = base.timestampConverter(json["value"],"availability_date",type="days_epoch",format="%Y-%m-%d")
    # json["value"] = base.timestampConverter(json["value"],"slot_from_time",type="micro_sec",format="%H:%M:%S")
    # json["value"] = base.timestampConverter(json["value"],"slot_to_time",type="micro_sec",format="%H:%M:%S")
    # json["value"] = base.timestampConverter(json["value"],"dbz_source_"+"ts_ms")
    # json["value"] = base.timestampConverter(json["value"],"cr_dtimes",type="micro_sec")
    # json["value"] = base.timestampConverter(json["value"],"upd_dtimes",type="micro_sec")
    # json["value"] = base.timestampConverter(json["value"],"del_dtimes",type="micro_sec")
    # json["value"] = base.selectNewFromList(json["value"],"upd_dtimes,cr_dtimes","@timestamp_gen")

    return json
    # return row

def audit_process_row(row):
    print("====> Row")
    json = {"key": str(row[0]), "value": str(row[1]), "topic": str(row[2])}

    json["key"] = base.stringToJson(json["key"])
    json["key"] = base.extractField(json["key"],"payload")
    json["key"] = base.processESId(json["key"])

    json["value"] = base.stringToJson(json["value"])
    json["value"] = base.extractSource(json["value"],source_prefix="dbz_source_")
    json["value"] = base.timestampConverter(json["value"],"dbz_source_"+"ts_ms")
    json["value"] = base.timestampConverter(json["value"],"action_dtimes",type="micro_sec")
    json["value"] = base.timestampConverter(json["value"],"log_dtimes",type="micro_sec")
    json["value"] = base.selectNewFromList(json["value"],"log_dtimes,action_dtimes","@timestamp_gen")

    return json

if __name__ == "__main__":
    print(" ")
    print("====> Successfully Started job with kafka server: " + kafka_serv + " and topics: ", topic_names)

    # for i in topic_names:
    base.mmain(file_name, kafka_serv, topic_names[0], batch_interval_time, reg_available_slot_process_row, put_to_es)
    # base.mmain(file_name, kafka_serv, topic_names[1], batch_interval_time, audit_process_row, lambda x: base.put_to_es(es_server,es_index_prefix,x))

    while True: time.sleep(1)

    print("====> Successfully Finished job")
    print(" ")
