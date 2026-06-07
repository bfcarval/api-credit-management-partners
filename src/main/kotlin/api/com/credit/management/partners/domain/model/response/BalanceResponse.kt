package api.com.credit.management.partners.domain.model.response

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal
import java.time.LocalDateTime

data class BalanceResponse(
    @field:JsonProperty("partner_id") val partnerId: String,
    @field:JsonProperty("balance") val balance: BigDecimal,
    @field:JsonProperty("updated_at") val updatedAt: LocalDateTime = LocalDateTime.now()
)
