package com.lihao.extracts.widget

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.Serializer
import com.lihao.extracts.data.database.AppDatabase
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class WidgetState(
    val noteContent: String = "暂无摘记",
    val noteSource: String = ""
)

object WidgetStateSerializer : Serializer<WidgetState> {
    override val defaultValue: WidgetState = WidgetState()

    override suspend fun readFrom(input: InputStream): WidgetState {
        return try {
            val text = input.readBytes().decodeToString()
            if (text.isEmpty()) defaultValue
            else Json.decodeFromString(WidgetState.serializer(), text)
        } catch (e: Exception) {
            defaultValue
        }
    }

    override suspend fun writeTo(t: WidgetState, output: OutputStream) {
        output.write(Json.encodeToString(WidgetState.serializer(), t).encodeToByteArray())
    }
}

object WidgetStateDefinition : androidx.glance.state.GlanceStateDefinition<WidgetState> {
    override fun getLocation(context: Context, fileKey: String): File {
        return File(context.filesDir, "widget_state_$fileKey")
    }

    override suspend fun getDataStore(context: Context, fileKey: String): DataStore<WidgetState> {
        return DataStoreFactory.create(
            serializer = WidgetStateSerializer,
            produceFile = { getLocation(context, fileKey) }
        )
    }
}
