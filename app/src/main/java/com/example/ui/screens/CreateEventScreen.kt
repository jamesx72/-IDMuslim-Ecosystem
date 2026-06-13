package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.ui.viewmodels.EventViewModel
import com.example.ui.locales.Translations

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventScreen(
    viewModel: EventViewModel,
    onNavigateBack: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var organizer by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var maxTickets by remember { mutableStateOf("") }
    val language by viewModel.language.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(Translations.get(language, "create_event_title")) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(Translations.get(language, "event_title")) },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(Translations.get(language, "description")) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text(Translations.get(language, "date_example")) },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text(Translations.get(language, "time_example")) },
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text(Translations.get(language, "location")) },
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = organizer,
                onValueChange = { organizer = it },
                label = { Text(Translations.get(language, "organizer")) },
                modifier = Modifier.fillMaxWidth()
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text(Translations.get(language, "ticket_price")) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = maxTickets,
                    onValueChange = { maxTickets = it },
                    label = { Text(Translations.get(language, "num_places")) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            Button(
                onClick = {
                    if (title.isNotBlank() && date.isNotBlank()) {
                        viewModel.createEvent(
                            title = title,
                            description = description,
                            date = date,
                            time = time,
                            location = location,
                            organizer = organizer,
                            price = price.toDoubleOrNull() ?: 0.0,
                            maxTickets = maxTickets.toIntOrNull() ?: 0
                        )
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(Translations.get(language, "publish_event"), fontWeight = FontWeight.Bold)
            }
        }
    }
}
