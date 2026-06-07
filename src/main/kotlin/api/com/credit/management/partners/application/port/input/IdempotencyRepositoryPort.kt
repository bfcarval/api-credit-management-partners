package api.com.credit.management.partners.application.port.input

interface IdempotencyRepositoryPort {
    fun tryRegisterKey(key: String, endpointPath: String): Boolean
}
