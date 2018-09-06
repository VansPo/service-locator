package example.di

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.TextView
import di.GlobalScope
import di.inject

class MainActivity : AppCompatActivity() {

    private lateinit var scope: MainActivityScope
    private val presenter: FirstPresenter by inject(scope)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        scope.init(listOf(mainScreenModule()), GlobalScope)

        val textView = findViewById<TextView>(R.id.text)
        findViewById<View>(R.id.button).setOnClickListener {
            textView.text = presenter.getMessage()
        }
    }
}
