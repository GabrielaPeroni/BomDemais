package com.estoque.bomdemais.financeiro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.estoque.bomdemais.R

class FinanceiroFragment : Fragment() {

    private val viewModel: FinanceiroViewModel by viewModels { FinanceiroViewModel.Factory }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_financeiro, container, false)
    }
}
