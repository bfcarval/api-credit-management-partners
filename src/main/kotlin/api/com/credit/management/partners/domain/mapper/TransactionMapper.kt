package api.com.credit.management.partners.domain.mapper

import api.com.credit.management.partners.domain.model.response.TransactionResponse
import api.com.credit.management.partners.domain.model.vo.TransactionVO
import api.com.credit.management.partners.infra.db.mongo.document.TransactionDocument
import java.time.LocalDateTime

fun TransactionVO.toResponse() = TransactionResponse(
    id = id.toString(),
    partnerId = partnerId,
    transactionType = type.name,
    amount = amount,
    description = description,
    transactionStatusType = status.name,
    createdAt = createdAt
)

fun TransactionDocument.toVO() = TransactionVO(
    id = id,
    partnerId = partnerId,
    type = type,
    amount = amount,
    description = description,
    status = status,
    createdAt = LocalDateTime.now(),
    completedAt = completedAt
)

fun TransactionDocument.toResponse() = TransactionResponse(
    id = id ?: "",
    partnerId = partnerId,
    transactionType = type.name,
    amount = amount,
    description = description,
    transactionStatusType = status.name,
    createdAt = createdAt
)