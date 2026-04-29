package com.estoque.bomdemais.listadecompras

import com.estoque.bomdemais.data.ShoppingItem
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ListaDeComprasViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() = Dispatchers.setMain(testDispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun repo(items: List<ShoppingItem> = emptyList()) = mockk<ShoppingRepository> {
        every { shoppingItems() } returns flowOf(items)
    }

    @Test
    fun `initial items value is empty`() {
        val vm = ListaDeComprasViewModel(repo())
        assertEquals(emptyList<ShoppingItem>(), vm.items.value)
    }

    @Test
    fun `items emits list from repository`() = runTest {
        val items = listOf(ShoppingItem(id = "1", name = "Leite"), ShoppingItem(id = "2", name = "Pão"))
        val vm = ListaDeComprasViewModel(repo(items))

        val job = launch { vm.items.collect {} }
        advanceUntilIdle()

        assertEquals(items, vm.items.value)
        job.cancel()
    }

    @Test
    fun `addItem delegates to repo with empty category`() = runTest {
        val r = repo().also { coEvery { it.addItem(any(), any()) } just Runs }
        val vm = ListaDeComprasViewModel(r)

        vm.addItem("Leite")
        advanceUntilIdle()

        coVerify { r.addItem("Leite", "") }
    }

    @Test
    fun `deleteItem passes item id to repo`() = runTest {
        val item = ShoppingItem(id = "abc", name = "Leite")
        val r = repo().also { coEvery { it.deleteItem(any()) } just Runs }
        val vm = ListaDeComprasViewModel(r)

        vm.deleteItem(item)
        advanceUntilIdle()

        coVerify { r.deleteItem("abc") }
    }

    @Test
    fun `restoreItem passes item to repo`() = runTest {
        val item = ShoppingItem(id = "abc", name = "Leite")
        val r = repo().also { coEvery { it.restoreItem(any()) } just Runs }
        val vm = ListaDeComprasViewModel(r)

        vm.restoreItem(item)
        advanceUntilIdle()

        coVerify { r.restoreItem(item) }
    }

    @Test
    fun `toggleChecked flips isChecked and calls repo`() = runTest {
        val item = ShoppingItem(id = "abc", name = "Leite", isChecked = false)
        val r = repo().also { coEvery { it.updateChecked(any()) } just Runs }
        val vm = ListaDeComprasViewModel(r)

        vm.toggleChecked(item)
        advanceUntilIdle()

        assertTrue(item.isChecked)
        coVerify { r.updateChecked(item) }
    }

    @Test
    fun `renameItem passes new name to repo`() = runTest {
        val item = ShoppingItem(id = "abc", name = "Leite")
        val r = repo().also { coEvery { it.renameItem(any(), any()) } just Runs }
        val vm = ListaDeComprasViewModel(r)

        vm.renameItem(item, "Leite Integral")
        advanceUntilIdle()

        coVerify { r.renameItem(item, "Leite Integral") }
    }
}
