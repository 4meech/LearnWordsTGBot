import java.io.File

const val ANSWERS_TO_LEARN = 3
const val VARIANTS_OF_ANSWERS = 4

fun main() {
    val wordsFile = File("words.txt")
    val dictionary: MutableList<Word> = mutableListOf()

    wordsFile.readLines().forEach { wordsLine ->
        val parsedWords = wordsLine.split("|")
        val word = Word(parsedWords[0], parsedWords[1], parsedWords[2].toIntOrNull() ?: 0)

        dictionary.add(word)
    }

    val totalWords = dictionary.size

    while (true) {
        println(
            """
            Введите номер пункта меню: 
            1 — Учить слова
            2 — Статистика
            0 — Выход
        """.trimIndent()
        )
        val userInput = readlnOrNull()
        val wordsToLearn = dictionary.filter { it.correctAnswersCount < ANSWERS_TO_LEARN }

        when (userInput) {
            "1" -> {
                if (wordsToLearn.isEmpty()) {
                    println("Поздравляем! Вы выучили все слова!")
                    break
                }

                var userAnswer = ""
                while (wordsToLearn.isNotEmpty() && userAnswer != "0") {
                    val possibleAnswers = wordsToLearn.shuffled().take(VARIANTS_OF_ANSWERS)
                    val correctAnswer = possibleAnswers.random()
                    var correctIndex = -1
                    val guessedWord = correctAnswer.translatedWord
                    val answersIndexed = possibleAnswers.mapIndexed { index, variant ->
                        if (variant == correctAnswer) correctIndex = index + 1
                        "${index + 1}. ${variant.originalWord}"
                    }.joinToString("\n")

                    println("Введите верный вариант перевода слова \"$guessedWord\":\n$answersIndexed")
                    println("0. Выход в главное меню")
                    userAnswer = readln()
                    if (userAnswer.toInt() == correctIndex) {
                        println("Верно!")
                        correctAnswer.correctAnswersCount++
                        saveDictionary(dictionary)
                    }
                }
            }

            "2" -> {
                val learnedWords = dictionary.filter { it.correctAnswersCount >= ANSWERS_TO_LEARN }.size
                val percentageLearned = ((learnedWords.toDouble() / totalWords.toDouble()) * 100.0).toInt()

                println("Выучено слов: $learnedWords из $totalWords | $percentageLearned%")
            }

            "0" -> {
                println("Завершение программы")
                break
            }

            else -> {
                println("Ошибка ввода: введите 1, 2 или 0")
            }
        }
    }
}

fun saveDictionary(dictionary: MutableList<Word>) {
    val file = File("words.txt")
    val newWordLines = dictionary.joinToString("\n") {
        "${it.originalWord}|${it.translatedWord}|${it.correctAnswersCount}"
    }
    file.writeText(newWordLines)
}