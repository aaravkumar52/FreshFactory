package com.example.freshfactory.ProfileScreen

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.freshfactory.DataBase.AppDatabase
import com.example.freshfactory.DataBase.UserProfile
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).dao

    val userProfile: StateFlow<UserProfile?> = dao.getProfile().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun saveProfile(name: String, phone: String, email: String) {
        viewModelScope.launch {
            dao.upsertProfile(
                UserProfile(
                    name = name,
                    phoneNumber = phone,
                    email = email
                )
            )
        }
    }
}
