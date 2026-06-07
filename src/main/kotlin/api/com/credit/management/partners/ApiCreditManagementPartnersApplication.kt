package api.com.credit.management.partners

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.resilience.annotation.EnableResilientMethods
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableResilientMethods
@EnableScheduling
@EnableCaching
class CreditManagementPartnersApplication

fun main(args: Array<String>) {
	runApplication<CreditManagementPartnersApplication>(*args)
}
