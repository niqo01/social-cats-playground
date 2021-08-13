package com.nicolasmilliard.testcdkpipeline

fun main() {
    println("Hello World!")

    // Try adding program arguments at Run/Debug configuration
    println("Program arguments: ${System.getenv()["TABLE_NAME"]}")
}