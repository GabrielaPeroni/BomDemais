package com.estoque.bomdemais.financeiro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.estoque.bomdemais.data.FinanceiroRepository
import com.estoque.bomdemais.data.Transaction
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class FinancialSummary(
    val totalReceita: Double = 0.0,
    val totalDespesa: Double = 0.0
) {
    val lucro: Double get() = totalReceita - totalDespesa
}

@OptIn(ExperimentalCoroutinesApi::class)
class FinanceiroViewModel(private val repo: FinanceiroRepository) : ViewModel() {

    private val fmt = SimpleDateFormat("yyyy-MM", Locale.getDefault())

    private val _monthKey = MutableStateFlow(fmt.format(Date()))
    val monthKey: StateFlow<String> = _monthKey

    val transactions: StateFlow<List<Transaction>> = _monthKey
        .flatMapLatest { repo.transactionsByMonth(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val summary: StateFlow<FinancialSummary> = transactions
        .map { list ->
            FinancialSummary(
                totalReceita = list.filter { it.type == "RECEITA" }.sumOf { it.amount },
                totalDespesa = list.filter { it.type == "DESPESA" }.sumOf { it.amount }
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FinancialSummary())

    fun goToPreviousMonth() { _monthKey.value = offsetMonth(_monthKey.value, -1) }
    fun goToNextMonth() { _monthKey.value = offsetMonth(_monthKey.value, 1) }

    fun addTransaction(type: String, amount: Double, description: String, date: Long) =
        viewModelScope.launch { repo.addTransaction(type, amount, description, date) }

    fun deleteTransaction(transaction: Transaction) =
        viewModelScope.launch { repo.deleteTransaction(transaction.id) }

    fun restoreTransaction(transaction: Transaction) =
        viewModelScope.launch { repo.restoreTransaction(transaction) }

    fun editTransaction(transaction: Transaction, type: String, amount: Double, description: String, date: Long) =
        viewModelScope.launch { repo.editTransaction(transaction, type, amount, description, date) }

    private fun offsetMonth(key: String, delta: Int): String {
        val cal = Calendar.getInstance().apply { time = fmt.parse(key) ?: Date() }
        cal.add(Calendar.MONTH, delta)
        return fmt.format(cal.time)
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { FinanceiroViewModel(FinanceiroRepository()) }
        }
    }
}
