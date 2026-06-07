package api.com.credit.management.partners.infra.db.mongo.repository

import api.com.credit.management.partners.infra.db.mongo.document.TransactionDocument
import org.springframework.data.mongodb.repository.MongoRepository
import java.time.LocalDateTime

interface TransactionMongoRepository : MongoRepository<TransactionDocument, String> {
    fun findByStatusAndCreatedAtBefore(status: String, createdAt: LocalDateTime): List<TransactionDocument>
    fun findByPartnerId(partnerId: String): List<TransactionDocument>
}