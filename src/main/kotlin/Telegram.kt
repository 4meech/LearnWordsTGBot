import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json


const val STATISTICS_CLICKED = "statistics_clicked"
const val LEARN_WORDS_CLICKED = "learn_words_clicked"
const val EXIT_CLICKED = "exitToMainMenu"
const val START_USER_INPUT = "/start"
const val STAT_RESET_CLICKED = "stat_reset_clicked"

@Serializable
data class Update(
    @SerialName("update_id")
    val updateId: Long,
    @SerialName("message")
    val message: Message? = null,
    @SerialName("callback_query")
    val callbackQuery: CallbackQuery? = null,
)

@Serializable
data class ReplyMarkup(
    @SerialName("inline_keyboard")
    val inlineKeyboard: List<List<InlineKeyboard>>,
)

@Serializable
data class InlineKeyboard(
    @SerialName("callback_data")
    val callbackData: String,
    @SerialName("text")
    val text: String,
)

@Serializable
data class SendMessageRequest(
    @SerialName("chat_id")
    val chatId: Long?,
    @SerialName("text")
    val text: String,
    @SerialName("reply_markup")
    val replyMarkup: ReplyMarkup,
)

@Serializable
data class Response(
    @SerialName("result")
    val result: List<Update>,
)

@Serializable
data class Message(
    @SerialName("text")
    val text: String,
    @SerialName("chat")
    val chat: Chat,
)

@Serializable
data class CallbackQuery(
    @SerialName("data")
    val data: String,
    @SerialName("message")
    val message: Message? = null,
)

@Serializable
data class Chat(
    @SerialName("id")
    val id: Long,
)

class ReplyMarkupFormatter {
    fun questionFormat(question: Question): ReplyMarkup {
        val variantsButtons = question.variants.mapIndexed { index, word ->
            listOf(
                InlineKeyboard(
                    text = word.originalWord,
                    callbackData = "$CALLBACK_DATA_ANSWER_PREFIX${index + 1}"
                )
            )
        } + listOf(
            listOf(
                InlineKeyboard(text = "⬅\uFE0F Назад", callbackData = EXIT_CLICKED)
            )
        )
        return ReplyMarkup(inlineKeyboard = variantsButtons)
    }

    fun menuFormat(): ReplyMarkup {
        val menuButtons = listOf(
            listOf(
                InlineKeyboard(text = "Учить слова", callbackData = LEARN_WORDS_CLICKED),
                InlineKeyboard(text = "Статистика", callbackData = STATISTICS_CLICKED),
            ),
            listOf(
                InlineKeyboard(text = "Сбросить прогресс", callbackData = STAT_RESET_CLICKED),
            )
        )

        return ReplyMarkup(inlineKeyboard = menuButtons)
    }
}

fun main(args: Array<String>) {

    val telegramBotService = TelegramBotService(botToken = args[0])
    val json = Json { ignoreUnknownKeys = true }
    val trainers = HashMap<Long, LearnWordsTrainer>()

    var lastUpdateId: Long? = 0
    var counter = 0

    while (true) {

        Thread.sleep(2000)
        val response: Response? = try {
            telegramBotService.getUpdates(lastUpdateId)
        } catch (e: Exception) {
            println("Исключение${e.message}"); null
        }

        println(response.toString())
        counter++
        println(counter)

        if (response == null) continue
        if (response.result.isEmpty()) continue

        val sortedUpdates = response.result.sortedBy { it.updateId }
        sortedUpdates.forEach { handleUpdate(it, json, trainers, telegramBotService) }
        lastUpdateId = sortedUpdates.last().updateId + 1
    }
}

fun handleUpdate(
    update: Update,
    json: Json,
    trainers: HashMap<Long, LearnWordsTrainer>,
    telegramBotService: TelegramBotService
) {
    val text = update.message?.text
    val chatId = update.message?.chat?.id ?: update.callbackQuery?.message?.chat?.id ?: return
    val data = update.callbackQuery?.data

    val trainer = trainers.getOrPut(chatId) { LearnWordsTrainer("$chatId.txt", 3, 4) }
    when {
        text == START_USER_INPUT -> {
            telegramBotService.sendMenu(json = json, chatId = chatId)
        }

        data == STATISTICS_CLICKED -> {
            telegramBotService.sendMessage(chatId = chatId, message = trainer.getStatistics().statMessage)
            telegramBotService.sendMenu(json, chatId)
        }

        data == LEARN_WORDS_CLICKED -> {
            telegramBotService.checkNextQuestionAndSend(json = json, trainer = trainer, chatId = chatId)
        }

        data?.startsWith(CALLBACK_DATA_ANSWER_PREFIX) == true -> {
            val userAnswerIndex = data.substringAfter(CALLBACK_DATA_ANSWER_PREFIX).toInt()

            if (trainer.checkAnswer(userAnswerIndex)) {
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
            telegramBotService.checkNextQuestionAndSend(json = json, trainer = trainer, chatId = chatId)
        }

        data == EXIT_CLICKED -> {
            telegramBotService.sendMenu(json, chatId)
        }

        data == STAT_RESET_CLICKED -> {
            trainer.resetProgress()
            telegramBotService.sendMessage(chatId, "Прогресс сброшен")
            telegramBotService.sendMenu(json, chatId)
        }
    }
}