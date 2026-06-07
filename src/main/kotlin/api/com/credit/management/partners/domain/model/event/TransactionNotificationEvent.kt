package api.com.credit.management.partners.domain.model.event

import java.math.BigDecimal

data class TransactionNotificationEvent(
    val transactionId: String,
    val partnerId: String,
    val amount: BigDecimal,
    val type: String
)