package api.com.credit.management.partners.domain.mapper

import api.com.credit.management.partners.domain.model.response.BalanceResponse
import api.com.credit.management.partners.domain.model.vo.MoneyVO
import java.time.LocalDateTime

fun MoneyVO.toResponse(partnerId: String) = BalanceResponse(
    partnerId = partnerId,
    balance = this.amount,
    updatedAt = updatedAt?: LocalDateTime.now()
)