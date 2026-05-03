package com.estoque.bomdemais.financeiro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.estoque.bomdemais.R
import com.estoque.bomdemais.utils.SwipeToDeleteCallback
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FinanceiroFragment : Fragment() {

    private val viewModel: FinanceiroViewModel by viewModels { FinanceiroViewModel.Factory }
    private val monthDisplayFmt = SimpleDateFormat("MMMM yyyy", Locale("pt", "BR"))
    private val parseFmt = SimpleDateFormat("yyyy-MM", Locale.getDefault())

    private lateinit var adapter: TransactionAdapter
    private lateinit var rootView: View
    private lateinit var fab: FloatingActionButton
    private var actionMode: ActionMode? = null

    private val actionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.menuInflater.inflate(R.menu.contextual_menu, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            val count = adapter.getSelectedItems().size
            mode.title = "$count selecionado(s)"
            menu.findItem(R.id.action_rename)?.isVisible = false
            return true
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            return when (item.itemId) {
                R.id.action_delete -> { handleMultiDelete(mode); true }
                else -> false
            }
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            actionMode = null
            adapter.exitSelectionMode()
            fab.show()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        rootView = inflater.inflate(R.layout.fragment_financeiro, container, false)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val textMonth = view.findViewById<TextView>(R.id.text_month)
        val textReceita = view.findViewById<TextView>(R.id.text_receita)
        val textDespesa = view.findViewById<TextView>(R.id.text_despesa)
        val textLucro = view.findViewById<TextView>(R.id.text_lucro)
        val btnPrev = view.findViewById<ImageButton>(R.id.btn_prev_month)
        val btnNext = view.findViewById<ImageButton>(R.id.btn_next_month)
        val recycler = view.findViewById<RecyclerView>(R.id.recycler_view_transacoes)
        val emptyState = view.findViewById<View>(R.id.empty_state_financeiro)
        fab = view.findViewById(R.id.fab_add_transacao)

        recycler.layoutManager = LinearLayoutManager(requireContext())

        adapter = TransactionAdapter(
            mutableListOf(),
            onTap = { transaction ->
                val sheet = AddTransactionBottomSheet.newInstance(transaction)
                sheet.onSave = { type, amount, description, date ->
                    viewModel.editTransaction(transaction, type, amount, description, date)
                }
                sheet.onDelete = {
                    adapter.removeTransaction(transaction)
                    Snackbar.make(rootView, "Lançamento removido", Snackbar.LENGTH_LONG)
                        .setAction("Desfazer") {
                            adapter.addTransaction(transaction)
                            viewModel.restoreTransaction(transaction)
                        }
                        .addCallback(object : Snackbar.Callback() {
                            override fun onDismissed(snackbar: Snackbar, event: Int) {
                                if (event != DISMISS_EVENT_ACTION) viewModel.deleteTransaction(transaction)
                            }
                        })
                        .show()
                }
                sheet.show(parentFragmentManager, "edit_transaction")
            },
            onLongPress = { transaction ->
                adapter.enterSelectionMode(transaction.id)
                actionMode = (requireActivity() as AppCompatActivity).startSupportActionMode(actionModeCallback)
                fab.hide()
            },
            onSelectionChanged = { actionMode?.invalidate() }
        )
        recycler.adapter = adapter

        val swipeCallback = SwipeToDeleteCallback(
            isEnabled = { !adapter.isSelectionMode },
            onSwiped = { position ->
                val transaction = adapter.getItemAt(position)
                adapter.removeTransaction(transaction)
                Snackbar.make(rootView, "Lançamento removido", Snackbar.LENGTH_LONG)
                    .setAction("Desfazer") {
                        adapter.addTransaction(transaction)
                        viewModel.restoreTransaction(transaction)
                    }
                    .addCallback(object : Snackbar.Callback() {
                        override fun onDismissed(snackbar: Snackbar, event: Int) {
                            if (event != DISMISS_EVENT_ACTION) viewModel.deleteTransaction(transaction)
                        }
                    })
                    .show()
            }
        )
        ItemTouchHelper(swipeCallback).attachToRecyclerView(recycler)

        btnPrev.setOnClickListener { viewModel.goToPreviousMonth() }
        btnNext.setOnClickListener { viewModel.goToNextMonth() }

        fab.setOnClickListener {
            val sheet = AddTransactionBottomSheet.newInstance()
            sheet.onSave = { type, amount, description, date ->
                viewModel.addTransaction(type, amount, description, date)
            }
            sheet.show(parentFragmentManager, "add_transaction")
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.monthKey.collect { key ->
                        val date = parseFmt.parse(key) ?: Date()
                        textMonth.text = monthDisplayFmt.format(date)
                            .replaceFirstChar { it.uppercase() }
                    }
                }
                launch {
                    viewModel.summary.collect { summary ->
                        textReceita.text = formatCurrency(summary.totalReceita)
                        textDespesa.text = formatCurrency(summary.totalDespesa)
                        textLucro.text = formatCurrency(summary.lucro)
                    }
                }
                launch {
                    viewModel.transactions.collect { list ->
                        adapter.updateTransactions(list)
                        emptyState.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                    }
                }
            }
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (hidden) { actionMode?.finish(); actionMode = null }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        actionMode?.finish()
        actionMode = null
    }

    private fun handleMultiDelete(mode: ActionMode) {
        val selected = adapter.getSelectedItems()
        if (selected.isEmpty()) return
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Excluir ${selected.size} lançamento(s)?")
            .setMessage("Esta ação não pode ser desfeita.")
            .setPositiveButton("Excluir") { _, _ ->
                selected.forEach {
                    viewModel.deleteTransaction(it)
                    adapter.removeTransaction(it)
                }
                mode.finish()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun formatCurrency(value: Double): String =
        String.format(Locale("pt", "BR"), "R$ %.2f", value)
}
