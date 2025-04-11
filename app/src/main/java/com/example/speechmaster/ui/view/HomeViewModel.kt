package com.example.speechmaster.ui.view


import android.os.Parcel
import android.os.Parcelable


import androidx.lifecycle.ViewModel


class HomeViewModel( ) : ViewModel(), Parcelable {
    private var mText: String? = null

    constructor(parcel: Parcel) : this() {
        mText = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(mText)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<HomeViewModel> {
        override fun createFromParcel(parcel: Parcel): HomeViewModel {
            return HomeViewModel(parcel)
        }

        override fun newArray(size: Int): Array<HomeViewModel?> {
            return arrayOfNulls(size)
        }
    }
}