data class Statistics(
    val totalWords: Int,
    val learnedWords: Int,
    val percentageLearned: Int,
    val statMessage: String = "Выучено слов: $learnedWords из $totalWords | $percentageLearned%",
)