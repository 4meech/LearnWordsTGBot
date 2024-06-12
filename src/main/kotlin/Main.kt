fun main() {

    val trainer = LearnWordsTrainer(3, 4)

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

        when (userInput) {
            "1" -> {
                while (true) {
                    val question = trainer.getNextQuestion()

                    if (question == null) {
                        println("Поздравляем! Вы выучили все слова!")
                        break
                    }
                    var correctIndex = -1
                    val guessedWord = question.correctAnswer.translatedWord
                    val answersIndexed = question.variants.mapIndexed { index, variant ->
                        if (variant == question.correctAnswer) correctIndex = index + 1
                        "${index + 1}. ${variant.originalWord}"
                    }.joinToString("\n")

                    println("Введите верный вариант перевода слова \"$guessedWord\":\n$answersIndexed")
                    println("0. Выход в главное меню")
                    val userAnswer: String = readln()

                    if (userAnswer == "0") break
                    if (userAnswer.toInt() == correctIndex) {
                        println("Верно!")
                        question.correctAnswer.correctAnswersCount++
                        trainer.saveDictionary(trainer.dictionary)
                    }
                }
            }

            "2" -> {
                val statistics = trainer.getStatisctics()

                println(
                    "Выучено слов: ${statistics.learnedWords} из ${statistics.totalWords} | " +
                            "${statistics.percentageLearned}%"
                )
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