package api.com.credit.management.partners.infra.db.mongo.adapter

import api.com.credit.management.partners.infra.db.mongo.document.IdempotencyKeyDocument
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.dao.DuplicateKeyException
import org.springframework.data.mongodb.core.MongoTemplate

@ExtendWith(MockKExtension::class)
class IdempotencyRepositoryAdapterTest {

    private val mongoTemplate = mockk<MongoTemplate>(relaxed = true)
    private val adapter = IdempotencyRepositoryAdapter(mongoTemplate)

    private val key = "f47ac10b-58cc-4372-a567-0e02b2c3d479"
    private val endpointPath = "/api/v1/credits/debit"

    @Test
    fun `deve registrar chave de idempotencia com sucesso`() {
        val document = IdempotencyKeyDocument(key = key, endpointPath = endpointPath)

        every { mongoTemplate.insert(any<IdempotencyKeyDocument>()) } returns document

        val result = adapter.tryRegisterKey(key, endpointPath)

        assertTrue(result)
        verify(exactly = 1) { mongoTemplate.insert(any<IdempotencyKeyDocument>()) }
    }

    @Test
    fun `deve retornar falso quando detectar uma chave duplicada no banco de dados`() {
        every { mongoTemplate.insert(any<IdempotencyKeyDocument>()) } throws DuplicateKeyException("Chave ja existente")

        val result = adapter.tryRegisterKey(key, endpointPath)

        assertFalse(result)
        verify(exactly = 1) { mongoTemplate.insert(any<IdempotencyKeyDocument>()) }
    }

    @Test
    fun `deve repassar a excecao original quando ocorrer um erro inesperado nao mapeado`() {
        every { mongoTemplate.insert(any<IdempotencyKeyDocument>()) } throws RuntimeException("Erro catastrophico")

        assertThrows<RuntimeException> {
            adapter.tryRegisterKey(key, endpointPath)
        }
    }
}
