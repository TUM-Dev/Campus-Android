package de.tum.in.tumcampusapp.auxiliary;

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;

public class ListMenuEntry implements Parcelable {

	public static final Parcelable.Creator<ListMenuEntry> CREATOR = new Parcelable.Creator<ListMenuEntry>() {
		public ListMenuEntry createFromParcel(Parcel in) {
			return new ListMenuEntry(in);
		}

		public ListMenuEntry[] newArray(int size) {
			return new ListMenuEntry[size];
		}
	};
	public int detailId;
	public int imageId;
	public Intent intent;

	public int titleId;

	public ListMenuEntry() {
	}

	public ListMenuEntry(int imageId, int titleId, int detailId, Intent intent) {
		this.imageId = imageId;
		this.titleId = titleId;
		this.detailId = detailId;
		this.intent = intent;
	}

	private ListMenuEntry(Parcel in) {
		imageId = in.readInt();
		titleId = in.readInt();
		detailId = in.readInt();
		intent = in.readParcelable(null);
	}

	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(imageId);
		dest.writeInt(titleId);
		dest.writeInt(detailId);
		dest.writeParcelable(intent, 0);
	}
}
