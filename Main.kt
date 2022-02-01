import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.experimental.xor
import kotlin.system.exitProcess

const val CONST_END_BITS = "000000000000000000000011"

fun main() {
    while (true) {
        println("Task (hide, show, exit):")
        when (readLine()!!) {
            "hide" -> mainFunction()
            "show" -> showImage()
            "exit" -> {
                println("Bye!")
                exitProcess(1)
            }
        }
    }
}

fun mainFunction() {
    println("Input image file:")
    val input = readLine()!!
    println("Output image file:")
    val output = readLine()!!
    println("Message to hide:")
    var bits = byteToBits(readLine()!!.encodeToByteArray())
    println("Password:")
    var password = byteToBits(readLine()!!.encodeToByteArray())

    bits = encryptedBits(bits, password)
    bits += CONST_END_BITS

    if (validInput(input) == 1) return

    val inputFile = File(input)
    val outputFile = File(output)
    val image: BufferedImage = ImageIO.read(inputFile)

    if(bits.length > image.width * image.height) {
        println("The input image is not large enough to hold this message.")
        return
    }

    var ins = 0
    for (y in 0 until image.height) { // --> y --> height
        for (x in 0 until image.width) { // --> x width
            if (ins < bits.length) {
                val color = Color(image.getRGB(x, y))
                val rgb = Color(
                    color.red,
                    color.green,
                    setLastBit(color.blue, bits[ins].toString().toInt()),
                )
                image.setRGB(x, y, rgb.rgb)
                ins++
            }
        }
    }

    if (saveImage(image, outputFile) == 1) return

    println("Message saved in $output image.")
}

fun showImage() {
    var bits = ""
    val controlMessage = CONST_END_BITS

    println("Input image file:")
    val input = readLine()!!
    println("Password:")
    val password = byteToBits(readLine()!!.encodeToByteArray())

    val file = File(input)
    val image = ImageIO.read(file)

    loop@for (y in 0 until image.height) {
        for (x in 0 until image.width) {
            val color = Color(image.getRGB(x, y))

            if ((color.blue and 1) == 0) bits += "0" else bits += "1"

            if (bits.length > 24 && bits.substring(bits.length - 24) == controlMessage) break@loop
        }
    }

    bits = encryptedBits(bits.substring(0, bits.length - 24), password)
    println("Message:\n${decodeMessage(bits + CONST_END_BITS)}")
}

fun validInput(input: String): Int {
    val file = File(input)
    return try {
        ImageIO.read(file)
        0
    } catch (e: Exception) {
        println("Can't read input file!")
        1
    }
}

fun saveImage(image: BufferedImage, imageFile: File): Int {
    return try {
        ImageIO.write(image, "png", imageFile)
        0
    } catch (e: Exception) {
        println("Can't read output file!")
        1
    }
}

fun decodeMessage(bites: String): String{
    var bits = bites
    var byte = ""
    var counter = 0
    var messageArray: ByteArray = byteArrayOf()
    bits = bits.substring(0, bits.length - 24)
    for (i in bits) {
        byte += i
        if (counter++ == 7) {
            messageArray += Integer.parseInt(byte, 2).toByte()
            byte = ""
            counter = 0
        }
    }
    return messageArray.toString(Charsets.UTF_8)
}

fun setLastBit(pixel: Int, bit: Int): Int {
    return pixel.and(254).or(bit)
}

fun encryptedBits(bits: String, password: String): String {
    var encryptedPassword = ""
    var count = 0
    for (element in bits) {
        encryptedPassword += (element.toString().toByte() xor password[count++].toString().toByte()).toString()
        if (count == password.length) count = 0
    }
    return encryptedPassword
}

fun byteToBits(hideArray: ByteArray): String {
    var bitTest = ""
    var bits = ""
    for (i in hideArray) {
        bitTest = i.toString(2)
        if(bitTest.length < 8) {
            for (j in 0 until 8 - bitTest.length) {
                bitTest = "0$bitTest"
            }
        }
        bits += bitTest
    }
    return bits
}


