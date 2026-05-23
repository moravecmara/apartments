package cz.rentflow

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class RentflowApplication

fun main(args: Array<String>) {
	runApplication<RentflowApplication>(*args)
}
