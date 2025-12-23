package data.db

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

// Эта функция запускает блок кода внутри транзакции БД
// и делает это в фоновом потоке (IO), чтобы не зависал интерфейс.
suspend fun <T> dbQuery(block: suspend () -> T): T =
    newSuspendedTransaction(Dispatchers.IO) { block() }

