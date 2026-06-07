package api.com.credit.management.partners.infra.db.mongo.document

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.math.BigDecimal
import java.time.LocalDateTime

@Document(collection = "credit_accounts")
data class CreditAccountDocument(
    @Id val id: String? = null,
    @Indexed(unique = true) val partnerId: String,
    val balance: BigDecimal,
    @Version val version: Long? = null,
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
