package com.estoque.bomdemais

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.estoque.bomdemais.categorias.CategoriasActivity
import com.estoque.bomdemais.notas.NotasActivity
import com.google.android.material.card.MaterialCardView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<MaterialCardView>(R.id.btn_estoque).setOnClickListener {
            startActivity(Intent(this, CategoriasActivity::class.java))
        }
        findViewById<MaterialCardView>(R.id.btn_notas).setOnClickListener {
            startActivity(Intent(this, NotasActivity::class.java))
        }
    }
}
