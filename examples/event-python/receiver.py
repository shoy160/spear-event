# coding=utf-8
# @Date    : 2022-11-21 10:57:39
# @Author  : songxueyan
# doc https://kafka-python.readthedocs.io/en/master/apidoc/KafkaClient.html

from kafka import KafkaConsumer

kfk_config = {'bootstrap_servers':'chengdu.y2b.site:18083','group_id':'authing-server','auto_offset_reset':'earliest',
'security_protocol':'SASL_PLAINTEXT',
'sasl_mechanism':'SCRAM-SHA-256',
'sasl_plain_username':'4168053424263168',
'sasl_plain_password':'c9db25d9b9f548968d06d7a7f56cd3ea'}

def test_sub():
	# 不同 group_id 会收到全量数据，相同 group_id 会去负载均衡数据
	consumer = KafkaConsumer('user.created',**kfk_config)
	print('topic',consumer.subscription())
	# if consumer.bootstrap_connected():
	for msg in consumer:
		print(msg.value)



if __name__ == '__main__':
	test_sub()
