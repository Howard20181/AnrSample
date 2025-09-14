package com.example.anrsample

import android.content.SharedPreferences
import android.os.Bundle
import android.os.SystemProperties
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.edit
import com.example.anrsample.ui.theme.AnrSampleTheme
import java.math.BigDecimal
import java.math.MathContext

class MainActivity : ComponentActivity() {
    companion object {
        fun checkEnableRescuePartyPlus(): Boolean {
            return SystemProperties.getBoolean("persist.sys.rescuepartyplus.enable", false)
                    && !SystemProperties.getBoolean("persist.sys.rescuepartyplus.disable", false)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val isFirstOpen = prefs.getBoolean("is_first_open", true)
        Log.d("MainActivity", "是否首次打开应用: $isFirstOpen")
        if (isFirstOpen) {
            prefs.edit(commit = true) { putBoolean("is_first_open", false) }
        }
        enableEdgeToEdge()
        setContent {
            AnrSampleTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Howard",
                        isFirstOpen,
                        modifier = Modifier.padding(innerPadding),
                        prefs
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(
    name: String,
    isFirstOpen: Boolean,
    modifier: Modifier = Modifier,
    prefs: SharedPreferences
) {
    var anrTriggered by remember { mutableStateOf(prefs.getBoolean("anr_triggered", false)) }
    var piValue by remember {
        mutableStateOf(
            prefs.getString("pi_value", "0")?.let { BigDecimal(it) } ?: BigDecimal.ZERO
        )
    }
    var denominator by remember {
        mutableStateOf(
            prefs.getString("pi_denominator", "1")?.let { BigDecimal(it) } ?: BigDecimal.ONE
        )
    }
    var sign by remember {
        mutableStateOf(
            prefs.getString("pi_sign", "1")?.let { BigDecimal(it) } ?: BigDecimal.ONE
        )
    }
    var piStep by remember { mutableIntStateOf(prefs.getInt("pi_step", 0)) }
    val mc = MathContext(50)
    var update = 0
    androidx.compose.foundation.layout.Column(modifier = modifier) {
        Text(
            text = "Hello $name!\n首次打开应用: $isFirstOpen\n支持 RescuePartyPlus: ${MainActivity.checkEnableRescuePartyPlus()}\n迭代次数: $piStep\n圆周率: $piValue",
            modifier = modifier
        )
        if (anrTriggered) {
            update++
            LaunchedEffect(piStep) {
                var pi = piValue
                var step = piStep
                var denom = denominator
                var s = sign
                repeat(10_000_000) {
                    val term = s.multiply(BigDecimal(4).divide(denom, mc))
                    pi = pi.add(term)
                    denom = denom.add(BigDecimal(2))
                    s = s.negate()
                    step++
                }
                piValue = pi
                denominator = denom
                sign = s
                piStep = step
                prefs.edit(commit = true) {
                    putString("pi_value", pi.toPlainString())
                    putString("pi_denominator", denom.toPlainString())
                    putString("pi_sign", s.toPlainString())
                    putInt("pi_step", step)
                }
            }
        } else {
            prefs.edit(commit = true) {
                putBoolean("anr_triggered", false)
            }
            Button(onClick = {
                anrTriggered = true
                prefs.edit(commit = true) {
                    putBoolean("anr_triggered", anrTriggered)
                }
                var pi = piValue
                var step = piStep
                var denom = denominator
                var s = sign
                repeat(10_000_000) {
                    val term = s.multiply(BigDecimal(4).divide(denom, mc))
                    pi = pi.add(term)
                    denom = denom.add(BigDecimal(2))
                    s = s.negate()
                    step++
                }
                piValue = pi
                denominator = denom
                sign = s
                piStep = step
                prefs.edit(commit = true) {
                    putString("pi_value", pi.toPlainString())
                    putString("pi_denominator", denom.toPlainString())
                    putString("pi_sign", s.toPlainString())
                    putInt("pi_step", step)
                }
            }) {
                Text("触发ANR")
            }
        }
    }
}
