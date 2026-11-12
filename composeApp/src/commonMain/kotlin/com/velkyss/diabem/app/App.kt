package com.velkyss.diabem.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.velkyss.diabem.app.resources.Res
import com.velkyss.diabem.app.resources.hello_body
import com.velkyss.diabem.app.resources.hello_title
import com.velkyss.diabem.app.resources.medical_notice
import org.jetbrains.compose.resources.stringResource

@Composable
fun App() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                Text(stringResource(Res.string.hello_title), fontSize = 32.sp, lineHeight = 40.sp)
                Text(stringResource(Res.string.hello_body), fontSize = 22.sp, lineHeight = 30.sp)
                Text(stringResource(Res.string.medical_notice), fontSize = 20.sp, lineHeight = 28.sp)
            }
        }
    }
}
