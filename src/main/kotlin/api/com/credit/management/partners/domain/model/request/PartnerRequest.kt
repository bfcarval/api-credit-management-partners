package api.com.credit.management.partners.domain.model.request

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

data class PartnerRequest(
    @field:NotBlank
    @field:JsonProperty("partner_id")
    @Schema(description = "ID único de identificação do parceiro B2B", example = "parceiro-1234")
    val partnerId: String
)