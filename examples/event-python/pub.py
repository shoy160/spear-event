#!/usr/bin/env python
# -*- coding: utf-8 -*-
# @Date    : 2022-11-18 10:57:39
# @Author  : songxueyan (sxy9103@gmail.com)
# @Link    : https://sxy91.com
# @Version : $Id$

from cloudevents.conversion import to_structured,to_binary
from cloudevents.http import CloudEvent
import requests
import base64


url = 'http://localhost:8080/app/event/pub'


def send_data(client_id,client_secret,att,args):
    attributes = {**{
        "type": "user",
        "source": "https://console.authing.cn/user/deleted"
    },**att}
    print(attributes)
    data = {**{
        "userPoolId": "xxxx",
        "email": "test@example.com",
        "phone": "188xxxx8888",
        "name": "张三",
    },**args}
    event = CloudEvent(attributes, data)

    # Creates the HTTP request representation of the CloudEvent in binary content mode
    headers, body = to_binary(event)
    token = base64.b64encode(f'{client_id}:{client_secret}'.encode('utf8'))
    headers['Authorization'] = 'Basic {}'.format(str(token, 'utf8'))
    headers['Content-Type'] = 'application/json'
    print(headers)
    print(body)
    resp = requests.put(url=url, data=body, headers=headers)
    print(str(resp.content, 'utf8'))


def event_send(client_id,client_secret,subject,user_id,delayminutes=1):
    # Create a CloudEvent
    # - The CloudEvent "id" is generated if omitted. "specversion" defaults to "1.0".
    send_data(client_id,client_secret,{'subject':subject,'delayminutes':f'delayminutes'},{'user_id':user_id,})


if __name__ == "__main__":
	client_id = '7945076986744832'
	client_secret = '6c7d9f16d8184f4ea358fb69fb594c03'
	subject = 'department.updated'
	user_id = 'user-01'
	














