package api.com.credit.management.partners.infra.db.mongo.adapter

import api.com.credit.management.partners.application.port.input.IdempotencyRepositoryPort
import api.com.credit.management.partners.infra.db.mongo.document.IdempotencyKeyDocument
import org.slf4j.LoggerFactory
import org.springframework.dao.DuplicateKeyException
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Component

@Component
class IdempotencyRepositoryAdapter(
    private val mongoTemplate: MongoTemplate
) : IdempotencyRepositoryPort {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun tryRegisterKey(key: String, endpointPath: String): Boolean {
        log.info("Tentando registrar chave de idempotencia para a rota: {}", endpointPath)

        return try {
            mongoTemplate.insert(IdempotencyKeyDocument(key = key, endpointPath = endpointPath))

            true.also {
                log.info("Chave de idempotencia registrada com sucesso.")
            }

        } catch (ex: Exception) {
            when (ex) {
                is DuplicateKeyException -> {
                    log.warn("Conflito de idempotencia detectado. A chave ja existe no banco de dados.")
                    false
                }
                else -> {
                    log.error("Erro inesperado no no banco de dados.", ex)
                    throw ex
                }
            }
        }
    }
}
