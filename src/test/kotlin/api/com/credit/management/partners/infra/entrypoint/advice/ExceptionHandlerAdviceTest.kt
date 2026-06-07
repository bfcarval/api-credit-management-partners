package api.com.credit.management.partners.infra.entrypoint.advice

import api.com.credit.management.partners.domain.exception.DatabaseOperationException
import api.com.credit.management.partners.domain.exception.InsufficientBalanceException
import api.com.credit.management.partners.domain.exception.MessagingOperationException
import api.com.credit.management.partners.domain.exception.PartnerAccountAlreadyExistsException
import api.com.credit.management.partners.domain.exception.PartnerAccountNotFoundException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import java.net.URI

class ExceptionHandlerAdviceTest {

    private val advice = ExceptionHandlerAdvice()

    @Test
    fun `deve tratar InsufficientBalanceException devolvendo status 422`() {
        val exception = InsufficientBalanceException("Saldo abaixo do esperado")

        val result = advice.handleBalance(exception)

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), result.status)
        assertEquals("Saldo abaixo do esperado", result.detail)
        assertEquals(URI("https://github.com"), result.type)
    }

    @Test
    fun `deve tratar PartnerAccountNotFoundException devolvendo status 404`() {
        val exception = PartnerAccountNotFoundException("Parceiro nao localizado")

        val result = advice.handlePartnerNotFound(exception)

        assertEquals(HttpStatus.NOT_FOUND.value(), result.status)
        assertEquals("Parceiro nao localizado", result.detail)
        assertEquals("Conta credito de parceiro ausente", result.title)
        assertEquals(URI("https://github.com"), result.type)
    }

    @Test
    fun `deve tratar DatabaseOperationException devolvendo status 500`() {
        val exception = DatabaseOperationException("Erro de conexao no Mongo")

        val result = advice.handleDatabaseOperationError(exception)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), result.status)
        assertEquals("Ocorreu um erro interno de persistência.", result.detail)
        assertEquals("Erro de banco de dados", result.title)
        assertNotNull(result.properties?.get("timestamp"))
    }

    @Test
    fun `deve tratar MessagingOperationException devolvendo status 500`() {
        val exception = MessagingOperationException("Erro de timeout no RabbitMQ")

        val result = advice.handleMessagingOperationError(exception)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), result.status)
        assertEquals("Ocorreu um erro interno de processamento na nossa malha de mensagens de auditoria.", result.detail)
        assertEquals("Erro de mensageria", result.title)
        assertNotNull(result.properties?.get("timestamp"))
    }

    @Test
    fun `deve tratar PartnerAccountAlreadyExistsException devolvendo status 400`() {
        val exception = PartnerAccountAlreadyExistsException("Conta ja cadastrada")

        val result = advice.handlePartnerAccountAlreadyExists(exception)

        assertEquals(HttpStatus.BAD_REQUEST.value(), result.status)
        assertEquals("Conta ja cadastrada", result.detail)
        assertEquals("Cadastro de parceiro duplicado", result.title)
        assertEquals(URI.create("https://github.com"), result.type)
        assertNotNull(result.properties?.get("timestamp"))
    }
}
