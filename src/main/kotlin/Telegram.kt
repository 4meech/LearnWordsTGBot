const val STATISTICS_CLICKED = "statistics_clicked"
const val LEARN_WORDS_CLICKED = "learn_words_clicked"


fun main(args: Array<String>) {

    val telegramBotService = TelegramBotService(botToken = args[0])

    var updateId: Int? = 0

    val updateIdRegex = "\"update_id\":(.+?),".toRegex()
    val messageTextRegex = "\"text\":\"(.+?)\"".toRegex()
    val chatIdRegex = "\"chat\":\\{\"id\":(.+?),".toRegex()
    val dataRegex = "\"data\":\"(.+?)\"".toRegex()

    val trainer = LearnWordsTrainer(3, 4)

    while (true) {
        Thread.sleep(2000)
        val updates: String = telegramBotService.getUpdates(updateId)
        println(updates)

        updateId = updateIdRegex.find(updates)?.groups?.get(1)?.value?.toInt()?.plus(1)
        println(updateId)

        val text = messageTextRegex.find(updates)?.groups?.get(1)?.value
        println(text)

        val chatId = chatIdRegex.find(updates)?.groups?.get(1)?.value
        println(chatId)

        val data = dataRegex.find(updates)?.groups?.get(1)?.value
        println(data)

//        if (text != null && chatId != null) {
//            telegramBotService.sendMessage(chatId = chatId, message = text)
//        }

        if (text == "/start" && chatId != null) {
            telegramBotService.sendMenu(chatId = chatId)
        }

        if (data == STATISTICS_CLICKED && chatId != null) {
            telegramBotService.sendMessage(chatId = chatId, message = trainer.getStatisctics().statMessage)
        } else if (data == LEARN_WORDS_CLICKED && chatId != null) {
            trainer.getNextQuestion()?.let {
                telegramBotService.sendQuestion(
                    chatId = chatId,
                    question = it
                )
            } ?: telegramBotService.sendMessage(chatId = chatId, message = "Поздравляем! Вы выучили все слова!")
        }
    }
}



