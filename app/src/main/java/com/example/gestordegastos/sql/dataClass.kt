package com.example.gestordegastos.sql

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.gestordegastos.data.BillDetails
import com.example.gestordegastos.data.DetailedExpense
import com.example.gestordegastos.data.Expense
import com.example.gestordegastos.data.Transfer

class DataBase(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        // Nombre de la base de datos y versión
        const val DATABASE_NAME = "expenseManager.db"
        const val DATABASE_VERSION = 2

        // Nombre de las tablas y columnas
        const val TABLE_BILLS = "bills"
        const val COLUMN_IDB = "idB" // ID de la factura
        const val COLUMN_TITLE = "title"
        const val COLUMN_DESCRIPTION = "description"
        const val COLUMN_TIME = "time"

        const val TABLE_PEOPLE = "people"
        const val COLUMN_IDP = "idP"
        const val COLUMN_NAME = "name"

        const val TABLE_AMOUNTS = "amounts"
        const val COLUMN_IDM = "idM"
        const val COLUMN_AMOUNT = "amount"
        const val COLUMN_AMOUNT_TITLE = "amount_title"

        const val TABLE_TYPE = "type"
        const val COLUMN_TYPE = "type"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        // Crear tabla `bills`
        val createTableBills = """
            CREATE TABLE IF NOT EXISTS $TABLE_BILLS (
                $COLUMN_IDB INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TITLE TEXT NOT NULL,
                $COLUMN_DESCRIPTION TEXT,
                $COLUMN_TIME TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
        """

        // Crear tabla `people` asociada a `bills`
        val createTablePeople = """
            CREATE TABLE IF NOT EXISTS $TABLE_PEOPLE (
                $COLUMN_IDP INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_IDB INTEGER NOT NULL,
                $COLUMN_NAME TEXT NOT NULL,
                FOREIGN KEY($COLUMN_IDB) REFERENCES $TABLE_BILLS($COLUMN_IDB)
            );
        """

        // Crear tabla `amounts` asociada a `bills` y `people`
        val createTableAmounts = """
            CREATE TABLE IF NOT EXISTS $TABLE_AMOUNTS (
                $COLUMN_IDM INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_IDB INTEGER NOT NULL,
                $COLUMN_IDP INTEGER,
                $COLUMN_AMOUNT REAL NOT NULL,
                $COLUMN_AMOUNT_TITLE TEXT NOT NULL,
                FOREIGN KEY($COLUMN_IDB) REFERENCES $TABLE_BILLS($COLUMN_IDB),
                FOREIGN KEY($COLUMN_IDP) REFERENCES $TABLE_PEOPLE($COLUMN_IDP)
            );
        """

        // Crear tabla `type` asociada a `bills`, `amounts` y `people`
        val createTableType = """
            CREATE TABLE IF NOT EXISTS $TABLE_TYPE (
                $COLUMN_TYPE TEXT NOT NULL,
                $COLUMN_IDM INTEGER NOT NULL,
                $COLUMN_IDP INTEGER,
                FOREIGN KEY($COLUMN_IDM) REFERENCES $TABLE_AMOUNTS($COLUMN_IDM),
                FOREIGN KEY($COLUMN_IDP) REFERENCES $TABLE_PEOPLE($COLUMN_IDP)
            );
        """

        // Ejecutar las declaraciones SQL
        db?.apply {
            execSQL(createTableBills)
            execSQL(createTablePeople)
            execSQL(createTableAmounts)
            execSQL(createTableType)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.apply {
            execSQL("DROP TABLE IF EXISTS $TABLE_TYPE")
            execSQL("DROP TABLE IF EXISTS $TABLE_AMOUNTS")
            execSQL("DROP TABLE IF EXISTS $TABLE_PEOPLE")
            execSQL("DROP TABLE IF EXISTS $TABLE_BILLS")
            onCreate(this)
        }
    }

    // Funciones para insertar datos en las tablas
    fun insertBill(title: String, description: String): Int {
        val db = writableDatabase

        val values = ContentValues().apply {
            put(COLUMN_TITLE, title)
            put(COLUMN_DESCRIPTION, description)
        }

        val rowId = db.insert(TABLE_BILLS, null, values) // Inserta y guarda el ID
        db.close()

        // Si la inserción falla, `rowId` será -1. Puedes manejarlo si es necesario.
        return rowId.toInt() // Retorna el ID convertido a entero
    }


    fun insertPersonToBill(idBill: Int, name: String) {
        val db = writableDatabase

        val values = ContentValues().apply {
            put(COLUMN_IDB, idBill)
            put(COLUMN_NAME, name)
        }

        db.insert(TABLE_PEOPLE, null, values)
        db.close()
    }

    fun insertAmountToBill(idBill: Int, idP: Int, amount: Double, title: String) {
        val db = writableDatabase

        val values = ContentValues().apply {
            put(COLUMN_IDB, idBill)
            put(COLUMN_IDP, idP)
            put(COLUMN_AMOUNT, amount)
            put(COLUMN_AMOUNT_TITLE, title)
        }

        db.insert(TABLE_AMOUNTS, null, values)
        db.close()
    }

    fun insertTypeToBill(idBill: Int, idM: Int, idP: Int?, type: String) {
        val db = writableDatabase

        val values = ContentValues().apply {
            put(COLUMN_IDB, idBill)
            put(COLUMN_IDM, idM)
            put(COLUMN_IDP, idP)
            put(COLUMN_TYPE, type)
        }

        db.insert(TABLE_TYPE, null, values)
        db.close()
    }

    //query
    fun getAllExpensesForBill(idBill: Int): Double {
        val db = readableDatabase
        var total = 0.0

        val sql = """
            SELECT 
                $TABLE_AMOUNTS.$COLUMN_AMOUNT 
            FROM 
                $TABLE_TYPE
            JOIN 
                $TABLE_AMOUNTS ON $TABLE_TYPE.$COLUMN_IDM = $TABLE_AMOUNTS.$COLUMN_IDM
            WHERE 
                $TABLE_TYPE.$COLUMN_TYPE LIKE 'gasto' AND 
                $TABLE_AMOUNTS.$COLUMN_IDB = ?
            ;
        """

        val cursor = db.rawQuery(sql, arrayOf(idBill.toString()))

        cursor.use {
            while (it.moveToNext()) {
                total += it.getDouble(it.getColumnIndexOrThrow(COLUMN_AMOUNT))
            }
        }

        db.close()
        return total
    }

    fun getNumberOfParticipantsForBill(idBill: Int): Int {
        val db = readableDatabase
        var total = 0

        val sql = "SELECT COUNT(DISTINCT $COLUMN_IDP) AS total_participants FROM $TABLE_PEOPLE WHERE $COLUMN_IDB = ? ;"

        val cursor = db.rawQuery(sql, arrayOf(idBill.toString()))

        cursor.use {
            if (it.moveToFirst()) {
                total = it.getInt(it.getColumnIndexOrThrow("total_participants"))
            }
        }

        db.close()
        return total
    }

    fun getExpenseAndPeopleForBill(idBill: Int): List<Expense> {
        val db = readableDatabase
        val expenses = mutableListOf<Expense>()

        val sql = """
            SELECT 
                $TABLE_PEOPLE.$COLUMN_NAME, 
                SUM($TABLE_AMOUNTS.$COLUMN_AMOUNT) AS total_spent
            FROM 
                $TABLE_PEOPLE
            JOIN 
                $TABLE_AMOUNTS ON $TABLE_PEOPLE.$COLUMN_IDP = $TABLE_AMOUNTS.$COLUMN_IDP
            JOIN 
                $TABLE_TYPE ON $TABLE_TYPE.$COLUMN_IDM = $TABLE_AMOUNTS.$COLUMN_IDM
            WHERE 
                $TABLE_TYPE.$COLUMN_TYPE LIKE 'gasto' AND 
                $TABLE_AMOUNTS.$COLUMN_IDB = ?
            GROUP BY 
                $TABLE_PEOPLE.$COLUMN_NAME
            ;
        """

        val cursor = db.rawQuery(sql, arrayOf(idBill.toString()))
        cursor.use {
            while (it.moveToNext()) {
                expenses.add(
                    Expense(
                        who = it.getString(it.getColumnIndexOrThrow(COLUMN_NAME)),
                        amount = it.getDouble(it.getColumnIndexOrThrow("total_spent"))
                    )
                )
            }
        }

        db.close()
        return expenses
    }

    fun getTransferForBill(idBill: Int): List<Transfer> {
        val db = readableDatabase
        val transfers = mutableListOf<Transfer>()

        val sql = """
            SELECT
                $TABLE_AMOUNTS.$COLUMN_AMOUNT AS transfer_amount,
                sender.$COLUMN_NAME AS sender_name,
                receiver.$COLUMN_NAME AS receiver_name
            FROM 
                $TABLE_AMOUNTS
            JOIN 
                $TABLE_TYPE ON $TABLE_TYPE.$COLUMN_IDM = $TABLE_AMOUNTS.$COLUMN_IDM
            JOIN 
                $TABLE_PEOPLE AS sender ON $TABLE_AMOUNTS.$COLUMN_IDP = sender.$COLUMN_IDP
            JOIN 
                $TABLE_PEOPLE AS receiver ON $TABLE_TYPE.$COLUMN_IDP = receiver.$COLUMN_IDP
            WHERE 
                $TABLE_TYPE.$COLUMN_TYPE LIKE 'transferencia' AND 
                $TABLE_AMOUNTS.$COLUMN_IDB = ?
            ;
        """

        val cursor = db.rawQuery(sql, arrayOf(idBill.toString()))

        cursor.use {
            while (it.moveToNext()) {
                transfers.add(
                    Transfer(
                        fromWhom = it.getString(it.getColumnIndexOrThrow("sender_name")),
                        toWhom = it.getString(it.getColumnIndexOrThrow("receiver_name")),
                        amount = it.getDouble(it.getColumnIndexOrThrow("transfer_amount"))
                    )
                )
            }
        }

        db.close()
        return transfers
    }

    fun getBills(): List<BillDetails> {
        val db = readableDatabase
        val billDetails = mutableListOf<BillDetails>()

        val sql = """
            SELECT 
                $COLUMN_TITLE, 
                $COLUMN_DESCRIPTION
            FROM 
                $TABLE_BILLS
            ;
        """

        val cursor = db.rawQuery(sql, null)

        cursor.use {
            if (it.moveToFirst()) {
                billDetails.add(
                    BillDetails(
                        title = it.getString(it.getColumnIndexOrThrow(COLUMN_TITLE)),
                        description = it.getString(it.getColumnIndexOrThrow(COLUMN_DESCRIPTION))
                    )
                )
            }
        }

        db.close()
        return billDetails
    }

    fun getDetailedExpensesForBill(idBill: Int): List<DetailedExpense> {
        val db = readableDatabase
        val detailedExpenses = mutableListOf<DetailedExpense>()

        val sql = """
            SELECT 
                $TABLE_AMOUNTS.$COLUMN_AMOUNT_TITLE, 
                $TABLE_AMOUNTS.$COLUMN_AMOUNT, 
                $TABLE_TYPE.$COLUMN_TYPE
            FROM 
                $TABLE_AMOUNTS
            JOIN 
                $TABLE_TYPE ON $TABLE_AMOUNTS.$COLUMN_IDM = $TABLE_TYPE.$COLUMN_IDM
            WHERE 
                $TABLE_AMOUNTS.$COLUMN_IDB = ?
            ;
        """

        val cursor = db.rawQuery(sql, arrayOf(idBill.toString()))
        cursor.use {
            while (it.moveToNext()) {
                detailedExpenses.add(
                    DetailedExpense(
                        title = it.getString(it.getColumnIndexOrThrow(COLUMN_AMOUNT_TITLE)),
                        amount = it.getDouble(it.getColumnIndexOrThrow(COLUMN_AMOUNT)),
                        type = it.getString(it.getColumnIndexOrThrow(COLUMN_TYPE))
                    )
                )
            }
        }

        db.close()
        return detailedExpenses
    }

}