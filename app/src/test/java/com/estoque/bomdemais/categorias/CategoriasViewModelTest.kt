package com.estoque.bomdemais.categorias

import com.estoque.bomdemais.data.CategoriasRepository
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
class CategoriasViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() = Dispatchers.setMain(testDispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun repo(categories: List<String> = emptyList()) = mockk<CategoriasRepository> {
        every { categories() } returns flowOf(categories)
    }

    @Test
    fun `initial categories value is empty`() {
        val vm = CategoriasViewModel(repo())
        assertEquals(emptyList<String>(), vm.categories.value)
    }

    @Test
    fun `categories emits list from repository`() = runTest {
        val cats = listOf("Laticínios", "Bebidas", "Limpeza")
        val vm = CategoriasViewModel(repo(cats))

        val job = launch { vm.categories.collect {} }
        advanceUntilIdle()

        assertEquals(cats, vm.categories.value)
        job.cancel()
    }

    @Test
    fun `addCategory delegates to repo`() = runTest {
        val r = repo().also { coEvery { it.addCategory(any()) } returns true }
        val vm = CategoriasViewModel(r)

        vm.addCategory("Laticínios")
        advanceUntilIdle()

        coVerify { r.addCategory("Laticínios") }
    }

    @Test
    fun `deleteCategories calls repo once per name`() = runTest {
        val r = repo().also { coEvery { it.deleteCategory(any()) } just Runs }
        val vm = CategoriasViewModel(r)

        vm.deleteCategories(listOf("Laticínios", "Bebidas"))
        advanceUntilIdle()

        coVerify { r.deleteCategory("Laticínios") }
        coVerify { r.deleteCategory("Bebidas") }
    }

    @Test
    fun `renameCategory passes success result to callback`() = runTest {
        val r = repo().also { coEvery { it.renameCategory(any(), any()) } returns true }
        val vm = CategoriasViewModel(r)

        var result: Boolean? = null
        vm.renameCategory("Laticínios", "Frios") { result = it }
        advanceUntilIdle()

        coVerify { r.renameCategory("Laticínios", "Frios") }
        assertEquals(true, result)
    }

    @Test
    fun `renameCategory passes failure result to callback`() = runTest {
        val r = repo().also { coEvery { it.renameCategory(any(), any()) } returns false }
        val vm = CategoriasViewModel(r)

        var result: Boolean? = null
        vm.renameCategory("Laticínios", "Frios") { result = it }
        advanceUntilIdle()

        assertEquals(false, result)
    }
}
