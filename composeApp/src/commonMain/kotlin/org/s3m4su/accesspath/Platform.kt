package org.s3m4su.accesspath

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform