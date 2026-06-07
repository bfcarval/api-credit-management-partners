package api.com.credit.management.partners.infra.entrypoint.rest.interceptor

import api.com.credit.management.partners.application.port.input.IdempotencyRepositoryPort
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

@Component
class IdempotencyInterceptor(
    private val idempotencyRepositoryPort: IdempotencyRepositoryPort
) : HandlerInterceptor {

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        if (request.method != "POST" && request.method != "PUT") return true

        val idempotencyKey = request.getHeader("x-idempotency-key")

        if (idempotencyKey.isNullOrBlank()) {
            response.sendError(HttpStatus.BAD_REQUEST.value(), "O header x-idempotency-key e obrigatorio.")
            return false
        }

        val isRegistered = idempotencyRepositoryPort.tryRegisterKey(idempotencyKey, request.requestURI)

        if (!isRegistered) {
            response.sendError(HttpStatus.CONFLICT.value(), "Operacao ja processada.")
            return false
        }

        return true
    }
}
