package com.crackdress.wordgrab.model

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "contacts")
data class Contact(@PrimaryKey
                   var id: Long = 0,
                   var displayName: String? = null,
                   var uri: String? = null,
                   var number: String? = null,
                   var thumbUri: String? = null) {


  override fun toString(): String {
    return "Contact{" +
        "id=" + id +
        ", displayName='" + displayName + '\''.toString() +
        ", uri='" + uri + '\''.toString() +
        ", number='" + number + '\''.toString() +
        ", thumbUri='" + thumbUri + '\''.toString() +
        '}'.toString()
  }
}
