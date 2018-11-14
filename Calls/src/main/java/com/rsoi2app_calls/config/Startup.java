package com.rsoi2app_calls.config;

public class Startup
{
        private static String MYSQLCONNECTION = "";
        private static String MYSQLDRIVER = "com.mysql.cj.jdbc.Driver";
        
        public static String GetConnectionStr() {
            return MYSQLCONNECTION;
        }

        public static String GetDriver() {
            return MYSQLDRIVER;
        }
}
