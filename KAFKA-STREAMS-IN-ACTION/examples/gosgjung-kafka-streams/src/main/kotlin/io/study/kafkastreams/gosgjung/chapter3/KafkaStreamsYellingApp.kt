package io.study.kafkastreams.gosgjung.chapter3

import io.study.kafkastreams.gosgjung.external.producer.MockDataProducer
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.kstream.Printed
import org.apache.kafka.streams.kstream.Produced
import java.util.*

class KafkaStreamsYellingApp{
}

fun main(args: Array<String>) {
    MockDataProducer.produceRandomTextData()

    val properties = Properties()
    properties.put(StreamsConfig.APPLICATION_ID_CONFIG, "고고고")
    properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9091")

    val streamsConfig = StreamsConfig(properties)

    val stringSerd = Serdes.String()
    val builder: StreamsBuilder = StreamsBuilder()
    val simpleFirstStream : KStream<String, String> = builder.stream("src-topic", Consumed.with(stringSerd, stringSerd))

    val upperCaseStream : KStream<String, String> = simpleFirstStream.mapValues(String::uppercase)

    upperCaseStream.to("out-topic", Produced.with(stringSerd, stringSerd))
    upperCaseStream.print(Printed.toSysOut<String,String>().withLabel("고고고고고고"))

    val topology = builder.build()
    val kafkaStreams: KafkaStreams = KafkaStreams(topology, streamsConfig)
    kafkaStreams.start()
    Thread.sleep(5000)
    kafkaStreams.close()
    MockDataProducer.shutdown()
}
