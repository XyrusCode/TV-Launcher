package xyruscode.tv.launcher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import xyruscode.tv.launcher.ui.App
import xyruscode.tv.launcher.ui.theme.TvLauncherTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TvLauncherTheme {
                App()
            }
        }
    }
}
