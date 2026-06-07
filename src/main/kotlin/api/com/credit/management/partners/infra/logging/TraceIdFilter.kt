package api.com.credit.management.partners.infra.logging

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class TraceIdFilter : Filter {

    companion object {
        private const val TRACE_ID_KEY = "traceId"
    }

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val httpRequest = request as HttpServletRequest
        val httpResponse = response as HttpServletResponse

        val headerValue = httpRequest.getHeader("x-trace-id") ?: httpRequest.getHeader("X-Trace-Id")
        val traceId = if (!headerValue.isNullOrBlank()) headerValue else UUID.randomUUID().toString()

        try {
            MDC.put(TRACE_ID_KEY, traceId)
            httpResponse.addHeader("x-trace-id", traceId)
            chain.doFilter(request, response)
        } finally {
            MDC.remove(TRACE_ID_KEY)
        }
    }
}
