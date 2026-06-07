package api.com.credit.management.partners.domain.model

import api.com.credit.management.partners.domain.exception.InsufficientBalanceException
import api.com.credit.management.partners.domain.utils.MoneyUtils.Companion.add
import api.com.credit.management.partners.domain.utils.MoneyUtils.Companion.isLessThan
import api.com.credit.management.partners.domain.utils.MoneyUtils.Companion.subtract
import java.math.BigDecimal
import java.time.LocalDateTime

data class CreditAccountModel(
    val id: String?,
    val partnerId: String,
    var balance: BigDecimal,
    val version: Long? = null,
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun addCredits(amount: BigDecimal) {
        this.balance = add(balance, amount)
    }

    fun debitCredits(amount: BigDecimal) {
        if (isLessThan(this.balance, amount)) {
            throw InsufficientBalanceException("Saldo insuficiente para o parceiro $partnerId.")
        }
        this.balance = subtract(this.balance, amount)
    }
}