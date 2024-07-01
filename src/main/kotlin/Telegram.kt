const val STATISTICS_CLICKED = "statistics_clicked"
const val LEARN_WORDS_CLICKED = "learn_words_clicked"
const val EXIT_CLICKED = "exitToMainMenu"
const val START_USER_INPUT = "/start"

fun main(args: Array<String>) {

    val telegramBotService = TelegramBotService(botToken = args[0])

    var updateId: Int? = 0

    val updateIdRegex = "\"update_id\":(.+?),".toRegex()
    val messageTextRegex = "\"text\":\"(.+?)\"".toRegex()
    val chatIdRegex = "\"chat\":\\{\"id\":(.+?),".toRegex()
    val dataRegex = "\"data\":\"(.+?)\"".toRegex()

    val trainer = LearnWordsTrainer(3, 4)

    telegramBotService.sendMenu(chatId = "")
    while (true) {
        Thread.sleep(2000)
        val updates: String = telegramBotService.getUpdates(updateId)
        println(updates)

        updateId = updateIdRegex.find(updates)?.groups?.get(1)?.value?.toInt()?.plus(1)
        println(updateId)

        val text = messageTextRegex.find(updates)?.groups?.get(1)?.value
        println(text)

        val chatId = chatIdRegex.find(updates)?.groups?.get(1)?.value ?: continue
        println(chatId)

        val data = dataRegex.find(updates)?.groups?.get(1)?.value
        println(data)

        when {
            text == START_USER_INPUT -> {
                telegramBotService.sendMenu(chatId = chatId)
            }
            data == STATISTICS_CLICKED -> {
                telegramBotService.sendMessage(chatId = chatId, message = trainer.getStatisctics().statMessage)
            }
            data == LEARN_WORDS_CLICKED -> {
                telegramBotService.checkNextQuestionAndSend(trainer = trainer, chatId = chatId)
            }
            data != null && data.startsWith(CALLBACK_DATA_ANSWER_PREFIX) -> {
                val userAnswerIndex = data.substringAfter(CALLBACK_DATA_ANSWER_PREFIX).toInt()

                if (trainer.checkAnswer(trainer.currentQuestion, userAnswerIndex)) {
                    telegramBotService.sendMessage(chatId, "Верно!")
                } else {
                    telegramBotService.sendMessage(chatId, "Неверно! " +
                            "Верный ответ: ${
                                trainer.currentQuestion?.correctIndex?.let {
                                    trainer.currentQuestion?.variants?.get(
                                        it
                                    )?.originalWord
                                }
                            }"
                    )
                }
                telegramBotService.checkNextQuestionAndSend(trainer = trainer, chatId = chatId)
            }
            data == EXIT_CLICKED -> {
                telegramBotService.sendMenu(chatId)
            }
        }
    }
}