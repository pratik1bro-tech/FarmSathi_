package com.example.service

import android.content.Context
import android.content.SharedPreferences
import com.example.data.local.FarmSathiDatabase
import com.example.data.local.UserEntity
import com.example.data.models.User
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.regex.Pattern

class AuthManager private constructor(context: Context) {

    private val db = FarmSathiDatabase.getInstance(context)
    private val userDao = db.userDao()
    private val prefs: SharedPreferences = context.getSharedPreferences("farm_sathi_auth_prefs", Context.MODE_PRIVATE)

    private val firebaseAuth: FirebaseAuth? = try {
        FirebaseAuth.getInstance()
    } catch (e: Exception) {
        null
    }

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val emailPattern = Pattern.compile(
        "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}\$"
    )

    init {
        val isLoggedIn = prefs.getBoolean("is_logged_in", false)
        val activeUid = prefs.getString("active_uid", null)

        if (isLoggedIn && !activeUid.isNullOrBlank()) {
            CoroutineScope(Dispatchers.IO).launch {
                val userEntity = userDao.getUserByUidDirect(activeUid)
                if (userEntity != null) {
                    _currentUser.value = userEntity.toDomain()
                    _isAuthenticated.value = true
                } else {
                    // Reset session if saved UID no longer exists locally
                    logout()
                }
            }
        } else {
            _currentUser.value = null
            _isAuthenticated.value = false
        }
    }

    fun isValidEmail(email: String): Boolean {
        return emailPattern.matcher(email.trim()).matches()
    }

    suspend fun login(email: String, pass: String): Result<User> = withContext(Dispatchers.IO) {
        val trimmedEmail = email.trim()
        val trimmedPass = pass.trim()

        if (trimmedEmail.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("Email address cannot be empty."))
        }
        if (!isValidEmail(trimmedEmail)) {
            return@withContext Result.failure(IllegalArgumentException("Please enter a valid email address format (e.g. farmer@domain.com)."))
        }
        if (trimmedPass.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("Password cannot be empty."))
        }
        if (trimmedPass.length < 6) {
            return@withContext Result.failure(IllegalArgumentException("Password must be at least 6 characters long."))
        }

        try {
            var uid: String
            var name: String = trimmedEmail.substringBefore("@").replaceFirstChar { it.uppercase() }

            if (firebaseAuth != null) {
                try {
                    val authResult = firebaseAuth.signInWithEmailAndPassword(trimmedEmail, trimmedPass).await()
                    val fbUser = authResult.user ?: throw Exception("Authentication returned no active user session.")
                    uid = fbUser.uid
                    name = fbUser.displayName ?: name
                } catch (e: Exception) {
                    return@withContext Result.failure(Exception("Authentication failed: ${e.message}"))
                }
            } else {
                uid = "usr_" + trimmedEmail.lowercase().replace("@", "_").replace(".", "_")
            }

            var existing = userDao.getUserByUidDirect(uid)
            val user = if (existing != null) {
                existing.toDomain()
            } else {
                val newUser = User(
                    uid = uid,
                    name = name,
                    email = trimmedEmail,
                    phone = "",
                    preferredLanguage = "Hindi",
                    location = "Madhya Pradesh, India",
                    farmingExperience = "3 Years"
                )
                userDao.insertUser(UserEntity.fromDomain(newUser))
                newUser
            }

            // Save active user session
            prefs.edit()
                .putBoolean("is_logged_in", true)
                .putString("active_uid", user.uid)
                .apply()

            _currentUser.value = user
            _isAuthenticated.value = true
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signup(
        name: String,
        email: String,
        pass: String,
        phone: String,
        language: String,
        location: String,
        experience: String
    ): Result<User> = withContext(Dispatchers.IO) {
        val trimmedName = name.trim()
        val trimmedEmail = email.trim()
        val trimmedPass = pass.trim()

        if (trimmedName.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("Full Name is required."))
        }
        if (trimmedEmail.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("Email address is required."))
        }
        if (!isValidEmail(trimmedEmail)) {
            return@withContext Result.failure(IllegalArgumentException("Please enter a valid email address format."))
        }
        if (trimmedPass.length < 6) {
            return@withContext Result.failure(IllegalArgumentException("Password must be at least 6 characters long for security."))
        }

        try {
            var uid: String

            if (firebaseAuth != null) {
                try {
                    val authResult = firebaseAuth.createUserWithEmailAndPassword(trimmedEmail, trimmedPass).await()
                    val fbUser = authResult.user ?: throw Exception("Registration created no active user.")
                    uid = fbUser.uid
                } catch (e: Exception) {
                    return@withContext Result.failure(Exception("Registration failed: ${e.message}"))
                }
            } else {
                uid = "usr_" + trimmedEmail.lowercase().replace("@", "_").replace(".", "_")
            }

            val newUser = User(
                uid = uid,
                name = trimmedName,
                email = trimmedEmail,
                phone = phone.ifBlank { "+91 98765 43210" },
                preferredLanguage = language.ifBlank { "Hindi" },
                location = location.ifBlank { "Madhya Pradesh, India" },
                farmingExperience = experience.ifBlank { "3 Years" }
            )

            userDao.insertUser(UserEntity.fromDomain(newUser))

            prefs.edit()
                .putBoolean("is_logged_in", true)
                .putString("active_uid", newUser.uid)
                .apply()

            _currentUser.value = newUser
            _isAuthenticated.value = true
            Result.success(newUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resetPassword(email: String): Result<String> = withContext(Dispatchers.IO) {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isEmpty() || !isValidEmail(trimmedEmail)) {
            return@withContext Result.failure(IllegalArgumentException("Please enter a valid email address to receive password reset instructions."))
        }

        try {
            if (firebaseAuth != null) {
                firebaseAuth.sendPasswordResetEmail(trimmedEmail).await()
            }
            Result.success("Password reset instructions have been sent to $trimmedEmail.")
        } catch (e: Exception) {
            Result.failure(Exception("Password reset failed: ${e.message}"))
        }
    }

    suspend fun updateUserProfile(updated: User) = withContext(Dispatchers.IO) {
        userDao.updateUser(UserEntity.fromDomain(updated))
        _currentUser.value = updated
    }

    suspend fun switchUserAccount(targetUser: User) = withContext(Dispatchers.IO) {
        val existing = userDao.getUserByUidDirect(targetUser.uid)
        if (existing == null) {
            userDao.insertUser(UserEntity.fromDomain(targetUser))
        }
        prefs.edit()
            .putBoolean("is_logged_in", true)
            .putString("active_uid", targetUser.uid)
            .apply()

        _currentUser.value = targetUser
        _isAuthenticated.value = true
    }

    fun logout() {
        try {
            firebaseAuth?.signOut()
        } catch (e: Exception) {
            // Ignore
        }
        prefs.edit().clear().apply()
        _currentUser.value = null
        _isAuthenticated.value = false
    }

    companion object {
        @Volatile
        private var INSTANCE: AuthManager? = null

        fun getInstance(context: Context): AuthManager {
            return INSTANCE ?: synchronized(this) {
                val instance = AuthManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
