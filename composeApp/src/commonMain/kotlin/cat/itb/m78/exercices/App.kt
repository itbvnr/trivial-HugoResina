import androidx.compose.foundation.layout.*

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key.Companion.Window
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay

//fun main() = application{
//    Window(
//        title = "Trivial App",
//        state = rememberWindowState(width = 540.dp, height = 960.dp),
//        onCloseRequest = ::exitApplication,
//    ) {
//        TrivialApp()
//    }
//}

enum class EScreen {
    Menu, Game, Result, Settings
}


@Composable
fun MenuScreen(onStartGame: () -> Unit, onSettings: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(onClick = onStartGame, modifier = Modifier.padding(8.dp)) {
            Text("Start Game")
        }
        Button(onClick = onSettings, modifier = Modifier.padding(8.dp)) {
            Text("Settings")
        }
    }
}
@Composable
fun TrivialApp(gameViewModel: GameViewModel = viewModel()) {
    var currentScreen by remember { mutableStateOf(EScreen.Menu) }

    when (currentScreen) {
        EScreen.Menu -> MenuScreen(onStartGame = { currentScreen = EScreen.Game }, onSettings = { currentScreen = EScreen.Settings })
        EScreen.Game -> GameScreen(onGameEnd = { currentScreen = EScreen.Result }, viewModel = gameViewModel)
        EScreen.Result -> ResultScreen(score = gameViewModel.score, onBackToMenu = { currentScreen = EScreen.Menu }, viewModel = gameViewModel)
        EScreen.Settings -> SettingsScreen(onBack = { currentScreen = EScreen.Menu }, viewModel = gameViewModel)
    }
}

@Composable
fun GameScreen(onGameEnd: () -> Unit, viewModel: GameViewModel) {
    val question = viewModel.questions.getOrNull(viewModel.currentQuestionIndex)
    var isAnswerSelected by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel.timeLeft) {
        if (viewModel.timeLeft > 0 && !viewModel.isGameOver) {
            delay(1000)
            viewModel.timeLeft -= 1
        } else if (viewModel.timeLeft <= 0 && !viewModel.isGameOver) {
            if (viewModel.currentQuestionIndex < viewModel.settings.rounds - 1) {
                viewModel.currentQuestionIndex++
                viewModel.timeLeft = viewModel.settings.timePerRound
            } else {
                onGameEnd()
            }
        }
    }

    val progress = (viewModel.timeLeft.toFloat() / viewModel.settings.timePerRound).coerceIn(0f, 1f)
    val currentRound = viewModel.currentQuestionIndex + 1
    val totalRounds = viewModel.settings.rounds

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Time Left: ${viewModel.timeLeft}", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(16.dp))

        question?.let {
            Text(it.text, style = MaterialTheme.typography.bodyLarge, overflow = TextOverflow.Ellipsis, maxLines = 2)
            Spacer(modifier = Modifier.height(16.dp))

            it.options.forEach { option ->
                Button(
                    onClick = {
                        if (!isAnswerSelected) {
                            viewModel.answerQuestion(option)
                            isAnswerSelected = true
                        }
                    },
                    modifier = Modifier.padding(8.dp),
                    enabled = !isAnswerSelected
                ) {
                    Text(option)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Round $currentRound of $totalRounds", style = MaterialTheme.typography.bodyLarge)
        }
    }


    if (isAnswerSelected) {
        LaunchedEffect(Unit) {
            delay(500)
            isAnswerSelected = false
        }
    }
}

@Composable
fun ResultScreen(score: Int, onBackToMenu: () -> Unit, viewModel: GameViewModel) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Your Score: $score", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            viewModel.resetGame()
            onBackToMenu()
        }) {
            Text("Back to Menu")
        }
    }
}

@Composable
fun SettingsScreen(onBack: () -> Unit, viewModel: GameViewModel) {
    var difficulty by remember { mutableStateOf(viewModel.settings.difficulty) }
    var rounds by remember { mutableStateOf(viewModel.settings.rounds.toString()) }
    var timePerRound by remember { mutableStateOf(viewModel.settings.timePerRound) }

    var isDifficultyDropdownExpanded by remember { mutableStateOf(false) }
    var isRoundsDropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(16.dp))


        Text("Difficulty", style = MaterialTheme.typography.bodyLarge)
        Box {
            Button(onClick = { isDifficultyDropdownExpanded = true }) {
                Text(difficulty)
            }
            DropdownMenu(
                expanded = isDifficultyDropdownExpanded,
                onDismissRequest = { isDifficultyDropdownExpanded = false }
            ) {
                listOf("Easy", "Normal", "Hard").forEach { level ->
                    DropdownMenuItem(text ={
                        Text(level)
                    },
                        onClick = {
                            difficulty = level
                            isDifficultyDropdownExpanded = false
                        })
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))


        Text("Rounds", style = MaterialTheme.typography.bodyLarge)
        Box {
            Button(onClick = { isRoundsDropdownExpanded = true }) {
                Text(rounds)
            }
            DropdownMenu(
                expanded = isRoundsDropdownExpanded,
                onDismissRequest = { isRoundsDropdownExpanded = false }
            ) {

                listOf(5, 10, 15).forEach { round ->
                    DropdownMenuItem(text = {
                        Text(round.toString())
                    },

                        onClick = {
                            rounds = round.toString()
                            isRoundsDropdownExpanded = false
                        })
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))


    Text("Time Per Round (seconds)", style = MaterialTheme.typography.bodyLarge)
    Slider(
        value = timePerRound.toFloat(),
        onValueChange = { timePerRound = it.toInt() },
        valueRange = 10f..60f,
        steps = 50

    )
    Text("${timePerRound} sec", style = MaterialTheme.typography.bodyLarge)

    Spacer(modifier = Modifier.height(16.dp))


    Button(onClick = {
        viewModel.updateSettings(
            difficulty = difficulty,
            rounds = rounds.toIntOrNull() ?: viewModel.settings.rounds,
            timePerRound = timePerRound
        )
        onBack()
    }) {
        Text("Save")
    }

    Spacer(modifier = Modifier.height(8.dp))


    Button(onClick = onBack) {
        Text("Back")
    }
}


class GameViewModel : ViewModel() {
    var score by mutableStateOf(0)
    var currentQuestionIndex by mutableStateOf(0)
    var timeLeft by mutableStateOf(30)
    var settings by mutableStateOf(GameSettings("Easy", 15, 30))
    val questions: List<Question> get() = when (settings.difficulty) {
        "Easy" -> easyQuestions
        "Normal" -> normalQuestions
        "Hard" -> hardQuestions
        else -> easyQuestions
    }

    val isGameOver get() = currentQuestionIndex >= settings.rounds || timeLeft <= 0

    fun answerQuestion(answer: String) {
        val currentQuestion = questions[currentQuestionIndex]
        if (answer == currentQuestion.correctAnswer) {
            score++
        }
        if (currentQuestionIndex < settings.rounds - 1) {
            currentQuestionIndex++
            timeLeft = settings.timePerRound
        } else {
            timeLeft = 0
        }
    }

    fun updateSettings(difficulty: String, rounds: Int, timePerRound: Int) {
        settings = GameSettings(difficulty, rounds, timePerRound)
        resetGame()
    }

    fun resetGame() {
        score = 0
        currentQuestionIndex = 0
        timeLeft = settings.timePerRound
    }
}

data class Question(val text: String, val options: List<String>, val correctAnswer: String)

data class GameSettings(var difficulty: String, var rounds: Int, var timePerRound: Int)

val easyQuestions = List(15) {
    Question("Easy Question #$it", listOf("Option 1", "Option 2", "Option 3", "Option 4"), "Option 1")
}

val normalQuestions = List(15) {
    Question("Normal Question #$it", listOf("Option 1", "Option 2", "Option 3", "Option 4"), "Option 2")
}

val hardQuestions = List(15) {
    Question("Hard Question #$it", listOf("Option 1", "Option 2", "Option 3", "Option 4"), "Option 3")
}