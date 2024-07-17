import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets

const val URL = "https://api.telegram.org/bot"
const val CALLBACK_DATA_ANSWER_PREFIX = "answer_"

class TelegramBotService(private val botToken: String) {
    private val httpClient = HttpClient.newBuilder().build()
    private val json = Json { ignoreUnknownKeys = true }

    fun getUpdates(updateId: Long?): Response? {
        val urlGetUpdates = "$URL${this.botToken}/getUpdates?offset=$updateId"
        val requestUpdate: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
        val responseUpdate = httpClient.send(requestUpdate, HttpResponse.BodyHandlers.ofString())

        val responseString = responseUpdate.body()

        return try {
            json.decodeFromString(responseString)
        } catch (e: Exception) {
            println("Невозможно распарсить ответ: ${e.message}")
            null
        }
    }

    fun sendMessage(chatId: Long?, message: String) {
        val encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8.toString())
        val urlSendMessage = "$URL$botToken/sendMessage?chat_id=$chatId&text=$encodedMessage"
        val requestUpdate: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMessage)).build()

        httpClient.send(requestUpdate, HttpResponse.BodyHandlers.ofString())
    }

    fun sendMenu(json: Json, chatId: Long?): String {
        val sendMessage = "$URL$botToken/sendMessage"

        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = "Основное меню",
            replyMarkup = ReplyMarkup(
                listOf(listOf(
                    InlineKeyboard(text = "Учить слова", callbackData = LEARN_WORDS_CLICKED),
                    InlineKeyboard(text = "Статистика", callbackData = STATISTICS_CLICKED),
                ),
                    listOf(
                        InlineKeyboard(text = "Сбросить прогресс", callbackData = STAT_RESET_CLICKED),
                    )
                )
            )
        )

        val requestBodyString = json.encodeToString(requestBody)

        val request = HttpRequest.newBuilder().uri(URI.create(sendMessage))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString)).build()

        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    private fun sendQuestion(json: Json, chatId: Long?, question: Question): String {
        val sendMessage = "$URL$botToken/sendMessage"
        val engCorrectAnswerText = question.variants[question.correctIndex].translatedWord

        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = "Выберите верный перевод для слова \"$engCorrectAnswerText\":\n",
            replyMarkup = ReplyMarkup(
                listOf(
                    question.variants.mapIndexed { index, word ->
                        InlineKeyboard(
                            text = word.originalWord,
                            callbackData = "$CALLBACK_DATA_ANSWER_PREFIX${index.plus(1)}"
                        )
                    },
                    listOf(
                        InlineKeyboard(text = "Назад", callbackData = EXIT_CLICKED)
                    )
                )
            )
        )

        val requestBodyString = json.encodeToString(requestBody)

        val request = HttpRequest.newBuilder().uri(URI.create(sendMessage))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString)).build()

        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        println(response.body())
        return response.body()
    }

    fun checkNextQuestionAndSend(json: Json, trainer: LearnWordsTrainer, chatId: Long?) {
        trainer.getNextQuestion()?.let {
            sendQuestion(
                json = json,
                chatId = chatId,
                question = it
            )
        } ?: sendMessage(chatId = chatId, message = "Поздравляем! Вы выучили все слова!")
    }
}