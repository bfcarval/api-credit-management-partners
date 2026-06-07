package api.com.credit.management.partners.application.port.input

import api.com.credit.management.partners.domain.model.request.CreditDebitRequest
import api.com.credit.management.partners.domain.model.response.BalanceResponse
import api.com.credit.management.partners.domain.model.response.TransactionResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping

@Tag(name = "Gestão de Créditos", description = "Endpoints operacionais para controle, consumo, adição e extrato de parceiros B2B")
@RequestMapping("/api/v1/credits")
interface CreditApi {

    @GetMapping("/{partnerId}/balance")
    @Operation(summary = "Consulta o saldo atual de créditos de um parceiro", description = "Retorna o saldo disponível de forma rápida usando cache.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Saldo localizado com sucesso."),
        ApiResponse(responseCode = "404", description = "Parceiro não encontrado.", content = [Content(schema = Schema(implementation = ProblemDetail::class))])
    ])
    fun getBalance(
        @Parameter(description = "ID do parceiro", example = "parceiro-123")
        @PathVariable partnerId: String
    ): ResponseEntity<BalanceResponse>

    @PostMapping("/add")
    @Operation(summary = "Adicionar créditos para um parceiro", description = "Insere novos créditos na conta do parceiro de forma idempotente.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Crédito adicionado com sucesso."),
        ApiResponse(responseCode = "400", description = "Requisição inválida ou falta de cabeçalhos.")
    ])
    fun addCredits(
        @RequestHeader("x-idempotency-key") key: String,
        @Valid @RequestBody request: CreditDebitRequest
    ): ResponseEntity<TransactionResponse>

    @PostMapping("/debit")
    @Operation(summary = "Debitar créditos de um parceiro", description = "Debita um valor específico do saldo garantindo concorrência segura.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Crédito debitado com sucesso."),
        ApiResponse(responseCode = "422", description = "Saldo insuficiente para a operação.", content = [Content(schema = Schema(implementation = ProblemDetail::class))])
    ])
    fun debitCredits(
        @RequestHeader("x-idempotency-key") key: String,
        @Valid @RequestBody request: CreditDebitRequest
    ): ResponseEntity<TransactionResponse>

    @GetMapping("/{partnerId}/transactions")
    @Operation(summary = "Histórico de transações por parceiro", description = "Retorna a listagem de todos os créditos e débitos realizados pelo parceiro.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Histórico recuperado com sucesso.",
            content = [Content(mediaType = "application/json", array = ArraySchema(schema = Schema(implementation = TransactionResponse::class)))])
    ])
    fun getTransactionHistory(
        @Parameter(description = "ID do parceiro", example = "parceiro-123")
        @PathVariable partnerId: String
    ): ResponseEntity<List<TransactionResponse>>
}
