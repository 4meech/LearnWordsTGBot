import java.io.File
import kotlin.random.Random

data class Question(
    val variants: List<Word>,
    val correctIndex: Int,
)

class LearnWordsTrainer(private val answersToLearn: Int, private val variantsOfAnswers: Int) {

    val dictionary = loadDictionary()

    private fun loadDictionary(): MutableList<Word> {
        val dictionary = mutableListOf<Word>()
        val wordsFile = File("words.txt")
        wordsFile.readLines().forEach { wordsLine ->
            val parsedWords = wordsLine.split("|")
            val word = Word(parsedWords[0], parsedWords[1], parsedWords[2].toIntOrNull() ?: 0)

            dictionary.add(word)
        }
        return dictionary
    }

    fun saveDictionary(dictionary: MutableList<Word>) {
        val file = File("words.txt")
        file.writeText("")

        dictionary.forEach { word ->
            val wordLine = "${word.originalWord}|${word.translatedWord}|${word.correctAnswersCount}\n"
            file.appendText(wordLine)
        }
    }

    fun getStatisctics(): Statistics {
        val totalWords = dictionary.size
        val learnedWords = dictionary.filter { it.correctAnswersCount >= answersToLearn }.size
        val percentageLearned = ((learnedWords.toDouble() / totalWords.toDouble()) * 100.0).toInt()

        return Statistics(totalWords, learnedWords, percentageLearned)
    }

    fun getNextQuestion(): Question? {
        val wordsToLearn = dictionary.filter { it.correctAnswersCount < answersToLearn }
        if (wordsToLearn.isEmpty()) return null

        val possibleAnswers = wordsToLearn.shuffled().take(variantsOfAnswers)
        val correctIndex = Random.nextInt(0, possibleAnswers.size)
        return Question(
            variants = possibleAnswers,
            correctIndex = correctIndex,
        )
    }
}