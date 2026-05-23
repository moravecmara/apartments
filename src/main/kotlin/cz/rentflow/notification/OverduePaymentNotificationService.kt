package cz.rentflow.notification

import cz.rentflow.domain.entity.Payment
import cz.rentflow.domain.entity.PaymentStatus
import cz.rentflow.domain.repository.LandlordRepository
import cz.rentflow.domain.repository.PaymentRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDate

data class OverduePaymentNotification(
    val paymentId: Long,
    val variableSymbol: String,
    val contractId: Long,
    val daysOverdue: Int,
    val amount: java.math.BigDecimal,
    val landlordEmail: String
)

@Service
class OverduePaymentNotificationService(
    private val paymentRepository: PaymentRepository,
    private val landlordRepository: LandlordRepository
) {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Find all payments overdue by 3+ days and return notifications.
     * In production this would send emails via Spring Mail.
     */
    fun checkAndNotifyOverduePayments(): List<OverduePaymentNotification> {
        val today = LocalDate.now()
        val overdueThreshold = today.minusDays(3)

        val overduePayments = paymentRepository.findAll().filter { payment ->
            payment.status == PaymentStatus.UNPAID &&
            payment.dueDate.isBefore(overdueThreshold)
        }

        val notifications = overduePayments.mapNotNull { payment ->
            buildNotification(payment, today)
        }

        notifications.forEach { notification ->
            log.warn(
                "OVERDUE PAYMENT: paymentId={}, VS={}, daysOverdue={}, amount={}, landlord={}",
                notification.paymentId,
                notification.variableSymbol,
                notification.daysOverdue,
                notification.amount,
                notification.landlordEmail
            )
            // TODO: Send email via Spring Mail when mail is configured
        }

        return notifications
    }

    private fun buildNotification(payment: Payment, today: LocalDate): OverduePaymentNotification? {
        val landlord = runCatching {
            payment.contract.unit.property.landlord
        }.getOrNull() ?: return null

        val daysOverdue = today.toEpochDay() - payment.dueDate.toEpochDay()
        return OverduePaymentNotification(
            paymentId = payment.id,
            variableSymbol = payment.variableSymbol,
            contractId = payment.contract.id,
            daysOverdue = daysOverdue.toInt(),
            amount = payment.amount,
            landlordEmail = landlord.email
        )
    }
}
