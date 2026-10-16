package com.nevruz.fourtimessecured.shizuku

class ZenithUserService : IZenithUserService.Stub() {

    override fun execute(command: String): String {
        return try {
            val process = Runtime.getRuntime().exec(
                arrayOf("sh", "-c", command)
            )

            val stdout = process.inputStream
                .bufferedReader()
                .use { it.readText() }

            val stderr = process.errorStream
                .bufferedReader()
                .use { it.readText() }

            val exitCode = process.waitFor()

            buildString {
                append("exit=")
                append(exitCode)

                if (stdout.isNotBlank()) {
                    append("\n")
                    append(stdout.trim())
                }

                if (stderr.isNotBlank()) {
                    append("\n")
                    append(stderr.trim())
                }
            }
        } catch (e: Exception) {
            "error=${e.javaClass.simpleName}: ${e.message ?: "unknown"}"
        }
    }

    override fun destroy() {
        System.exit(0)
    }
}
