package org.s3m4su.accesspath.ui.place

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.s3m4su.accesspath.data.accessibility.ContributionResult
import org.s3m4su.accesspath.data.accessibility.Criterion
import org.s3m4su.accesspath.data.api.ContributionApi
import org.s3m4su.accesspath.data.api.SubmissionApi
import org.s3m4su.accesspath.test.Fixtures
import org.s3m4su.accesspath.test.TestResets
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ContributeViewModelTest {

    @BeforeTest fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        TestResets.all()
    }
    @AfterTest fun teardown() {
        Dispatchers.resetMain()
        TestResets.all()
    }

    @Test
    fun `init loads next question`() = runTest {
        val q = Fixtures.nextQuestion()
        ContributionApi.nextQuestionOverride = { Result.success(q) }
        val vm = ContributeViewModel(placeId = 1L)
        assertEquals(false, vm.state.value.loading)
        assertEquals(q, vm.state.value.question)
    }

    @Test
    fun `init failure stores generic error message`() = runTest {
        ContributionApi.nextQuestionOverride = { Result.failure(RuntimeException("offline")) }
        val vm = ContributeViewModel(placeId = 1L)
        assertEquals(false, vm.state.value.loading)
        assertNotNull(vm.state.value.error)
    }

    @Test
    fun `answer sends contribution and stores result`() = runTest {
        val criterion = Fixtures.criterion()
        val question = Fixtures.nextQuestion(criterion = criterion)
        val result = ContributionResult(
            contributionId = 42L,
            criterion = Fixtures.criterionScore(),
            dimension = Fixtures.dimension()
        )
        // Primera llamada (init): pregunta con criterio. Segunda llamada
        // (tras guardar): sin preguntas restantes.
        var calls = 0
        ContributionApi.nextQuestionOverride = {
            calls++
            Result.success(
                if (calls == 1) question
                else Fixtures.nextQuestion(criterion = null)
            )
        }
        ContributionApi.saveOverride = { Result.success(result) }

        val vm = ContributeViewModel(placeId = 1L)
        vm.answer(Fixtures.answerOption())

        assertEquals(1, vm.state.value.answeredCount)
        assertEquals(result, vm.state.value.lastResult)
        assertNull(vm.state.value.question?.criterion)
    }

    @Test
    fun `answer without a current question is a no-op`() = runTest {
        ContributionApi.nextQuestionOverride = { Result.success(Fixtures.nextQuestion(criterion = null)) }
        ContributionApi.saveOverride = { error("should not be called") }
        val vm = ContributeViewModel(placeId = 1L)
        vm.answer(Fixtures.answerOption())
        assertEquals(0, vm.state.value.answeredCount)
    }

    @Test
    fun `answer failure stores error and does not increment count`() = runTest {
        ContributionApi.nextQuestionOverride = { Result.success(Fixtures.nextQuestion()) }
        ContributionApi.saveOverride = { Result.failure(RuntimeException("no se pudo")) }

        val vm = ContributeViewModel(placeId = 1L)
        vm.answer(Fixtures.answerOption())

        assertEquals(false, vm.state.value.saving)
        assertNotNull(vm.state.value.error)
        assertEquals(0, vm.state.value.answeredCount)
    }

    @Test
    fun `undo deletes last contribution and reloads question`() = runTest {
        val criterion = Fixtures.criterion()
        val question = Fixtures.nextQuestion(criterion = criterion)
        val result = ContributionResult(
            contributionId = 42L,
            criterion = Fixtures.criterionScore(),
            dimension = Fixtures.dimension()
        )
        ContributionApi.nextQuestionOverride = { Result.success(question) }
        ContributionApi.saveOverride = { Result.success(result) }
        var deleteCalled = false
        ContributionApi.deleteOverride = {
            deleteCalled = true
            Result.success(result.copy(contributionId = null))
        }

        val vm = ContributeViewModel(placeId = 1L)
        vm.answer(Fixtures.answerOption())
        vm.undo()

        assertEquals(true, deleteCalled)
        assertEquals(0, vm.state.value.answeredCount)
        assertNull(vm.state.value.lastResult)
    }

    @Test
    fun `undo without lastResult is a no-op`() = runTest {
        ContributionApi.nextQuestionOverride = { Result.success(Fixtures.nextQuestion(criterion = null)) }
        ContributionApi.deleteOverride = { error("should not be called") }
        val vm = ContributeViewModel(placeId = 1L)
        vm.undo()
    }

    @Test
    fun `saveComment trims whitespace and rejects empty`() = runTest {
        SubmissionApi.saveOverride = { error("should not be called for blank") }
        val vm = ContributeViewModel(placeId = 1L)
        vm.saveComment("   ")
        assertEquals(false, vm.state.value.commentSaved)
    }

    @Test
    fun `saveComment success sets commentSaved`() = runTest {
        SubmissionApi.saveOverride = { Result.success(Unit) }
        val vm = ContributeViewModel(placeId = 1L)
        vm.saveComment("  muy buena rampa  ")
        assertEquals(true, vm.state.value.commentSaved)
    }

    @Test
    fun `saveComment failure stores error`() = runTest {
        SubmissionApi.saveOverride = { Result.failure(RuntimeException("fail")) }
        val vm = ContributeViewModel(placeId = 1L)
        vm.saveComment("hola")
        assertEquals(false, vm.state.value.commentSaved)
        assertNotNull(vm.state.value.error)
    }

    @Test
    fun `loadNextQuestion clears error before reloading`() = runTest {
        ContributionApi.nextQuestionOverride = { Result.failure(RuntimeException("offline")) }
        val vm = ContributeViewModel(placeId = 1L)
        assertNotNull(vm.state.value.error)

        ContributionApi.nextQuestionOverride = { Result.success(Fixtures.nextQuestion()) }
        vm.loadNextQuestion()
        assertNull(vm.state.value.error)
    }

    @Test
    fun `criterion-only-nextQuestion represents end of questionnaire`() = runTest {
        ContributionApi.nextQuestionOverride = {
            Result.success(Fixtures.nextQuestion(criterion = null))
        }
        val vm = ContributeViewModel(placeId = 1L)
        assertNull(vm.state.value.question?.criterion)
    }
}