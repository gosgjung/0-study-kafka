package io.study.kafkastreams.gosgjung.config.kafka

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.listener.ContainerProperties

@Configuration
class KafkaConsumerConfig {

    @Value("\${spring.kafka.consumer.bootstrap-servers}")
    private lateinit var BOOTSTRAP_SERVERS: String

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    private fun kafkaConsumerProperties(): Map<String, Any> =
        mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to BOOTSTRAP_SERVERS,
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "latest", // 마지막 읽은 부분부터 조회
            ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to false,
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java
        )

    @Bean(name = [KafkaEnvironements.MESSAGE_LISTENER_NAME_SAMPLE1])
    fun messageListenerContainer() : ConcurrentKafkaListenerContainerFactory<String, String> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, String>()

        factory.setConcurrency(2)
        factory.consumerFactory = DefaultKafkaConsumerFactory(kafkaConsumerProperties())
        factory.containerProperties.pollTimeout = 500
        // AckMode 를 MANUAL 로 설정하려 할 경우 아래와 같이 세팅
        // AckMode 를 MANUAL 로 설정하면 Offset Commit 을 클라이언트 로직에서 수동으로 하도록 세팅한다.
        // 즉, AckMode 를 MANUAL 로 세팅하면 listener 에서 데이터를 받고 처리를 완료한 후 로직에서 Commit 해야 Commit 된다.
        factory.containerProperties.ackMode = ContainerProperties.AckMode.MANUAL

        return factory
    }

    @Bean(name = [KafkaEnvironements.BATCH_LISTENER_NAME_SAMPLE1])
    fun batchListenerFactory() : ConcurrentKafkaListenerContainerFactory<String, String>{
        val factory = ConcurrentKafkaListenerContainerFactory<String, String>()
        factory.setConcurrency(3)
        factory.consumerFactory = DefaultKafkaConsumerFactory(kafkaConsumerProperties())
        factory.containerProperties.pollTimeout = 500
        factory.containerProperties.ackMode = ContainerProperties.AckMode.MANUAL
        factory.isBatchListener = true
        return factory
    }
}