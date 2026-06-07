package api.com.credit.management.partners.infra.entrypoint.rest

import api.com.credit.management.partners.application.usecase.CreditAccountUseCase
import api.com.credit.management.partners.application.usecase.QueryBalanceUseCase
import api.com.credit.management.partners.application.usecase.TransactionUseCase
import api.com.credit.management.partners.domain.model.request.CreditDebitRequest
import api.com.credit.management.partners.domain.model.response.BalanceResponse
import api.com.credit.management.partners.domain.model.vo.MoneyVO
import api.com.credit.management.partners.domain.model.vo.TransactionVO
import api.com.credit.management.partners.infra.db.mongo.document.TransactionDocument
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import java.math.BigDecimal
import java.time.LocalDateTime

class CreditControllerTest {

    private val queryBalanceUseCase: QueryBalanceUseCase = mockk()
    private val creditAccountUseCase: CreditAccountUseCase = mockk()
    private val transactionUseCase: TransactionUseCase = mockk()

    private val creditController = CreditController(
        queryBalanceUseCase,
        creditAccountUseCase,
        transactionUseCase
    )

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `should return balance with success`() {
        val partnerId = "partner-123"
        val mockBalanceResponse = mockk<BalanceResponse>()
        every { mockBalanceResponse.balance } returns BigDecimal("100.00")
        every { queryBalanceUseCase.getBalance(partnerId) } returns MoneyVO(amount = mockBalanceResponse.balance, updatedAt = LocalDateTime.now())

        val response = creditController.getBalance(partnerId)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        verify(exactly = 1) { queryBalanceUseCase.getBalance(partnerId) }
    }

    @Test
    fun `should add credits with success`() {
        val key = "api-key"
        val request = CreditDebitRequest(partnerId = "partner-123", amount = BigDecimal("50.00"), description = "Bônus")
        val mockTransaction = mockk<TransactionVO>()

        every { mockTransaction.id } returns "tx-123"
        every { mockTransaction.partnerId } returns request.partnerId
        every { mockTransaction.type } returns mockk { every { name } returns "CREDIT" }
        every { mockTransaction.amount } returns request.amount
        every { mockTransaction.description } returns request.description
        every { mockTransaction.status } returns mockk { every { name } returns "SUCCESS" }
        every { mockTransaction.createdAt } returns LocalDateTime.now()

        every {
            creditAccountUseCase.addCredits(request.partnerId, request.amount, request.description)
        } returns mockTransaction

        val response = creditController.addCredits(key, request)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertEquals("tx-123", response.body?.id)
        verify(exactly = 1) { creditAccountUseCase.addCredits(request.partnerId, request.amount, request.description) }
    }

    @Test
    fun `should debit credits with success`() {
        val key = "api-key"
        val request = CreditDebitRequest(partnerId = "partner-123", amount = BigDecimal("30.00"), description = "Compra")
        val mockTransaction = mockk<TransactionVO>()

        every { mockTransaction.id } returns "tx-456"
        every { mockTransaction.partnerId } returns request.partnerId
        every { mockTransaction.type } returns mockk { every { name } returns "DEBIT" }
        every { mockTransaction.amount } returns request.amount
        every { mockTransaction.description } returns request.description
        every { mockTransaction.status } returns mockk { every { name } returns "SUCCESS" }
        every { mockTransaction.createdAt } returns LocalDateTime.now()

        every {
            creditAccountUseCase.debitCredits(request.partnerId, request.amount, request.description)
        } returns mockTransaction

        val response = creditController.debitCredits(key, request)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertEquals("tx-456", response.body?.id)
        verify(exactly = 1) { creditAccountUseCase.debitCredits(request.partnerId, request.amount, request.description) }
    }

    @Test
    fun `should return transaction history with success`() {
        val partnerId = "partner-123"
        val mockDoc = mockk<TransactionDocument>()

        every { mockDoc.id } returns "tx-789"
        every { mockDoc.partnerId } returns partnerId
        every { mockDoc.type } returns mockk { every { name } returns "CREDIT" }
        every { mockDoc.amount } returns BigDecimal("10.00")
        every { mockDoc.description } returns "Teste"
        every { mockDoc.status } returns mockk { every { name } returns "SUCCESS" }
        every { mockDoc.createdAt } returns LocalDateTime.now()

        every { transactionUseCase.getTransactionHistory(partnerId) } returns listOf(mockDoc)

        val response = creditController.getTransactionHistory(partnerId)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertEquals(1, response.body?.size)
        assertEquals("tx-789", response.body?.first()?.id)
        verify(exactly = 1) { transactionUseCase.getTransactionHistory(partnerId) }
    }
}
