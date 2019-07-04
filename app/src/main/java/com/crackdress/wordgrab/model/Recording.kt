package com.crackdress.wordgrab.model


import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey


@Entity(tableName = "recordings")
data class Recording(@PrimaryKey(autoGenerate = true)
                     var id: Long = 0,
                     @ColumnInfo(name = "phone_number")
                     var phoneNumber: String? = null,

                     var date: Long = 0,
                     var duration: Long = 0,
                     var incoming: Boolean = false,

                     @ColumnInfo(name = "description")
                     var comment: String? = null,
                     var uri: String? = null,

                     @Ignore
                     var contactName: String? = null) {


  override fun toString(): String {
    return "Recording{" +
        "id=" + id +
        ", phoneNumber='" + phoneNumber + '\''.toString() +
        ", date=" + date +
        ", duration=" + duration +
        ", incoming=" + incoming +
        ", comment='" + comment + '\''.toString() +
        ", uri='" + uri + '\''.toString() +
        ", contactName='" + contactName + '\''.toString() +
        '}'.toString()
  }
}
