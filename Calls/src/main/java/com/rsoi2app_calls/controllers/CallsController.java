package com.rsoi2app_calls.controllers;

import com.rsoi2app_calls.models.CallsModel;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

@Controller
public class CallsController {

	@GetMapping("/New")
	@ResponseBody
	public HashMap<String, Object> Login(@RequestParam(name="username", required=false, defaultValue= "") String username,
										  @RequestParam(name="duration", required=false, defaultValue= "") String duration,
										 @RequestParam(name="usernameto", required=false, defaultValue= "") String usernameto,
										  HttpServletResponse response) {
		java.util.Date dt = new java.util.Date();

		java.text.SimpleDateFormat sdf =
				new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		String currentTime = sdf.format(dt);
		CallsModel model = new CallsModel();
		model.SetLogs("/New/?username="+username+"&duration="+duration);
		HashMap<String, Object> jsonAnswer = new LinkedHashMap<String, Object>();
		if(!username.isEmpty() && !duration.isEmpty())
		{
			if(model.AddCall(duration,username,usernameto,currentTime)) {
				setStatus(201,response,jsonAnswer);
			}
			else {
				setStatus(500,response,jsonAnswer);
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


	@GetMapping("/Show")
	@ResponseBody
	public HashMap<String, Object> UserList(@RequestParam(name="username", required=false, defaultValue= "") String username,
											@RequestParam(name="page", required=false, defaultValue= "0") String page,
										 	HttpServletResponse response) {
		CallsModel model = new CallsModel();
		model.SetLogs("/Show/?username="+username+"&page="+page);
		HashMap<String, Object> jsonAnswer = new LinkedHashMap<String, Object>();
		if(!username.isEmpty())
		{
			List<String> callsHistory = model.ShowCallHistory(username,Integer.parseInt(page));
			if(model.GetQueryStatus()) {
				setStatus(200,response,jsonAnswer);
				jsonAnswer.put("History",callsHistory);
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

	@GetMapping("/Logs")
	@ResponseBody
	public HashMap<String, Object> Logs(@RequestParam(name="page", required=false, defaultValue= "0") String page,
										HttpServletResponse response) {
		CallsModel model = new CallsModel();
		HashMap<String, Object> jsonAnswer = new LinkedHashMap<String, Object>();
				List<String> logs = model.GetLogs(Integer.parseInt(page));
				if (model.GetQueryStatus()){
					setStatus(200, response, jsonAnswer);
					jsonAnswer.put("logs", logs);
				}
				else {
					setStatus(500,response,jsonAnswer);
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