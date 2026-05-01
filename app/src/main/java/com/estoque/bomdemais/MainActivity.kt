package com.estoque.bomdemais

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.estoque.bomdemais.categorias.CategoriasFragment
import com.estoque.bomdemais.listadecompras.ListaDeComprasFragment
import com.estoque.bomdemais.notas.NotasFragment
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var estoque: CategoriasFragment
    private lateinit var lista: ListaDeComprasFragment
    private lateinit var notas: NotasFragment
    private lateinit var active: Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById<MaterialToolbar>(R.id.toolbar))

        if (savedInstanceState == null) {
            estoque = CategoriasFragment()
            lista = ListaDeComprasFragment()
            notas = NotasFragment()
            active = estoque
            supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, notas, "notas").hide(notas)
                .add(R.id.fragment_container, lista, "lista").hide(lista)
                .add(R.id.fragment_container, estoque, "estoque")
                .commit()
        } else {
            estoque = supportFragmentManager.findFragmentByTag("estoque") as CategoriasFragment
            lista = supportFragmentManager.findFragmentByTag("lista") as ListaDeComprasFragment
            notas = supportFragmentManager.findFragmentByTag("notas") as NotasFragment
            active = supportFragmentManager.fragments.firstOrNull { !it.isHidden } ?: estoque
        }

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        val tabTitles = mapOf(
            R.id.nav_estoque to "Estoque",
            R.id.nav_lista to "Lista de Compras",
            R.id.nav_notas to "Notas"
        )

        findViewById<BottomNavigationView>(R.id.bottom_nav).setOnItemSelectedListener { item ->
            val next: Fragment = when (item.itemId) {
                R.id.nav_estoque -> estoque
                R.id.nav_lista -> lista
                R.id.nav_notas -> notas
                else -> return@setOnItemSelectedListener false
            }
            if (next !== active) {
                supportFragmentManager.beginTransaction()
                    .hide(active)
                    .show(next)
                    .commit()
                active = next
            }
            toolbar.title = tabTitles[item.itemId]
            true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
