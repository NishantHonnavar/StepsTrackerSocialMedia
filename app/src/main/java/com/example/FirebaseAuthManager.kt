package com.example

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private const val TAG = "FirebaseAuthManager"

object FirebaseAuthManager {

    val auth: FirebaseAuth?
        get() = try {
            Firebase.auth
        } catch (e: Exception) {
            null
        }

    val currentUser: FirebaseUser?
        get() = try {
            auth?.currentUser
        } catch (e: Exception) {
            null
        }

    /**
     * Reactive Kotlin Flow observing Firebase Auth state changes.
     */
    fun authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
        val currentAuth = auth
        if (currentAuth == null) {
            trySend(null)
            awaitClose { }
            return@callbackFlow
        }
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser)
        }
        currentAuth.addAuthStateListener(listener)
        awaitClose { currentAuth.removeAuthStateListener(listener) }
    }

    /**
     * Silent background auto sign-in attempt on app launch.
     */
    fun attemptAutoSignIn(
        context: Context,
        scope: CoroutineScope,
        onSuccess: (FirebaseUser) -> Unit = {},
        onUnauthenticated: () -> Unit = {}
    ) {
        val user = currentUser
        if (user != null) {
            onSuccess(user)
            return
        }

        val clientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            Log.w(TAG, "default_web_client_id string not found in resources", e)
            onUnauthenticated()
            return
        }

        val credentialManager = CredentialManager.create(context)
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(clientId)
            .setAutoSelectEnabled(true)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        scope.launch {
            try {
                val result = credentialManager.getCredential(context, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    val currentAuth = auth
                    if (currentAuth == null) {
                        onUnauthenticated()
                        return@launch
                    }
                    val authResult = currentAuth.signInWithCredential(authCredential).await()
                    authResult.user?.let(onSuccess) ?: onUnauthenticated()
                } else {
                    onUnauthenticated()
                }
            } catch (e: Exception) {
                Log.d(TAG, "Silent auto-sign-in skipped or no stored credentials: ${e.message}")
                onUnauthenticated()
            }
        }
    }

    /**
     * Interactive Google Sign-In triggered by user clicking 'Sign in with Google'.
     */
    fun signInWithGoogle(
        activity: Activity,
        scope: CoroutineScope,
        onSuccess: (FirebaseUser) -> Unit,
        onError: (String) -> Unit,
        onCancelled: () -> Unit = {}
    ) {
        val currentAuth = auth
        if (currentAuth == null) {
            onError("Firebase is not initialized")
            return
        }

        val clientId = try {
            activity.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            val msg = "Google Sign-In configuration missing: default_web_client_id not found"
            Log.e(TAG, msg, e)
            onError(msg)
            return
        }

        val credentialManager = CredentialManager.create(activity)
        val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInOption)
            .build()

        scope.launch {
            try {
                val result = credentialManager.getCredential(activity, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    val authResult = currentAuth.signInWithCredential(authCredential).await()
                    authResult.user?.let(onSuccess) ?: onError("Failed to obtain signed-in Firebase user")
                } else {
                    onError("Unexpected credential format received")
                }
            } catch (e: GetCredentialCancellationException) {
                Log.w(TAG, "Google Sign-In dismissed by user: ${e.message}")
                onCancelled()
            } catch (e: Exception) {
                Log.e(TAG, "Google Sign-In failed", e)
                onError(e.localizedMessage ?: "Sign in failed")
            }
        }
    }

    /**
     * Signs out of Firebase and clears Credential Manager session state.
     */
    fun signOut(
        context: Context,
        scope: CoroutineScope,
        onComplete: () -> Unit
    ) {
        auth?.signOut()
        val credentialManager = CredentialManager.create(context)
        scope.launch {
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.e(TAG, "Failed to clear credential manager state", e)
            } finally {
                onComplete()
            }
        }
    }
}
