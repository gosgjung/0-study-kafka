package io.study.kafkastreams.gosgjung.external.producer

import org.apache.kafka.clients.producer.Callback
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.Executors

class MockDataProducer {

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(javaClass)
        private lateinit var producer: KafkaProducer<String, String>
        private val properties: Properties = Properties()
        private val executorService = Executors.newFixedThreadPool(1)

        val callback = Callback{ metadata, exception ->
            if(exception != null){
                exception.printStackTrace()
            }
        }

        init {
            properties.put("bootstrap.servers", "localhost:9091")
            properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
            properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
            properties.put("acks", "1")
            properties.put("retries", "3")

            producer = KafkaProducer<String, String>(properties)

            logger.info("init complete")
        }

        fun shutdown(){
            producer.close()
            executorService.shutdownNow()
        }

        fun produceRandomTextData(){
            val generateTask = Runnable {
                for(i in 1..5){
                    val list : List<String> = listOf("AAA", "BBB", "CCC")

                    for(word in list){
                        val record = ProducerRecord<String, String>("src-topic", null, word)
                        producer.send(record, callback)
                    }

                    logger.info(">>> text batch sent")
                    try{
                        Thread.sleep(2000)
                    } catch (e: Exception){
                        logger.info(">>> 예외발생!!! ${e.cause}")
                        shutdown()
                        e.printStackTrace()
                        Thread.interrupted()
                    }
                }
            }

            executorService.submit(generateTask)
        }

    }

}