package com.example.gestordegastos.data

import android.content.Context
import com.example.gestordegastos.sql.DataBase


fun debtCalculator(context: Context, idBill: Int): List<String> {
    val db = DataBase(context)

    val debtors = mutableListOf<Expense>()
    val creditors = mutableListOf<Expense>()
    val balances = mutableListOf<String>()

    val totalExpense = db.getAllExpensesForBill(idBill)
    val numberOfParticipants = db.getNumberOfParticipantsForBill(idBill)
    val expensePerPerson = totalExpense / numberOfParticipants
    val peopleAndExpense = db.getExpenseAndPeopleForBill(idBill)
    val transfers = db.getTransferForBill(idBill)

    for (j in peopleAndExpense) {
        // Calcular el impacto de las transferencias para cada persona
        val transfersFromWhom = transfers.filter { it.fromWhom == j.who }.sumOf { it.amount }
        val transfersToWhom = transfers.filter { it.toWhom == j.who }.sumOf { it.amount }

        val debit = (j.amount - transfersToWhom + transfersFromWhom) - expensePerPerson
        if (debit < 0) {
            debtors.add(Expense(j.who, debit))
        }
        if (debit > 0) {
            creditors.add(Expense(j.who, debit))
        }
    }

    for (i in debtors) {
        for (j in creditors) {
            if (i.amount * -1 == j.amount) {
                balances.add("${i.who} debe a ${j.who} ${i.amount}")
            }
        }
    }

    return balances
}