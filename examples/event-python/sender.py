# coding=utf-8

from cloudevents.http import CloudEvent
from cloudevents.conversion import to_binary
from nanoid import generate
import requests
import base64


def event_send():

    # Create a CloudEvent
    # - The CloudEvent "id" is generated if omitted. "specversion" defaults to "1.0".
    attributes = {
        "id": generate(),
        "type": "python",
        "subject": "user.created",
        "source": "https://console.authing.cn/user/created"
    }
    name = "skkfsl"
    data = {
        "id": generate(),
        "name": name,
        "mobile": "13866666666"
    }
    event = CloudEvent(attributes, data)

    # Creates the HTTP request representation of the CloudEvent in binary content mode
    headers, body = to_binary(event)
    token = base64.b64encode(
        '4168053424263168:c9db25d9b9f548968d06d7a7f56cd3ea'.encode('utf8'))
    headers['Authorization'] = 'Basic {}'.format(str(token, 'utf8'))
    headers['Content-Type'] = 'application/json'
    print(headers)
    print(body)
    url = 'http://localhost:8080/app/event/pub'
    resp = requests.put(url=url, data=body, headers=headers)
    print(str(resp.content, 'utf8'))


if __name__ == "__main__":
    event_send()
