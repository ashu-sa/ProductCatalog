package com.spirelab.productcatalog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.spirelab.productcatalog.ui.navigation.CatalogNavHost

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as CatalogApp).container
        setContent {
            MaterialTheme {
                Surface {
                    CatalogNavHost(container = container)
                }
            }
        }
    }
}
