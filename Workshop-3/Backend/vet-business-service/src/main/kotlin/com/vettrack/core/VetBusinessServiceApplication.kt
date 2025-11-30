package com.vettrack.core

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class VetBusinessServiceApplication

fun main(args: Array<String>) {
    runApplication<VetBusinessServiceApplication>(*args)
}
