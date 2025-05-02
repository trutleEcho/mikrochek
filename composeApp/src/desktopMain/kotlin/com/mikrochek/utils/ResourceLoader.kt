package com.mikrochek.utils

import java.io.InputStream
import kotlin.jvm.java

object ResourceLoader {
    fun loadIcon(path: String): InputStream {
        return ResourceLoader::class.java.classLoader.getResourceAsStream(path)
            ?: error("Resource not found: $path")
    }
}