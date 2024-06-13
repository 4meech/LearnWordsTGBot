import java.io.File

class LearnWordsTrainer(private val answersToLearn: Int, private val variantsOfAnswers: Int) {

    private val dictionary = loadDictionary()

    private fun loadDictionary(): MutableList<Word> {
        try {
            val dictionary = mutableListOf<Word>()
            val wordsFile = File("words.txt")
            wordsFile.readLines().forEach { wordsLine ->
                val parsedWords = wordsLine.split("|")
                val word = Word(parsedWords[0], parsedWords[1], parsedWords[2].toIntOrNull() ?: 0)

                dictionary.add(word)
            }
            return dictionary
        } catch (e: IndexOutOfBoundsException) {
            throw IllegalStateException("Некорректный файл .\\words.txt")
        }
    }

    private fun saveDictionary(dictionary: MutableList<Word>) {
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

        val possibleAnswers = if (wordsToLearn.size < variantsOfAnswers) {
            val wordsLearned = dictionary.filter { it.correctAnswersCount >= answersToLearn }.shuffled()
            wordsToLearn.shuffled().take(variantsOfAnswers) +
                    wordsLearned.take(variantsOfAnswers - wordsToLearn.size)
        } else {
            wordsToLearn.shuffled().take(variantsOfAnswers)
        }.shuffled()

        val correctIndex = possibleAnswers.indices.random()

        return Question(
            variants = possibleAnswers,
            correctIndex = correctIndex,
        )
    }

    fun checkAnswer(question: Question?, userAnswer: Int): Boolean {
        return question?.let {
            val correctIndex = it.correctIndex

            if (userAnswer == correctIndex + 1) {
                it.variants[correctIndex].correctAnswersCount++
                saveDictionary(dictionary)
                true
            } else {
                false
            }
        } ?: false
    }
}