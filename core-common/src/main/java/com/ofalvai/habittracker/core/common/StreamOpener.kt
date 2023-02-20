/*
 * Copyright 2022 Olivér Falvai
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ofalvai.habittracker.core.common

import android.app.Application
import java.io.InputStream
import java.io.OutputStream
import java.net.URI
import javax.inject.Inject

/**
 * Abstraction over Android's Uri and ContentResolver in order to use them in ViewModels
 * without using Android classes directly
 */
interface StreamOpener {
    fun openInputStream(uri: URI): InputStream
    fun openOutputStream(uri: URI): OutputStream
}

class AndroidStreamOpener @Inject constructor(private val app: Application): StreamOpener {

    override fun openInputStream(uri: URI): InputStream {
        return app.contentResolver.openInputStream(uri.toAndroidURI())!!
    }

    override fun openOutputStream(uri: URI): OutputStream {
        return app.contentResolver.openOutputStream(uri.toAndroidURI())!!
    }

    private fun URI.toAndroidURI() = android.net.Uri.parse(toString())
}