package org.s3m4su.accesspath.ui.place

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.s3m4su.accesspath.data.accessibility.AnswerOption
import org.s3m4su.accesspath.data.accessibility.ContributionResult
import org.s3m4su.accesspath.data.accessibility.NextQuestion
import org.s3m4su.accesspath.data.api.ContributionApi
import org.s3m4su.accesspath.data.api.ContributionRequestDto
import org.s3m4su.accesspath.data.api.SubmissionApi
import org.s3m4su.accesspath.data.api.SubmissionRequestDto

/**
 * Estado del bucle de contribucion: una pregunta a la vez, Deshacer visible
 * tras responder y semaforo en vivo. Sin gamificacion, salida sin culpa.
 */
data class ContributeUiState(
    val loading: Boolean = true,
    val saving: Boolean = false,
    // Pregunta actual; criterion == null y !loading => nada util que preguntar.
    val question: NextQuestion? = null,
    // Ultima respuesta guardada (para Deshacer + semaforo en vivo).
    val lastResult: ContributionResult? = null,
    val answeredCount: Int = 0,
    val error: String? = null,
    // Paso final opcional: comentario.
    val commentSaved: Boolean = false
)

class ContributeViewModel(private val placeId: Long) : ViewModel() {

    private val _state = MutableStateFlow(ContributeUiState())
    val state: StateFlow<ContributeUiState> = _state

    init {
        loadNextQuestion()
    }

    /** Pide al backend la siguiente pregunta util (respeta depends_on y perfil). */
    fun loadNextQuestion() {
        _state.value = _state.value.copy(loading = true, error = null)
        viewModelScope.launch {
            ContributionApi.nextQuestion(placeId)
                .onSuccess { question ->
                    _state.value = _state.value.copy(loading = false, question = question)
                }
                .onFailure {
                    _state.value = _state.value.copy(
                        loading = false,
                        error = "No se pudo cargar la pregunta. Comprueba tu conexión."
                    )
                }
        }
    }

    /** Guarda la respuesta elegida (upsert) y avanza a la siguiente pregunta. */
    fun answer(option: AnswerOption) {
        val criterion = _state.value.question?.criterion ?: return
        _state.value = _state.value.copy(saving = true, error = null)
        viewModelScope.launch {
            ContributionApi.save(
                ContributionRequestDto(
                    placeId = placeId,
                    criterionId = criterion.id,
                    answerOptionId = option.id
                )
            )
                .onSuccess { result ->
                    _state.value = _state.value.copy(
                        saving = false,
                        lastResult = result,
                        answeredCount = _state.value.answeredCount + 1
                    )
                    loadNextQuestion()
                }
                .onFailure {
                    _state.value = _state.value.copy(
                        saving = false,
                        error = "No se pudo guardar la respuesta."
                    )
                }
        }
    }

    /** Deshacer: borra la ultima respuesta (soft delete + recalculo en backend). */
    fun undo() {
        val contributionId = _state.value.lastResult?.contributionId ?: return
        _state.value = _state.value.copy(saving = true, error = null)
        viewModelScope.launch {
            ContributionApi.delete(contributionId)
                .onSuccess {
                    _state.value = _state.value.copy(
                        saving = false,
                        lastResult = null,
                        answeredCount = (_state.value.answeredCount - 1).coerceAtLeast(0)
                    )
                    // La pregunta deshecha vuelve a estar disponible.
                    loadNextQuestion()
                }
                .onFailure {
                    _state.value = _state.value.copy(saving = false, error = "No se pudo deshacer.")
                }
        }
    }

    /** Paso final opcional: guarda el comentario en la valoracion del usuario. */
    fun saveComment(comment: String) {
        val trimmed = comment.trim()
        if (trimmed.isEmpty()) return
        _state.value = _state.value.copy(saving = true, error = null)
        viewModelScope.launch {
            SubmissionApi.save(SubmissionRequestDto(placeId = placeId, comment = trimmed))
                .onSuccess {
                    _state.value = _state.value.copy(saving = false, commentSaved = true)
                }
                .onFailure {
                    _state.value = _state.value.copy(
                        saving = false,
                        error = "No se pudo guardar el comentario."
                    )
                }
        }
    }
}
