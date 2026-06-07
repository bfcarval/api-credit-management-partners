package api.com.credit.management.partners.application.port.input

import api.com.credit.management.partners.domain.model.request.PartnerRequest
import api.com.credit.management.partners.domain.model.response.PartnerAccountResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

@Tag(name = "Gestão de Conta de Parceiros", description = "Endpoints administrativos para cadastro e exclusão de parceiros B2B")
@RequestMapping("/api/v1/partner-accounts")
interface PartnerAccountApi {

    @PostMapping
    @Operation(summary = "Cadastrar uma nova conta para parceiro B2B", description = "Inicializa a carteira digital de um parceiro com saldo zerado.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "201", description = "Parceiro cadastrado com sucesso."),
        ApiResponse(responseCode = "400", description = "Parceiro já possui uma conta ativa.", content = [Content(schema = Schema(implementation = ProblemDetail::class))])
    ])
    fun createAccountPartner(
        @Valid @RequestBody partnerRequest: PartnerRequest
    ): ResponseEntity<PartnerAccountResponse>

    @DeleteMapping("/{partnerId}")
    @Operation(summary = "Apagar uma conta de parceiro", description = "Remove a conta do banco de dados e força a limpeza imediata do cache no Redis.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "204", description = "Parceiro removido com sucesso."),
        ApiResponse(responseCode = "404", description = "Parceiro não encontrado.", content = [Content(schema = Schema(implementation = ProblemDetail::class))])
    ])
    fun deleteAccountPartner(
        @Parameter(description = "ID único do parceiro B2B a ser removido", example = "parceiro-1234")
        @PathVariable partnerId: String
    ): ResponseEntity<Void>
}
