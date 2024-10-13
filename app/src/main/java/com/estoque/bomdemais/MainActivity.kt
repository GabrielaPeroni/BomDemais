package com.estoque.bomdemais

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.estoque.bomdemais.estoque.EstoqueActivity
import com.estoque.bomdemais.financeiro.FinanceiroActivity
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Configure buttons to redirect to EstoqueActivity and FinanceiroActivity
        val btnEstoque = findViewById<MaterialButton>(R.id.btn_estoque)
        val btnFinanceiro = findViewById<MaterialButton>(R.id.btn_financeiro)

        btnEstoque.setOnClickListener {
            val intent = Intent(this, EstoqueActivity::class.java)
            startActivity(intent)
        }

        btnFinanceiro.setOnClickListener {
            val intent = Intent(this, FinanceiroActivity::class.java)
            startActivity(intent)
        }
    }
}
