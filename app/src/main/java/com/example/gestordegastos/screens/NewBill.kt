@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.gestordegastos.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gestordegastos.sql.DataBase
import com.example.gestordegastos.ui.theme.GestorDeGastosTheme

@Composable
fun SnackBar() {
    Snackbar {
        Text("Error. Vuelva a intentar mas tarde")
    }
}

@Composable
fun TopAppBarNewBill(
    title: String,
    description: String,
    participants: List<String>
) {
    val db = DataBase(context = LocalContext.current)
    val isError = false

    TopAppBar(
        title = {
            Text(
                text = "Nueva Factura",
                fontSize = MaterialTheme.typography.titleLarge.fontSize
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.onSecondary
        ),
        navigationIcon = {
            IconButton(
                onClick = { /*TODO*/ }
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        actions = {
            IconButton(
                onClick = {
                    val idBill = db.insertBill(title, description)

                    if (idBill != -1) {
                        for (participant in participants) {
                            db.insertPersonToBill(idBill, participant)
                        }
                    }else {
                        !isError
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Check"
                )
            }
        }
    )
    if (isError) {
        SnackBar()
    }
}

@Composable
fun TextFieldNewBill(
    onChangeTitle: (String) -> Unit,
    onChangeDescription: (String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    Column (
        modifier = Modifier
            .padding(
                vertical = 40.dp
            )
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = {
                title = it
                onChangeTitle(it)
            },
            label = { Text("Título") }
        )
        Spacer(modifier = Modifier.padding(25.dp))

        OutlinedTextField(
            value = description,
            onValueChange = {
                description = it
                onChangeDescription(it)
            },
            label = { Text("Descripción") }
        )
    }

}

@Composable
fun SubTopAppBar() {
    Box (
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.onSecondary)
            .padding(
                vertical = 18.dp,
                horizontal = 10.dp
            )

    ) {
        Text(
            text = "Participantes 0/50",
            fontSize = MaterialTheme.typography.titleLarge.fontSize
        )
    }
}

@Composable
fun AddParticipants(participants: (List<String>) -> Unit) {
    var participant by remember { mutableStateOf("") }
    var listOfParticipants by remember { mutableStateOf(listOf<String>()) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.padding(20.dp))

        OutlinedTextField(
            value = participant,
            onValueChange = { participant = it },
            label = { Text("agrege un participante") },
            trailingIcon = {
                IconButton(
                    onClick = {
                        if (participant.isNotBlank()) {
                            listOfParticipants += participant
                            participant = ""
                        }
                    },
                    enabled = participant.isNotBlank()
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "add"
                    )
                }
            }
        )
    }
    Spacer(modifier = Modifier.padding(10.dp))

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(listOfParticipants) { participant ->
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = participant,
                    fontSize = MaterialTheme.typography.titleLarge.fontSize,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )

                IconButton(
                    onClick = {
                        listOfParticipants -= participant
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "delete"
                    )
                }
            }

        }
    }
    participants(listOfParticipants)
}


@Composable
fun MainPageNewBill() {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var participants by remember { mutableStateOf(listOf<String>()) }

    Scaffold (
        topBar = {
            TopAppBarNewBill(
                title = title,
                description = description,
                participants = participants
            )
        }
    ){paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues)
        ){
            TextFieldNewBill(
                onChangeTitle = {
                    title = it
                },
                onChangeDescription = {
                    description = it
                }
            )
            SubTopAppBar()
            AddParticipants(
                participants = {
                    participants = it
                }
            )
        }
    }
}
