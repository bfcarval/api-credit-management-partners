package api.com.credit.management.partners.infra.scheduler

import api.com.credit.management.partners.application.usecase.ReconcilePendingTransactionsUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class TransactionReconciliationJobTest {

    private val reconcileUseCase = mockk<ReconcilePendingTransactionsUseCase>(relaxed = true)
    private val job = TransactionReconciliationJob(reconcileUseCase)

    @Test
    fun `deve executar a rotina de conciliacao com sucesso`() {
        every { reconcileUseCase.execute() } returns Unit

        assertDoesNotThrow {
            job.runReconciliation()
        }

        verify(exactly = 1) { reconcileUseCase.execute() }
    }

    @Test
    fun `deve lancar excecao quando o caso de uso de conciliacao falhar`() {
        every { reconcileUseCase.execute() } throws RuntimeException("Erro de conciliacao")

        assertThrows<RuntimeException> {
            job.runReconciliation()
        }

        verify(exactly = 1) { reconcileUseCase.execute() }
    }
}
