package api.com.credit.management.partners.domain.model.response

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

data class PartnerAccountResponse(
    @field:JsonProperty("partner_id") val partnerId: String,
    @field:JsonProperty("status") val status: String,
    @field:JsonProperty("created_at") val createdAt: LocalDateTime = LocalDateTime.now()
)
