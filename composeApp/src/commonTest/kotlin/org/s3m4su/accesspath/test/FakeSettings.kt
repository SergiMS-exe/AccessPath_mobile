package org.s3m4su.accesspath.test

import com.russhwolf.settings.Settings

class FakeSettings : Settings {
    private val store = mutableMapOf<String, String>()

    override val keys: Set<String> get() = store.keys
    override val size: Int get() = store.size
    override fun clear() { store.clear() }
    override fun remove(key: String) { store.remove(key) }
    override fun hasKey(key: String): Boolean = store.containsKey(key)
    override fun putInt(key: String, value: Int): Unit = error("not used")
    override fun getInt(key: String, defaultValue: Int): Int = error("not used")
    override fun getIntOrNull(key: String): Int? = error("not used")
    override fun putLong(key: String, value: Long): Unit = error("not used")
    override fun getLong(key: String, defaultValue: Long): Long = error("not used")
    override fun getLongOrNull(key: String): Long? = error("not used")
    override fun putString(key: String, value: String) { store[key] = value }
    override fun getString(key: String, defaultValue: String): String = store[key] ?: defaultValue
    override fun getStringOrNull(key: String): String? = store[key]
    override fun putFloat(key: String, value: Float): Unit = error("not used")
    override fun getFloat(key: String, defaultValue: Float): Float = error("not used")
    override fun getFloatOrNull(key: String): Float? = error("not used")
    override fun putDouble(key: String, value: Double): Unit = error("not used")
    override fun getDouble(key: String, defaultValue: Double): Double = error("not used")
    override fun getDoubleOrNull(key: String): Double? = error("not used")
    override fun putBoolean(key: String, value: Boolean): Unit = error("not used")
    override fun getBoolean(key: String, defaultValue: Boolean): Boolean = error("not used")
    override fun getBooleanOrNull(key: String): Boolean? = error("not used")
}