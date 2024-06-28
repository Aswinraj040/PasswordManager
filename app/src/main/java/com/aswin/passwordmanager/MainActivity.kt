package com.aswin.passwordmanager

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aswin.passwordmanager.ui.theme.MyApplicationTheme

class MainActivity : AppCompatActivity() {
    private lateinit var databaseHelper: DatabaseHelper
    private var res by mutableStateOf("")
    private val promptManager by lazy {
        BiometricPromptManager(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        databaseHelper = DatabaseHelper(this)

        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
                    val navController = rememberNavController()
                    SetupNavGraph(navController, promptManager)
                }
            }
        }
    }
}

@Composable
fun SetupNavGraph(navController: NavHostController, promptManager: BiometricPromptManager) {
    val biometricResult by promptManager.promptResults.collectAsState(
        initial = null
    )
    val enrollLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = {
            println("Activity result: $it")
        }
    )

    LaunchedEffect(biometricResult) {
        when (biometricResult) {
            is BiometricPromptManager.BiometricResult.AuthenticationNotSet -> {
                navController.navigate("password_manager_scene")
                if (Build.VERSION.SDK_INT >= 30) {
                    val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                        putExtra(
                            Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                            BIOMETRIC_STRONG or DEVICE_CREDENTIAL
                        )
                    }
                    enrollLauncher.launch(enrollIntent)
                }
            }
            is BiometricPromptManager.BiometricResult.HardwareUnavailable,
            is BiometricPromptManager.BiometricResult.FeatureUnavailable -> {
                navController.navigate("password_manager_scene")
            }
            is BiometricPromptManager.BiometricResult.AuthenticationSuccess -> {
                navController.navigate("password_manager_scene")
            }
            else -> {
                // Handle other cases if needed
            }
        }
    }

    NavHost(navController = navController, startDestination = "main_screen") {
        composable("main_screen") {
            MainScreen(promptManager, biometricResult)
        }
        composable("password_manager_scene") {
            PasswordManagerScreen(DatabaseHelper(LocalContext.current))
        }
    }
}
@Composable
fun MainScreen(promptManager: BiometricPromptManager, biometricResult: BiometricPromptManager.BiometricResult?) {
    // Track whether biometric prompt has been shown before
    val isFirstTime = remember { mutableStateOf(true) }

    // Effect to show biometric prompt automatically the first time
    LaunchedEffect(isFirstTime.value) {
        if (isFirstTime.value) {
            promptManager.showBiometricPrompt(
                title = "Authenticate",
                description = "Authenticate to enter into the app"
            )
            isFirstTime.value = false  // Mark as shown
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Button to manually trigger biometric prompt (optional)
        Button(
            onClick = {
                promptManager.showBiometricPrompt(
                    title = "Authenticate",
                    description = "Authenticate to enter into the app"
                )
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White)
        ) {
            Text(text = "Authenticate")
        }

        // Display biometric result if available
        biometricResult?.let { result ->
            Text(
                text = when (result) {
                    is BiometricPromptManager.BiometricResult.AuthenticationError -> {
                        result.error
                    }
                    BiometricPromptManager.BiometricResult.AuthenticationFailed -> {
                        "Authentication failed"
                    }
                    BiometricPromptManager.BiometricResult.AuthenticationNotSet -> {
                        "Authentication not set"
                    }
                    BiometricPromptManager.BiometricResult.AuthenticationSuccess -> {
                        "Authentication success"
                    }
                    BiometricPromptManager.BiometricResult.FeatureUnavailable -> {
                        "Feature unavailable"
                    }
                    BiometricPromptManager.BiometricResult.HardwareUnavailable -> {
                        "Hardware unavailable"
                    }
                    else -> "Unknown result"
                }
            )
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordManagerScreen(databaseHelper: DatabaseHelper) {
    val accounts = remember { mutableStateListOf<Account>() }
    val sheetState = rememberModalBottomSheetState()
    val detailsSheetState = rememberModalBottomSheetState()
    val editSheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }
    var showDetailsSheet by remember { mutableStateOf(false) }
    var showEditSheet by remember { mutableStateOf(false) }
    var accountName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var accountNameError by remember { mutableStateOf(false) }
    var usernameError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var usernameErrorTwo by remember { mutableStateOf(false) }
    var passwordErrorTwo by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(true) }
    var selectedAccount by remember { mutableStateOf<Account?>(null) }
    val (showDialog, setShowDialog) = remember { mutableStateOf(false) }
    val icon = if(passwordVisible)
        painterResource(id = R.drawable.ic_eye_off_outline)
    else
        painterResource(id = R.drawable.ic_eye_outline)

    LaunchedEffect(Unit) {
        accounts.addAll(databaseHelper.getAllAccounts())
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showBottomSheet = false
            },
            sheetState = sheetState
        ) {
            // Sheet content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = accountName,
                    onValueChange = { accountName = it },
                    label = { Text("Account Name") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = accountNameError,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color.Black,
                        unfocusedBorderColor = Color.LightGray,
                        cursorColor = Color.Black,
                        focusedLabelColor = Color.Black
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username/ Email") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = usernameError,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color.Black,
                        unfocusedBorderColor = Color.LightGray,
                        cursorColor = Color.Black,
                        focusedLabelColor = Color.Black
                    ),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password= it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = passwordError,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color.Black,
                        unfocusedBorderColor = Color.LightGray,
                        cursorColor = Color.Black,
                        focusedLabelColor = Color.Black
                    ),
                    singleLine = true,
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible}) {
                            Icon(icon , contentDescription = "Password Visibility" , modifier = Modifier.size(24.dp))
                        }
                    },
                    visualTransformation = if(!passwordVisible) VisualTransformation.None
                    else{
                        PasswordVisualTransformation()
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (accountName.isEmpty()) {
                            accountNameError = true
                        } else {
                            accountNameError = false
                        }
                        if (username.isEmpty()) {
                            usernameError = true
                        } else {
                            usernameError = false
                        }
                        if (password.isEmpty()) {
                            passwordError = true
                        } else {
                            passwordError = false
                        }

                        if (!accountNameError && !usernameError && !passwordError) {
                            // Proceed with adding the account
                            scope.launch {
                                val encryptedPassword = EncryptionUtils.encrypt(password)
                                databaseHelper.insertAccount(accountName, username, encryptedPassword)
                                accounts.clear()
                                accounts.addAll(databaseHelper.getAllAccounts())
                                sheetState.hide()
                                showBottomSheet = false
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 40.dp), // Adding bottom margin of 40dp
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White)
                ) {
                    Text("Add New Account")
                }

            }
        }
    }

    if (showDetailsSheet && selectedAccount != null) {
        ModalBottomSheet(
            onDismissRequest = {
                showDetailsSheet = false
            },
            sheetState = detailsSheetState
        ) {
            // Sheet content for account details
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Account Details",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(id = R.color.lb),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text(
                    text = "Account name",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
                Text(
                    text = selectedAccount!!.accountName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text(
                    text = "Username/ Email",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
                Text(
                    text = selectedAccount!!.username,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text(
                    text = "Password",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
                Text(
                    text = selectedAccount!!.password,  // Masked password
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = {
                            accountName = selectedAccount!!.accountName
                            username = selectedAccount!!.username
                            password = selectedAccount!!.password
                            scope.launch { detailsSheetState.hide()
                                editSheetState.show() }
                            showEditSheet = true
                            showDetailsSheet = false
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White)
                    ) {
                        Text("Edit")
                    }
                    Button(
                        onClick = { setShowDialog(true) },
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red, contentColor = Color.White)
                    ) {
                        Text("Delete")
                    }
                }
                Spacer(modifier = Modifier.height(20.dp)) // Spacer to raise the bottom sheet above
            }
        }
    }

    if (showEditSheet && selectedAccount != null) {
        ModalBottomSheet(
            onDismissRequest = {
                showEditSheet = false
            },
            sheetState = editSheetState
        ) {
            // Sheet content for editing account details
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Account name",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
                Text(
                    text = selectedAccount!!.accountName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username/ Email") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = usernameErrorTwo,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color.Black,
                        unfocusedBorderColor = Color.LightGray,
                        cursorColor = Color.Black,
                        focusedLabelColor = Color.Black
                    ),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = passwordErrorTwo,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color.Black,
                        unfocusedBorderColor = Color.LightGray,
                        cursorColor = Color.Black,
                        focusedLabelColor = Color.Black
                    ),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        scope.launch {
                            if (username.isEmpty()) {
                                usernameErrorTwo = true
                            } else {
                                usernameErrorTwo = false
                            }
                            if (password.isEmpty()) {
                                passwordErrorTwo = true
                            } else {
                                passwordErrorTwo = false
                            }
                            if (!usernameErrorTwo && !passwordErrorTwo) {
                                val encryptedPassword = EncryptionUtils.encrypt(password)
                                selectedAccount?.let {
                                    databaseHelper.updateAccount(
                                        accountName,
                                        username,
                                        encryptedPassword
                                    )
                                    accounts.clear()
                                    accounts.addAll(databaseHelper.getAllAccounts())
                                }
                                editSheetState.hide()
                                showEditSheet = false
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 40.dp), // Adding bottom margin of 40dp
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White)
                ) {
                    Text("Save Changes")
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { setShowDialog(false) },
            title = { Text(text = "Confirm Deletion") },
            text = { Text("Are you sure you want to delete this account?") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            databaseHelper.deleteAccount(selectedAccount!!.username)
                            accounts.clear()
                            accounts.addAll(databaseHelper.getAllAccounts())
                            setShowDialog(false)
                            detailsSheetState.hide()
                            showDetailsSheet = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White)
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                Button(
                    onClick = { setShowDialog(false) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White)

                ) {
                    Text("No")
                }
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    showBottomSheet = true
                    accountName = ""
                    username = ""
                    password = ""
                    scope.launch { sheetState.show() } // Show the bottom sheet
                },
                shape = RoundedCornerShape(12.dp),
                containerColor = colorResource(id = R.color.lb),
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add")
            }
        }
    ) { contentPadding ->
        Column(modifier = Modifier
            .padding(contentPadding)
            .padding(16.dp)) {
            Text(
                text = "Password Manager",
                fontSize = 24.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            accounts.forEach { account ->
                AccountItem(account) {
                    selectedAccount = account
                    showDetailsSheet = true
                    scope.launch { detailsSheetState.show() } // Show the details sheet
                }
            }
        }
    }
}
@Composable
fun AccountItem(account: Account, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(
            defaultElevation = 10.dp
        ),
        colors =  CardDefaults.cardColors(containerColor = Color.White , contentColor = Color.Black),
        shape = RoundedCornerShape(15.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = account.accountName,
                fontSize = 20.sp,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = maskPassword(account.password),
                fontSize = 20.sp,
                modifier = Modifier.padding(end = 10.dp)
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.Black
            )
        }
    }
}
fun maskPassword(password: String): String {
    // Replace each character with *
    return "*".repeat(password.length)
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MyApplicationTheme {
        PasswordManagerScreen(DatabaseHelper(LocalContext.current))
    }
}
