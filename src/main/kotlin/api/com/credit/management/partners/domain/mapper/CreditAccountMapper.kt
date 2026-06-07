package api.com.credit.management.partners.domain.mapper

import api.com.credit.management.partners.domain.model.CreditAccountModel
import api.com.credit.management.partners.domain.model.response.PartnerAccountResponse
import api.com.credit.management.partners.domain.model.vo.MoneyVO
import api.com.credit.management.partners.infra.db.mongo.document.CreditAccountDocument
import java.time.LocalDateTime

fun CreditAccountDocument.toModel() = CreditAccountModel(
    id = id,
    partnerId = partnerId,
    balance = balance,
    version = version,
    updatedAt = updatedAt
)

fun CreditAccountModel.toDocument() = CreditAccountDocument(
    id = this.id,
    partnerId = this.partnerId,
    balance = this.balance,
    version = this.version,
    updatedAt = LocalDateTime.now()
)

fun CreditAccountModel.toMoneyVO() = MoneyVO(
    amount = this.balance,
    updatedAt = LocalDateTime.now()
)

fun CreditAccountModel.toResponse(time: LocalDateTime) = PartnerAccountResponse(
    partnerId = this.partnerId,
    status = "PARTNER_ACCOUNT_REGISTERED",
    createdAt = time
)