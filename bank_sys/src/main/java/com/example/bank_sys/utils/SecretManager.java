package com.example.bank_sys.utils;

import com.example.esee_poc.dto.CryptoConfigDto;
import com.example.esee_poc.interfaces.VaultInteractionUtility;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.Base64;
import java.util.Scanner;

@Component
public class SecretManager implements VaultInteractionUtility {
    @Override
    public String getKeyName(CryptoConfigDto config, String keyType, String keyVersion) {
        return "vault/" + keyType + "_" + config.getClientId() + "_" + config.getPartnerId() + "_" + keyVersion + ".txt";
    }

    @Override
    public byte[] getSecret(String key) throws IOException {
        StringBuilder data = new StringBuilder();
        try {
            File Obj = new File(key);
            Scanner fileReader = new Scanner(Obj);

            // Traversing File Data
            while (fileReader.hasNextLine()) {
                data.append(fileReader.nextLine());
            }

            fileReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error has occurred.");
            e.printStackTrace();
        }

        return Base64.getDecoder().decode(data.toString());
    }

    @Override
    public void setSecret(String key, byte[] secret) throws IOException {
        FileWriter fileWriter = new FileWriter(key);
        fileWriter.write(Base64.getEncoder().encodeToString(secret));
        fileWriter.close();
    }

    @Override
    public void removeSecret(String key) throws IOException {
        File file = new File(key);
        if (file.exists()) {
            if (!file.delete()) {
                throw new IOException("Failed to delete the secret file: " + key);
            }
        }
    }
}
