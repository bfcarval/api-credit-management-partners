package api.com.credit.management.partners.application.port.output

import api.com.credit.management.partners.infra.db.mongo.document.TransactionDocument
import java.time.LocalDateTime

interface TransactionRepositoryPort {
    fun save(transaction: TransactionDocument): TransactionDocument
    fun findByPartnerId(partnerId: String): List<TransactionDocument>
    fun findByStatusAndCreatedAtBefore(status: String, threshold: LocalDateTime): List<TransactionDocument>
}
