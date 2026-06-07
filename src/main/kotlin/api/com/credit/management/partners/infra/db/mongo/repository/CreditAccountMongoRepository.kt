package api.com.credit.management.partners.infra.db.mongo.repository

import api.com.credit.management.partners.infra.db.mongo.document.CreditAccountDocument
import org.springframework.data.mongodb.repository.MongoRepository
import java.util.Optional

interface CreditAccountMongoRepository : MongoRepository<CreditAccountDocument, String> {
    fun findByPartnerId(partnerId: String): Optional<CreditAccountDocument>
}
