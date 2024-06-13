import kotlin.system.exitProcess

fun main() {

    try {
        val trainer = try {
            LearnWordsTrainer(3, 4)
        } catch (e: Exception) {
            println("Невозможно загрузить словарь. ${e.message}")
            return
        }
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

                        val correctIndex = question.correctIndex
                        println(question.asConsoleString())

                        val userAnswer = readln().toIntOrNull() ?: -1

                        if (userAnswer == 0) {
                            break
                        } else if (userAnswer !in question.variants.indices) {
                            println("Ошибка ввода: введите число от 1 до ${question.variants.size}")
                            println()
                            continue
                        }

                        if (trainer.checkAnswer(question, userAnswer)) {
                            println("Верно!")
                            println()
                        } else {
                            println("Неверно! Правильный ответ: ${question.variants[correctIndex].originalWord}")
                            println()
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
    } catch (e: Exception) {
        println("Ошибка! ${e.localizedMessage}")
        exitProcess(1)
    }
}