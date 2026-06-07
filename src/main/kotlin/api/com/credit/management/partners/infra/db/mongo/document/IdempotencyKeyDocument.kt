package api.com.credit.management.partners.infra.db.mongo.document

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "idempotency_keys")
data class IdempotencyKeyDocument(
    @Id val key: String,
    val endpointPath: String,
    @Indexed(expireAfterSeconds = 86400) val createdAt: LocalDateTime = LocalDateTime.now()
)
