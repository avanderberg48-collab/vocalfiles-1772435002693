package com.vocal.filemanager

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) 
        checkPermissions()
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(primary = Color(0xFFFFEB3B), onPrimary = Color.Black)) {
                FileManagerScreen()
            }
        }
    }

    private fun checkPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            }
        } else {
            val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                registerForActivityResult(ActivityResultContracts.RequestPermission()) {}.launch(permission)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileManagerScreen() {
    var currentPath by remember { mutableStateOf(Environment.getExternalStorageDirectory()) }
    val files = currentPath.listFiles()?.toList()?.sortedByDescending { it.isDirectory } ?: emptyList()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vocal Files Manager", fontSize = 24.sp) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black, titleContentColor = Color.Yellow)
            )
        }
    ) {
        Column(modifier = Modifier.padding(it).fillMaxSize().background(Color.Black)) {
            Button(
                onClick = { if (currentPath.parentFile != null) currentPath = currentPath.parentFile!! },
                modifier = Modifier.fillMaxWidth().padding(8.dp).height(80.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333))
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.Yellow)
                Spacer(Modifier.width(16.dp))
                Text("Go Back Up", fontSize = 22.sp, color = Color.Yellow)
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(files) { file ->
                    FileItem(file) { 
                        if (file.isDirectory) currentPath = file 
                        else Toast.makeText(null, "Opening file: ${file.name}", Toast.LENGTH_SHORT).show()
                    }
                    HorizontalDivider(color = Color.Gray, thickness = 1.dp)
                }
            }
        }
    }
}

@Composable
fun FileItem(file: File, onClick: () -> Unit) {
    val description = if (file.isDirectory) "Directory: ${file.name}" else "File: ${file.name}"
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable(onClickLabel = "Open ${file.name}") { onClick() }
            .semantics { contentDescription = description }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (file.isDirectory) Icons.Default.Folder else Icons.Default.Description,
            contentDescription = null,
            tint = if (file.isDirectory) Color(0xFFFFEB3B) else Color.White,
            modifier = Modifier.size(48.dp)
        )
        Spacer(Modifier.width(20.dp))
        Column {
            Text(file.name, fontSize = 20.sp, color = Color.White, maxLines = 1)
            Text(
                if (file.isDirectory) "Folder" else "${file.length() / 1024} KB",
                fontSize = 16.sp,
                color = Color.LightGray
            )
        }
    }
}