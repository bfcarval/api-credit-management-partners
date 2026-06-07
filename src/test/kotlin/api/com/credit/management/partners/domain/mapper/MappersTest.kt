package api.com.credit.management.partners.domain.mapper

import api.com.credit.management.partners.domain.model.CreditAccountModel
import api.com.credit.management.partners.domain.model.enums.TransactionStatusType
import api.com.credit.management.partners.domain.model.enums.TransactionType
import api.com.credit.management.partners.domain.model.vo.MoneyVO
import api.com.credit.management.partners.domain.model.vo.TransactionVO
import api.com.credit.management.partners.infra.db.mongo.document.CreditAccountDocument
import api.com.credit.management.partners.infra.db.mongo.document.TransactionDocument
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDateTime

class MappersTest {

    private val partnerId = "partner-b2b-99"
    private val description = "Operacao de teste"
    private val now = LocalDateTime.now()

    @Test
    fun `deve mapear CreditAccountDocument para CreditAccountModel`() {
        val document = CreditAccountDocument(id = "doc-1", partnerId = partnerId, balance = BigDecimal("150.00"), version = 1L, updatedAt = now)

        val model = document.toModel()

        assertEquals(document.id, model.id)
        assertEquals(document.partnerId, model.partnerId)
        assertEquals(document.balance, model.balance)
        assertEquals(document.version, model.version)
        assertEquals(document.updatedAt, model.updatedAt)
    }

    @Test
    fun `deve mapear CreditAccountModel para CreditAccountDocument`() {
        val model = CreditAccountModel(id = "mod-1", partnerId = partnerId, balance = BigDecimal("200.00"), version = 2L, updatedAt = now)

        val document = model.toDocument()

        assertEquals(model.id, document.id)
        assertEquals(model.partnerId, document.partnerId)
        assertEquals(model.balance, document.balance)
        assertEquals(model.version, document.version)
        assertNotNull(document.updatedAt)
    }

    @Test
    fun `deve mapear CreditAccountModel para MoneyVO`() {
        val model = CreditAccountModel(id = "mod-1", partnerId = partnerId, balance = BigDecimal("350.00"), version = 1L, updatedAt = now)

        val moneyVO = model.toMoneyVO()

        assertEquals(model.balance, moneyVO.amount)
        assertNotNull(moneyVO.updatedAt)
    }

    @Test
    fun `deve mapear CreditAccountModel para PartnerAccountResponse`() {
        val model = CreditAccountModel(id = "mod-1", partnerId = partnerId, balance = BigDecimal.ZERO, version = 0L, updatedAt = now)

        val response = model.toResponse(now)

        assertEquals(model.partnerId, response.partnerId)
        assertEquals("PARTNER_ACCOUNT_REGISTERED", response.status)
        assertEquals(now, response.createdAt)
    }

    @Test
    fun `deve mapear MoneyVO para BalanceResponse`() {
        val moneyVO = MoneyVO(amount = BigDecimal("500.00"), updatedAt = now)

        val response = moneyVO.toResponse(partnerId)

        assertEquals(partnerId, response.partnerId)
        assertEquals(moneyVO.amount, response.balance)
        assertEquals(now, response.updatedAt)
    }

    @Test
    fun `deve mapear MoneyVO para BalanceResponse usando hora atual caso updatedAt seja nulo`() {
        val moneyVO = MoneyVO(amount = BigDecimal("10.00"), updatedAt = null)

        val response = moneyVO.toResponse(partnerId)

        assertEquals(partnerId, response.partnerId)
        assertEquals(moneyVO.amount, response.balance)
        assertNotNull(response.updatedAt)
    }

    @Test
    fun `deve mapear TransactionVO para TransactionResponse`() {
        val transactionVO = TransactionVO(id = "tx-123", partnerId = partnerId, type = TransactionType.CREDIT, amount = BigDecimal("50.00"), description = description, status = TransactionStatusType.COMPLETED, createdAt = now, completedAt = now)

        val response = transactionVO.toResponse()

        assertEquals(transactionVO.id.toString(), response.id)
        assertEquals(transactionVO.partnerId, response.partnerId)
        assertEquals(transactionVO.type.name, response.transactionType)
        assertEquals(transactionVO.amount, response.amount)
        assertEquals(transactionVO.description, response.description)
        assertEquals(transactionVO.status.name, response.transactionStatusType)
        assertEquals(transactionVO.createdAt, response.createdAt)
    }

    @Test
    fun `deve mapear TransactionDocument para TransactionVO`() {
        val document = TransactionDocument(id = "doc-tx", partnerId = partnerId, type = TransactionType.DEBIT, amount = BigDecimal("20.00"), description = description, status = TransactionStatusType.PENDING, createdAt = now, completedAt = null)

        val vo = document.toVO()

        assertEquals(document.id, vo.id)
        assertEquals(document.partnerId, vo.partnerId)
        assertEquals(document.type, vo.type)
        assertEquals(document.amount, vo.amount)
        assertEquals(document.description, vo.description)
        assertEquals(document.status, vo.status)
        assertNotNull(vo.createdAt)
        assertNull(vo.completedAt)
    }
}
