import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

fun main() {
    while (true) {
        println("Task (hide, show, exit):")
        when (val task = readLine()!!) {
            "hide" -> hideMessage()
            "show" -> showMessage()
            "exit" -> {
                println("Bye!")
                return
            }
            else -> println("Wrong task: $task")
        }
    }
}

fun hideMessage() {
    try {
        println("Input image file:")
        val inputFileName = readLine()!!
        println("Output image file:")
        val outputFileName = readLine()!!
        val inputFile = File(inputFileName)
        val image = ImageIO.read(inputFile) ?: throw Exception("Can't read input file!")

        println("Message to hide:")
        val message = readLine()!!

        println("Password:")
        val password = readLine()!!
        if (password.isEmpty()) throw Exception("Password cannot be empty")

        // Convert message and password to byte arrays
        val messageBytes = message.toByteArray(Charsets.UTF_8)
        val passwordBytes = password.toByteArray(Charsets.UTF_8)

        // XOR encrypt message with password (repeating password if needed)
        val encryptedBytes = ByteArray(messageBytes.size) { i ->
            messageBytes[i].toInt().xor(passwordBytes[i % passwordBytes.size].toInt()).toByte()
        } + byteArrayOf(0, 0, 3)  // terminator

        val totalBits = encryptedBytes.size * 8
        if (totalBits > image.width * image.height) {
            println("The input image is not large enough to hold this message.")
            return
        }

        // Hide encrypted bits in blue channel LSB
        var bitIndex = 0
        outer@ for (y in 0 until image.height) {
            for (x in 0 until image.width) {
                if (bitIndex >= totalBits) break@outer
                val color = Color(image.getRGB(x, y))
                val byteIndex = bitIndex / 8
                val bitInByte = 7 - (bitIndex % 8)
                val bit = (encryptedBytes[byteIndex].toInt() shr bitInByte) and 1
                val newBlue = (color.blue and 0xFE) or bit
                val newColor = Color(color.red, color.green, newBlue)
                image.setRGB(x, y, newColor.rgb)
                bitIndex++
            }
        }

        ImageIO.write(image, "png", File(outputFileName))
        println("Message saved in $outputFileName image.")
    } catch (e: Exception) {
        println("Can't read input file!")
    }
}

fun showMessage() {
    try {
        println("Input image file:")
        val inputFileName = readLine()!!
        val image = ImageIO.read(File(inputFileName)) ?: throw Exception("Can't read input file!")

        println("Password:")
        val password = readLine()!!
        if (password.isEmpty()) throw Exception("Password cannot be empty")
        val passwordBytes = password.toByteArray(Charsets.UTF_8)

        val bytes = mutableListOf<Byte>()
        var currentByte = 0
        var bitCount = 0

        outer@ for (y in 0 until image.height) {
            for (x in 0 until image.width) {
                val blue = Color(image.getRGB(x, y)).blue
                val bit = blue and 1
                currentByte = (currentByte shl 1) or bit
                bitCount++
                if (bitCount == 8) {
                    bytes.add(currentByte.toByte())
                    if (bytes.size >= 3 &&
                        bytes[bytes.size - 3] == 0.toByte() &&
                        bytes[bytes.size - 2] == 0.toByte() &&
                        bytes[bytes.size - 1] == 3.toByte()
                    ) break@outer
                    currentByte = 0
                    bitCount = 0
                }
            }
        }

        val encryptedBytes = bytes.dropLast(3).toByteArray()
        // Decrypt using XOR with password
        val decryptedBytes = ByteArray(encryptedBytes.size) { i ->
            encryptedBytes[i].toInt().xor(passwordBytes[i % passwordBytes.size].toInt()).toByte()
        }
        val message = decryptedBytes.toString(Charsets.UTF_8)
        println("Message:\n$message")
    } catch (e: Exception) {
        println("Can't read input file!")
    }
}