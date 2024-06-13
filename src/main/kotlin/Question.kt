data class Question(
    val variants: List<Word>,
    val correctIndex: Int,
)

fun Question.asConsoleString(): String {
    val guessedWord = this.variants[this.correctIndex].translatedWord
    val answersIndexed = this.variants.mapIndexed { index, variant ->
        "${index + 1}. ${variant.originalWord}"
    }.joinToString("\n")

    return "Введите верный вариант перевода слова \"$guessedWord\":\n$answersIndexed\n\n0. Выход в главное меню"
}