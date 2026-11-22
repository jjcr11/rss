package com.jjcr11.rss.data.remote.datasource

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.Flow

open class AuthRemoteDataSource(
    private val supabaseClient: SupabaseClient,
) {
    open val sessionStatus: Flow<SessionStatus>
        get() = supabaseClient.auth.sessionStatus

    open suspend fun signUp(email: String, password: String) {
        supabaseClient.auth.signUpWith(provider = Email) {
            this.email = email
            this.password = password
        }
    }
}
