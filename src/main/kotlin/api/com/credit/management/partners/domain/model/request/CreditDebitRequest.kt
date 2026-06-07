package api.com.credit.management.partners.domain.model.request

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import java.math.BigDecimal

data class CreditDebitRequest(
    @field:NotBlank
    @field:JsonProperty("partner_id")
    @param:Schema(
        description = "ID único do parceiro B2B",
        example = "parceiro-123",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val partnerId: String,

    @field:DecimalMin("0.01")
    @param:Schema(
        description = "Valor a ser debitado dos créditos",
        example = "150.00",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val amount: BigDecimal,

    @field:NotBlank
    @param:Schema(
        description = "Motivo ou identificador da venda que gerou o débito",
        example = "Venda de Licença API - Pedido #987",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val description: String
)
