package com.dma.finance.data.sync

import com.dma.finance.data.remote.FirestoreProjectSync
import com.dma.finance.data.repository.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Démarre et arrête l'écoute Firestore temps réel des projets selon l'état de connexion :
 * dès qu'un utilisateur est connecté, ses projets partagés se synchronisent en continu ;
 * à la déconnexion, l'écoute s'arrête. Démarré une seule fois au lancement de l'application
 * (voir [com.dma.finance.FinanceApp]).
 */
@Singleton
class SyncCoordinator @Inject constructor(
    private val authRepository: AuthRepository,
    private val firestoreProjectSync: FirestoreProjectSync
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun start() {
        scope.launch {
            authRepository.currentUser.collectLatest { user ->
                if (user != null) {
                    firestoreProjectSync.startListening()
                } else {
                    firestoreProjectSync.stopListening()
                }
            }
        }
    }
}
