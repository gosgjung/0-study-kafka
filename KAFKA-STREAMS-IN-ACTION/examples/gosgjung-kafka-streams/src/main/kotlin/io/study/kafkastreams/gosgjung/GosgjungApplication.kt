package io.study.kafkastreams.gosgjung

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@SpringBootApplication
class GosgjungApplication

fun main(args: Array<String>) {
	runApplication<GosgjungApplication>(*args)
}
