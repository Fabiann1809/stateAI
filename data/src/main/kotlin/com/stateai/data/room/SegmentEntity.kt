package com.stateai.data.room

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

/** Summary of a segment; the activity is stored as a snapshot so history survives renames. */
@Entity(tableName = "segments", indices = [Index("startMillis")])
data class SegmentEntity(
    @PrimaryKey val id: String,
    val activityId: String,
    val category: String,
    val displayName: String?,
    val startMillis: Long,
    val endMillis: Long,
    val plannedSeconds: Long,
    val lowSeconds: Long,
    val mediumSeconds: Long,
    val highSeconds: Long,
    val unknownSeconds: Long,
    val restlessSeconds: Long,
    val pauseSuggestions: Int,
    val feedback: String?,
    @ColumnInfo(defaultValue = "") val traceSymbols: String,
    /** Comma-separated minutes from the start at which the app played a time cue. */
    @ColumnInfo(defaultValue = "") val cueMinutes: String,
    val calmHeartRate: Double?,
    val cleanMovement: Double?,
)

@Entity(
    tableName = "pauses",
    foreignKeys = [
        ForeignKey(
            entity = SegmentEntity::class,
            parentColumns = ["id"],
            childColumns = ["segmentId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("segmentId")],
)
data class PauseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val segmentId: String,
    val startMillis: Long,
    val endMillis: Long,
    val beforeLevel: String?,
    val beforeRestless: Boolean?,
    val afterLevel: String?,
    val afterRestless: Boolean?,
)

data class SegmentWithPauses(
    @Embedded val segment: SegmentEntity,
    @Relation(parentColumn = "id", entityColumn = "segmentId") val pauses: List<PauseEntity>,
)
