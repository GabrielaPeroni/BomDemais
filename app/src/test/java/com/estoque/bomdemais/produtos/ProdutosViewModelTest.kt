package com.estoque.bomdemais.produtos

import com.estoque.bomdemais.data.Product
import com.estoque.bomdemais.data.ProdutosRepository
import com.estoque.bomdemais.data.ShoppingRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.Runs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProdutosViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() = Dispatchers.setMain(testDispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun prodRepo(products: List<Product> = emptyList()) = mockk<ProdutosRepository> {
        every { productsByCategory(any()) } returns flowOf(products)
    }

    private fun shoppingRepo() = mockk<ShoppingRepository>()

    @Test
    fun `initial products value is empty`() {
        val vm = ProdutosViewModel(prodRepo(), shoppingRepo(), "Laticínios")
        assertEquals(emptyList<Product>(), vm.products.value)
    }

    @Test
    fun `products emits list from repository`() = runTest {
        val products = listOf(
            Product(id = "1", name = "Leite", category = "Laticínios"),
            Product(id = "2", name = "Queijo", category = "Laticínios")
        )
        val vm = ProdutosViewModel(prodRepo(products), shoppingRepo(), "Laticínios")

        val job = launch { vm.products.collect {} }
        advanceUntilIdle()

        assertEquals(products, vm.products.value)
        job.cancel()
    }

    @Test
    fun `addProduct delegates to repo`() = runTest {
        val r = prodRepo().also { coEvery { it.addProduct(any(), any()) } returns null }
        val vm = ProdutosViewModel(r, shoppingRepo(), "Laticínios")

        vm.addProduct("Leite", "Laticínios")
        advanceUntilIdle()

        coVerify { r.addProduct("Leite", "Laticínios") }
    }

    @Test
    fun `deleteProduct passes product id to repo`() = runTest {
        val product = Product(id = "abc", name = "Leite", category = "Laticínios")
        val r = prodRepo().also { coEvery { it.deleteProduct(any()) } just Runs }
        val vm = ProdutosViewModel(r, shoppingRepo(), "Laticínios")

        vm.deleteProduct(product)
        advanceUntilIdle()

        coVerify { r.deleteProduct("abc") }
    }

    @Test
    fun `restoreProduct passes product to repo`() = runTest {
        val product = Product(id = "abc", name = "Leite", category = "Laticínios")
        val r = prodRepo().also { coEvery { it.restoreProduct(any()) } just Runs }
        val vm = ProdutosViewModel(r, shoppingRepo(), "Laticínios")

        vm.restoreProduct(product)
        advanceUntilIdle()

        coVerify { r.restoreProduct(product) }
    }

    @Test
    fun `renameProduct passes product and new name to repo`() = runTest {
        val product = Product(id = "abc", name = "Leite", category = "Laticínios")
        val r = prodRepo().also { coEvery { it.renameProduct(any(), any()) } just Runs }
        val vm = ProdutosViewModel(r, shoppingRepo(), "Laticínios")

        vm.renameProduct(product, "Leite Integral")
        advanceUntilIdle()

        coVerify { r.renameProduct(product, "Leite Integral") }
    }

    @Test
    fun `addToShoppingList delegates to shopping repo`() = runTest {
        val sr = shoppingRepo().also { coEvery { it.addItem(any(), any()) } just Runs }
        val vm = ProdutosViewModel(prodRepo(), sr, "Laticínios")

        vm.addToShoppingList("Leite", "Laticínios")
        advanceUntilIdle()

        coVerify { sr.addItem("Leite", "Laticínios") }
    }
}
