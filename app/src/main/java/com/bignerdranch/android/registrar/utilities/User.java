package com.bignerdranch.android.registrar.utilities;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {
    int id;
    String name;
    String email;
    String phone;
    int activation;
    int type;
    String image;
    String reset_password_code;
    String api_token;
    String created_at;
    String updated_at;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public int getActivation() {
        return activation;
    }

    public int getType() {
        return type;
    }

    public String getImage() {
        return image;
    }

    public String getReset_password_code() {
        return reset_password_code;
    }

    public String getApi_token() {
        return api_token;
    }

    public String getCreated_at() {
        return created_at;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator(){

        @Override
        public User createFromParcel(Parcel parcel) {
            return  new User(parcel);
        }

        @Override
        public User[] newArray(int i) {
            return new User[i];
        }
    };

    public User(Parcel parcel){
        this.id = parcel.readInt();
        this.name = parcel.readString();
        this.email = parcel.readString();
        this.phone = parcel.readString();
        this.activation = parcel.readInt();
        this.type = parcel.readInt();
        this.image = parcel.readString();
        this.reset_password_code = parcel.readString();
        this.api_token = parcel.readString();
        this.created_at = parcel.readString();
        this.updated_at = parcel.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(this.id);
        parcel.writeString(this.name);
        parcel.writeString(this.email);
        parcel.writeString(this.phone);
        parcel.writeInt(this.activation);
        parcel.writeInt(this.type);
        parcel.writeString(image);
        parcel.writeString(this.reset_password_code);
        parcel.writeString(this.api_token);
        parcel.writeString(this.created_at);
        parcel.writeString(this.updated_at);
    }
}
