package api.com.credit.management.partners.domain.utils

import java.math.BigDecimal
import java.math.RoundingMode

class MoneyUtils {

    companion object {
        fun isLessThan(actualBalance: BigDecimal, otherBalance: BigDecimal): Boolean = actualBalance < otherBalance
        fun subtract(actualBalance: BigDecimal, otherBalance: BigDecimal): BigDecimal = actualBalance.subtract(otherBalance).setScale(2, RoundingMode.HALF_UP)
        fun add(actualBalance: BigDecimal, otherBalance: BigDecimal): BigDecimal = actualBalance.add(otherBalance).setScale(2, RoundingMode.HALF_UP)
    }
}
