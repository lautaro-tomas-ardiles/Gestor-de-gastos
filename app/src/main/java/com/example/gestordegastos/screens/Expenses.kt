package com.example.gestordegastos.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.gestordegastos.R
import com.example.gestordegastos.data.DetailedExpense
import com.example.gestordegastos.sql.DataBase

@Composable
fun TopAppBarExpenses() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.onSecondary)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.padding(10.dp))

            Row (
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { /*TODO*/ }
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Text(
                    text = "Expenses",
                    fontSize = MaterialTheme.typography.titleLarge.fontSize,
                    textAlign = TextAlign.Start,
                    maxLines = 1,
                    color = Color.White
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind {
                        drawLine(
                            color = Color.Yellow,
                            start = Offset(0f, size.height),
                            end = Offset(size.width / 2, size.height),
                            strokeWidth = 2.5.dp.toPx()
                        )
                        drawLine(
                            color = Color.White,
                            start = Offset(size.width / 2, size.height),
                            end = Offset(size.width, size.height),
                            strokeWidth = 2.5.dp.toPx()
                        )
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Spacer(modifier = Modifier.padding(5.dp))

                Column (
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconButton(
                        onClick = { /*TODO*/ },
                        modifier = Modifier.size(80.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.plugin_icon___1__1_),
                            contentDescription = "Expense",
                            tint = Color.White
                        )
                    }
                    Text(
                        text = "Expenses",
                        fontSize = MaterialTheme.typography.titleLarge.fontSize,
                        maxLines = 1,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.padding(5.dp))
                }
                Spacer(modifier = Modifier.padding(50.dp))

                Column (
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { /* TODO */ }
                ) {
                    IconButton(
                        onClick = { /*TODO*/ },
                        modifier = Modifier.size(70.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.group_4),
                            contentDescription = "Expense",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.padding(5.dp))
                    Text(
                        text = "Balance",
                        fontSize = MaterialTheme.typography.titleLarge.fontSize,
                        maxLines = 1,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.padding(5.dp))
            }
        }
    }
}

@Composable
fun Expenses(data: DetailedExpense) {
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
                text = data.title,
                fontSize = MaterialTheme.typography.titleMedium.fontSize,
                textAlign = TextAlign.Start,
                maxLines = 1
            )
            Row(
                modifier = Modifier
                    .padding(end = 10.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "${data.amount}",
                    fontSize = MaterialTheme.typography.titleMedium.fontSize,
                    textAlign = TextAlign.Start,
                    maxLines = 1
                )
            }
            Text(
                text = data.type,
                fontSize = MaterialTheme.typography.titleMedium.fontSize,
                textAlign = TextAlign.Start,
                maxLines = 1
            )
            Spacer(modifier = Modifier.padding(2.dp))
        }
    }
}

@Composable
fun ExpenseList(dataList: List<DetailedExpense>) {
    LazyColumn{
        items(dataList) {
            data -> Expenses(data)
        }
    }
}

@Composable
fun MainPageExpense(id: Int) {
    val db = DataBase(LocalContext.current)

    Scaffold (
        topBar = { TopAppBarExpenses() },
        floatingActionButton = { Fab {/* TODO */ } }
    ){paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues)
        ){
            ExpenseList(db.getDetailedExpensesForBill(id))
        }
    }
}
