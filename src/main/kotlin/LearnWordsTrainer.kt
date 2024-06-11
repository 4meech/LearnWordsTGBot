import java.io.File

data class Question(
    val variants: List<Word>,
    val correctAnswer: Word,
//    val possibleAnswers: List<Word>,

)

class LearnWordsTrainer {

    val dictionary = loadDictionary()

    fun loadDictionary(): MutableList<Word> {
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
        val learnedWords = dictionary.filter { it.correctAnswersCount >= ANSWERS_TO_LEARN }.size
        val percentageLearned = ((learnedWords.toDouble() / totalWords.toDouble()) * 100.0).toInt()

        return Statistics(totalWords, learnedWords, percentageLearned)
    }

    fun getNextQuestion(): Question? {
        val wordsToLearn = dictionary.filter { it.correctAnswersCount < ANSWERS_TO_LEARN }
        if (wordsToLearn.isEmpty()) return null

        val possibleAnswers = wordsToLearn.shuffled().take(VARIANTS_OF_ANSWERS)
        val correctAnswer = possibleAnswers.random()
        return Question(
            variants = possibleAnswers,
            correctAnswer = correctAnswer,
//            possibleAnswers = possibleAnswers,
        )
    }
}