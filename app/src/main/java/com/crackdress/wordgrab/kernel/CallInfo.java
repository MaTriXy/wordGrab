package com.crackdress.wordgrab.kernel;

import android.os.Parcel;
import android.os.Parcelable;


public class CallInfo implements Parcelable {

    public String phoneNumber;
    public boolean incoming;


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.phoneNumber);
        dest.writeByte(this.incoming ? (byte) 1 : (byte) 0);
    }

    public CallInfo() {
    }

    protected CallInfo(Parcel in) {
        this.phoneNumber = in.readString();
        this.incoming = in.readByte() != 0;
    }

    public static final Parcelable.Creator<CallInfo> CREATOR = new Parcelable.Creator<CallInfo>() {
        @Override
        public CallInfo createFromParcel(Parcel source) {
            return new CallInfo(source);
        }

        @Override
        public CallInfo[] newArray(int size) {
            return new CallInfo[size];
        }
    };
}
