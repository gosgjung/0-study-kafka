# Kafka Sample Producer, Consumer 구현해보기

일단 오늘 시간이 별로 없어서 설명하는 글 보다는 그냥 코드만 일일이 쭉 적어놓을 예정이다.나중에 다시 돌아와서 설명을 정리해놓든가 해야겠다.<br>



### 참고자료

- [@KafkaListener 를 이용한 Consumer 구현](https://jessyt.tistory.com/146)
  - 감사합니다 ㅠㅠ

<br>



### build.gradle 의존성 추가

```kotlin
implementation("org.springframework.kafka:spring-kafka")
```

<br>



### application.yml

```yaml
spring:
  kafka:
    producer:
      bootstrap-servers: localhost:9091, localhost:9092, localhost:9093
    consumer:
      bootstrap-servers: localhost:9091, localhost:9092, localhost:9093

```

<br>



### docker-compose

시간되면 나중에 추가



### Consumer

#### ListenerContainerFactory 빈 객체 생성/등록

```kotlin
package io.study.kafkastreams.gosgjung.config

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory

@Configuration
class KafkaConsumerConfig {

    @Value("\${spring.kafka.consumer.bootstrap-servers}")
    private lateinit var BOOTSTRAP_SERVER: String

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    private fun kafkaConsumerProperties(): Map<String, Any> =
        mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9091",
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "latest", // 마지막 읽은 부분부터 조회
            ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to false,
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java
        )

    @Bean(name = ["messageListenerContainer"])
    fun messageListenerContainer() : ConcurrentKafkaListenerContainerFactory<String, String> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, String>()

        factory.setConcurrency(2)
        factory.consumerFactory = DefaultKafkaConsumerFactory(kafkaConsumerProperties())
        factory.containerProperties.pollTimeout = 500

        return factory
    }
}
```

