package com.estoque.bomdemais

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.estoque.bomdemais.categorias.CategoriasFragment
import com.estoque.bomdemais.financeiro.FinanceiroFragment
import com.estoque.bomdemais.listadecompras.ListaDeComprasFragment
import com.estoque.bomdemais.notas.NotasFragment
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var estoque: CategoriasFragment
    private lateinit var lista: ListaDeComprasFragment
    private lateinit var notas: NotasFragment
    private lateinit var financeiro: FinanceiroFragment
    private lateinit var active: Fragment
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var toolbar: MaterialToolbar

    private val tabTitles = mapOf(
        R.id.nav_estoque to "Estoque",
        R.id.nav_lista to "Lista de Compras",
        R.id.nav_notas to "Notas",
        R.id.nav_financeiro to "Financeiro"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        bottomNav = findViewById(R.id.bottom_nav)

        if (savedInstanceState == null) {
            estoque = CategoriasFragment()
            lista = ListaDeComprasFragment()
            notas = NotasFragment()
            financeiro = FinanceiroFragment()
            active = estoque
            supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, financeiro, "financeiro").hide(financeiro)
                .add(R.id.fragment_container, notas, "notas").hide(notas)
                .add(R.id.fragment_container, lista, "lista").hide(lista)
                .add(R.id.fragment_container, estoque, "estoque")
                .commit()
        } else {
            val restoredEstoque = supportFragmentManager.findFragmentByTag("estoque") as? CategoriasFragment
            val restoredLista = supportFragmentManager.findFragmentByTag("lista") as? ListaDeComprasFragment
            val restoredNotas = supportFragmentManager.findFragmentByTag("notas") as? NotasFragment
            val restoredFinanceiro = supportFragmentManager.findFragmentByTag("financeiro") as? FinanceiroFragment

            if (restoredEstoque != null && restoredLista != null && restoredNotas != null && restoredFinanceiro != null) {
                estoque = restoredEstoque
                lista = restoredLista
                notas = restoredNotas
                financeiro = restoredFinanceiro
                active = supportFragmentManager.fragments.firstOrNull { !it.isHidden } ?: estoque
            } else {
                // Saved state is from a previous version that didn't have all 4 tabs — rebuild fresh
                estoque = restoredEstoque ?: CategoriasFragment()
                lista = restoredLista ?: ListaDeComprasFragment()
                notas = restoredNotas ?: NotasFragment()
                financeiro = restoredFinanceiro ?: FinanceiroFragment()
                active = estoque
                val tx = supportFragmentManager.beginTransaction()
                if (restoredFinanceiro == null) tx.add(R.id.fragment_container, financeiro, "financeiro").hide(financeiro)
                if (restoredNotas == null) tx.add(R.id.fragment_container, notas, "notas").hide(notas)
                if (restoredLista == null) tx.add(R.id.fragment_container, lista, "lista").hide(lista)
                if (restoredEstoque == null) tx.add(R.id.fragment_container, estoque, "estoque")
                tx.commitNow()
            }
        }

        toolbar.title = tabTitles[bottomNav.selectedItemId]

        supportFragmentManager.addOnBackStackChangedListener {
            val hasBack = supportFragmentManager.backStackEntryCount > 0
            bottomNav.visibility = if (hasBack) android.view.View.GONE else android.view.View.VISIBLE
            if (!hasBack) {
                toolbar.navigationIcon = null
                toolbar.title = tabTitles[bottomNav.selectedItemId]
            }
        }

        bottomNav.setOnItemSelectedListener { item ->
            val next: Fragment = when (item.itemId) {
                R.id.nav_estoque -> estoque
                R.id.nav_lista -> lista
                R.id.nav_notas -> notas
                R.id.nav_financeiro -> financeiro
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
