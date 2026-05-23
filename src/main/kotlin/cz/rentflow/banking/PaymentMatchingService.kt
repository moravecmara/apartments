package cz.rentflow.banking

import cz.rentflow.domain.entity.PaymentStatus
import cz.rentflow.domain.repository.PaymentRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

data class MatchingResult(
    val matched: Int,
    val unmatched: Int,
    val details: List<String>
)

@Service
class PaymentMatchingService(
    private val fioBankClient: FioBankClient,
    private val paymentRepository: PaymentRepository
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun matchNewTransactions(): MatchingResult {
        val transactions = fioBankClient.getNewTransactions()
        return processTransactions(transactions)
    }

    @Transactional
    fun matchTransactionsForPeriod(from: LocalDate, to: LocalDate): MatchingResult {
        val transactions = fioBankClient.getTransactions(from, to)
        return processTransactions(transactions)
    }

    private fun processTransactions(transactions: List<BankTransaction>): MatchingResult {
        var matched = 0
        var unmatched = 0
        val details = mutableListOf<String>()

        for (tx in transactions) {
            val vs = tx.variableSymbol
            if (vs.isNullOrBlank()) {
                unmatched++
                details.add("No variable symbol: txId=${tx.id}, amount=${tx.amount}")
                continue
            }

            val payment = paymentRepository.findByVariableSymbol(vs).orElse(null)
            if (payment == null) {
                unmatched++
                details.add("No payment for VS=$vs, amount=${tx.amount}")
                continue
            }

            when {
                tx.amount >= payment.amount -> {
                    payment.status = PaymentStatus.PAID
                    payment.receivedDate = tx.date
                    paymentRepository.save(payment)
                    matched++
                    details.add("PAID: VS=$vs, amount=${tx.amount}")
                    log.info("Payment matched and marked PAID: VS={}, amount={}", vs, tx.amount)
                }
                tx.amount > java.math.BigDecimal.ZERO -> {
                    payment.status = PaymentStatus.PARTIALLY_PAID
                    payment.receivedDate = tx.date
                    paymentRepository.save(payment)
                    matched++
                    details.add("PARTIAL: VS=$vs, received=${tx.amount}, expected=${payment.amount}")
                    log.warn("Partial payment: VS={}, received={}, expected={}", vs, tx.amount, payment.amount)
                }
                else -> {
                    unmatched++
                    details.add("Negative/zero amount skipped: VS=$vs")
                }
            }
        }
        return MatchingResult(matched, unmatched, details)
    }
}
