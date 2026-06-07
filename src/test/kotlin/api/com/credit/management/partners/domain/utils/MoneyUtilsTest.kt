package api.com.credit.management.partners.domain.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class MoneyUtilsTest {

    @Test
    fun `deve retornar verdadeiro quando o valor atual for menor que o outro`() {
        val actualBalance = BigDecimal("50.00")
        val otherBalance = BigDecimal("100.00")

        val result = MoneyUtils.isLessThan(actualBalance, otherBalance)

        assertTrue(result)
    }

    @Test
    fun `deve retornar falso quando o valor atual nao for menor que o outro`() {
        val actualBalance = BigDecimal("150.00")
        val otherBalance = BigDecimal("100.00")

        val result = MoneyUtils.isLessThan(actualBalance, otherBalance)

        assertFalse(result)
    }

    @Test
    fun `deve retornar falso quando os valores forem iguais`() {
        val actualBalance = BigDecimal("100.00")
        val otherBalance = BigDecimal("100.00")

        val result = MoneyUtils.isLessThan(actualBalance, otherBalance)

        assertFalse(result)
    }

    @Test
    fun `deve subtrair os valores e aplicar duas casas decimais com arredondamento`() {
        val actualBalance = BigDecimal("100.555")
        val otherBalance = BigDecimal("50.222")

        val result = MoneyUtils.subtract(actualBalance, otherBalance)

        assertEquals(BigDecimal("50.33"), result)
    }

    @Test
    fun `deve somar os valores e aplicar duas casas decimais com arredondamento`() {
        val actualBalance = BigDecimal("100.555")
        val otherBalance = BigDecimal("50.222")

        val result = MoneyUtils.add(actualBalance, otherBalance)

        assertEquals(BigDecimal("150.78"), result)
    }
}
