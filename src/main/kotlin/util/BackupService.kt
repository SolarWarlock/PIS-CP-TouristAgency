package util

import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset

object BackupService {

    fun makeBackup(filePath: String): Boolean {

        val host = "127.0.0.1"
        val port = "5432"
        val dbName = "TravelAgency"

        val user = "postgres"
        val pass = "2005"

        val pgDumpPath = "C:\\Program Files\\PostgreSQL\\18\\bin\\pg_dump.exe"

        val command = listOf(
            pgDumpPath,
            "-h", host,
            "-p", port,
            "-U", user,
            "-d", dbName,
            "-f", filePath,
            "-F", "p",
            "--no-password"
        )

        return try {
            val pb = ProcessBuilder(command)

            val env = pb.environment()
            env["PGPASSWORD"] = pass
            pb.redirectErrorStream(true)

            val process = pb.start()

            // Читаем логи
            val reader = BufferedReader(InputStreamReader(process.inputStream, Charset.forName("CP866"))) // CP866 для русской Windows консоли
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                println("BACKUP LOG: $line")
            }

            val exitCode = process.waitFor()
            println("Process finished with exit code: $exitCode")

            exitCode == 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}