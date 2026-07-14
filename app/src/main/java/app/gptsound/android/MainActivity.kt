package app.gptsound.android

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import app.gptsound.android.ui.AppViewModel
import app.gptsound.android.ui.GPTsoundApp
import app.gptsound.android.ui.theme.GPTsoundTheme

class MainActivity : ComponentActivity() {
    private val viewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        viewModel.handleOAuthCallback(intent?.data)
        setContent {
            val state by viewModel.state.collectAsState()
            GPTsoundTheme(state.theme) {
                GPTsoundApp(state = state, viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        viewModel.handleOAuthCallback(intent.data)
    }
}
