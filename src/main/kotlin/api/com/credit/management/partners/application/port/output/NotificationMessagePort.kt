package api.com.credit.management.partners.application.port.output

import api.com.credit.management.partners.domain.model.event.TransactionNotificationEvent

interface NotificationMessagePort {
    fun sendTransactionNotification(transactionNotificationEvent: TransactionNotificationEvent)
}
