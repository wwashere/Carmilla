package me.vasir;

import me.vasir.handler.JdaHandler;
import me.vasir.manager.DatabaseManager;


public class Carmilla {
    public static void main(String[] args) {
        JdaHandler jdaHandler = new JdaHandler();
        DatabaseManager databaseHandler = new DatabaseManager();

        jdaHandler.startBot();
        databaseHandler.setDatabaseTable();


    }


}