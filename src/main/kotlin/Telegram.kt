import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

fun main() {

    val botToken = "7014989361:AAHb9K9ZfRX4D7_0d591nfx5osWrbcczMh8"
    var updateId = 0

    while (true) {
        Thread.sleep(2000)
        val updates: String = getUpdates(botToken, updateId)
        println(updates)

        val startUpdateId: Int = updates.lastIndexOf("update_id")
        val endUpdateId: Int = updates.lastIndexOf(",\n\"message\"")
        if (startUpdateId == -1 || endUpdateId == -1) continue
        val updateIdString: String = updates.substring(startUpdateId + 11, endUpdateId)
        updateId = updateIdString.toInt() + 1
    }

}

fun getUpdates(botToken: String, updateId: Int): String {
    val urlGetUpdates = "https://api.telegram.org/bot$botToken/getUpdates?offset=$updateId"

    val client: HttpClient = HttpClient.newBuilder().build()
    val requestUpdate: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
    val responseUpdate = client.send(requestUpdate, HttpResponse.BodyHandlers.ofString())

    return responseUpdate.body()
}