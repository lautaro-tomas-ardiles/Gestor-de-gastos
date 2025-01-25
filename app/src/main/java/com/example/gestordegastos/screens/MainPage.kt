package com.example.gestordegastos.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.gestordegastos.data.BillDetails
import com.example.gestordegastos.sql.DataBase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar() {
    TopAppBar(
        title = {
            Text(
                text = "Gestor de Gastos",
                fontSize = MaterialTheme.typography.titleLarge.fontSize
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.onSecondary
        ),
        modifier = Modifier.height(74.dp)
    )
}

@Composable
fun Bills(billDetails: BillDetails) {
    Box (
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.onSecondary),
    ) {
        HorizontalDivider(
            thickness = 1.dp,
            color = Color.White
        )
        Column (
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
        ) {
            Spacer(modifier = Modifier.padding(2.dp))

            Text(
                text = billDetails.title,
                fontSize = MaterialTheme.typography.titleMedium.fontSize,
                textAlign = TextAlign.Start,
                maxLines = 1
            )

            Text(
                text = billDetails.description,
                fontSize = MaterialTheme.typography.titleMedium.fontSize,
                textAlign = TextAlign.Start,
                maxLines = 1
            )
            Spacer(modifier = Modifier.padding(2.dp))
        }
    }
}

@Composable
fun BillList(billList: List<BillDetails>) {
    LazyColumn {
        items(billList) { billDetails ->
            Bills(billDetails = billDetails)
        }
    }
}

@Composable
fun Fab(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = { onClick() },
        containerColor = MaterialTheme.colorScheme.primary,
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = "Add"
        )
    }
}

@Composable
fun MainPage() {
    val db = DataBase(context = LocalContext.current)

    Scaffold (
        topBar = { TopAppBar() },
        floatingActionButton = { Fab {} }
    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues)
        ) {

            BillList(db.getBills())
        }

    }
}
