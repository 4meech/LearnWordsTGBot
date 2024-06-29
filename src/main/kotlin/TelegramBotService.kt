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

    fun getUpdates(updateId: Int?): String {
        val urlGetUpdates = "$URL${this.botToken}/getUpdates?offset=$updateId"
        val requestUpdate: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
        val responseUpdate = httpClient.send(requestUpdate, HttpResponse.BodyHandlers.ofString())

        return responseUpdate.body()
    }

    fun sendMessage(chatId: String, message: String) {
        val encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8.toString())
        val urlSendMessage = "$URL$botToken/sendMessage?chat_id=$chatId&text=$encodedMessage"
        val requestUpdate: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMessage)).build()

        httpClient.send(requestUpdate, HttpResponse.BodyHandlers.ofString())
    }

    fun sendMenu(chatId: String): String {
        val sendMessage = "$URL$botToken/sendMessage"

        val sendMenuBody = """
            {
              "chat_id": $chatId,
              "text": "Основное меню",
              "reply_markup": {
                "inline_keyboard": [
                  [
                    {
                      "text": "Учить слова",
                      "callback_data": "learn_words_clicked"
                    },
                    {
                      "text": "Статистика",
                      "callback_data": "statistics_clicked"
                    }
                  ]
                ]
              }
            }
        """.trimIndent()


        val request = HttpRequest.newBuilder().uri(URI.create(sendMessage))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(sendMenuBody)).build()

        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    private fun sendQuestion(chatId: String, question: Question): String {
            val sendMessage = "$URL$botToken/sendMessage"
            val engCorrectAnswerText = question.variants[question.correctIndex].translatedWord

            val answerVariantsJson = question.variants.mapIndexed { index: Int, word: Word ->
                """
                    {
                    "text": "${word.originalWord}",
                    "callback_data": "${CALLBACK_DATA_ANSWER_PREFIX}${index + 1}"
                    }
                """.trimIndent()
            }.joinToString(",")

            val sendQuestionBody = """
                {
                      "chat_id": $chatId,
                      "text": "Выберите верный перевод для слова \"$engCorrectAnswerText\":\n",
                      "reply_markup": {
                        "inline_keyboard": [
                          [
                            $answerVariantsJson
                          ]
                        ]
                      }
                }
            """.trimIndent()

        val request = HttpRequest.newBuilder().uri(URI.create(sendMessage))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(sendQuestionBody)).build()

        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        println(response.body())
        return response.body()
    }

    fun checkNextQuestionAndSend(trainer: LearnWordsTrainer, chatId: String) {
        trainer.getNextQuestion()?.let {
            sendQuestion(
                chatId = chatId,
                question = it
            )
        } ?: sendMessage(chatId = chatId, message = "Поздравляем! Вы выучили все слова!")
    }
}