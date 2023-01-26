package io.study.kafkastreams.gosgjung.producer_consumer.sample1

import io.study.kafkastreams.gosgjung.config.kafka.KafkaEnvironements
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.protocol.Message
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.annotation.PartitionOffset
import org.springframework.kafka.annotation.TopicPartition
import org.springframework.kafka.listener.adapter.ConsumerRecordMetadata
import org.springframework.kafka.support.Acknowledgment
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component

//@Component
class Sample1Consumer {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    // (1)
    @KafkaListener(
        id = "sample1",
        topics = [KafkaEnvironements.TOPIC_NAME_SAMPLE1],
        clientIdPrefix = "sample1ClientId",
        containerFactory = KafkaEnvironements.MESSAGE_LISTENER_NAME_SAMPLE1,
        autoStartup = "\${listen.auto.start:true}", // 필요하면 추가하자.
        concurrency = "\${listen.concurrency:3}",   // 필요하면 추가하자.
    )
    fun listen1(received: String){
        logger.info("received = $received")
    }

    // (2)
    @KafkaListener(
        id = "sample1",
        topics = [KafkaEnvironements.TOPIC_NAME_SAMPLE1],
        clientIdPrefix = "sample1ClientId",
        containerFactory = KafkaEnvironements.MESSAGE_LISTENER_NAME_SAMPLE1,
        autoStartup = "\${listen.auto.start:true}", // 필요하면 추가하자.
        concurrency = "\${listen.concurrency:3}",   // 필요하면 추가하자.
        // 특정 파티션의 레코드만 listen 하려고 할 경우는 아래와 같이 partitions = ["0"] 처럼 지정해준다.
         topicPartitions = [TopicPartition(topic = KafkaEnvironements.TOPIC_NAME_SAMPLE1, partitions = ["0"])],
    )
    fun listen2(received: String){
        logger.info("received = $received")
    }

    // (3)
    @KafkaListener(
        id = "sample1",
        topics = [KafkaEnvironements.TOPIC_NAME_SAMPLE1],
        clientIdPrefix = "sample1ClientId",
        containerFactory = KafkaEnvironements.MESSAGE_LISTENER_NAME_SAMPLE1,
        autoStartup = "\${listen.auto.start:true}", // 필요하면 추가하자.
        concurrency = "\${listen.concurrency:3}",   // 필요하면 추가하자.
        // 어디부터 읽어들일지 명시하고 싶다면 아래와 같이 initialOffset 을 지정해주자.
        topicPartitions = [
            TopicPartition(
                topic = KafkaEnvironements.MESSAGE_LISTENER_NAME_SAMPLE1,
                partitions = ["1"],
                // initialOffset 을 통해 어디부터 읽어들이지 명시했다.
                partitionOffsets = [PartitionOffset(partition = "0", initialOffset = "5000")]
             )
         ]
    )
    fun listen3(received: String){
        logger.info("received = $received")
    }


    // (4)
    // ACK 모드를 MANUAL 로 세팅했을 경우
    // 메서드의 파라미터에 Acknowledgement 를 추가해준다.
    @KafkaListener(
        id = "sample1",
        topics = [KafkaEnvironements.TOPIC_NAME_SAMPLE1],
        clientIdPrefix = "sample1ClientId",
        containerFactory = KafkaEnvironements.MESSAGE_LISTENER_NAME_SAMPLE1,
        autoStartup = "\${listen.auto.start:true}", // 필요하면 추가하자.
        concurrency = "\${listen.concurrency:3}",   // 필요하면 추가하자.
    )
    fun listen4(received: String, ack: Acknowledgment){     // ACK 모드를 MANUAL 로 세팅했을 경우
                                                            // @KafkaListener 메서드에서는 ack: Acknowledgment 를 인자로 받게끔 해준다.
        logger.info("received = $received")
        ack.acknowledge() // 오프셋을 커밋한다.
    }

    // (5)
    /// 레코드의 메타 데이터를 가져오는 방식
    // = (1) @Payload, @Header 를 이용하는 방식
    // = (2) ConsumerRecordMetadata 객체를 이용하는 방법
    
    // 레코드의 메타 데이터를 가져오는 방식 (1) @Payload, @Header 를 이용하는 방식
    @KafkaListener(
        id = "sample1",
        topics = [KafkaEnvironements.TOPIC_NAME_SAMPLE1],
        clientIdPrefix = "sample1ClientId",
        containerFactory = KafkaEnvironements.MESSAGE_LISTENER_NAME_SAMPLE1,
        autoStartup = "\${listen.auto.start:true}", // 필요하면 추가하자.
        concurrency = "\${listen.concurrency:3}",   // 필요하면 추가하자.
    )
    fun listen5(
        @Payload received: String,
        @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY, required = false) key: Int?,
        @Header(KafkaHeaders.RECEIVED_PARTITION_ID) partition: Int,
        @Header(KafkaHeaders.RECEIVED_TOPIC) topic: String,
        @Header(KafkaHeaders.RECEIVED_TIMESTAMP) timestamp: Long
    ){
        logger.info("data = $received, key = $key, partition = $partition, topic = $topic, timestamp = $timestamp")
    }

    // 레코드의 메타 데이터를 가져오는 방식 (2) ConsumerRecordMetadata 객체를 이용하는 방식
    @KafkaListener(
        id = "sample1",
        topics = [KafkaEnvironements.TOPIC_NAME_SAMPLE1],
        clientIdPrefix = "sample1ClientId",
        containerFactory = KafkaEnvironements.MESSAGE_LISTENER_NAME_SAMPLE1,
        autoStartup = "\${listen.auto.start:true}", // 필요하면 추가하자.
        concurrency = "\${listen.concurrency:3}",   // 필요하면 추가하자.
    )
    fun listen6(
        recived: String, meta: ConsumerRecordMetadata
    ){
        logger.info("data = $recived, partition = ${meta.partition()}, topic = ${meta.topic()}, timestamp = ${meta.timestamp()}")
    }

    ///
    // Batch Listener
    // 먼저 batchFactory 를 빈으로 등록해야 한다. (컨피그 파일 참조할 것)

    // (1) batch 1
    @KafkaListener(
        id = "sample1",
        topics = [KafkaEnvironements.TOPIC_NAME_SAMPLE1],
        clientIdPrefix = "sample1ClientId",
        containerFactory = KafkaEnvironements.BATCH_LISTENER_NAME_SAMPLE1,
        autoStartup = "\${listen.auto.start:true}", // 필요하면 추가하자.
        concurrency = "\${listen.concurrency:3}",   // 필요하면 추가하자.
    )
    fun listen7(recieved: List<String>){
        println("received : $recieved")
    }

    // (2) Message 의 List (List<Message> 로 데이터를 받는 방식)
    @KafkaListener(
        id = "sample1",
        topics = [KafkaEnvironements.TOPIC_NAME_SAMPLE1],
        clientIdPrefix = "sample1ClientId",
        containerFactory = KafkaEnvironements.BATCH_LISTENER_NAME_SAMPLE1,
        autoStartup = "\${listen.auto.start:true}", // 필요하면 추가하자.
        concurrency = "\${listen.concurrency:3}",   // 필요하면 추가하자.
    )
    fun listen8(received: List<Message>){
        println("received : $received")
    }

    // (3) nullable 한 List<Message> 로 데이터 받기
    @KafkaListener(
        id = "sample1",
        topics = [KafkaEnvironements.TOPIC_NAME_SAMPLE1],
        clientIdPrefix = "sample1ClientId",
        containerFactory = KafkaEnvironements.BATCH_LISTENER_NAME_SAMPLE1,
        autoStartup = "\${listen.auto.start:true}", // 필요하면 추가하자.
        concurrency = "\${listen.concurrency:3}",   // 필요하면 추가하자.
    )
    fun listen9(received: List<Message>?){
        println("received : $received")
    }

    // (4) ack 모드 = MANUAL 로 데이터 받기.
    // batchMessageListener 컨피그에 별도의 설정을 해줘야 한다.
    // 코드에서는 Acknowledgement 객체를 메서드의 파라미터로 지정해줘야 한다.
    @KafkaListener(
        id = "sample1",
        topics = [KafkaEnvironements.TOPIC_NAME_SAMPLE1],
        clientIdPrefix = "sample1ClientId",
        containerFactory = KafkaEnvironements.BATCH_LISTENER_NAME_SAMPLE1,
        autoStartup = "\${listen.auto.start:true}", // 필요하면 추가하자.
        concurrency = "\${listen.concurrency:3}",   // 필요하면 추가하자.
    )
    fun listen10(received: List<Message>?, acknowledgement: Acknowledgment){
        println("received : $received")
        acknowledgement.acknowledge()
    }

    // (5) Consumer<K,V> 도 함께 받기
    @KafkaListener(
        id = "sample1",
        topics = [KafkaEnvironements.TOPIC_NAME_SAMPLE1],
        clientIdPrefix = "sample1ClientId",
        containerFactory = KafkaEnvironements.BATCH_LISTENER_NAME_SAMPLE1,
        autoStartup = "\${listen.auto.start:true}", // 필요하면 추가하자.
        concurrency = "\${listen.concurrency:3}",   // 필요하면 추가하자.
    )
    fun listen11(received: List<Message>?, acknowledgement: Acknowledgment, consumer: Consumer<String, String>){
        println("received : $received")
        acknowledgement.acknowledge()
    }

    // (6) ConsumerRecord의 List로 데이터 받기
    // = List<ConsumerRecord<String, String>>
    @KafkaListener(
        id = "sample1",
        topics = [KafkaEnvironements.TOPIC_NAME_SAMPLE1],
        clientIdPrefix = "sample1ClientId",
        containerFactory = KafkaEnvironements.BATCH_LISTENER_NAME_SAMPLE1,
        autoStartup = "\${listen.auto.start:true}", // 필요하면 추가하자.
        concurrency = "\${listen.concurrency:3}",   // 필요하면 추가하자.
    )
    fun listen12(received: List<ConsumerRecord<String, String>>){
        received.forEach{
            println("batch data :: ${it.value()} topic = ${it.topic()} partition = ${it.partition()}, offset = ${it.offset()}")
        }
    }

    // (7) List<Consumer<K,V>> 와 Acknowledgement 도 함께 받기
    @KafkaListener(
        id = "sample1",
        topics = [KafkaEnvironements.TOPIC_NAME_SAMPLE1],
        clientIdPrefix = "sample1ClientId",
        containerFactory = KafkaEnvironements.BATCH_LISTENER_NAME_SAMPLE1,
        autoStartup = "\${listen.auto.start:true}", // 필요하면 추가하자.
        concurrency = "\${listen.concurrency:3}",   // 필요하면 추가하자.
    )
    fun listen13(received: List<ConsumerRecord<String, String>>, acknowledgment: Acknowledgment){
        received.forEach{
            println("batch data :: ${it.value()} topic = ${it.topic()} partition = ${it.partition()}, offset = ${it.offset()}")
            acknowledgment.acknowledge()
        }
    }

    /// GroupId 지정해서 Listen 하는 방식
    // GroupId=1 에 대한 @KafkaListener 메서드 들에 컨슈머 1, 컨슈머 2가 지정되어 있고,
    // GroupId=2 에 대한 @KafkaListener 메서드 들에 컨슈머 3, 컨슈머 4가 지정되어 있다고 해보자.
    // 이때
    // GroupId = 1 에 데이터가 들어오면, 컨슈머 1, 컨슈머 2 둘 중 하나만 실행되고
    // GroupId = 2 에 데이터가 들어오면, 컨슈머 3, 컨슈머 4 둘 중 하나만 실행된다.
    @KafkaListener(id = "listener1", topics = [KafkaEnvironements.TOPIC_NAME_SAMPLE1], groupId = "group1")
    fun listener1(received: String){
        println("listener1 >>> received = $received")
    }

    @KafkaListener(id = "listener2", topics = [KafkaEnvironements.TOPIC_NAME_SAMPLE1], groupId = "group1")
    fun listener2(received: String){
        println("listener2 >>> received = $received")
    }

    @KafkaListener(id = "listener3", topics = [KafkaEnvironements.TOPIC_NAME_SAMPLE1], groupId = "group2")
    fun listener3(received: String){
        println("listener3 >>> received = $received")
    }

    @KafkaListener(id = "listener4", topics = [KafkaEnvironements.TOPIC_NAME_SAMPLE1], groupId = "group2")
    fun listener4(received: String){
        println("listener4 >>> received = $received")
    }
}

