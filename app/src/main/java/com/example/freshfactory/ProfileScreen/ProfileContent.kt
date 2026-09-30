package com.example.freshfactory.ProfileScreen

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.freshfactory.DataBase.UserProfile
import com.example.freshfactory.ui.theme.FreshFactoryTheme
import coil.compose.AsyncImage

data class ProfileCategory(
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit = {}
)

@Composable
fun ProfileContent(viewModel: ProfileViewModel = viewModel()) {
    var isEditing by remember { mutableStateOf(false) }
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()

    if (isEditing) {
        EditProfileContent(
            userProfile = userProfile,
            onSave = { name, phone, email ->
                viewModel.saveProfile(name, phone, email)
                isEditing = false
            },
            onCancel = { isEditing = false }
        )
    } else {
        ProfileMainContent(
            userProfile = userProfile,
            onEditClick = { isEditing = true }
        )
    }
}

@Composable
fun ProfileMainContent(
    userProfile: UserProfile?,
    onEditClick: () -> Unit
) {
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showImagePickerSheet by remember { mutableStateOf(false) }

    val categories = listOf(
        ProfileCategory("Edit Profile", Icons.Default.Person, onEditClick),
        ProfileCategory("My Orders", Icons.Default.ShoppingCart),
        ProfileCategory("My Address", Icons.Default.LocationOn),
        ProfileCategory("Payment Methods", Icons.Default.AccountBox),
        ProfileCategory("Notification", Icons.Default.Notifications),
        ProfileCategory("Help & Support", Icons.Default.Info),
        ProfileCategory("Logout", Icons.AutoMirrored.Filled.ExitToApp)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        // Profile Image
        Box(
            modifier = Modifier.size(120.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                when {
                    selectedImageUri != null -> {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    capturedBitmap != null ->{
                    Image(
                        bitmap = capturedBitmap!!.asImageBitmap(),
                        contentDescription = "Profile Photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    }
                    else -> {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            // Gallery Launcher
            val galleryLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.PickVisualMedia()
            ) { uri: Uri? ->
                uri?.let { selectedImageUri = it }
            }
            // Camera Launcher
            val cameraLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.TakePicturePreview()
            ) { bitmap: Bitmap? ->
                bitmap?.let {
                    capturedBitmap = it
                    selectedImageUri = null
                }
            }

            IconButton(
                onClick = { showImagePickerSheet = true },
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Edit Image",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Render Bottom sheet Dashboard when triggered
            if (showImagePickerSheet) {
                ImagePickerBottomSheet(
                    onDismissRequest = { showImagePickerSheet = false },
                    onCameraSelect = {
                        cameraLauncher.launch(null)
                    },
                    onGallerySelect = {
                        galleryLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onRemoveSelect = {
                        selectedImageUri = null
                    }
                    )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = userProfile?.name ?: "User Name",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = userProfile?.email ?: "username@example.com",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Profile Categories
        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            items(categories) { category ->
                ProfileOptionItem(category)
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImagePickerBottomSheet(
    onDismissRequest:() -> Unit,
    onCameraSelect:() -> Unit,
    onGallerySelect:() -> Unit,
    onRemoveSelect:() -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Select Profile Photo",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            // Take photo
            ListItem(
                headlineContent = {Text("Click Photo") },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Camera",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        onCameraSelect()
                        onDismissRequest()
                    }
            )

            // Choose from Gallery
            ListItem(
                headlineContent = { Text("Choose from Gallery") },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = "Gallery",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        onGallerySelect()
                        onDismissRequest()
                    }
            )

            // Remove Current Photo
            ListItem(
                headlineContent = { Text("Remove Photo", color = MaterialTheme.colorScheme.error) },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove",
                        tint = MaterialTheme.colorScheme.error)
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        onRemoveSelect()
                        onDismissRequest()
                    }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileContent(
    userProfile: UserProfile?,
    onSave: (String, String, String) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf(userProfile?.name ?: "") }
    var phone by remember { mutableStateOf(userProfile?.phoneNumber ?: "") }
    var email by remember { mutableStateOf(userProfile?.email ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone Number") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email ID") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = { onSave(name, phone, email) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Save")
                }
            }
        }
    }
}

@Composable
fun ProfileOptionItem(category: ProfileCategory) {
    ListItem(
        headlineContent = {
            Text(
                text = category.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        },
        leadingContent = {
            Icon(
                imageVector = category.icon,
                contentDescription = category.title,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        trailingContent = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Go",
                tint = MaterialTheme.colorScheme.outline
            )
        },
        modifier = Modifier.clickable { category.onClick() }
    )
}

@Preview(showBackground = true)
@Composable
fun ProfileContentPreview() {
    FreshFactoryTheme {
        Surface {
            ProfileContent()
        }
    }
}
