const { Kafka } = require('kafkajs')
const { CloudEvent, Kafka: CloudKafka } = require('cloudevents')
// 以下信息由事件中台提供
const eventServers = 'sxy21.cn:30192'
const eventGroup = 'event-client'
const eventTopic = 'test.delayed-shoy'
const clientId = '5687494188339200'
const clientSecret = 'd45c1caf4c6846c5917cbbf1440c66e8'

// const clientId = '4168053424263168'
// const clientSecret = 'c9db25d9b9f548968d06d7a7f56cd3ea'

const kafka = new Kafka({
  clientId: 'nodejs-client',
  brokers: eventServers.split(','),
  ssl: false,
  sasl: {
    mechanism: 'scram-sha-256',
    username: clientId,
    password: clientSecret,
  },
})
/**
 * 定义消费参数
 */
const consumerOption = {
  groupId: eventGroup,
  autoCommit: true,
}
/**
 * 定义consumer，指定从分区0获取数据
 */
const consumer = kafka.consumer(consumerOption)

const run = async () => {
  await consumer.connect()
  await consumer.subscribe({
    topic: eventTopic,
    fromBeginning: true,
  })
  await consumer.run({
    eachMessage: async ({ topic, partition, message }) => {
      // CloudEvents 不支持 Buffer 转换...
      for (const key in message.headers) {
        const value = message.headers[key]
        if (value instanceof Buffer) {
          message.headers[key] = value.toString('utf8')
        }
      }
      const event = CloudKafka.toEvent(message)
      const { id, time, delayMinutes, delayAt } = event.data
      if (delayMinutes) {
        // 计算延迟
        const delay =
          message.timestamp - (+new Date(time) + delayMinutes * 60 * 1000)
        // if (delay > 5000) {
        console.log(`事件 [${id}, delay: ${delayMinutes}] 误差 ${delay} ms`)
        // }
      } else if (delayAt) {
        // 定时消息计算延迟
        const delay = message.timestamp - +new Date(delayAt)
        // if (delay > 5000) {
        console.log(`事件 [${id}, delay: ${delayAt}] 误差 ${delay} ms`)
      }
      console.log({
        time: new Date(~~message.timestamp),
        topic,
        partition,
        offset: message.offset,
        event,
      })
    },
  })
}
run().catch(console.error)
