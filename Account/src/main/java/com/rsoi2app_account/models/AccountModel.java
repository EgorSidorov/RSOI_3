package com.rsoi2app_account.models;

import com.rsoi2app_account.config.Startup;
import com.rsoi2app_account.external.EasyAuth;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AccountModel {
    public Connection connection;
    private Boolean dbStatus;
    private Boolean queryStatus;
    public EasyAuth auth;
    public ResultSet resObj;
    int sizePage = 2;

    public AccountModel()
    {
        dbStatus = CreateConnection();
    }

    public Boolean GetDbStatus()
    {
        return  dbStatus;
    }

    public Boolean GetQueryStatus()
    {
        return  queryStatus;
    }


    public Boolean CreateConnection()
    {
        String db_uri = Startup.GetConnectionStr();
        try {
            Class.forName(Startup.GetDriver());
        } catch (ClassNotFoundException e) {
            System.out.print("\nError find sql driver\n");
            return false;
        }
        try {
            connection = DriverManager.getConnection(db_uri);
        } catch (SQLException e) {
            System.out.print("\nError get connection " + e.getMessage() + "\n");
            return false;
        }
        auth = new EasyAuth(db_uri, "Account.Info", "Username", "Password", "Cookie");
        return true;
    }

    @Override
    public void finalize() throws SQLException {
        connection.close();
        auth.finalize();
    }

    public List<String> GetUserNames(String token, int numberPage)
    {
        List<String> UserNames = new ArrayList<>();
        if (!IsLogged(token)) {
            queryStatus = false;
        } else {
            Statement stmtObj = RequestDB(
                    "SELECT Username,  Name_Role " +
                            "FROM Account.Info us " +
                            "JOIN Account.Roles rl ON(us.Role=rl.ID_Role) " +
                            "LIMIT " + String.valueOf(sizePage) + " OFFSET " + String.valueOf(numberPage * sizePage), true);
            if (!GetQueryStatus())
                return UserNames;
            try {
                while (resObj.next()) {
                    UserNames.add(resObj.getString("Username")); //+ ":" + resObj.getString("Name_Role"));
                }
            } catch (SQLException e) {
                queryStatus = false;
                return UserNames;
            }
            try {
                if (stmtObj != null)
                    stmtObj.close();
            } catch (SQLException e) {
            }
        }
        return UserNames;
    }

    public List<String> GetAllRoles()
    {
        List<String> Roles = new ArrayList<>();
        Statement stmtObj = RequestDB("SELECT Name_Role FROM Account.Roles", true);
        if (!GetQueryStatus())
            return Roles;
        try {
            while (resObj.next()) {
                Roles.add(resObj.getString("Name_Role"));
            }
        } catch (SQLException e) {
            queryStatus = false;
            return Roles;
        }
        try {
            if (stmtObj != null)
                stmtObj.close();
        } catch (SQLException e) {
        }
        return Roles;
    }

    public Boolean CreateUser(String username, String password,String role)
    {
        Boolean success = auth.CreateAccount(username, password);
        if (success) {
            //Statement stmtObj = RequestDB("UPDATE Account.Info SET Role=" + role + " WHERE Username='" + username + "'", false);
            //if (!GetQueryStatus())
            //    return false;
            Statement stmtObj = RequestDB("insert into Speakstars.CallRequest (fromuser,touser) VALUES('" + username + "','')", false);
            if (!GetQueryStatus())
                return false;
            try {
                if (stmtObj != null)
                    stmtObj.close();
            } catch (SQLException e) {
            }
        }

        return success;
    }

    public Boolean AddRole(int ID, String role)
    {
        Statement stmtObj = RequestDB("Insert INTO Account.Roles VALUES(" + Integer.toString(ID) + ",'" + role + "')", false);
        try {
            if (stmtObj != null)
                stmtObj.close();
        } catch (SQLException e) {
        }
        return GetQueryStatus();
    }

    public String Login(String username, String password)
    {
        queryStatus = true;
        String authCookie = auth.LogIn(username, password);
        if (auth.GetQueryStatus())
            return authCookie;
        else {
            queryStatus = false;
            return "";
        }
    }

    public List<String> GetPhonebookNames(String token)
    {
        List<String> UserNames = new ArrayList<>();
        if (!IsLogged(token)) {
            queryStatus = false;
        } else {
            Statement stmtObj = RequestDB(
                    "SELECT Username,  Usernameto " +
                            "FROM Speakstars.phonebook pb WHERE Username='"+GetUsername(token)+"'",true);// +
            //"LIMIT " + String.valueOf(sizePage) + " OFFSET " + String.valueOf(numberPage * sizePage), true);
            if (!GetQueryStatus())
                return UserNames;
            try {
                String iam = GetUsername(token);
                while (resObj.next()) {
                    if(!resObj.getString("Usernameto").equals(iam))
                        UserNames.add(resObj.getString("Usernameto"));// + ":" + resObj.getString("Name_Role"));
                }
            } catch (SQLException e) {
                queryStatus = false;
                return UserNames;
            }
            try {
                if (stmtObj != null)
                    stmtObj.close();
            } catch (SQLException e) {
            }
        }
        return UserNames;
    }

    public Boolean SetCallRequest(String fromuser, String touser, Boolean first_time, String countfromicecandidates)
    {
        queryStatus = true;
        String time = String.valueOf(System.currentTimeMillis());
        Statement stmtObj;
        if(first_time)
            stmtObj = RequestDB("UPDATE Speakstars.CallRequest SET touser='"+touser+"',  time="+time+", status=0, countfromicecandidates="+countfromicecandidates+" WHERE fromuser='"+fromuser+"'",false);
        else
            stmtObj = RequestDB("UPDATE Speakstars.CallRequest SET touser='"+touser+"',  time="+time+" WHERE fromuser='"+fromuser+"'",false);
        if(!GetQueryStatus()) {
            return false;
        }
        try {
            if(stmtObj != null)
                stmtObj.close();
        } catch (SQLException e) {}
        return true;
    }

    public Boolean SetCallRequestFromIceCandidates(String fromuser, String icecandidates)
    {
        queryStatus = true;
        Statement stmtObj;
        stmtObj = RequestDB("UPDATE Speakstars.CallRequest SET fromicecandidates='"+icecandidates+"' WHERE fromuser='"+fromuser+"'",false);
        if(!GetQueryStatus()) {
            return false;
        }
        try {
            if(stmtObj != null)
                stmtObj.close();
        } catch (SQLException e) {}
        return true;
    }

    public Boolean AddPhoneBook(String user, String touser)
    {
        queryStatus = true;
        Statement stmtObj;
        stmtObj = RequestDB("INSERT INTO Speakstars.phonebook select * from (select '"+user+"','"+touser+"') as tmp where not exists (select * from Speakstars.phonebook where username='"+user+"' and usernameto='"+touser+"')",false);
        if(!GetQueryStatus()) {
            return false;
        }
        try {
            if(stmtObj != null)
                stmtObj.close();
        } catch (SQLException e) {}
        return true;
    }

    public Boolean SetCallRequestToIceCandidates(String fromuser, String icecandidates )
    {
        queryStatus = true;
        Statement stmtObj;
        stmtObj = RequestDB("UPDATE Speakstars.CallRequest SET toicecandidates='"+icecandidates+"' WHERE fromuser='"+fromuser+"'",false);
        if(!GetQueryStatus()) {
            return false;
        }
        try {
            if(stmtObj != null)
                stmtObj.close();
        } catch (SQLException e) {}
        return true;
    }

    public Boolean SetCallRequestFromDescription(String fromuser, String description)
    {
        queryStatus = true;
        Statement stmtObj;
        stmtObj = RequestDB("UPDATE Speakstars.CallRequest SET fromdescription="+description+" WHERE fromuser='"+fromuser+"'",false);
        if(!GetQueryStatus()) {
            return false;
        }
        try {
            if(stmtObj != null)
                stmtObj.close();
        } catch (SQLException e) {}
        return true;
    }

    public Boolean SetCallRequestToDescription(String fromuser,String description)
    {
        queryStatus = true;
        Statement stmtObj;
        stmtObj = RequestDB("UPDATE Speakstars.CallRequest SET todescription="+description+" WHERE fromuser='"+fromuser+"'",false);
        if(!GetQueryStatus()) {
            return false;
        }
        try {
            if(stmtObj != null)
                stmtObj.close();
        } catch (SQLException e) {}
        return true;
    }

    public Boolean SetCallAnswer(String touser, String fromuser, String status, String counttoicecandidates)
    {
        queryStatus = true;
        Statement stmtObj = RequestDB("UPDATE Speakstars.CallRequest SET status="+status+", counttoicecandidates="+counttoicecandidates+" WHERE fromuser='"+fromuser+"' AND touser='"+touser+"'",false);
        if(!GetQueryStatus()) {
            return false;
        }
        try {
            if(stmtObj != null)
                stmtObj.close();
        } catch (SQLException e) {}
        return true;
    }

    public List<String> GetCallRequest(String username)
    {
        queryStatus = true;
        List<String> users = new ArrayList<>();
        String time = String.valueOf(System.currentTimeMillis());
        Statement stmtObj = RequestDB("SELECT fromuser FROM Speakstars.CallRequest WHERE touser='"+username+"' and ("+time+"-time)<5000 and status=0",true);
        if(!GetQueryStatus()) {
            return users;
        }
        try {
            while(resObj.next()) {
                users.add(resObj.getString("fromuser"));
            }
        } catch (SQLException e) {
            queryStatus = false;
            return users;
        }
        try {
            if(stmtObj != null)
                stmtObj.close();
        } catch (SQLException e) {}
        return users;
    }

    public List<String> GetCallAnswer(String fromuser, String touser)
    {
        queryStatus = true;
        List<String> users = new ArrayList<>();
        String time = String.valueOf(System.currentTimeMillis());
        Statement stmtObj = RequestDB("SELECT status FROM Speakstars.CallRequest WHERE touser='"+touser+"' and fromuser='"+fromuser+"'",true);
        if(!GetQueryStatus()) {
            return users;
        }
        try {
            while(resObj.next()) {
                users.add(resObj.getString("status"));
            }
        } catch (SQLException e) {
            queryStatus = false;
            return users;
        }
        finally {
            try {
                if(stmtObj != null)
                    stmtObj.close();
            } catch (SQLException e){}
        }
        return users;
    }

    public String GetStringFromTable (String namefield, String nametable, String condition)
    {
        queryStatus = true;
        String result = "";
        String time = String.valueOf(System.currentTimeMillis());
        Statement stmtObj = RequestDB("SELECT " + namefield  + " FROM "+nametable +" "+condition,true);
        if(!GetQueryStatus()) {
            return result;
        }
        try {
            resObj.next();
            result = resObj.getString(namefield);
        } catch (SQLException e) {
            queryStatus = false;
            return result;
        }
        try {
            if(stmtObj != null)
                stmtObj.close();
        } catch (SQLException e) {}
        return result;
    }

    public List<String> GetFromIceCandidates(String touser , String fromuser)
    {
        queryStatus = true;
        List<String> users = new ArrayList<>();
        Statement stmtObj = RequestDB("SELECT fromicecandidates FROM Speakstars.CallRequest WHERE touser='"+touser+"' and fromuser='"+fromuser+"'",true);
        if(!GetQueryStatus()) {
            return users;
        }
        try {
            while(resObj.next()) {
                users.add(resObj.getString("fromicecandidates"));
            }
        } catch (SQLException e) {
            queryStatus = false;
            return users;
        }
        try {
            if(stmtObj != null)
                stmtObj.close();
        } catch (SQLException e) {}
        return users;
    }

    public List<String> GetToIceCandidates(String fromuser , String touser)
    {
        queryStatus = true;
        List<String> users = new ArrayList<>();
        String time = String.valueOf(System.currentTimeMillis());
        Statement stmtObj = RequestDB("SELECT toicecandidates FROM Speakstars.CallRequest where fromuser='"+fromuser+"'",true);
        if(!GetQueryStatus()) {
            return users;
        }
        try {
            while(resObj.next()) {
                users.add(resObj.getString("toicecandidates"));
            }
        } catch (SQLException e) {
            queryStatus = false;
            return users;
        }
        try {
            if(stmtObj != null)
                stmtObj.close();
        } catch (SQLException e) {}
        return users;
    }

    public List<String> GetFromDescription(String touser , String fromuser)
    {
        queryStatus = true;
        List<String> users = new ArrayList<>();
        String time = String.valueOf(System.currentTimeMillis());
        Statement stmtObj = RequestDB("SELECT fromdescription FROM Speakstars.CallRequest WHERE touser='"+touser+"' and fromuser='"+fromuser+"'",true);
        if(!GetQueryStatus()) {
            return users;
        }
        try {
            while(resObj.next()) {
                users.add(resObj.getString("fromdescription"));
            }
        } catch (SQLException e) {
            queryStatus = false;
            return users;
        }
        try {
            if(stmtObj != null)
                stmtObj.close();
        } catch (SQLException e) {}
        return users;
    }

    public List<String> GetToDescription(String fromuser, String touser)
    {
        queryStatus = true;
        List<String> users = new ArrayList<>();
        String time = String.valueOf(System.currentTimeMillis());
        Statement stmtObj = RequestDB("SELECT todescription FROM Speakstars.CallRequest WHERE fromuser='"+fromuser+"'",true);
        if(!GetQueryStatus()) {
            return users;
        }
        try {
            while(resObj.next()) {
                users.add(resObj.getString("todescription"));
            }
        } catch (SQLException e) {
            queryStatus = false;
            return users;
        }
        try {
            if(stmtObj != null)
                stmtObj.close();
        } catch (SQLException e) {}
        return users;
    }

    public Boolean Logout(String token)
    {
        if(auth.HasLogged(token))
            return auth.LogOut(token);
        else return false;
    }

    public Boolean IsLogged(String token)
    {
        return auth.HasLogged(token);
    }

    public String GetUsername(String token)
    {
        queryStatus = true;
        String username = auth.GetUserName(token);
        if (auth.GetQueryStatus()) {
            return username;
        } else {
            queryStatus = false;
            return "";
        }
    }

    public String GetRole(String token)
    {
        String role = "";
        Statement stmtObj = RequestDB("SELECT Name_Role FROM Account.Roles rl JOIN Account.Info inf ON(inf.Role = rl.ID_Role) WHERE inf.Cookie=" + token, true);
        if (!GetQueryStatus())
            return "";
        try {
            while (resObj.next()) {
                role = resObj.getString("Name_Role");
            }
        } catch (SQLException e) {
            queryStatus = false;
            return "";
        }
        try {
            if (stmtObj != null)
                stmtObj.close();
        } catch (SQLException e) {
        }
        return role;
    }
    
    public List<String> GetLogs(int numberPage)
    {
        queryStatus = true;
        List<String> Logs = new ArrayList<>();
        Statement stmtObj = RequestDB("SELECT str FROM Account.Logger LIMIT "+String.valueOf(sizePage)+" OFFSET "+String.valueOf(numberPage),true);
        if(!GetQueryStatus()) {
            return Logs;
        }
        try {
            while(resObj.next()) {
                Logs.add(resObj.getString("str"));
            }
        } catch (SQLException e) {
            queryStatus = false;
            return Logs;
        }
        try {
            if(stmtObj != null)
                stmtObj.close();
        } catch (SQLException e) {}
        return Logs;
    }
    
   public Boolean SetLogs(String logString)
    {
        queryStatus = true;
        Statement stmtObj = RequestDB("INSERT INTO Account.Logger (str) VALUES('"+logString+"')",false);
        if(!GetQueryStatus()) {
            return false;
        }
        try {
            if(stmtObj != null)
                stmtObj.close();
        } catch (SQLException e) {}
        return true;
    }

    public Statement RequestDB(String sql, Boolean response)
    {
        queryStatus = true;
        if(!GetDbStatus())
        {
            queryStatus = false;
            return null;
        }
        Statement stmtObj = null;
        try {
            stmtObj = connection.createStatement();
        } catch (SQLException e) {
            queryStatus = false;
            return null;
        }
        try {
            if(response)
                resObj = stmtObj.executeQuery(sql);
            else
                stmtObj.execute(sql);
        } catch (SQLException e) {
            queryStatus = false;
        }
        finally {
            return stmtObj;
        }
    }
}
