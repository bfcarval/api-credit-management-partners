package api.com.credit.management.partners.domain.model.vo

import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDateTime

data class MoneyVO(
    val amount: BigDecimal = BigDecimal.ZERO,
    val updatedAt: LocalDateTime? = null,
) : Serializable
