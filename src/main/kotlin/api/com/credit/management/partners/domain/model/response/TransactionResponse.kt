package api.com.credit.management.partners.domain.model.response

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal
import java.time.LocalDateTime

data class TransactionResponse(
    @field:JsonProperty("id") val id: String,
    @field:JsonProperty("partner_id") val partnerId: String,
    @field:JsonProperty("type") val transactionType: String,
    @field:JsonProperty("amount") val amount: BigDecimal,
    @field:JsonProperty("description") val description: String,
    @field:JsonProperty("status") val transactionStatusType: String,
    @field:JsonProperty("created_at") val createdAt: LocalDateTime
)
