package example.di

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.TextView
import di.GlobalScope
import di.inject
import di.injectByName

class MainActivity : AppCompatActivity() {

    private var scope = MainActivityScope()
    private val presenter: FirstPresenter by inject(scope)
    private val string: String by injectByName("first")

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        scope.init(listOf(mainScreenModule()), GlobalScope)

        val textView = findViewById<TextView>(R.id.text)
        findViewById<View>(R.id.button).setOnClickListener {
            textView.text = "${presenter.getMessage()}\n$string"
        }
    }
}
