/*
 *
 *  *  Copyright 2026 The Hyve
 *  *
 *  *  Licensed under the Apache License, Version 2.0 (the "License");
 *  *  you may not use this file except in compliance with the License.
 *  *  You may obtain a copy of the License at
 *  *
 *  *    http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *  Unless required by applicable law or agreed to in writing, software
 *  *  distributed under the License is distributed on an "AS IS" BASIS,
 *  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *  See the License for the specific language governing permissions and
 *  *  limitations under the License.
 *
 */

package org.radarbase.datadashboard.backend.util

import jakarta.ws.rs.container.AsyncResponse
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import org.radarbase.jersey.service.AsyncCoroutineService
import kotlin.time.Duration

class MockAsyncCoroutineService : AsyncCoroutineService {
    override fun <T> runAsCoroutine(
        asyncResponse: AsyncResponse,
        timeout: Duration,
        block: suspend () -> T,
    ) {
        runBlocking {
            val result = block()
            asyncResponse.resume(result)
        }
    }

    override fun <T> runBlocking(timeout: Duration, block: suspend () -> T): T =
        kotlinx.coroutines.runBlocking {
            block()
        }

    override suspend fun <T> runInRequestScope(block: () -> T): T = block()

    override suspend fun <T> suspendInRequestScope(block: (CancellableContinuation<T>) -> Unit): T =
        suspendCancellableCoroutine(block)
    override suspend fun <T> withContext(name: String, block: suspend () -> T): T {
        TODO("Not yet implemented")
    }
}
