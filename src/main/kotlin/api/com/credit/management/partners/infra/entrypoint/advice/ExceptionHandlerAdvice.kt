package api.com.credit.management.partners.infra.entrypoint.advice

import api.com.credit.management.partners.domain.exception.DatabaseOperationException
import api.com.credit.management.partners.domain.exception.InsufficientBalanceException
import api.com.credit.management.partners.domain.exception.MessagingOperationException
import api.com.credit.management.partners.domain.exception.PartnerAccountAlreadyExistsException
import api.com.credit.management.partners.domain.exception.PartnerAccountNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.net.URI
import java.time.Instant

@RestControllerAdvice
class ExceptionHandlerAdvice {

    @ExceptionHandler(InsufficientBalanceException::class)
    fun handleBalance(ex: InsufficientBalanceException): ProblemDetail {
        return ProblemDetail.forStatusAndDetail(
            HttpStatus.UNPROCESSABLE_ENTITY,
            ex.message ?: "Saldo Insuficiente"
        ).apply {
            type = URI("https://github.com")
        }
    }

    @ExceptionHandler(PartnerAccountNotFoundException::class)
    fun handlePartnerNotFound(ex: PartnerAccountNotFoundException): ProblemDetail {
        return ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND,
            ex.message ?: "Recurso nao encontrado"
        ).apply {
            title = "Conta credito de parceiro ausente"
            type = URI("https://github.com")
        }
    }

    @ExceptionHandler(DatabaseOperationException::class)
    fun handleDatabaseOperationError(ex: DatabaseOperationException): ProblemDetail {
        return ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Ocorreu um erro interno de persistência."
        ).apply {
            title = "Erro de banco de dados"
            setProperty("timestamp", Instant.now())
        }
    }

    @ExceptionHandler(MessagingOperationException::class)
    fun handleMessagingOperationError(ex: MessagingOperationException): ProblemDetail {
        return ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Ocorreu um erro interno de processamento na nossa malha de mensagens de auditoria."
        ).apply {
            title = "Erro de mensageria"
            setProperty("timestamp", Instant.now())
        }
    }

    @ExceptionHandler(PartnerAccountAlreadyExistsException::class)
    fun handlePartnerAccountAlreadyExists(ex: PartnerAccountAlreadyExistsException): ProblemDetail {
        return ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            ex.message ?: "Conflito nos dados enviados para o parceiro."
        ).apply {
            title = "Cadastro de parceiro duplicado"
            type = URI.create("https://github.com")
            setProperty("timestamp", Instant.now())
        }
    }
}
