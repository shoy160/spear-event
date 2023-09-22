const axios = require('axios')
const { HTTP, CloudEvent } = require('cloudevents')

// client_id 和 client_secret 由事件中台提供
// 以下信息由事件中台提供
// const eventHost = 'https://event.hydra.authing-inc.co'
// const eventTopic = 'permissions.namespace-deleted'
// const clientId = '12634726187798528'
// const clientSecret = '4b354b7e18ac4b8e90d8a18031acc2f2'
const eventHost = 'http://localhost:8080'
const eventTopic = 'test.delayed-shoy'
const clientId = '5687494188339200'
const clientSecret = 'd45c1caf4c6846c5917cbbf1440c66e8'
const createUuid = () => {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
    var r = (Math.random() * 16) | 0,
      v = c == 'x' ? r : (r & 0x3) | 0x8
    return v.toString(16)
  })
}

const sendCloudEvent = (
  data,
  extensions = {},
  delayMinutes = null,
  delayAt = null
) => {
  const event = Object.assign(
    {
      subject: eventTopic,
      type: 'javascript',
      source: 'event-test-nodejs',
      data,
    },
    extensions || {}
  )
  if (delayMinutes) {
    console.log(delayMinutes)
    data.delayMinutes = delayMinutes
    event.delayminutes = delayMinutes
    // event.delayhours = xxx
    // event.delaydays = xx
  } else if (delayAt) {
    console.log(delayAt)
    data.delayAt = new Date(delayAt)
    event.delayat = delayAt
  }

  const ce = new CloudEvent(event)
  const message = HTTP.binary(ce) // Or HTTP.structured(ce)

  const token = Buffer.from(`${clientId}:${clientSecret}`, 'utf8').toString(
    'base64'
  )

  message.headers['Authorization'] = `Basic ${token}`
  axios
    .put(`${eventHost}/app/event/pub`, message.body, {
      headers: message.headers,
    })
    .then((resp) => {
      if (200 === resp.status) {
        console.log(resp.data)
      } else {
        console.log('response state:', resp.status)
      }
    })
}
// sendCloudEvent({
//   id: createUuid(),
//   name: 'user_name',
//   mobile: '13777777777',
// })

const delayAt = new Date().getTime() + 30 * 1000

for (let i = 0; i < 50; i++) {
  setTimeout(() => {
    sendCloudEvent(
      {
        id: createUuid(),
        time: new Date(),
      },
      {},
      null,
      delayAt
    )
  }, Math.ceil(Math.random() * 3 * 1000))
}
