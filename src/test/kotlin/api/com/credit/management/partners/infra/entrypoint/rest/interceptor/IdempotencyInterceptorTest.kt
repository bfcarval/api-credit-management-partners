package api.com.credit.management.partners.infra.entrypoint.rest.interceptor

import api.com.credit.management.partners.application.port.input.IdempotencyRepositoryPort
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.http.HttpStatus

@ExtendWith(MockKExtension::class)
class IdempotencyInterceptorTest {

    private val idempotencyRepositoryPort = mockk<IdempotencyRepositoryPort>(relaxed = true)
    private val mongoTemplate: MongoTemplate = mockk()
    private val interceptor = IdempotencyInterceptor(idempotencyRepositoryPort)

    private val request = mockk<HttpServletRequest>(relaxed = true)
    private val response = mockk<HttpServletResponse>(relaxed = true)
    private val handler = Any()

    private val key = "f47ac10b-58cc-4372-a567-0e02b2c3d479"
    private val requestUri = "/api/v1/credits/debit"

    @Test
    fun `deve permitir a execucao e retornar verdadeiro quando o metodo HTTP for GET`() {
        every { request.method } returns "GET"

        val result = interceptor.preHandle(request, response, handler)

        assertTrue(result)
        verify(exactly = 0) { request.getHeader(any()) }
    }

    @Test
    fun `deve retornar falso e responder HTTP 400 se o cabecalho x-idempotency-key nao for enviado`() {
        every { request.method } returns "POST"
        every { request.getHeader("x-idempotency-key") } returns null

        val result = interceptor.preHandle(request, response, handler)

        assertFalse(result)
        verify(exactly = 1) { response.sendError(HttpStatus.BAD_REQUEST.value(), "O header x-idempotency-key e obrigatorio.") }
    }

    @Test
    fun `deve retornar falso e responder HTTP 409 se a chave enviada ja tiver sido processada`() {
        every { request.method } returns "POST"
        every { request.getHeader("x-idempotency-key") } returns key
        every { request.requestURI } returns requestUri
        every { idempotencyRepositoryPort.tryRegisterKey(key, requestUri) } returns false

        val result = interceptor.preHandle(request, response, handler)

        assertFalse(result)
        verify(exactly = 1) { response.sendError(HttpStatus.CONFLICT.value(), "Operacao ja processada.") }
    }

    @Test
    fun `deve registrar a chave com sucesso retornar verdadeiro e liberar a execucao`() {
        every { request.method } returns "POST"
        every { request.getHeader("x-idempotency-key") } returns key
        every { request.requestURI } returns requestUri
        every { idempotencyRepositoryPort.tryRegisterKey(key, requestUri) } returns true

        val result = interceptor.preHandle(request, response, handler)

        assertTrue(result)
        verify(exactly = 0) { response.sendError(any(), any()) }
    }
}
