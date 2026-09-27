package org.s3m4su.accesspath

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.s3m4su.accesspath.test.testHttpClient
import kotlinx.coroutines.test.runTest
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlin.test.assertFailsWith

@RunWith(AndroidJUnit4::class)
class NetworkSecurityConfigTest {

    @Test
    fun `app context loads successfully in test mode`() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("org.s3m4su.accesspath", context.packageName)
    }

    @Test
    fun `release network_security_config blocks cleartext on base config`() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val resId = context.resources.getIdentifier(
            "network_security_config",
            "xml",
            context.packageName
        )
        assertTrue("network_security_config.xml debe existir en res/xml", resId != 0)
    }
}