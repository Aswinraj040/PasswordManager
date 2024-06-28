package com.aswin.passwordmanager

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.content.ContentValues
import android.util.Log

const val DATABASE_NAME = "accounts.db"
const val DATABASE_VERSION = 1
const val TABLE_NAME = "accounts"
const val COLUMN_ID = "id"
const val COLUMN_ACCOUNT_NAME = "accountName"
const val COLUMN_USERNAME = "username"
const val COLUMN_PASSWORD = "password"

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        val createTable = "CREATE TABLE $TABLE_NAME ($COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COLUMN_ACCOUNT_NAME TEXT, $COLUMN_USERNAME TEXT, $COLUMN_PASSWORD TEXT)"
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun insertAccount(accountName: String, username: String, password: String) {
        val encryptedPassword = EncryptionUtils.encrypt(password)
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_ACCOUNT_NAME, accountName)
            put(COLUMN_USERNAME, username)
            put(COLUMN_PASSWORD, encryptedPassword)
        }
        db.insert(TABLE_NAME, null, contentValues)
    }

    fun getAllAccounts(): List<Account> {
        val accounts = mutableListOf<Account>()
        val db = readableDatabase
        val cursor = db.query(TABLE_NAME, null, null, null, null, null, null)
        with(cursor) {
            while (moveToNext()) {
                val accountName = getString(getColumnIndexOrThrow(COLUMN_ACCOUNT_NAME))
                val username = getString(getColumnIndexOrThrow(COLUMN_USERNAME))
                val encryptedPassword = getString(getColumnIndexOrThrow(COLUMN_PASSWORD))
                Log.d("Tagee" , encryptedPassword)
                val decryptedPassword = EncryptionUtils.decrypt(encryptedPassword)
                Log.d("Tagee" , EncryptionUtils.decrypt(decryptedPassword))
                accounts.add(Account(accountName, username, EncryptionUtils.decrypt(decryptedPassword)))
            }
        }
        cursor.close()
        return accounts
    }
    fun deleteAccount(username: String) {
        val db = writableDatabase
        db.delete(TABLE_NAME, "$COLUMN_USERNAME = ?", arrayOf(username))
    }
    fun fetchPass(accountName: String): String {
        val db = readableDatabase
        var password = ""

        val query = "SELECT $COLUMN_PASSWORD FROM $TABLE_NAME WHERE $COLUMN_ACCOUNT_NAME = ?"
        val cursor = db.rawQuery(query, arrayOf(accountName))
        cursor.use {
            if (it.moveToFirst()) {
                password = it.getString(it.getColumnIndexOrThrow(COLUMN_PASSWORD))
                Log.d("Tag1" , "$password")
            }
        }
        cursor.close()
        return password
    }
    fun updateAccount(accountName: String, username: String, encryptedPassword: String) {
        val database = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USERNAME, username)
            put(COLUMN_PASSWORD, EncryptionUtils.encrypt(encryptedPassword))
        }
        val selection = "$COLUMN_ACCOUNT_NAME = ?"
        val selectionArgs = arrayOf(accountName)

        database.update(TABLE_NAME, values, selection, selectionArgs)
    }
}

data class Account(val accountName: String, val username: String, val password: String)
