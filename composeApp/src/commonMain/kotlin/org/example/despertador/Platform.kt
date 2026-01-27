package org.example.despertador

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform