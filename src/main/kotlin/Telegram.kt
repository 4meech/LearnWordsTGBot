fun main(args: Array<String> ) {

    val telegramBotService = TelegramBotService()

    val botToken = args[0]
    var updateId: Int? = 0

    while (true) {
        Thread.sleep(2000)
        val updates: String = telegramBotService.getUpdates(botToken, updateId)
        println(updates)

        val updateIdRegex = "\"update_id\":(.+?),".toRegex()
        val updateIdMatchResult: MatchResult? = updateIdRegex.find(updates)
        val idGroups = updateIdMatchResult?.groups
        updateId = idGroups?.get(1)?.value?.toInt()?.plus(1)
        println(updateId)

        val messageTextRegex = "\"text\":\"(.+?)\"".toRegex()
        val textMatchResult: MatchResult? = messageTextRegex.find(updates)
        val textGroups = textMatchResult?.groups
        val text = textGroups?.get(1)?.value
        println(text)

        val chatIdRegex = "\"chat\":\\{\"id\":(.+?),".toRegex()
        val chatIdMatchResult: MatchResult? = chatIdRegex.find(updates)
        val chatIdGroups = chatIdMatchResult?.groups
        val chatId = chatIdGroups?.get(1)?.value
        println(chatId)

        if (text != null && chatId != null) {
            telegramBotService.sendMessage(botToken, chatId, text)
        }
    }
}



