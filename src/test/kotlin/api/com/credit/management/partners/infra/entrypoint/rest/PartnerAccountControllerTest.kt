package api.com.credit.management.partners.infra.entrypoint.rest

import api.com.credit.management.partners.application.usecase.PartnerAccountUseCase
import api.com.credit.management.partners.domain.model.CreditAccountModel
import api.com.credit.management.partners.domain.model.request.PartnerRequest
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class PartnerAccountControllerTest {

    private val partnerAccountUseCase: PartnerAccountUseCase = mockk()

    private val partnerAccountController = PartnerAccountController(partnerAccountUseCase)

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `should create partner account with success`() {
        val partnerId = "partner-123"
        val request = PartnerRequest(partnerId = partnerId)
        val mockCreditAccountModel = mockk<CreditAccountModel>()

        every { mockCreditAccountModel.partnerId } returns partnerId

        every { partnerAccountUseCase.createAccountPartner(partnerId) } returns mockCreditAccountModel

        val response = partnerAccountController.createAccountPartner(request)

        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertNotNull(response.body)
        verify(exactly = 1) { partnerAccountUseCase.createAccountPartner(partnerId) }
    }

    @Test
    fun `should delete partner account with success`() {
        val partnerId = "partner-123"
        every { partnerAccountUseCase.deleteAccountPartner(partnerId) } returns Unit

        val response = partnerAccountController.deleteAccountPartner(partnerId)

        assertEquals(HttpStatus.NO_CONTENT, response.statusCode)
        verify(exactly = 1) { partnerAccountUseCase.deleteAccountPartner(partnerId) }
    }
}
