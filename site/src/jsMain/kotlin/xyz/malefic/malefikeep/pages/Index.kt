package xyz.malefic.malefikeep.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxSize
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxWidth
import com.varabyte.kobweb.compose.ui.modifiers.maxWidth
import com.varabyte.kobweb.core.Page
import kotlinx.browser.localStorage
import kotlinx.serialization.json.Json
import org.jetbrains.compose.web.css.px
import org.w3c.dom.get
import org.w3c.dom.set
import xyz.malefic.malefikeep.components.CreateNote
import xyz.malefic.malefikeep.components.Header
import xyz.malefic.malefikeep.components.NotesGrid
import xyz.malefic.malefikeep.models.Note

@Page
@Composable
fun HomePage() {
    // Load notes from localStorage if available
    val notesKey = "kobweb-notes"
    val storedNotes =
        remember {
            try {
                val notesJson = localStorage[notesKey]
                if (notesJson != null) {
                    Json.decodeFromString<List<Note>>(notesJson)
                } else {
                    listOf()
                }
            } catch (e: Exception) {
                console.error("Failed to load notes:", e)
                listOf()
            }
        }

    var notes by remember { mutableStateOf(storedNotes) }

    // Update localStorage when notes change
    LaunchedEffect(notes) {
        try {
            localStorage[notesKey] = Json.encodeToString(notes)
        } catch (e: Exception) {
            console.error("Failed to save notes:", e)
        }
    }

    Column(Modifier.fillMaxSize()) {
        // Header
        Header()

        // Main Content
        Box(
            Modifier.fillMaxWidth().fillMaxSize(),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(Modifier.fillMaxWidth().maxWidth(1200.px)) {
                // Create Note Component
                CreateNote { newNote ->
                    notes = listOf(newNote) + notes
                }

                // Notes Grid
                NotesGrid(
                    notes = notes,
                    onDeleteNote = { noteToDelete ->
                        notes = notes.filter { it.id != noteToDelete.id }
                    },
                )
            }
        }
    }
}
