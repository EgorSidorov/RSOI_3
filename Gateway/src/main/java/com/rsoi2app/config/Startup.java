package com.rsoi2app.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Startup
{
        private static String MYSQLCONNECTION = "";
        //private static String MYSQLCONNECTION = "";
        private static String MYSQLDRIVER = "com.mysql.cj.jdbc.Driver";
        private static Boolean isDbTest = false;
        
        public static String GetConnectionStr() {
            return MYSQLCONNECTION;
        }

        public static String GetDriver() {
            return MYSQLDRIVER;
        }

        public static int GetTimeout() {
        return 5000;
    }

        public static String GetGatewayHostPort() {return "http://194.58.121.174:8080";}

        public static Boolean isDbTest() {
    return isDbTest;
}
        
        public static void InitializeDB(){
            Connection connection = null;
            try {
                Class.forName(GetDriver());
            } catch (ClassNotFoundException e) {
            }
            try {
                connection = DriverManager.getConnection(GetConnectionStr());
            } catch (SQLException e) {
                return;
            }
            String query1 = "DROP TABLE Account.Info; DROP TABLE Account.Roles;";
            String query2 = "CREATE DATABASE IF NOT EXISTS Account;";
                            
            String query3 = "CREATE TABLE IF NOT EXISTS Account.Info\n" +
                            "\t(\n" +
                            "\tID int AUTO_INCREMENT PRIMARY KEY,\n" +
                            "\tUsername varchar(50) NOT NULL,\n" +
                            "\tPassword varchar(50) NOT NULL,\n" +
                            "\tRole int,\n" +
                            "\tCookie varchar(50) \n" +
                            "\t);\n" +
                            "CREATE TABLE IF NOT EXISTS Account.Roles\n" +
                            "\t(\n" +
                            "\tID_Role int AUTO_INCREMENT PRIMARY KEY,\n" +
                            "\tName_Role varchar(50) NOT NULL\n" +
                            "\t);\n" +
                            "\t);";

            Statement stmtObj = null;
            try {
                stmtObj = connection.createStatement();
            } catch (SQLException e) {
		        return;
            }
            try {
                stmtObj.execute(query1);
            } catch (SQLException e) {
            }
            try {
                stmtObj.execute(query2);
            } catch (SQLException e) {
            }
            try {
                stmtObj.execute(query3);
            } catch (SQLException e) {
            }
        }
}
