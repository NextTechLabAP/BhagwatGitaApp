package com.example.bhagwadgitachatbot.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.navigation.NavHostController
import com.example.bhagwadgitachatbot.ui.theme.customFont
import com.example.bhagwadgitachatbot.api.ChatRequest
import com.example.bhagwadgitachatbot.api.RetrofitInstance

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(navController: NavHostController) {
    var userInput by remember { mutableStateOf("") }
    val chatMessages = remember { mutableStateListOf<Pair<String, Boolean>>() }
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Bhagavad Gita Chatbot",
                        color = Color(0xFFFFD700),
                        fontSize = 22.sp,
                        fontFamily = customFont
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFFFFD700)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1E1E1E)
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(0xFF1E1E1E), Color(0xFF121212))))
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Chat messages area
                Box(modifier = Modifier.weight(1f)) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        reverseLayout = false
                    ) {
                        items(chatMessages) { message ->
                            ChatBubble(message.first, message.second)
                        }
                    }

                    if (isLoading) {
                        Text(
                            text = "AI is thinking...",
                            color = Color.Gray,
                            fontFamily = customFont,
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(8.dp)
                        )
                    }
                }

                // Input field
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF2E2E2E), RoundedCornerShape(16.dp))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = userInput,
                        onValueChange = { userInput = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { 
                            Text(
                                "Ask Bhagavad Gita...", 
                                color = Color.Gray,
                                fontFamily = customFont
                            ) 
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color(0xFFFFD700),
                            unfocusedIndicatorColor = Color.Gray
                        ),
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                sendMessage(
                                    userInput,
                                    chatMessages,
                                    coroutineScope,
                                    { isLoading = it },
                                    { userInput = "" }
                                )
                            }
                        )
                    )

                    IconButton(
                        onClick = {
                            sendMessage(
                                userInput,
                                chatMessages,
                                coroutineScope,
                                { isLoading = it },
                                { userInput = "" }
                            )
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = Color(0xFFFFD700)
                        )
                    }
                }
            }
        }
    }
}

fun sendMessage(
    userInput: String,
    chatMessages: MutableList<Pair<String, Boolean>>,
    coroutineScope: CoroutineScope,
    setLoading: (Boolean) -> Unit,
    clearInput: () -> Unit
) {
    if (userInput.isNotBlank()) {
        chatMessages.add(userInput to true)
        clearInput()

        coroutineScope.launch {
            setLoading(true)
            val aiReply = fetchAIResponse(userInput)
            setLoading(false)
            chatMessages.add(aiReply to false)
        }
    }
}

suspend fun fetchAIResponse(userMessage: String): String {
    return try {
        val response = RetrofitInstance.api.getChatResponse(ChatRequest(userMessage))
        if (response.isSuccessful) {
            response.body()?.response ?: "Sorry, I couldn't process that."
        } else {
            "Sorry, there was an error processing your request."
        }
    } catch (e: Exception) {
        "Sorry, there was an error: ${e.localizedMessage}"
    }
}

@Composable
fun ChatBubble(message: String, isUser: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = if (isUser) Color(0xFF3B5998) else Color(0xFFFFD700),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(12.dp)
        ) {
            Text(
                text = message,
                color = if (isUser) Color.White else Color.Black,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun MenuButton(text: String) {
    Button(
        onClick = { /* Handle menu item click */ },
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start
        )
    }
}
