package io.study.kafkastreams.gosgjung.producer_consumer.sample1

import io.study.kafkastreams.gosgjung.config.kafka.KafkaEnvironements
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class Sample1Producer (
    @Qualifier("sample1Template")
    private val sample1Template : KafkaTemplate<String, String>
){

    @Scheduled(initialDelay = 3000, fixedRate = 500)
    fun scheduleMsg(){
        val localDateTime = LocalDateTime.now()
        sendMessage("(프로듀서가 보냄) current Time = ${localDateTime})")
    }

    fun sendMessage(msg: String){
        sample1Template.send(KafkaEnvironements.TOPIC_NAME_SAMPLE1, msg)
    }

}