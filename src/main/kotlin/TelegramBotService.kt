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

class TelegramBotService(private val botToken: String, private val json: Json) {
    private val httpClient = HttpClient.newBuilder().build()
    private val replyMarkupFormatter = ReplyMarkupFormatter()

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

    fun sendMessage(chatId: Long, message: String) {
        val encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8.toString())
        val urlSendMessage = "$URL$botToken/sendMessage?chat_id=$chatId&text=$encodedMessage"
        val requestUpdate: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMessage)).build()

        httpClient.send(requestUpdate, HttpResponse.BodyHandlers.ofString())
    }

    fun sendMenu(chatId: Long): String {
        val sendMessageUrl = "$URL$botToken/sendMessage"

        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = "Основное меню",
            replyMarkup = replyMarkupFormatter.menuFormat()
        )

        val requestBodyString = json.encodeToString(requestBody)

        val request = HttpRequest.newBuilder().uri(URI.create(sendMessageUrl))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString)).build()

        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    private fun sendQuestion(chatId: Long?, question: Question): String {
        val sendMessage = "$URL$botToken/sendMessage"
        val engCorrectAnswerText = question.variants[question.correctIndex].translatedWord


        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = "Выберите верный перевод для слова \"$engCorrectAnswerText\":\n",
            replyMarkup = replyMarkupFormatter.questionFormat(question)
        )

        val requestBodyString = json.encodeToString(requestBody)

        val request = HttpRequest.newBuilder().uri(URI.create(sendMessage))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString)).build()

        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        println(response.body())
        return response.body()
    }

    fun checkNextQuestionAndSend(trainer: LearnWordsTrainer, chatId: Long) {
        trainer.getNextQuestion()?.let {
            sendQuestion(
                chatId = chatId,
                question = it
            )
        } ?: sendMessage(chatId = chatId, message = "Поздравляем! Вы выучили все слова!")
    }
}