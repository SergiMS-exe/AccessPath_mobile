package org.s3m4su.accesspath.data.auth

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.s3m4su.accesspath.test.FakeSettings
import org.s3m4su.accesspath.test.Fixtures
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AuthRepositoryTest {

    @BeforeTest fun setup() = AuthRepository.reset(FakeSettings())
    @AfterTest fun teardown() = AuthRepository.reset(FakeSettings())

    @Test
    fun `start state is Unauthenticated when Settings is empty`() = runTest {
        assertEquals(AuthState.Unauthenticated, AuthRepository.state.value)
        assertNull(AuthRepository.token)
        assertNull(AuthRepository.refreshToken)
    }

    @Test
    fun `start state is Authenticated when Settings has valid token and user`() = runTest {
        AuthRepository.settingsOverride = FakeSettings().apply {
            putString("auth_token", "abc")
            putString("auth_refresh_token", "xyz")
            putString("auth_user", """{"id":1,"code":"A","username":"ana","email":"a@b.com","created_at":"2026-01-01T00:00:00Z"}""")
        }
        AuthRepository.reset()
        val s = AuthRepository.state.value
        assertTrue(s is AuthState.Authenticated)
        assertEquals("abc", (s as AuthState.Authenticated).token)
        assertEquals("ana", s.user.username)
    }

    @Test
    fun `start state clears corrupted user JSON and falls back to Unauthenticated`() = runTest {
        AuthRepository.settingsOverride = FakeSettings().apply {
            putString("auth_token", "abc")
            putString("auth_user", "{ esto no es JSON valido }")
        }
        AuthRepository.reset()
        assertEquals(AuthState.Unauthenticated, AuthRepository.state.value)
    }

    @Test
    fun `login success persists tokens and emits Authenticated`() = runTest {
        AuthRepository.loginOverride = { _, _ ->
            Result.success(LoginResponse(token = "t", refreshToken = "r", user = Fixtures.testUser))
        }
        AuthRepository.state.test {
            assertEquals(AuthState.Unauthenticated, awaitItem())
            AuthRepository.login("a@b.com", "1234")
            val s = awaitItem()
            assertTrue(s is AuthState.Authenticated)
            assertEquals("ana", (s as AuthState.Authenticated).user.username)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `login failure does not change state`() = runTest {
        AuthRepository.loginOverride = { _, _ -> Result.failure(RuntimeException("bad")) }
        AuthRepository.login("a@b.com", "wrong")
        assertEquals(AuthState.Unauthenticated, AuthRepository.state.value)
        assertNull(AuthRepository.token)
    }

    @Test
    fun `logout clears state and removes access token from settings`() = runTest {
        AuthRepository.loginOverride = { _, _ ->
            Result.success(LoginResponse("t", "r", Fixtures.testUser))
        }
        AuthRepository.login("a@b.com", "1234")
        val settings = AuthRepository.settingsOverride as FakeSettings
        assertTrue(settings.hasKey("auth_token"))

        AuthRepository.logout()
        assertEquals(AuthState.Unauthenticated, AuthRepository.state.value)
        // El refresh token se conserva a proposito para poder renovar la sesion
        // sin pedir credenciales; ver `refreshToken is read from settings even
        // after logout`.
        assertTrue(!settings.hasKey("auth_token"))
        assertTrue(settings.hasKey("auth_refresh_token"))
        assertTrue(!settings.hasKey("auth_user"))
    }

    @Test
    fun `updateTokens keeps the same user and replaces both tokens`() = runTest {
        AuthRepository.loginOverride = { _, _ ->
            Result.success(LoginResponse("t", "r", Fixtures.testUser))
        }
        AuthRepository.login("a@b.com", "1234")
        AuthRepository.updateTokens("new-token", "new-refresh")
        assertEquals("new-token", AuthRepository.token)
        assertEquals("new-refresh", AuthRepository.refreshToken)
    }

    @Test
    fun `updateTokens is no-op when there is no authenticated user`() = runTest {
        AuthRepository.updateTokens("a", "b")
        assertEquals(AuthState.Unauthenticated, AuthRepository.state.value)
    }

    @Test
    fun `register flow persists user and emits Authenticated`() = runTest {
        AuthRepository.registerOverride = { _, _, _ ->
            Result.success(LoginResponse("t", "r", Fixtures.testUser))
        }
        AuthRepository.register("ana", "a@b.com", "1234")
        assertTrue(AuthRepository.state.value is AuthState.Authenticated)
    }

    @Test
    fun `refreshToken is read from settings even after logout`() = runTest {
        val s = FakeSettings().apply { putString("auth_refresh_token", "sticky") }
        AuthRepository.settingsOverride = s
        AuthRepository.reset()
        AuthRepository.logout()
        assertEquals("sticky", AuthRepository.refreshToken)
    }
}