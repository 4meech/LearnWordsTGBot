import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets

const val URL = "https://api.telegram.org/bot"

class TelegramBotService(private val botToken: String){
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
}