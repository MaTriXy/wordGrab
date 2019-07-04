package com.crackdress.wordgrab.details;

import com.crackdress.wordgrab.BasePresenter;
import com.crackdress.wordgrab.BaseView;
import com.crackdress.wordgrab.model.Recording;

public interface DetailsContract {

    interface View extends BaseView{
        void showRecordingDetails(Recording recording);
        void showContactEditor();
        void openDialer(String phoneNumber);
        void addToContacts(String phoneNumber);
        void sendMessage(String phoneNumber);
        void sendWhatsapp(String phoneNumber);
    }

    interface Presenter extends BasePresenter{
        void loadRecording(long recordingId);
        void addToContactClicked();
        void deleteRecording();
        void callActionClicked();
        void smsActionClicked();
        void whatsappActionClicked();
    }
}
