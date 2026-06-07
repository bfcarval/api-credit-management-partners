package api.com.credit.management.partners.application.port.output

import api.com.credit.management.partners.domain.model.CreditAccountModel
import java.util.Optional

interface CreditAccountRepositoryPort {
    fun findByPartnerId(partnerId: String): CreditAccountModel
    fun findNewPartnerId(partnerId: String): Optional<CreditAccountModel>
    fun save(creditAccountModel: CreditAccountModel): CreditAccountModel
    fun deleteById(partnerId: String)
}
