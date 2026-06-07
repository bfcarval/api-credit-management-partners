package api.com.credit.management.partners.domain.model.vo

import api.com.credit.management.partners.domain.model.enums.TransactionStatusType
import api.com.credit.management.partners.domain.model.enums.TransactionType
import java.math.BigDecimal
import java.time.LocalDateTime

data class TransactionVO(
    val id: String? = null,
    val partnerId: String,
    val type: TransactionType,
    val amount: BigDecimal,
    val description: String,
    val status: TransactionStatusType,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val completedAt: LocalDateTime? = null
)
