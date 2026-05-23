package cz.rentflow.banking

import cz.rentflow.notification.OverduePaymentNotificationService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class BankSyncScheduler(
    private val paymentMatchingService: PaymentMatchingService,
    private val overdueNotificationService: OverduePaymentNotificationService
) {
    private val log = LoggerFactory.getLogger(javaClass)

    /** Run every day at 07:00 — download new bank transactions and match payments. */
    @Scheduled(cron = "0 0 7 * * *")
    fun syncBankTransactions() {
        log.info("Starting daily bank sync...")
        val result = paymentMatchingService.matchNewTransactions()
        log.info("Bank sync complete: matched={}, unmatched={}", result.matched, result.unmatched)
    }

    /** Run every day at 08:00 — check for overdue payments (3+ days) and send notifications. */
    @Scheduled(cron = "0 0 8 * * *")
    fun checkOverduePayments() {
        log.info("Checking overdue payments...")
        val notifications = overdueNotificationService.checkAndNotifyOverduePayments()
        log.info("Overdue check complete: {} overdue payments found", notifications.size)
    }
}
