package com.rsoi2app.controllers;

import com.rsoi2app.config.Startup;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

@Controller
public class GatewayController {

	@GetMapping("/login")
	public String Login(@RequestParam(name="username", required=false, defaultValue= "") String username,
										  @RequestParam(name="password", required=false, defaultValue= "") String password,
										  Model model,
										  HttpServletResponse response) throws IOException, ParseException {
		if(!username.isEmpty() && !password.isEmpty() && !username.contains(" ") && !password.contains(" "))
		{
			String responseStr1 = RequestForService(Startup.GetGatewayHostPort()+"/service/account/Login?username="+username+"&password="+password,"none");
			if (!responseStr1.contains("Error:")) {
				JSONParser parser = new JSONParser();
				JSONObject userJson = (JSONObject) parser.parse(responseStr1);
				if(userJson.get("Status").toString().equals("Success")) {
					response.addCookie(new Cookie("Token", userJson.get("Cookie").toString()));
					return "redirect:https://194.58.121.174:8443/historycall";
				}
				else {
					model.addAttribute("name","Error login");
					response.setStatus(401);
				}
			}
			else {
				model.addAttribute("name", "Error login");
				response.setStatus(500);
			}


		}
		else {
			model.addAttribute("name","Error parameters");
			response.setStatus(400);
		}
		return "authorize";
	}

	@GetMapping("/")
	public String StartRedirect()
	{
		return "redirect:https://194.58.121.174:8443/payment";
	}

	@GetMapping("/logout")
	public String Logout(@CookieValue(name="Token", defaultValue= "") String token,
						Model model,
						HttpServletResponse response) throws IOException, ParseException {
		RequestForService(Startup.GetGatewayHostPort()+"/service/account/Logout",token);
		return "authorize";
	}

	@GetMapping("/call")
	public String Call(@CookieValue(name="Token", defaultValue= "") String token,
						 Model model,
						 HttpServletResponse response) throws IOException, ParseException {
		String username;
		String responseStr = RequestForService(Startup.GetGatewayHostPort()+"/service/account/PhonebookList",token);
		if (!responseStr.contains("Error:")) {
			JSONParser parser = new JSONParser();
			JSONObject userJson = (JSONObject) parser.parse(responseStr);
			if (userJson.get("Status").toString().equals("Success")) {
				String responseStr2 = RequestForService(Startup.GetGatewayHostPort()+"/service/account/UserInfo",token);
				if (!responseStr2.contains("Error:")) {
					JSONParser parser2 = new JSONParser();
					JSONObject userJson2 = (JSONObject) parser2.parse(responseStr2);
					if(userJson2.get("Status").equals("Success") && userJson2.get("IsLogged").toString().equals("true")) {
						username = userJson2.get("username").toString();
						String responseStr3 = RequestForService(Startup.GetGatewayHostPort() + "/service/payment/Show_cash?username=" + username, token);
						if (!responseStr3.contains("Error:")) {
							JSONParser parser3 = new JSONParser();
							JSONObject userJson3 = (JSONObject) parser3.parse(responseStr3);
							if (userJson3.get("Status").equals("Success")) {
								if(Integer.parseInt(userJson3.getAsString("cash"))>0)
									model.addAttribute("alllist",userJson.get("UserList"));
								else
								{
									model.addAttribute("name","Пополните счет");
									response.setStatus(401);
									return "greeting";
								}
							}
						} else {
							model.addAttribute("name","Ошибка получения счета");
							response.setStatus(401);
							return "greeting";
						}
					}
				}
				else {
					model.addAttribute("name","Error token");
					response.setStatus(401);
					return "greeting";
				}
			}
			else {
				model.addAttribute("name","Error token");
				response.setStatus(401);
				return "greeting";
			}
		}
		else {
			model.addAttribute("name", "Error login");
			response.setStatus(500);
			return "greeting";
		}
		return "callrequest";
	}

	@GetMapping("/unauthorize")
	public String Unauthorize(@RequestParam(name="username", required=false, defaultValue= "") String username,
						@RequestParam(name="password", required=false, defaultValue= "") String password,
							  @RequestParam(name="status", required=false, defaultValue= "") String status,
						Model model,
						HttpServletResponse response) throws IOException, ParseException {
			if(!status.equals("action"))
				return "authorize";
		if(!username.isEmpty() && !password.isEmpty() && !username.contains(" ") && !password.contains(" "))
		{
			String responseStr1 = RequestForService(Startup.GetGatewayHostPort()+"/service/account/Login?username="+username+"&password="+password,"none");
			if (!responseStr1.contains("Error:")) {
				JSONParser parser = new JSONParser();
				JSONObject userJson = (JSONObject) parser.parse(responseStr1);
				if(userJson.get("Status").toString().equals("Success")) {
					response.addCookie(new Cookie("Token", userJson.get("Cookie").toString()));
					model.addAttribute("name","Success");
				}
				else {
					model.addAttribute("name","Error login");
					response.setStatus(401);
					return "greeting";
				}
			}
			else {
				model.addAttribute("name", "Error login");
				response.setStatus(500);
				return "greeting";
			}


		}
		else {
			model.addAttribute("name","Error parameters");
			response.setStatus(400);
			return "greeting";
		}
		return "authorize";
	}


	@GetMapping("/historycall")
	public String Historycall(
			@RequestParam(name="page", required=false, defaultValue= "0") String page,
			@CookieValue(name="Token", defaultValue= "") String token,
			Model model,
			HttpServletResponse response) throws IOException, ParseException {
		if(!token.isEmpty() && !token.contains(" ") && !page.contains(" "))
		{
			boolean status1 = false;
			boolean status2 = false;
			JSONObject userJson = new JSONObject();
			String username = "";
			String responseStr1 = RequestForService(Startup.GetGatewayHostPort()+"/service/account/UserInfo",token);
			if (!responseStr1.contains("Error:")) {
				JSONParser parser = new JSONParser();
				userJson = (JSONObject) parser.parse(responseStr1);
				if(userJson.get("Status").equals("Success") && userJson.get("IsLogged").toString().equals("true")) {
					username = userJson.get("username").toString();
					status1 = true;
				}
			}
			if(status1) {
				String responseStr2 = RequestForService(Startup.GetGatewayHostPort() + "/service/calls/Show?username=" + username+"&page="+page, "none");
				if (!responseStr2.contains("Error:")) {
					JSONParser parser = new JSONParser();
					userJson = (JSONObject) parser.parse(responseStr2);
					if (userJson.get("Status").toString().equals("Success")) {
						status2 = true;
					}
				}
			}
			if(status2) {
				List<String> listjson = (List<String>)userJson.get("History");
				List<String> datestring = new ArrayList<>();
				List<String> typestring = new ArrayList<>();
				List<String> namestring = new ArrayList<>();
				List<String> durationstring = new ArrayList<>();
				List<Integer> historystringiter = new ArrayList<>();
				for(int i = 0; i < listjson.size(); i++)
				{
					String[] elements = listjson.get(i).split(";");
					datestring.add(elements[0]);
					if(elements[1].equals("1"))
						typestring.add("Входящий");
					else if(elements[1].equals("2"))
						typestring.add("Исходящий");
					else
						typestring.add("NULL");

					namestring.add(elements[2]);
					durationstring.add(elements[3]);
					historystringiter.add(i);
				}
				model.addAttribute("datestring", datestring);
				model.addAttribute("typestring", typestring);
				model.addAttribute("namestring", namestring);
				model.addAttribute("durationstring", durationstring);
				model.addAttribute("historystringiter", historystringiter);
				model.addAttribute("username", username);
				int intPage = Integer.parseInt(page);
				if(intPage>0) {
					model.addAttribute("page", String.valueOf(intPage - 1));
					model.addAttribute("page2", String.valueOf(intPage));
					model.addAttribute("page3", String.valueOf(intPage + 1));
				} else {
					model.addAttribute("page2", String.valueOf(intPage));
					model.addAttribute("page3", String.valueOf(intPage + 1));
				}
			}
			else
				return "redirect:https://194.58.121.174:8443/unauthorize";
		}
		else {
			response.setStatus(400);
		}
		return "historycall";
	}

	@GetMapping("/phonebook")
	public String Phonebook(
			@RequestParam(name="page", required=false, defaultValue= "0") String page,
			@RequestParam(name="username", required=false, defaultValue= "0") String inputusername,
			@RequestParam(name="status", required=false, defaultValue= "0") String status,
			@CookieValue(name="Token", defaultValue= "") String token,
			Model model,
			HttpServletResponse response) throws IOException, ParseException {
		if(!token.isEmpty() && !token.contains(" ") && !page.contains(" "))
		{
			boolean status1 = false;
			boolean status2 = false;
			JSONObject userJson = new JSONObject();
			String username = "";
			String responseStr1 = RequestForService(Startup.GetGatewayHostPort()+"/service/account/UserInfo",token);
			if (!responseStr1.contains("Error:")) {
				JSONParser parser = new JSONParser();
				userJson = (JSONObject) parser.parse(responseStr1);
				if(userJson.get("Status").equals("Success") && userJson.get("IsLogged").toString().equals("true")) {
					username = userJson.get("username").toString();
					status1 = true;
				}
			}
			if(status1) {
				String responseStr2 = RequestForService(Startup.GetGatewayHostPort() + "/service/account/UsersList?page="+page, token);
				if (!responseStr2.contains("Error:")) {
					JSONParser parser = new JSONParser();
					userJson = (JSONObject) parser.parse(responseStr2);
					if (userJson.get("Status").toString().equals("Success")) {
						status2 = true;
					}
				}
			}
			if(status2) {
				List<String> listjson = (List<String>)userJson.get("UserList");
				List<String> namestring = new ArrayList<>();
				List<Integer> historystringiter = new ArrayList<>();
				for(int i = 0; i < listjson.size(); i++)
				{
					//String[] elements = listjson.get(i).split(";");
					namestring.add(listjson.get(i));
					historystringiter.add(i);
				}
				model.addAttribute("namestring", namestring);
				model.addAttribute("historystringiter", historystringiter);
				model.addAttribute("username", username);
				int intPage = Integer.parseInt(page);
				if(intPage>0) {
					model.addAttribute("page", String.valueOf(intPage - 1));
					model.addAttribute("page2", String.valueOf(intPage));
					model.addAttribute("page3", String.valueOf(intPage + 1));
				} else {
					model.addAttribute("page2", String.valueOf(intPage));
					model.addAttribute("page3", String.valueOf(intPage + 1));
				}
			}
			else
				return "redirect:https://194.58.121.174:8443/unauthorize";
		}
		else {
			response.setStatus(400);
		}
		if(status.equals("action"))
		{
			RequestForService(Startup.GetGatewayHostPort()+"/service/account/Add_phonebook?username="+inputusername,token);
		}
		return "phonebook";

	}

	private boolean isNumber(String str) {
		if (str == null || str.isEmpty()) return false;
		for (int i = 0; i < str.length(); i++) {
			if (!Character.isDigit(str.toCharArray()[i])) return false;
		}
		return true;
	}

	@GetMapping("/payment")
	public String ShowCash(@CookieValue(name="Token", defaultValue= "") String token,
						   @RequestParam(name="coins", required=false, defaultValue= "") String cash,
						Model model,
						HttpServletResponse response) throws IOException, ParseException {

		if(!token.isEmpty() && !token.contains(" "))
		{
			boolean status1 = false;
			boolean status2 = false;
			JSONObject userJson = new JSONObject();
			String username = "";
			String responseStr1 = RequestForService(Startup.GetGatewayHostPort()+"/service/account/UserInfo",token);
			if (!responseStr1.contains("Error:")) {
				JSONParser parser = new JSONParser();
				userJson = (JSONObject) parser.parse(responseStr1);
				if(userJson.get("Status").equals("Success") && userJson.get("IsLogged").toString().equals("true")) {
					username = userJson.get("username").toString();
					status1 = true;
					if(isNumber(cash))
						RequestForService(Startup.GetGatewayHostPort() + "/service/payment/Add_cash?username=" + username+"&cash="+cash, "none");
				}
			}
			if(status1) {
				String responseStr2 = RequestForService(Startup.GetGatewayHostPort() + "/service/payment/Show_cash?username=" + username, "none");
				if (!responseStr2.contains("Error:")) {
					JSONParser parser = new JSONParser();
					userJson = (JSONObject) parser.parse(responseStr2);
					if (userJson.get("Status").equals("Success")) {
						status2 = true;
					}
				}
			}
			if(status2) {
				model.addAttribute("cash", userJson.getAsString("cash"));
				model.addAttribute("username", username);
			}
			else
				return "redirect:https://194.58.121.174:8443/unauthorize";
		}
		else {
			response.setStatus(400);
		}
		return "payment";
	}

	@GetMapping("/registration")
	public String Register(@RequestParam(name="username", required=false, defaultValue= "") String username,
						@RequestParam(name="password", required=false, defaultValue= "") String password,
						@RequestParam(name="role", required=false, defaultValue= "2") String role,
						   @RequestParam(name="status", required=false, defaultValue= "") String status,
						Model model,
						HttpServletResponse response) throws IOException, ParseException {
		if(!status.equals("action"))
			return "register";
		if(!username.isEmpty() && !password.isEmpty() && !username.contains(" ") && !password.contains(" "))
		{
			boolean status1 = false;
			boolean status2 = false;
			String responseStr1 = RequestForService(Startup.GetGatewayHostPort()+"/service/account/Create?username="+username+"&password="+password+"&role="+role,"none");
			if (!responseStr1.contains("Error:")) {
				JSONParser parser = new JSONParser();
				JSONObject userJson = (JSONObject) parser.parse(responseStr1);
				if(userJson.get("Status").equals("Success")) {
					response.addCookie(new Cookie("Token", userJson.get("Cookie").toString()));
					status1 = true;
				}
			}
			String responseStr2 = RequestForService(Startup.GetGatewayHostPort()+"/service/payment/New_purse?username="+username,"none");
			if (!responseStr2.contains("Error:")) {
				JSONParser parser = new JSONParser();
				JSONObject userJson = (JSONObject) parser.parse(responseStr2);
				if(userJson.get("Status").equals("Success")) {
					status2 = true;
					return "redirect:https://194.58.121.174:8443/unauthorize";
				}
			}
			if(status1 && status2)
				model.addAttribute("name","Success");
			else
				model.addAttribute("name","Error create user or purse");
		}
		else {
			model.addAttribute("name","Error parameters");
			response.setStatus(400);
		}
		return "greeting";
	}

	@GetMapping("/addcash")
	public String AddCash(@CookieValue(name="Token", defaultValue= "") String token,
						  @RequestParam(name="cash", required=false, defaultValue= "") String cash,
						   Model model,
						   HttpServletResponse response) throws IOException, ParseException {
		if(!token.isEmpty() && !token.contains(" ") && !cash.contains(" "))
		{
			boolean status1 = false;
			boolean status2 = false;
			JSONObject userJson = new JSONObject();
			String username = "";
			String responseStr1 = RequestForService(Startup.GetGatewayHostPort()+"/service/account/UserInfo",token);
			if (!responseStr1.contains("Error:")) {
				JSONParser parser = new JSONParser();
				userJson = (JSONObject) parser.parse(responseStr1);
				if(userJson.get("Status").equals("Success") && userJson.get("IsLogged").toString().equals("true")) {
					username = userJson.get("username").toString();
					status1 = true;
				}
			}
			if(status1) {
				String responseStr2 = RequestForService(Startup.GetGatewayHostPort() + "/service/payment/Add_cash?username=" + username+"&cash="+cash, "none");
				if (!responseStr2.contains("Error:")) {
					JSONParser parser = new JSONParser();
					userJson = (JSONObject) parser.parse(responseStr2);
					if (userJson.get("Status").equals("Success")) {
						status2 = true;
					}
				}
			}
			if(status2)
				model.addAttribute("name","Success add");
			else
				model.addAttribute("name","Error token");
		}
		else {
			model.addAttribute("name","Error parameters");
			response.setStatus(400);
		}
		return "payment";
	}


	@GetMapping("/withdraw")
	public String WithdrawCash(@CookieValue(name="Token", defaultValue= "") String token,
							   @RequestParam(name="cash", required=false, defaultValue= "") String cash,
							   Model model,
							   HttpServletResponse response) throws IOException, ParseException {
		if(!token.isEmpty() && !token.contains(" ") && !cash.contains(" "))
		{
			boolean status1 = false;
			boolean status2 = false;
			JSONObject userJson = new JSONObject();
			String username = "";
			String responseStr1 = RequestForService(Startup.GetGatewayHostPort()+"/service/account/UserInfo",token);
			if (!responseStr1.contains("Error:")) {
				JSONParser parser = new JSONParser();
				userJson = (JSONObject) parser.parse(responseStr1);
				if(userJson.get("Status").equals("Success") && userJson.get("IsLogged").toString().equals("true")) {
					username = userJson.get("username").toString();
					status1 = true;
				}
			}
			if(status1) {
				String responseStr2 = RequestForService(Startup.GetGatewayHostPort() + "/service/payment/Withdraw_cash?username=" + username+"&cash="+cash, "none");
				if (!responseStr2.contains("Error:")) {
					JSONParser parser = new JSONParser();
					userJson = (JSONObject) parser.parse(responseStr2);
					if (userJson.get("Status").equals("Success")) {
						status2 = true;
					}
				}
			}
			if(status2)
				model.addAttribute("name","Success withdraw");
			else
				model.addAttribute("name","Error token");
		}
		else {
			model.addAttribute("name","Error parameters");
			response.setStatus(400);
		}
		return "greeting";
	}

	@GetMapping("/addcall")
	public String AddCall(@CookieValue(name="Token", defaultValue= "") String token,
						  @RequestParam(name="duration", required=false, defaultValue= "") String duration,
						  @RequestParam(name="usernameto", required=false, defaultValue= "") String usernameto,
						  @RequestParam(name="datetime", required=false, defaultValue= "") String datetime,
						  Model model,
						  HttpServletResponse response) throws IOException, ParseException {
		if(!token.isEmpty() && !token.contains(" ") && !duration.contains(" "))
		{
			boolean status1 = false;
			boolean status2 = false;
			JSONObject userJson = new JSONObject();
			String username = "";
			String responseStr1 = RequestForService(Startup.GetGatewayHostPort()+"/service/account/UserInfo",token);
			if (!responseStr1.contains("Error:")) {
				JSONParser parser = new JSONParser();
				userJson = (JSONObject) parser.parse(responseStr1);
				if(userJson.get("Status").equals("Success") && userJson.get("IsLogged").toString().equals("true")) {
					username = userJson.get("username").toString();
					status1 = true;
				}
			}
			if(status1) {
				String responseStr2 = RequestForService(Startup.GetGatewayHostPort() + "/service/calls/New?username=" + username+"&duration="+duration+"&usernameto="+usernameto+"&datetime="+datetime, "none");
				if (!responseStr2.contains("Error:")) {
					JSONParser parser = new JSONParser();
					userJson = (JSONObject) parser.parse(responseStr2);
					if (userJson.get("Status").equals("Success")) {
						status2 = true;
					}
				}
			}
			if(status2)
				model.addAttribute("name","Success add");
			else
				model.addAttribute("name","Error token");
		}
		else {
			model.addAttribute("name","Error parameters");
			response.setStatus(400);
		}
		return "greeting";
	}

	@GetMapping("/Account_logs")
	public String AccountLogs(@CookieValue(name="Token", defaultValue= "") String token,
						  @RequestParam(name="page", required=false, defaultValue= "") String page,
						  Model model,
						  HttpServletResponse response) throws IOException, ParseException {
		if(!token.isEmpty() && !token.contains(" ") && !page.contains(" "))
		{
			boolean status1 = false;
			JSONObject userJson = new JSONObject();
			String logs = "";
			String responseStr1 = RequestForService(Startup.GetGatewayHostPort()+"/service/account/Logs?page="+page,token);
			if (!responseStr1.contains("Error:")) {
				JSONParser parser = new JSONParser();
				userJson = (JSONObject) parser.parse(responseStr1);
				if(userJson.get("Status").equals("Success") && userJson.containsKey("logs")) {
					logs = userJson.get("logs").toString();
					status1 = true;
				}
			}
			if(status1)
				model.addAttribute("name",logs);
			else
				model.addAttribute("name","Error token");
		}
		else {
			model.addAttribute("name","Error parameters");
			response.setStatus(400);
		}
		return "greeting";
	}

	@GetMapping("/Calls_logs")
	public String CallsLogs(
							  @RequestParam(name="page", required=false, defaultValue= "") String page,
							  Model model,
							  HttpServletResponse response) throws IOException, ParseException {
		if(!page.contains(" "))
		{
			boolean status1 = false;
			JSONObject userJson = new JSONObject();
			String logs = "";
			String responseStr1 = RequestForService(Startup.GetGatewayHostPort()+"/service/calls/Logs?page="+page,"none");
			if (!responseStr1.contains("Error:")) {
				JSONParser parser = new JSONParser();
				userJson = (JSONObject) parser.parse(responseStr1);
				if(userJson.get("Status").equals("Success") && userJson.containsKey("logs")) {
					logs = userJson.get("logs").toString();
					status1 = true;
				}
			}
			if(status1)
				model.addAttribute("name",logs);
			else
				model.addAttribute("name","Error");
		}
		else {
			model.addAttribute("name","Error parameters");
			response.setStatus(400);
		}
		return "greeting";
	}

	@GetMapping("/Payment_logs")
	public String PaymentLogs(
			@RequestParam(name="page", required=false, defaultValue= "") String page,
			Model model,
			HttpServletResponse response) throws IOException, ParseException {
		if(!page.contains(" "))
		{
			boolean status1 = false;
			JSONObject userJson = new JSONObject();
			String logs = "";
			String responseStr1 = RequestForService(Startup.GetGatewayHostPort()+"/service/payment/Logs?page="+page,"none");
			if (!responseStr1.contains("Error:")) {
				JSONParser parser = new JSONParser();
				userJson = (JSONObject) parser.parse(responseStr1);
				if(userJson.get("Status").equals("Success") && userJson.containsKey("logs")) {
					logs = userJson.get("logs").toString();
					status1 = true;
				}
			}
			if(status1)
				model.addAttribute("name",logs);
			else
				model.addAttribute("name","Error");
		}
		else {
			model.addAttribute("name","Error parameters");
			response.setStatus(400);
		}
		return "greeting";
	}




/*
	@GetMapping("/UsersList")
	@ResponseBody
	public HashMap<String, Object> UserList(@RequestParam(name="page", required=false, defaultValue= "0") String page,
											@CookieValue(name="Token", defaultValue="") String token,
										 	HttpServletResponse response) {
		AccountModel model = new AccountModel();
		HashMap<String, Object> jsonAnswer = new LinkedHashMap<String, Object>();
		if(!token.isEmpty())
		{
			List<String> userList = model.GetUserNames(token,Integer.parseInt(page));
			if(model.GetQueryStatus()) {
				setStatus(200,response,jsonAnswer);
				jsonAnswer.put("UserList",userList);
			}
			else {
				if(!model.GetDbStatus()) {
					setStatus(500,response,jsonAnswer);
				} else {
					setStatus(404,response,jsonAnswer);
				}
			}
		}
		else {
			setStatus(401,response,jsonAnswer);
		}
		return jsonAnswer;
	}

	@GetMapping("/RoleList")
	@ResponseBody
	public HashMap<String, Object> user(HttpServletResponse response) {
		AccountModel model = new AccountModel();
		HashMap<String, Object> jsonAnswer = new LinkedHashMap<String, Object>();
			List<String> userRoles = model.GetAllRoles();
			if(model.GetQueryStatus()) {
				setStatus(200,response,jsonAnswer);
				jsonAnswer.put("UserRoles",userRoles);
			}
			else {
					setStatus(500,response,jsonAnswer);
			}
		return jsonAnswer;
	}


	@GetMapping("/UserInfo")
	@ResponseBody
	public HashMap<String, Object> UserInfo(@CookieValue(name="Token", defaultValue="") String token,
											HttpServletResponse response) {
		AccountModel model = new AccountModel();
		HashMap<String, Object> jsonAnswer = new LinkedHashMap<String, Object>();
		if(!token.isEmpty())
		{
			boolean IsLogged = model.IsLogged(token);
			boolean queryStatus1 = IsLogged;
			String role = model.GetRole(token);
			boolean queryStatus2 = model.GetQueryStatus();
			String username = model.GetUsername(token);
			boolean queryStatus3 = model.GetQueryStatus();
			if(queryStatus1 & queryStatus2 & queryStatus1) {
				setStatus(200,response,jsonAnswer);
				jsonAnswer.put("IsLogged",IsLogged);
				jsonAnswer.put("username",username);
				jsonAnswer.put("role",role);
			}
			else {
				if(!model.GetDbStatus()) {
					setStatus(500,response,jsonAnswer);
				} else {
					setStatus(401,response,jsonAnswer);
				}
			}
		}
		else {
			setStatus(401,response,jsonAnswer);
		}
		return jsonAnswer;
	}


	@GetMapping("/Create")
	@ResponseBody
	public HashMap<String, Object> Create(@RequestParam(name="username", required=false, defaultValue= "") String username,
										@RequestParam(name="password", required=false, defaultValue= "") String password,
										  @RequestParam(name="role", required=false, defaultValue= "") String role,
										HttpServletResponse response) {
		AccountModel model = new AccountModel();
		HashMap<String, Object> jsonAnswer = new LinkedHashMap<String, Object>();
		if (!username.isEmpty() && !password.isEmpty() && !role.isEmpty()) {
			if (model.CreateUser(username, password, role)) {
				setStatus(201, response, jsonAnswer);
			} else {
				if (!model.GetDbStatus()) {
					setStatus(500, response, jsonAnswer);
				} else {
					setStatus(406, response, jsonAnswer);
				}
			}
		} else {
			setStatus(400, response, jsonAnswer);
		}
		return jsonAnswer;
	}

	@GetMapping("/Logs")
	@ResponseBody
	public HashMap<String, Object> Logs(@CookieValue(name="Token", defaultValue="") String token,
										@RequestParam(name="page", required=false, defaultValue= "0") String page,
											HttpServletResponse response) {
		AccountModel model = new AccountModel();
		HashMap<String, Object> jsonAnswer = new LinkedHashMap<String, Object>();
		if(!token.isEmpty())
		{
			if(model.IsLogged(token)) {
				List<String> logs = model.GetLogs(Integer.parseInt(page));
				if (model.GetQueryStatus()){
					setStatus(200, response, jsonAnswer);
					jsonAnswer.put("logs", logs);
				}
				else {
					setStatus(500,response,jsonAnswer);
				}
			}
			else {
				if(!model.GetDbStatus()) {
					setStatus(500,response,jsonAnswer);
				} else {
					setStatus(401,response,jsonAnswer);
				}
			}
		}
		else {
			setStatus(401,response,jsonAnswer);
		}
		return jsonAnswer;
	}


*/

	@GetMapping("/Video/CallRequest")
	public String SetCallRequest(@CookieValue(name="Token", defaultValue="") String token,
								 @RequestParam(name="username", required=false, defaultValue= "") String username,
								 @RequestParam(name="firstTime", required=false, defaultValue= "") String firstTime,
								 @RequestParam(name="countfromicecandidates", required=false, defaultValue= "") String countfromicecandidates,
								 Model uimodel,
								 HttpServletResponse response) throws IOException {
		RequestForService(Startup.GetGatewayHostPort()+"/service/account/Video/CallRequest?username="+username+"&firstTime="+firstTime+"&countfromicecandidates"+countfromicecandidates,token);
		return "testfile";
	}

	@PostMapping("/Video/CallRequestFromIceCandidates")
	public String SetCallRequestFromIceCandidates(@CookieValue(name="Token", defaultValue="") String token,
												  @RequestBody String jsonInputString,
												  Model uimodel,
												  HttpServletResponse response) throws java.text.ParseException, IOException {
		RequestPostForService(Startup.GetGatewayHostPort()+"/service/account/Video/CallRequestFromIceCandidates",jsonInputString,token);
		return "testfile";
	}

	@PostMapping("/Video/CallRequestToIceCandidates")
	public String SetCallRequestToIceCandidates(@CookieValue(name="Token", defaultValue="") String token,
												@RequestParam(name="username", required=false, defaultValue= "") String username,
												@RequestBody String jsonInputString,
												Model uimodel,
												HttpServletResponse response) throws java.text.ParseException, IOException {
		RequestPostForService(Startup.GetGatewayHostPort()+"/service/account/Video/CallRequestToIceCandidates?username="+username,jsonInputString,token);
		return "testfile";
	}

	@PostMapping("/Video/CallRequestFromDescription")
	public String SetCallRequestFromDescription(@CookieValue(name="Token", defaultValue="") String token,
												@RequestBody String jsonInputString,
												Model uimodel,
												HttpServletResponse response) throws java.text.ParseException, IOException {
		RequestPostForService(Startup.GetGatewayHostPort()+"/service/account/Video/CallRequestFromDescription",jsonInputString,token);
		return "testfile";
	}

	@PostMapping("/Video/CallRequestToDescription")
	public String SetCallRequestToDescription(@CookieValue(name="Token", defaultValue="") String token,
											  @RequestParam(name="username", required=false, defaultValue= "") String username,
											  @RequestBody String jsonInputString,
											  Model uimodel,
											  HttpServletResponse response) throws java.text.ParseException, IOException {
		RequestPostForService(Startup.GetGatewayHostPort()+"/service/account/Video/CallRequestToDescription?username="+username,jsonInputString,token);
		return "testfile";
	}

private String RequestForService(String urlString,String cookie)  throws IOException
{
	InputStream is;
	InputStreamReader reader;
	try {
		URLConnection connection = new URL(urlString).openConnection();
		connection.setConnectTimeout(Startup.GetTimeout());
		connection.setDoOutput(true);
		connection.setDoInput(true);
		if(!cookie.contains("none"))
			connection.addRequestProperty("Cookie", "Token=" + cookie);
		is = connection.getInputStream();
		reader = new InputStreamReader(is);
	}
	catch (Exception e)
	{
		return "Error:"+e.getMessage();
	}
	char[] buffer = new char[1024];
	int rc;

	StringBuilder sb = new StringBuilder();
	while ((rc = reader.read(buffer)) != -1)
		sb.append(buffer, 0, rc);
	reader.close();
	return sb.toString();
}

	private String RequestPostForService(String urlString,String postdata, String cookie)  throws IOException
	{
		InputStream is;
		InputStreamReader reader;
		try {
			URLConnection connection = new URL(urlString).openConnection();
			connection.setConnectTimeout(Startup.GetTimeout());
			connection.setDoOutput(true);
			connection.setDoInput(true);
			if(!cookie.contains("none"))
				connection.addRequestProperty("Cookie", "Token=" + cookie);
			connection.addRequestProperty("Content-type", "application/json");
			OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
			writer.write(postdata);
			writer.flush();
			is = connection.getInputStream();
			reader = new InputStreamReader(is);
		}
		catch (Exception e)
		{
			return "Error:"+e.getMessage();
		}
		char[] buffer = new char[1024];
		int rc;

		StringBuilder sb = new StringBuilder();
		while ((rc = reader.read(buffer)) != -1)
			sb.append(buffer, 0, rc);
		reader.close();
		return sb.toString();
	}
}