package com.rsoi2app_account.controllers;

import com.rsoi2app_account.models.AccountModel;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

@Controller
public class AccountController {

	@GetMapping("/Login")
	@ResponseBody
	public HashMap<String, Object> Login(@RequestParam(name="username", required=false, defaultValue= "") String username,
										  @RequestParam(name="password", required=false, defaultValue= "") String password,
										  HttpServletResponse response) {
		AccountModel model = new AccountModel();
		model.SetLogs("/Login?username="+username+"&password="+password);
		HashMap<String, Object> jsonAnswer = new LinkedHashMap<String, Object>();
		if(!username.isEmpty() && !password.isEmpty())
		{
			String authCookie = model.Login(username,password);
			if(model.GetQueryStatus()) {
				response.addCookie(new Cookie("Token", authCookie));
				jsonAnswer.put("Cookie",authCookie);
				setStatus(200,response,jsonAnswer);
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
			setStatus(400,response,jsonAnswer);
		}
		try {
			model.finalize();
		} catch (SQLException exc) {}
		return jsonAnswer;
	}

	@GetMapping("/PhonebookList")
	@ResponseBody
	public HashMap<String, Object> PhonebookList(
			@CookieValue(name="Token", defaultValue="") String token,
			HttpServletResponse response) {
		AccountModel model = new AccountModel();
		model.SetLogs("/PhonebookList?Token="+token);
		HashMap<String, Object> jsonAnswer = new LinkedHashMap<String, Object>();
		if(!token.isEmpty())
		{
			List<String> userList = model.GetPhonebookNames(token);
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
		try {
			model.finalize();
		} catch (SQLException exc) {}
		return jsonAnswer;
	}


	@GetMapping("/UsersList")
	@ResponseBody
	public HashMap<String, Object> UserList(@RequestParam(name="page", required=false, defaultValue= "0") String page,
											@CookieValue(name="Token", defaultValue="") String token,
										 	HttpServletResponse response) {
		AccountModel model = new AccountModel();
		model.SetLogs("/UsersList?Token="+token+"&page="+page);
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
		try {
			model.finalize();
		} catch (SQLException exc) {}
		return jsonAnswer;
	}

	@GetMapping("/Logout")
	@ResponseBody
	public HashMap<String, Object> UserList(@CookieValue(name="Token", defaultValue="") String token,
											HttpServletResponse response) {
		AccountModel model = new AccountModel();
		HashMap<String, Object> jsonAnswer = new LinkedHashMap<String, Object>();
		if(!token.isEmpty())
		{
			if(model.Logout(token)) {
				setStatus(200,response,jsonAnswer);
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
		try {
			model.finalize();
		} catch (SQLException exc) {}
		return jsonAnswer;
	}

	@GetMapping("/RoleList")
	@ResponseBody
	public HashMap<String, Object> user(HttpServletResponse response) {
		AccountModel model = new AccountModel();
		model.SetLogs("/RoleList");
		HashMap<String, Object> jsonAnswer = new LinkedHashMap<String, Object>();
			List<String> userRoles = model.GetAllRoles();
			if(model.GetQueryStatus()) {
				setStatus(200,response,jsonAnswer);
				jsonAnswer.put("UserRoles",userRoles);
			}
			else {
					setStatus(500,response,jsonAnswer);
			}
		try {
			model.finalize();
		} catch (SQLException exc) {}
		return jsonAnswer;
	}


	@GetMapping("/UserInfo")
	@ResponseBody
	public HashMap<String, Object> UserInfo(@CookieValue(name="Token", defaultValue="") String token,
											HttpServletResponse response) {
		AccountModel model = new AccountModel();
		model.SetLogs("/UserInfo?Token="+token);
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
		try {
			model.finalize();
		} catch (SQLException exc) {}
		return jsonAnswer;
	}


	@GetMapping("/Create")
	@ResponseBody
	public HashMap<String, Object> Create(@RequestParam(name="username", required=false, defaultValue= "") String username,
										@RequestParam(name="password", required=false, defaultValue= "") String password,
										  @RequestParam(name="role", required=false, defaultValue= "") String role,
										HttpServletResponse response) {
		AccountModel model = new AccountModel();
		model.SetLogs("/Create?username="+username+"&password="+password+"&password="+role);
		HashMap<String, Object> jsonAnswer = new LinkedHashMap<String, Object>();
		if (!username.isEmpty() && !password.isEmpty() && !role.isEmpty()) {
			if (model.CreateUser(username, password, role)) {
				setStatus(200, response, jsonAnswer);
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
		try {
			model.finalize();
		} catch (SQLException exc) {}
		return jsonAnswer;
	}

	@GetMapping("/Add_phonebook")
	@ResponseBody
	public HashMap<String, Object> AddPhoneBook(@RequestParam(name="username", required=false, defaultValue= "") String usernameto,
												@CookieValue(name="Token", defaultValue="") String token,
										  HttpServletResponse response) {
		AccountModel model = new AccountModel();
		HashMap<String, Object> jsonAnswer = new LinkedHashMap<String, Object>();
		boolean IsLogged = model.IsLogged(token);
		if (!usernameto.isEmpty() && !usernameto.contains(" ") && IsLogged) {
			if (model.AddPhoneBook(model.GetUsername(token), usernameto)) {
				setStatus(200, response, jsonAnswer);
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
		try {
			model.finalize();
		} catch (SQLException exc) {}
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
		try {
			model.finalize();
		} catch (SQLException exc) {}
		return jsonAnswer;
	}

	@GetMapping("/Video")
	public String Video(@CookieValue(name="Token", defaultValue="") String token,
						Model uimodel,
						HttpServletResponse response) {
		AccountModel model = new AccountModel();
		model.SetLogs("/Video?Token="+token);
		if(!token.isEmpty() && model.IsLogged(token))
		{
			uimodel.addAttribute("alllist",model.GetPhonebookNames(token));
		}
		else {
			if(!model.GetDbStatus()) {
				uimodel.addAttribute("name","Error db status");
			} else {
				uimodel.addAttribute("name","You are not authorized");
			}
			return "testfile";
		}
		try {
			model.finalize();
		} catch (SQLException exc) {}
		return "greeting";
	}

	@GetMapping("/Video/CallRequest")
	public String SetCallRequest(@CookieValue(name="Token", defaultValue="") String token,
								 @RequestParam(name="username", required=false, defaultValue= "") String username,
								 @RequestParam(name="firstTime", required=false, defaultValue= "") String firstTime,
								 @RequestParam(name="countfromicecandidates", required=false, defaultValue= "") String countfromicecandidates,
								 Model uimodel,
								 HttpServletResponse response) {
		AccountModel model = new AccountModel();
		model.SetLogs("/Video/Callrequest?Token="+token+"&username="+username+"&usernamefrom="+model.GetUsername(token)+"&firstTime="+firstTime);
		if(!token.isEmpty() && model.IsLogged(token) && !username.isEmpty() && !username.contains(" ") && !firstTime.isEmpty() && !firstTime.contains(" "))
		{
			model.SetCallRequest( model.GetUsername(token),username,Boolean.valueOf(firstTime),countfromicecandidates);
			uimodel.addAttribute("name","Success");
		}
		else {
			if(!model.GetDbStatus()) {
				uimodel.addAttribute("name","Error db status");
			} else {
				uimodel.addAttribute("name","You are not authorized");
			}
		}
		try {
			model.finalize();
		} catch (SQLException exc) {}
		return "testfile";
	}

	@PostMapping("/Video/CallRequestFromIceCandidates")
	public String SetCallRequestFromIceCandidates(@CookieValue(name="Token", defaultValue="") String token,
												  @RequestBody String jsonInputString,
												  Model uimodel,
												  HttpServletResponse response) throws ParseException {
		AccountModel model = new AccountModel();
		model.SetLogs("/Video/CallrequestFromIceCandidates?Token="+token);
		if(!jsonInputString.isEmpty())
		{
			model.SetCallRequestFromIceCandidates( model.GetUsername(token),jsonInputString);
			uimodel.addAttribute("name","Success");
		}
		else {
			if(!model.GetDbStatus()) {
				uimodel.addAttribute("name","Error db status");
			} else {
				uimodel.addAttribute("name","You are not authorized");
			}
		}
		try {
			model.finalize();
		} catch (SQLException exc) {}
		return "testfile";
	}

	@PostMapping("/Video/CallRequestToIceCandidates")
	public String SetCallRequestToIceCandidates(@CookieValue(name="Token", defaultValue="") String token,
												@RequestParam(name="username", required=false, defaultValue= "") String username,
												@RequestBody String jsonInputString,
												Model uimodel,
												HttpServletResponse response) throws ParseException {
		AccountModel model = new AccountModel();
		model.SetLogs("/Video/CallrequestTomIceCandidates?Token="+token+"&username="+username);
		if(!jsonInputString.isEmpty())
		{
			model.SetCallRequestToIceCandidates( username,jsonInputString);
			uimodel.addAttribute("name","Success");
		}
		else {
			if(!model.GetDbStatus()) {
				uimodel.addAttribute("name","Error db status");
			} else {
				uimodel.addAttribute("name","You are not authorized");
			}
		}
		try {
			model.finalize();
		} catch (SQLException exc) {}
		return "testfile";
	}

	@PostMapping("/Video/CallRequestFromDescription")
	public String SetCallRequestFromDescription(@CookieValue(name="Token", defaultValue="") String token,
												@RequestBody String jsonInputString,
												Model uimodel,
												HttpServletResponse response) throws ParseException {
		AccountModel model = new AccountModel();
		model.SetLogs("/Video/CallrequestFromDescription?Token="+token);
		if(!jsonInputString.isEmpty())
		{
			model.SetCallRequestFromDescription( model.GetUsername(token),jsonInputString);
			uimodel.addAttribute("name","Success");
		}
		else {
			if(!model.GetDbStatus()) {
				uimodel.addAttribute("name","Error db status");
			} else {
				uimodel.addAttribute("name","You are not authorized");
			}
		}
		try {
			model.finalize();
		} catch (SQLException exc) {}
		return "testfile";
	}

	@PostMapping("/Video/CallRequestToDescription")
	public String SetCallRequestToDescription(@CookieValue(name="Token", defaultValue="") String token,
											  @RequestParam(name="username", required=false, defaultValue= "") String username,
											  @RequestBody String jsonInputString,
											  Model uimodel,
											  HttpServletResponse response) throws ParseException {
		AccountModel model = new AccountModel();
		model.SetLogs("/Video/CallrequestToDescription?Token="+token+"&username="+username+"&jsoninputstring="+jsonInputString);
		if(!jsonInputString.isEmpty())
		{
			model.SetCallRequestToDescription(username,jsonInputString);
			uimodel.addAttribute("name","Success");
		}
		else {
			if(!model.GetDbStatus()) {
				uimodel.addAttribute("name","Error db status");
			} else {
				uimodel.addAttribute("name","You are not authorized");
			}
		}
		try {
			model.finalize();
		} catch (SQLException exc) {}
		return "testfile";
	}

	@GetMapping("/Video/CallAnswer")
	public String SetCallAnswer(@CookieValue(name="Token", defaultValue="") String token,
								@RequestParam(name="username", required=false, defaultValue= "") String username,
								@RequestParam(name="status", required=false, defaultValue= "") String status,
								@RequestParam(name="counttoicecandidates", required=false, defaultValue= "0") String counttoicecandidates,
								Model uimodel,
								HttpServletResponse response) {
		AccountModel model = new AccountModel();
		model.SetLogs("/Video/Callanswer?Token="+token+"&username="+username+"&usernamefrom="+model.GetUsername(token)+"&status="+status);
		if(!token.isEmpty() && model.IsLogged(token) && !username.isEmpty() && !username.contains(" ") && !status.isEmpty() && !status.contains(" "))
		{
			model.SetCallAnswer( model.GetUsername(token),username,status,counttoicecandidates);
			uimodel.addAttribute("name","Success");
		}
		else {
			if(!model.GetDbStatus()) {
				uimodel.addAttribute("name","Error db status");
			} else {
				uimodel.addAttribute("name","You are not authorized");
			}
		}
		try {
			model.finalize();
		} catch (SQLException exc) {}
		return "testfile";
	}

	@GetMapping("/Video/GetCallAnswer")
	@ResponseBody
	public HashMap<String, Object> GetCallAnswer(@CookieValue(name="Token", defaultValue="") String token,
												 @RequestParam(name="username", required=false, defaultValue= "") String username,
												 HttpServletResponse response) {
		AccountModel model = new AccountModel();
		HashMap<String, Object> jsonAnswer = new LinkedHashMap<String, Object>();
		model.SetLogs("/Video/GetCallanswer?Token="+token+"&username="+username+"&usernamefrom="+model.GetUsername(token));
		if(!token.isEmpty() && model.IsLogged(token) && !username.isEmpty() && !username.contains(" "))
		{
			setStatus(200,response,jsonAnswer);
			List<String> callAnswer = model.GetCallAnswer( model.GetUsername(token),username);
			if(model.GetQueryStatus()) {
				jsonAnswer.put("StatusCall", callAnswer.get(0));
			} else {
				if(!model.GetDbStatus()) {
					setStatus(500,response,jsonAnswer);
				} else {
					setStatus(401,response,jsonAnswer);
				}
			}
		}
		else {
			if(!model.GetDbStatus()) {
				setStatus(500,response,jsonAnswer);
			} else {
				setStatus(401,response,jsonAnswer);
			}
		}
		try {
			model.finalize();
		} catch (SQLException exc) {}
		return jsonAnswer;
	}

	@GetMapping("/Video/MyCallRequest")
	@ResponseBody
	public HashMap<String, Object> MyCallRequest(@CookieValue(name="Token", defaultValue="") String token,
												 HttpServletResponse response) {
		AccountModel model = new AccountModel();
		HashMap<String, Object> jsonAnswer = new LinkedHashMap<String, Object>();
		model.SetLogs("/Video/MyCallrequest?Token="+token+"&usernamefrom="+model.GetUsername(token));
		if(!token.isEmpty() && model.IsLogged(token))
		{
			setStatus(200,response,jsonAnswer);
			jsonAnswer.put("Request",model.GetCallRequest( model.GetUsername(token)));
		}
		else {
			if(!model.GetDbStatus()) {
				setStatus(500,response,jsonAnswer);
			} else {
				setStatus(401,response,jsonAnswer);
			}
		}
		try {
			model.finalize();
		} catch (SQLException exc) {}
		return jsonAnswer;
	}

	@GetMapping("/Video/GetFromIceCandidates")
	@ResponseBody
	public HashMap<String, Object> GetFromIceCandidates(@CookieValue(name="Token", defaultValue="") String token,
														@RequestParam(name="username", required=false, defaultValue= "") String username,
														HttpServletResponse response) {
		AccountModel model = new AccountModel();
		HashMap<String, Object> jsonAnswer = new LinkedHashMap<String, Object>();
		model.SetLogs("/Video/GetFromIceCandidates?Token="+token+"&username="+username+"&usernamefrom="+model.GetUsername(token));
		if(!token.isEmpty() && model.IsLogged(token) && !username.isEmpty() && !username.contains(" "))
		{
			setStatus(200,response,jsonAnswer);
			List<String> iceCandidatesAnswer = model.GetFromIceCandidates( model.GetUsername(token),username);
			if(model.GetQueryStatus()) {
				jsonAnswer.put("fromicecandidates",iceCandidatesAnswer.get(0));
			} else {
				if(!model.GetDbStatus()) {
					setStatus(500,response,jsonAnswer);
				} else {
					setStatus(401,response,jsonAnswer);
				}
			}
		}
		else {
			if(!model.GetDbStatus()) {
				setStatus(500,response,jsonAnswer);
			} else {
				setStatus(401,response,jsonAnswer);
			}
		}
		try {
			model.finalize();
		} catch (SQLException exc) {}
		return jsonAnswer;
	}

	@GetMapping("/Video/GetCountToIceCandidates")
	@ResponseBody
	public HashMap<String, Object> GetCountToIceCandidates(@CookieValue(name="Token", defaultValue="") String token,
														   @RequestParam(name="username", required=false, defaultValue= "") String username,
														   HttpServletResponse response) {
		HashMap<String, Object> jsonAnswer = new LinkedHashMap<String, Object>();
		AccountModel model = new AccountModel();
		GetCountIceCandidates(token,model.GetUsername(token),response,"counttoicecandidates", jsonAnswer );
		try {
			model.finalize();
		} catch (SQLException exc) {}
		return jsonAnswer;
	}

	@GetMapping("/Video/GetCountFromIceCandidates")
	@ResponseBody
	public HashMap<String, Object> GetCountFromIceCandidates(@CookieValue(name="Token", defaultValue="") String token,
															 @RequestParam(name="username", required=false, defaultValue= "") String username,
															 HttpServletResponse response) {
		HashMap<String, Object> jsonAnswer = new LinkedHashMap<String, Object>();
		GetCountIceCandidates(token,username, response,"countfromicecandidates",jsonAnswer );
		return jsonAnswer;
	}


	private HashMap<String, Object> GetCountIceCandidates(String token,String username, HttpServletResponse response,String nameField, HashMap<String, Object> jsonAnswer)
	{
		AccountModel model = new AccountModel();
		model.SetLogs("/Video/GetCountIceCandidates?username="+username);
		if(!token.isEmpty() && model.IsLogged(token) && !username.isEmpty() && !username.contains(" "))
		{
			setStatus(200,response,jsonAnswer);
			String countIceCandidates = model.GetStringFromTable(nameField,"Speakstars.CallRequest","WHERE fromuser='"+username+"'");
			if(model.GetQueryStatus()) {
				jsonAnswer.put(nameField,countIceCandidates);
			} else {
				if(!model.GetDbStatus()) {
					setStatus(500,response,jsonAnswer);
				} else {
					setStatus(401,response,jsonAnswer);
				}
			}
		}
		else {
			if(!model.GetDbStatus()) {
				setStatus(500,response,jsonAnswer);
			} else {
				setStatus(401,response,jsonAnswer);
			}
		}
		try {
			model.finalize();
		} catch (SQLException exc) {}
		return jsonAnswer;
	}

	@GetMapping("/Video/GetToIceCandidates")
	@ResponseBody
	public HashMap<String, Object> GetToIceCandidates(@CookieValue(name="Token", defaultValue="") String token,
													  @RequestParam(name="username", required=false, defaultValue= "") String username,
													  HttpServletResponse response) {
		AccountModel model = new AccountModel();
		HashMap<String, Object> jsonAnswer = new LinkedHashMap<String, Object>();
		model.SetLogs("/Video/GetToIceCandidates?Token="+token+"&username="+username+"&usernamefrom="+model.GetUsername(token));
		if(!token.isEmpty() && model.IsLogged(token) && !username.isEmpty() && !username.contains(" "))
		{
			setStatus(200,response,jsonAnswer);
			List<String> iceCandidatesAnswer = model.GetToIceCandidates( model.GetUsername(token),username);
			if(model.GetQueryStatus()) {
				jsonAnswer.put("toicecandidates",iceCandidatesAnswer.get(0));
			} else {
				if(!model.GetDbStatus()) {
					setStatus(500,response,jsonAnswer);
				} else {
					setStatus(401,response,jsonAnswer);
				}
			}
		}
		else {
			if(!model.GetDbStatus()) {
				setStatus(500,response,jsonAnswer);
			} else {
				setStatus(401,response,jsonAnswer);
			}
		}
		try {
			model.finalize();
		} catch (SQLException exc) {}
		return jsonAnswer;
	}

	@GetMapping("/Video/GetFromDescription")
	@ResponseBody
	public HashMap<String, Object> GetFromDescription(@CookieValue(name="Token", defaultValue="") String token,
													  @RequestParam(name="username", required=false, defaultValue= "") String username,
													  HttpServletResponse response) {
		AccountModel model = new AccountModel();
		HashMap<String, Object> jsonAnswer = new LinkedHashMap<String, Object>();
		model.SetLogs("/Video/GetFromDescription?Token="+token+"&username="+username+"&usernamefrom="+model.GetUsername(token));
		if(!token.isEmpty() && model.IsLogged(token) && !username.isEmpty() && !username.contains(" "))
		{
			setStatus(200,response,jsonAnswer);
			List<String> fromDescriptionAnswer = model.GetFromDescription( model.GetUsername(token),username);
			if(model.GetQueryStatus()) {
				jsonAnswer.put("fromdescription",fromDescriptionAnswer.get(0));
			} else {
				if(!model.GetDbStatus()) {
					setStatus(500,response,jsonAnswer);
				} else {
					setStatus(401,response,jsonAnswer);
				}
			}
		}
		else {
			if(!model.GetDbStatus()) {
				setStatus(500,response,jsonAnswer);
			} else {
				setStatus(401,response,jsonAnswer);
			}
		}
		try {
			model.finalize();
		} catch (SQLException exc) {}
		return jsonAnswer;
	}

	@GetMapping("/Video/GetToDescription")
	@ResponseBody
	public HashMap<String, Object> GetToDescription(@CookieValue(name="Token", defaultValue="") String token,
													@RequestParam(name="username", required=false, defaultValue= "") String username,
													HttpServletResponse response) {
		AccountModel model = new AccountModel();
		HashMap<String, Object> jsonAnswer = new LinkedHashMap<String, Object>();
		model.SetLogs("/Video/GetFromDescription?Token="+token+"&username="+username+"&usernamefrom="+model.GetUsername(token));
		if(!token.isEmpty() && model.IsLogged(token) && !username.isEmpty() && !username.contains(" "))
		{
			setStatus(200,response,jsonAnswer);
			List<String> toDescriptionAnswer = model.GetToDescription( model.GetUsername(token),username);
			if(model.GetQueryStatus()) {
				jsonAnswer.put("todescription",toDescriptionAnswer.get(0));
			} else {
				if(!model.GetDbStatus()) {
					setStatus(500,response,jsonAnswer);
				} else {
					setStatus(401,response,jsonAnswer);
				}
			}
		}
		else {
			if(!model.GetDbStatus()) {
				setStatus(500,response,jsonAnswer);
			} else {
				setStatus(401,response,jsonAnswer);
			}
		}
		try {
			model.finalize();
		} catch (SQLException exc) {}
		return jsonAnswer;
	}


	void setStatus(int status, HttpServletResponse response, HashMap<String, Object> jsonAnswer)
	{
		response.setStatus(status);
		if(status == 200)//Success
		{
			jsonAnswer.put("Status","Success");
			response.setStatus(200);//Success
		}
		if(status == 201)//Created
		{
			jsonAnswer.put("Status","Success");
		}
		if(status == 500)//Internal Server Error
		{
			jsonAnswer.put("Status", "Error");
			jsonAnswer.put("Status message", "Internal Server Error");
		}
		if(status == 404)//Not Found
		{
			jsonAnswer.put("Status","Error");
			jsonAnswer.put("Status message","Not Found");
		}
		if(status == 401)//Unauthorized
		{
			jsonAnswer.put("Status","Error");
			jsonAnswer.put("Status message","Unauthorized");
		}
		if(status == 400)//Bad Request
		{
			jsonAnswer.put("Status","Error");
			jsonAnswer.put("Status message","Bad Request");
		}
		if(status == 406)//Not Acceptable
		{
			jsonAnswer.put("Status","Error");
			jsonAnswer.put("Status message","Username is used");
		}
	}

}