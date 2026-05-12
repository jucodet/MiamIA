package com.miamia.ingredients

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun IngredientEditorScreen(
    viewModel: IngredientEditorViewModel,
    scanId: String,
    onValidated: (Boolean) -> Unit
) {
    val items by viewModel.items.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Edition des ingredients")
        statusMessage?.let { Text(it) }
        items.forEach { Text("- $it") }
        Button(onClick = { viewModel.validateAndSave(scanId, editedByUser = true, onValidated = onValidated) }) {
            Text("Valider")
        }
    }
}
