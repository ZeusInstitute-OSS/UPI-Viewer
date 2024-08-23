package com.zeusinstitute.upiapp
import android.os.Parcel
import android.os.Parcelable
class VoiceInfo {


    data class VoiceInfo(
        val name: String,
        val gender: String,
        val country: String,
        val language: String
    ) : Parcelable {
        constructor(parcel: Parcel) : this(
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: ""
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(name)
            parcel.writeString(gender)
            parcel.writeString(country)
            parcel.writeString(language)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<VoiceInfo> {
            override fun createFromParcel(parcel: Parcel): VoiceInfo {
                return VoiceInfo(parcel)
            }

            override fun newArray(size: Int): Array<VoiceInfo?> {
                return arrayOfNulls(size)
            }
        }
    }
}