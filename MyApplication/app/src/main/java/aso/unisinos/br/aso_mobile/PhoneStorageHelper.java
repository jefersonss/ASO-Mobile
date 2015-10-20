package aso.unisinos.br.aso_mobile;

import android.content.Context;
import android.support.annotation.NonNull;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

public class PhoneStorageHelper {
    private String PATIENT_LIST_FILE_NAME = "patient_list";

    public String getPatientList(Context context) {
        String returningList = "";
        try {
            returningList = readFromFile(context, returningList, PATIENT_LIST_FILE_NAME);
        }catch(Exception e){

        }
        return returningList;
    }

    public String retrievePatientInfo(String fileName, Context context) {
        String returningPatient = "";
        try {
            returningPatient = readFromFile(context, returningPatient, fileName);
        }catch(Exception e){

        }
        return returningPatient;
    }

    public void storePatientList(String jsonResult, Context context) {
        FileOutputStream fos = null;
        try {
            fos = context.openFileOutput(PATIENT_LIST_FILE_NAME, Context.MODE_PRIVATE);
            fos.write(jsonResult.getBytes());
            fos.close();
        }catch(Exception e){

        }
    }

    public void savePatientInfo(String fileName, String patientDetail, Context context) {
        FileOutputStream fos = null;
        try {
            fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            fos.write(patientDetail.getBytes());
            fos.close();
        }catch(Exception e){

        }
    }

    @NonNull
    private String readFromFile(Context context, String returningList, String fileName) throws IOException {
        FileInputStream fis;
        fis = context.openFileInput(fileName);
        byte[] buffer = new byte[fis.available()];

        int content;
        for (int i = 0; (content = fis.read()) != -1; i ++) {
            buffer[i] = (byte)content;
        }

        returningList = new String(buffer, Charset.defaultCharset());
        fis.close();
        return returningList;
    }
}
