package io.study.kafkastreams.gosgjung.producer_consumer.sample1

import io.study.kafkastreams.gosgjung.config.kafka.KafkaEnvironements
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class Sample1Consumer2 {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    @KafkaListener(
        id = "sample1",
        topics = [KafkaEnvironements.TOPIC_NAME_SAMPLE1],
        clientIdPrefix = "sample1ClientId",
        containerFactory = KafkaEnvironements.MESSAGE_LISTENER_NAME_SAMPLE1,
    )
    fun listen1(received: String){
        logger.info("received = $received")
    }


}