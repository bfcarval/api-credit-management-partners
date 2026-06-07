package api.com.credit.management.partners.infra.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.media.StringSchema
import io.swagger.v3.oas.models.parameters.Parameter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {

    @Bean
    fun customOpenAPI(): OpenAPI {
        val contact = Contact().apply {
            name = "Repositório do Projeto - GitHub"
            url = "https://github.com/bfcarval/api-credit-management-partners"
        }

        return OpenAPI()
            .components(
                Components()
                    .addParameters("x-idempotency-key", Parameter().apply {
                        `in` = "header"
                        name = "x-idempotency-key"
                        description = "Chave UUID única gerada pelo cliente para evitar duplicidade de transações"
                        required = false
                        schema = StringSchema().example("f47ac10b-58cc-4372-a567-0e02b2c3d479")
                    })
                    .addParameters("x-trace-id", Parameter().apply {
                        `in` = "header"
                        name = "x-trace-id"
                        description = "ID opcional de rastreamento para auditoria fina de logs (MDC)"
                        required = false
                        schema = StringSchema().example("5a4b8c9d-1234-4567-89ab-cdef12345678")
                    })
            )
            .info(
                Info().apply {
                    title = "B2B API Credit Management Partners"
                    version = "1.0.0"
                    description = "Microsserviço de alta concorrência para gestão, consumo, adição e conciliação de créditos para parceiros operacionais."
                    this.contact = contact
                }
            )
    }
}
