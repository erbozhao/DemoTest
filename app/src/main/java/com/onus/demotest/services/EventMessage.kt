package com.onus.demotest.services

import android.annotation.SuppressLint
import android.os.Parcel
import android.os.Parcelable

/**
 * @Author: onuszhao
 * @Date: 2024-04-15 21:03
 * @Description:
 */
class EventMessage() : Parcelable {

    var eventName: String? = null
    var arg0 = 0
    var arg1 = 0
    var obj: Any? = null

    fun EventMessage(eventName: String?, arg0: Int, arg1: Int, obj: Any?) {
        this.eventName = eventName
        this.arg0 = arg0
        this.arg1 = arg1
        this.obj = obj
    }

    protected fun EventMessage(source: Parcel) {
        eventName = source.readString()
        arg0 = source.readInt()
        arg1 = source.readInt()
        if (source.readInt() != 0) {
            obj = source.readParcelable(javaClass.classLoader)
        }
    }

    @SuppressLint("DefaultLocale")
    override fun toString(): String {
        val b = StringBuilder()
        b.append("eventName=")
        b.append(eventName)
        b.append(", arg0=")
        b.append(arg0)
        b.append(", arg1=")
        b.append(arg1)
        if (obj != null) {
            b.append("obj=")
            b.append(obj)
        }
        return b.toString()
    }


    constructor(parcel: Parcel) : this() {
        eventName = parcel.readString()
        arg0 = parcel.readInt()
        arg1 = parcel.readInt()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(eventName)
        dest.writeInt(arg0)
        dest.writeInt(arg1)
        if (obj != null) {
            try {
                val p = obj as Parcelable
                dest.writeInt(1)
                dest.writeParcelable(p, flags)
            } catch (e: ClassCastException) {
                throw RuntimeException(
                    "Error, Can't marshal non-Parcelable objects across processes."
                )
            }
        } else {
            dest.writeInt(0)
        }
    }

    companion object CREATOR : Parcelable.Creator<EventMessage> {
        override fun createFromParcel(parcel: Parcel): EventMessage {
            return EventMessage(parcel)
        }

        override fun newArray(size: Int): Array<EventMessage?> {
            return arrayOfNulls(size)
        }
    }
}