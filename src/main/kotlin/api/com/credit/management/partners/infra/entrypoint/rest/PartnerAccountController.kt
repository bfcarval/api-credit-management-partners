package api.com.credit.management.partners.infra.entrypoint.rest

import api.com.credit.management.partners.application.port.input.PartnerAccountApi
import api.com.credit.management.partners.application.usecase.PartnerAccountUseCase
import api.com.credit.management.partners.domain.mapper.toResponse
import api.com.credit.management.partners.domain.model.request.PartnerRequest
import api.com.credit.management.partners.domain.model.response.PartnerAccountResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RestController
class PartnerAccountController(
    private val partnerAccountUseCase: PartnerAccountUseCase,
) : PartnerAccountApi {

    override fun createAccountPartner(partnerRequest: PartnerRequest): ResponseEntity<PartnerAccountResponse> {
        val accountPartner = partnerAccountUseCase.createAccountPartner(partnerRequest.partnerId)

        return ResponseEntity.status(HttpStatus.CREATED).body(
            accountPartner.toResponse(LocalDateTime.now())
        )
    }

    override fun deleteAccountPartner(partnerId: String): ResponseEntity<Void> {
        partnerAccountUseCase.deleteAccountPartner(partnerId)

        return ResponseEntity.noContent().build()
    }
}
