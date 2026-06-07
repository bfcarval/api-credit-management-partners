package api.com.credit.management.partners.infra.db.mongo.document

import api.com.credit.management.partners.domain.model.enums.TransactionStatusType
import api.com.credit.management.partners.domain.model.enums.TransactionType
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.math.BigDecimal
import java.time.LocalDateTime

@Document(collection = "partner_transactions")
data class TransactionDocument(
    @Id val id: String? = null,
    @Indexed val partnerId: String,
    val type: TransactionType,
    val amount: BigDecimal,
    val description: String,
    @Indexed val status: TransactionStatusType,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val completedAt: LocalDateTime? = null
)
