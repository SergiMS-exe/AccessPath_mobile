package org.s3m4su.accesspath.ui.auth

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.s3m4su.accesspath.data.auth.AuthRepository
import org.s3m4su.accesspath.data.auth.LoginResponse
import org.s3m4su.accesspath.data.auth.setLoginResultForTest
import org.s3m4su.accesspath.data.auth.setRegisterResultForTest
import org.s3m4su.accesspath.test.FakeSettings
import org.s3m4su.accesspath.test.Fixtures
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        AuthRepository.reset(FakeSettings())
    }

    @AfterTest
    fun teardown() {
        Dispatchers.resetMain()
        AuthRepository.reset(FakeSettings())
    }

    @Test
    fun `initial state is LOGIN tab and empty fields`() = runTest {
        val vm = AuthViewModel()
        assertEquals(AuthTab.LOGIN, vm.state.value.tab)
        assertEquals("", vm.state.value.loginEmail)
        assertEquals("", vm.state.value.loginPassword)
        assertEquals(false, vm.state.value.isLoading)
        assertNull(vm.state.value.errorMessage)
    }

    @Test
    fun `switchTab changes tab`() = runTest {
        val vm = AuthViewModel()
        vm.switchTab(AuthTab.REGISTER)
        assertEquals(AuthTab.REGISTER, vm.state.value.tab)
    }

    @Test
    fun `field setters update state`() = runTest {
        val vm = AuthViewModel()
        vm.onLoginEmailChange("a@b.com")
        vm.onLoginPasswordChange("1234")
        assertEquals("a@b.com", vm.state.value.loginEmail)
        assertEquals("1234", vm.state.value.loginPassword)
    }

    @Test
    fun `submit login with success emits Authenticated event`() = runTest {
        setLoginResultForTest(Result.success(LoginResponse("t", "r", Fixtures.testUser)))
        val vm = AuthViewModel()
        vm.onLoginEmailChange("a@b.com")
        vm.onLoginPasswordChange("1234")

        vm.events.test {
            vm.submit()
            assertEquals(AuthEvent.Authenticated, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        assertEquals(false, vm.state.value.isLoading)
        assertNull(vm.state.value.errorMessage)
    }

    @Test
    fun `submit login with failure stores error and stops loading`() = runTest {
        setLoginResultForTest(Result.failure(RuntimeException("credenciales invalidas")))
        val vm = AuthViewModel()
        vm.onLoginEmailChange("a@b.com")
        vm.onLoginPasswordChange("bad")
        vm.submit()

        assertEquals(false, vm.state.value.isLoading)
        assertEquals("credenciales invalidas", vm.state.value.errorMessage)
    }

    @Test
    fun `submit register rejects mismatched passwords without hitting backend`() = runTest {
        var registerCalled = false
        AuthRepository.registerOverride = { _, _, _ ->
            registerCalled = true
            Result.success(org.s3m4su.accesspath.data.auth.LoginResponse("t", "r", org.s3m4su.accesspath.test.Fixtures.testUser))
        }
        val vm = AuthViewModel()
        vm.switchTab(AuthTab.REGISTER)
        vm.onRegUsernameChange("ana")
        vm.onRegEmailChange("a@b.com")
        vm.onRegPasswordChange("1234")
        vm.onRegPasswordConfirmChange("5678")
        vm.submit()

        assertEquals(false, vm.state.value.isLoading)
        assertEquals("Las contrasenas no coinciden", vm.state.value.errorMessage)
        assertEquals(false, registerCalled)
    }

    @Test
    fun `submit register with success emits Authenticated`() = runTest {
        setRegisterResultForTest(Result.success(LoginResponse("t", "r", Fixtures.testUser)))
        val vm = AuthViewModel()
        vm.switchTab(AuthTab.REGISTER)
        vm.onRegUsernameChange("ana")
        vm.onRegEmailChange("a@b.com")
        vm.onRegPasswordChange("1234")
        vm.onRegPasswordConfirmChange("1234")

        vm.events.test {
            vm.submit()
            assertEquals(AuthEvent.Authenticated, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `submit register with failure stores error`() = runTest {
        setRegisterResultForTest(Result.failure(RuntimeException("email duplicado")))
        val vm = AuthViewModel()
        vm.switchTab(AuthTab.REGISTER)
        vm.onRegEmailChange("a@b.com")
        vm.onRegPasswordChange("1234")
        vm.onRegPasswordConfirmChange("1234")
        vm.submit()

        assertNotNull(vm.state.value.errorMessage)
    }

    @Test
    fun `error message defaults to unknown when throwable has no message`() = runTest {
        setLoginResultForTest(Result.failure(RuntimeException()))
        val vm = AuthViewModel()
        vm.onLoginEmailChange("a@b.com")
        vm.onLoginPasswordChange("x")
        vm.submit()
        assertEquals("Error desconocido", vm.state.value.errorMessage)
    }
}