package com.estoque.bomdemais

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.estoque.bomdemais.categorias.CategoriasActivity
import com.estoque.bomdemais.notas.NotasActivity
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

        val btnEstoque = findViewById<MaterialButton>(R.id.btn_estoque)
        val btnNotas = findViewById<MaterialButton>(R.id.btn_notas)

        btnEstoque.setOnClickListener {
            startActivity(Intent(this, CategoriasActivity::class.java))
        }
        btnNotas.setOnClickListener {
            startActivity(Intent(this, NotasActivity::class.java))
        }
    }
}
