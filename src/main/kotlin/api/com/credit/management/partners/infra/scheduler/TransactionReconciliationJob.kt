package api.com.credit.management.partners.infra.scheduler

import api.com.credit.management.partners.application.usecase.ReconcilePendingTransactionsUseCase
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class TransactionReconciliationJob(
    private val reconcileUseCase: ReconcilePendingTransactionsUseCase
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(cron = "0 */5 * * * *")
    fun runReconciliation() {
        log.info("Despertador acionado! Iniciando rotina automática de conciliação, time {}...", LocalDateTime.now())

        try {
            reconcileUseCase.execute().also {
                log.info("Rotina automática de conciliação finalizada com sucesso.")
            }
        } catch (ex: Exception) {
            log.error("Falha crítica na execução do Job automático de conciliação.", ex)
            throw ex
        }
    }
}
