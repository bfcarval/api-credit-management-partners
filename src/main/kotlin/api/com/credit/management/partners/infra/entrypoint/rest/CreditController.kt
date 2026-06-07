package api.com.credit.management.partners.infra.entrypoint.rest

import api.com.credit.management.partners.application.port.input.CreditApi
import api.com.credit.management.partners.application.usecase.CreditAccountUseCase
import api.com.credit.management.partners.application.usecase.QueryBalanceUseCase
import api.com.credit.management.partners.application.usecase.TransactionUseCase
import api.com.credit.management.partners.domain.mapper.toResponse
import api.com.credit.management.partners.domain.model.request.CreditDebitRequest
import api.com.credit.management.partners.domain.model.response.BalanceResponse
import api.com.credit.management.partners.domain.model.response.TransactionResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class CreditController(
    private val queryBalanceUseCase: QueryBalanceUseCase,
    private val creditAccountUseCase: CreditAccountUseCase,
    private val transactionUseCase: TransactionUseCase
) : CreditApi {

    override fun getBalance(partnerId: String): ResponseEntity<BalanceResponse> =
        ResponseEntity.ok(
            queryBalanceUseCase.getBalance(partnerId).toResponse(partnerId)
        )

    override fun addCredits(key: String, request: CreditDebitRequest): ResponseEntity<TransactionResponse> =
        ResponseEntity.ok(
            creditAccountUseCase.addCredits(
                partnerId = request.partnerId,
                amount = request.amount,
                description = request.description
            ).toResponse()
        )

    override fun debitCredits(key: String, request: CreditDebitRequest): ResponseEntity<TransactionResponse> =
        ResponseEntity.ok(
            creditAccountUseCase.debitCredits(
                partnerId = request.partnerId,
                amount = request.amount,
                description = request.description
            ).toResponse()
        )

    override fun getTransactionHistory(partnerId: String): ResponseEntity<List<TransactionResponse>> =
        ResponseEntity.ok(
            transactionUseCase.getTransactionHistory(partnerId).map { doc ->
                TransactionResponse(
                    id = doc.id ?: "",
                    partnerId = doc.partnerId,
                    transactionType = doc.type.name,
                    amount = doc.amount,
                    description = doc.description,
                    transactionStatusType = doc.status.name,
                    createdAt = doc.createdAt
                )
            }
        )
}
