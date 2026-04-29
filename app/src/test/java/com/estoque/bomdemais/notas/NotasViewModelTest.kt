package com.estoque.bomdemais.notas

import com.estoque.bomdemais.data.Note
import com.estoque.bomdemais.data.NotasRepository
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
class NotasViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() = Dispatchers.setMain(testDispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun repo(notes: List<Note> = emptyList()) = mockk<NotasRepository> {
        every { notes() } returns flowOf(notes)
    }

    @Test
    fun `initial notes value is empty`() {
        val vm = NotasViewModel(repo())
        assertEquals(emptyList<Note>(), vm.notes.value)
    }

    @Test
    fun `notes emits list from repository`() = runTest {
        val notes = listOf(Note(id = "1", text = "Comprar leite"), Note(id = "2", text = "Ligar pro médico"))
        val vm = NotasViewModel(repo(notes))

        val job = launch { vm.notes.collect {} }
        advanceUntilIdle()

        assertEquals(notes, vm.notes.value)
        job.cancel()
    }

    @Test
    fun `addNote delegates to repo`() = runTest {
        val r = repo().also { coEvery { it.addNote(any()) } just Runs }
        val vm = NotasViewModel(r)

        vm.addNote("Comprar leite")
        advanceUntilIdle()

        coVerify { r.addNote("Comprar leite") }
    }

    @Test
    fun `deleteNote passes note id to repo`() = runTest {
        val note = Note(id = "abc", text = "Comprar leite")
        val r = repo().also { coEvery { it.deleteNote(any()) } just Runs }
        val vm = NotasViewModel(r)

        vm.deleteNote(note)
        advanceUntilIdle()

        coVerify { r.deleteNote("abc") }
    }

    @Test
    fun `restoreNote passes note to repo`() = runTest {
        val note = Note(id = "abc", text = "Comprar leite")
        val r = repo().also { coEvery { it.restoreNote(any()) } just Runs }
        val vm = NotasViewModel(r)

        vm.restoreNote(note)
        advanceUntilIdle()

        coVerify { r.restoreNote(note) }
    }

    @Test
    fun `editNote passes note and new text to repo`() = runTest {
        val note = Note(id = "abc", text = "Comprar leite")
        val r = repo().also { coEvery { it.editNote(any(), any()) } just Runs }
        val vm = NotasViewModel(r)

        vm.editNote(note, "Comprar leite desnatado")
        advanceUntilIdle()

        coVerify { r.editNote(note, "Comprar leite desnatado") }
    }
}
