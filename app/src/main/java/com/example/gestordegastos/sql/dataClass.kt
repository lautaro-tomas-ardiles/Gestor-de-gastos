package com.example.gestordegastos.sql

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.gestordegastos.data.Expense
import com.example.gestordegastos.data.Transfer

class DataBase(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        // Nombre de la base de datos y versión
        const val DATABASE_NAME = "expenseManager.db"
        const val DATABASE_VERSION = 1

        // Nombre de las tablas y columnas
        const val TABLE_PEOPLE = "people"
        const val COLUMN_IDP = "idP"
        const val COLUMN_NAME = "name"

        const val TABLE_AMOUNTS = "amounts"
        const val COLUMN_IDM = "idM"
        const val COLUMN_AMOUNT = "amount"

        const val TABLE_TYPE = "type"
        const val COLUMN_TIPO = "tipo"
        const val COLUMN_IDM_FK = "idM"
        const val COLUMN_IDP_FK = "idP"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTablePeople = """
            CREATE TABLE IF NOT EXISTS $TABLE_PEOPLE (
                $COLUMN_IDP INTEGER PRIMARY KEY,
                $COLUMN_NAME TEXT
            );
        """

        val createTableAmounts = """
            CREATE TABLE IF NOT EXISTS $TABLE_AMOUNTS (
                $COLUMN_IDM INTEGER PRIMARY KEY,
                $COLUMN_IDP INTEGER,
                $COLUMN_AMOUNT REAL,
                FOREIGN KEY($COLUMN_IDP) REFERENCES $TABLE_PEOPLE($COLUMN_IDP)
            );
        """

        val createTableType = """
            CREATE TABLE IF NOT EXISTS $TABLE_TYPE (
                $COLUMN_TIPO TEXT,
                $COLUMN_IDM_FK INTEGER,
                $COLUMN_IDP_FK INTEGER,
                FOREIGN KEY($COLUMN_IDP_FK) REFERENCES $TABLE_PEOPLE($COLUMN_IDP),
                FOREIGN KEY($COLUMN_IDM_FK) REFERENCES $TABLE_AMOUNTS($COLUMN_IDM)
            );
        """

        db?.apply {
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
            onCreate(this)
        }
    }

    // Funciones para insertar datos en las tablas
    fun insertPerson(name: String) {
        val db = writableDatabase

        val values = ContentValues().apply {
            put(COLUMN_NAME, name)
        }

        db.insert(TABLE_PEOPLE, null, values)
        db.close()
    }

    fun insertAmount(idP: Int, amount: Double) {
        val db = writableDatabase

        val values = ContentValues().apply {
            put(COLUMN_IDP, idP)
            put(COLUMN_AMOUNT, amount)
        }

        db.insert(TABLE_AMOUNTS, null, values)
        db.close()
    }

    fun insertType(tipo: String, idM: Int, idP: Int?){
        val db = writableDatabase

        val values = ContentValues().apply {
            put(COLUMN_TIPO, tipo)
            put(COLUMN_IDM_FK, idM)
            put(COLUMN_IDP_FK, idP)
        }

        db.insert(TABLE_TYPE, null, values)
        db.close()
    }

    //query
    fun getAllExpenses(): Double {
        val db = readableDatabase
        var total = 0.0
        val sql = """
            SELECT 
                monto.monto 
            FROM 
                personas 
            JOIN 
                monto ON personas.idP = monto.idP
            JOIN
                tipo ON tipo.idM = monto.idM
            WHERE
                tipo.tipo LIKE 'gasto'
        """
        val cursor = db.rawQuery(sql, null)

        cursor.use {
            while (it.moveToNext()) {
                total += it.getDouble(it.getColumnIndexOrThrow(COLUMN_AMOUNT))
            }
        }

        db.close()
        return total
    }

    fun getNumberOfParticipants(): Int{
        val db = readableDatabase
        var total = 0
        val sql = "SELECT MAX(personas.idP) FROM personas;"
        val cursor = db.rawQuery(sql, null)

        cursor.use {
            while (it.moveToNext()) {
                total += it.getInt(it.getColumnIndexOrThrow(COLUMN_AMOUNT))
            }
        }

        db.close()
        return total
    }

    fun getIndividualSpending(): List<Expense>{
        val db = readableDatabase
        val expenses = mutableListOf<Expense>()

        val sql = """
            SELECT 
                monto.monto,
                personas.nombre 
            FROM 
                personas 
            JOIN 
                monto ON personas.idP = monto.idP
            JOIN
                tipo ON tipo.idM = monto.idM
            WHERE
                tipo.tipo LIKE 'gasto'
            ;            
        """
        val cursor = db.rawQuery(sql, null)
        cursor.use {
            while (it.moveToNext()) {
                expenses.add(
                    Expense(
                        it.getString(it.getColumnIndexOrThrow(COLUMN_NAME)),
                        it.getDouble(it.getColumnIndexOrThrow(COLUMN_AMOUNT))
                    )
                )
            }
        }

        db.close()
        return expenses
    }

    fun getTransfer(): List<Transfer>{
        val db = readableDatabase
        val transfers = mutableListOf<Transfer>()
        val sql = """
            SELECT
                monto.monto AS monto_transferencia,
                p1.nombre AS quien_realizo,
                p2.nombre AS quien_recibio
            FROM 
                monto
            JOIN 
                tipo ON tipo.idM = monto.idM
            JOIN 
                personas p1 ON monto.idP = p1.idP
            JOIN 
                personas p2 ON tipo.idP = p2.idP
            WHERE 
                tipo.tipo LIKE 'transferencia';
        """
        val cursor = db.rawQuery(sql, null)

        cursor.use {
            while (it.moveToNext()) {
                transfers.add(
                    Transfer(
                        fromWhom =  it.getString(it.getColumnIndexOrThrow("quien_realizo")),
                        toWhom =  it.getString(it.getColumnIndexOrThrow("quien_recibio")),
                        amount = it.getDouble(it.getColumnIndexOrThrow("monto_transferencia"))
                    )
                )
            }
        }
        return transfers
    }

}