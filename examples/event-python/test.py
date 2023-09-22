#!/usr/bin/env python
# -*- coding: utf-8 -*-
# @Date    : 2022-11-20 23:35:50
# @Author  : songxueyan (sxy9103@gmail.com)
# @Link    : https://sxy91.com
# @Version : $Id$

from pub import event_send, send_data
import sys
import random
import json
from kafka import KafkaConsumer
import time
import requests

# host = 'https://event.hydra.authing-inc.co/manage'
host = 'http://localhost:8080/manage'
token = 'Bearer Authing-event'


default_cofig = {
    "partition": 2,
    "replications": 1,
    "retention": 30
}

headers = {'Authorization': token}


def create_event(event):
    body = {**event, **default_cofig}

    r = requests.post(f'{host}/event', headers=headers, json=body)
    is_success = r.json()["success"]
    print(f'create_event:{is_success}')
    if not is_success:
        print(body)
        print(r.text)
    return is_success


def create_client(name):
    body = {'name': name}
    r = requests.post(f'{host}/client', headers=headers, json=body)
    # print(r.text)
    res = r.json()
    print(res["data"])
    return res


def create_grant(clientId, role, event, group_id=None):
    if not group_id:
        group_id = f'{event["code"]}.gp1'
    body = {
        "patternType": "Literal",
        "topic": event['code'],
        "group": group_id,
        "source": "xxx",
        "type": role
    }
    r = requests.post(f'{host}/client/grant/{clientId}',
                      headers=headers, json=body)
    print(r.json())
    print(f'create_grant:{r.json()["success"]},{clientId},{role},{event}')
    return body['group']


def test_pub(event, wait=5):
    create_event(event)
    client = create_client(name=f'pub.{event["code"]}')['data']
    # Consumer,Producer
    create_grant(clientId=client['id'], role='Producer', event=event)
    user_id = 0
    while True:
        user_id += 1
        event_send(client['id'], client['secret'], event['code'], user_id)
        time.sleep(wait)


def tes_sub(event, wait=10):
    '''
    '''
    client = create_client(name=f'sub.{event["code"]}')['data']
    group_id = create_grant(
        clientId=client['id'], role='Consumer', event=event)  # Consumer,Producer

    kfk_config = {'bootstrap_servers': 'chengdu.y2b.site:18083', 'group_id': group_id, 'auto_offset_reset': 'earliest',
                  'client_id': client['id'],
                  'security_protocol': 'SASL_PLAINTEXT',
                  'sasl_mechanism': 'SCRAM-SHA-256',
                  'sasl_plain_username': client['id'],
                  'sasl_plain_password': client['secret']}

    consumer = KafkaConsumer(event['code'], **kfk_config)
    print('topic', consumer.subscription())
    info = {'group_id': group_id, 'sasl_plain_username':
            client['id'], 'sasl_plain_password': client['secret'], 'event': event['code']}
    print(info)
    # if consumer.bootstrap_connected():
    for msg in consumer:
        data = json.loads(msg.value)
        now = int(time.time())
        sendAt = int(data['sendAt'])
        delayminutes = int(data['delayminutes'])
        delay = now - (sendAt + delayminutes * 60)
        print(
            f'sendAt={sendAt}, now={now}, delayminutes={delayminutes}, delay={delay}s')
        if delay > 60:
            print(f'delay={delay}s, data={data}')
        if wait > 0:
            time.sleep(wait)


def test_same_gp():
    e1 = {'code': 'event.test5', 'desc': 'test5', 'name': 'test5'}
    e2 = {'code': 'event.test6', 'desc': 'test6', 'name': 'test6'}
    create_event(e1)
    create_event(e2)
    c1 = create_client(name=f'pub.{e1["code"]}')['data']
    c2 = create_client(name=f'pub.{e2["code"]}')['data']
    create_grant(clientId=c1['id'], role='Producer', event=e1,
                 group_id='sxy_testgroup')  # Consumer,Producer
    create_grant(clientId=c2['id'], role='Producer', event=e2,
                 group_id='sxy_testgroup')  # Consumer,Producer
    event_send(c1['id'], c1['secret'], e1['code'], 'test-1')
    event_send(c2['id'], c2['secret'], e2['code'], 'test-c2-e1')
    # 取消 c1 的授权
    # event_send(c2['id'],c2['secret'],e2['code'],'test-c2-e1')


# c = {"id":"7863951241121792","name":"pub.event.test3","secret":"62d0e8e8bd2945f294d244ef5e6af181","status":"Enabled"}
# event_send(c['id'],c['secret'],'event.test3','test-c2-e1')
# test_same_gp()
def test_delayed_pub(event, wait=10):
    create_event(event)
    client = create_client(name=f'pub.{event["code"]}')['data']
    # Consumer,Producer
    create_grant(clientId=client['id'], role='Producer', event=event)
    user_id = 0
    time.sleep(5)
    while True:
        user_id += 1
        # {1, 2, 5, 10, 30, 60}
        delayminutes = f'{random.randint(1,70)}'
        att = {'subject': event['code'], 'delayminutes': delayminutes}
        arg = {'user_id': user_id, 'sendAt': int(
            time.time()), 'delayminutes': f'{delayminutes}'}
        send_data(client['id'], client['secret'], att, arg)
        if wait > 0:
            time.sleep(wait)


if __name__ == '__main__':
    # 发布事件 python test.py
    # 订阅事件 python test.py sub
    event = {'code': 'test.delayed-03', 'desc': '延迟事件3', 'name': '延迟事件3'}
    if len(sys.argv) > 1:
        tes_sub(event, 0)
    else:
        test_delayed_pub(event, 0)
