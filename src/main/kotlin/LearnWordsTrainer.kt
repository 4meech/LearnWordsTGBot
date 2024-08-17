import kotlinx.serialization.Serializable
import java.io.File

@Serializable
data class Word(
    val originalWord: String,
    val translatedWord: String,
    var correctAnswersCount: Int,
)

class LearnWordsTrainer(
    private val fileName: String = "words.txt",
    private val answersToLearn: Int,
    private val variantsOfAnswers: Int
) {
    private val dictionary = loadDictionary()
    internal var currentQuestion: Question? = null

    private fun loadDictionary(): MutableList<Word> {
        try {
            val wordsFile = File(fileName)
            if (!wordsFile.exists()) {
                File("words.txt").copyTo(wordsFile)
            }
            val dictionary = mutableListOf<Word>()

            wordsFile.readLines().forEach { wordsLine ->
                val parsedWords = wordsLine.split("|")
                val word = Word(parsedWords[0], parsedWords[1], parsedWords[2].toIntOrNull() ?: 0)

                dictionary.add(word)
            }
            return dictionary
        } catch (e: IndexOutOfBoundsException) {
            throw IllegalStateException("Некорректный файл словаря. ${e.message}")
        }
    }

    private fun saveDictionary() {
        val file = File(fileName)
        file.writeText("")

        dictionary.forEach { word ->
            val wordLine = "${word.originalWord}|${word.translatedWord}|${word.correctAnswersCount}\n"
            file.appendText(wordLine)
        }
    }

    fun getStatistics(): Statistics {
        val totalWords = dictionary.size
        val learnedWords = dictionary.filter { it.correctAnswersCount >= answersToLearn }.size
        val percentageLearned = ((learnedWords.toDouble() / totalWords.toDouble()) * 100.0).toInt()

        return Statistics(totalWords, learnedWords, percentageLearned)
    }

    fun getNextQuestion(): Question? {
        val wordsToLearn = dictionary.filter { it.correctAnswersCount < answersToLearn }
        if (wordsToLearn.isEmpty()) return null

        val possibleAnswers = if (wordsToLearn.size < variantsOfAnswers) {
            val wordsLearned = dictionary.filter { it.correctAnswersCount >= answersToLearn }.shuffled()
            wordsToLearn.shuffled().take(variantsOfAnswers) +
                    wordsLearned.take(variantsOfAnswers - wordsToLearn.size)
        } else {
            wordsToLearn.shuffled().take(variantsOfAnswers)
        }.shuffled()

        val correctIndex = possibleAnswers.indices.random()

        currentQuestion = Question(
            variants = possibleAnswers,
            correctIndex = correctIndex,
        )

        return currentQuestion
    }

    fun checkAnswer(userAnswer: Int): Boolean {
        return currentQuestion?.let {
            val correctIndex = it.correctIndex

            if (userAnswer == correctIndex + 1) {
                it.variants[correctIndex].correctAnswersCount++
                saveDictionary()
                true
            } else {
                false
            }
        } ?: false
    }

    fun resetProgress() {
        dictionary.forEach { it.correctAnswersCount = 0 }
        saveDictionary()
    }
}