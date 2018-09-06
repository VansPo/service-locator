package example.di

import android.content.Context
import android.util.Log
import java.util.Random

interface Repository {

    fun provideHelloWorldString(): String
}

class StringRepository(
    context: Context,
    private val random: Random,
    private val header: String
) : Repository {

    private val strings: List<String> =
        listOf(
            "hello",
            "world",
            "di is fun",
            "service locators are too",
            context.getString(R.string.app_name)
        )

    init {
        Log.d("DI", "${StringRepository::class} init was called!")
    }

    override fun provideHelloWorldString(): String =
        "$header: ${strings[random.nextInt(strings.size)]}"
}
