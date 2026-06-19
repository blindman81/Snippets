import com.google.gson.Gson

data class Photo(
    val id: String,
    val isPublic: Boolean = false,
    val locationLink: String? = null
)

fun main() {
    val gson = Gson()
    val p = Photo("1", true, "http://maps")
    val json = gson.toJson(p)
    println(json)
    val parsed = gson.fromJson(json, Photo::class.java)
    println(parsed)
}
