window.onload = init;
var socket = new WebSocket("ws://localhost:8080/NET-PRO-Server/play");
socket.onmessage = onMessage;

function onMessage(event)
{
	var message = JSON.parse(event.data);
	
	if (message.action === "add")			printDeviceElement(message);
	else if (message.action === "toggle")
	{
		var node = document.getElementById(message.id);
		var statusText = node.children[2];
		
		if (message.status === "On") {
			statusText.innerHTML = "Status: " + message.status + " (<a href=\"#\" OnClick=toggleDevice(" + message.id + ")>Turn off</a>)";
		} else if (message.status === "Off") {
			statusText.innerHTML = "Status: " + message.status + " (<a href=\"#\" OnClick=toggleDevice(" + message.id + ")>Turn on</a>)";
		}
	}
	else if (message.action === "login")
	{
		if (message.status !== "success") console.log("Status: " + message.status);
		else {
			document.getElementById("login").innerHTML = message.username;
			document.getElementById("refresh").style.display = '';
			document.getElementById("create").style.display = '';
			showLobbies();
		}
	}
	else if (message.action === "browser")
	{
		console.log("Got browser message");
		if (message.status !== "update") console.log("Status: " + message.status);
		else {
			document.getElementById("content").innerHTML = "";
			
			message.lobbies.forEach(function(lobby) {
				printLobby(lobby);
			});
		}
	}
	else if (message.action === "lobby")
	{
		console.log("Got lobby message");
		
		if (message.status !== "update") console.log("Status: " + message.status);
		else {
			console.log("Name " + message.name);
			console.log("Type " + message.type);
			
			document.getElementById("content").innerHTML = "";
			
			message.players.forEach(function(player) {
				printPlayer(player);
			});
		}
	}
	else if (message.action === "create-lobby")
	{
		console.log("Creating lobby, got: " + message.status);
	}
}

/* Usage removed; kept as notes */
function printDeviceElement(device)
{
	var content = document.getElementById("content");
	
	var deviceDiv = document.createElement("div");
	deviceDiv.setAttribute("id", device.id);
	deviceDiv.setAttribute("class", "device " + device.type);
	content.appendChild(deviceDiv);
	
	var deviceName = document.createElement("span");
	deviceName.setAttribute("class", "deviceName");
	deviceName.innerHTML = device.name;
	deviceDiv.appendChild(deviceName);
	
	var deviceType = document.createElement("span");
	deviceType.innerHTML = "<b>Type:</b> " + device.type;
	deviceDiv.appendChild(deviceType);
	
	var deviceStatus = document.createElement("span");
	if (device.status === "On") {
		deviceStatus.innerHTML = "<b>Status:</b> " + device.status + " (<a href=\"#\" OnClick=toggleDevice(" + device.id + ")>Turn off</a>)";
	} else if (device.status === "Off") {
		deviceStatus.innerHTML = "<b>Status:</b> " + device.status + " (<a href=\"#\" OnClick=toggleDevice(" + device.id + ")>Turn on</a>)";
		//deviceDiv.setAttribute("class", "device off");
	}
	deviceDiv.appendChild(deviceStatus);
	
	var deviceDescription = document.createElement("span");
	deviceDescription.innerHTML = "<b>Comments:</b> " + device.description;
	deviceDiv.appendChild(deviceDescription);
	
	var removeDevice = document.createElement("span");
	removeDevice.setAttribute("class", "removeDevice");
	removeDevice.innerHTML = "<a href=\"#\" OnClick=removeDevice(" + device.id + ")>Remove device</a>";
	deviceDiv.appendChild(removeDevice);
}

/* ---------- */

function printLobby(lobby)
{
	console.log("Printing lobby");
	var content = document.getElementById("content");
	
	var deviceDiv = document.createElement("div");
	deviceDiv.setAttribute("type", lobby.type);				// TODO undefined?
	deviceDiv.setAttribute("class", "device Appliance");	// TODO fix
	content.appendChild(deviceDiv);
	
	var deviceName = document.createElement("span");
	deviceName.setAttribute("class", "deviceName");
	deviceName.innerHTML = lobby.name;
	deviceDiv.appendChild(deviceName);
	
	var deviceType = document.createElement("span");
	deviceType.innerHTML = "<b>Game type:</b> " + lobby.gameType;
	deviceDiv.appendChild(deviceType);
	
	var deviceDescription = document.createElement("span");
	deviceDescription.innerHTML = "<b>Participants:</b> " + lobby.noPlayers + " / " + lobby.maxNoPlayers;
	deviceDiv.appendChild(deviceDescription);
	
	var removeDevice = document.createElement("span");
	removeDevice.setAttribute("class", "removeDevice");
	removeDevice.innerHTML = "<a href=\"#\" OnClick=joinLobby(" + lobby.id + ")>Join</a>";
	deviceDiv.appendChild(removeDevice);
}

function printPlayer(player)
{
var content = document.getElementById("content");
	
	var deviceDiv = document.createElement("div");
	deviceDiv.setAttribute("class", "device Appliance");	// TODO fix
	content.appendChild(deviceDiv);
	
	var deviceName = document.createElement("span");
	deviceName.setAttribute("class", "deviceName");
	deviceName.innerHTML = player.username;
	deviceDiv.appendChild(deviceName);
	
	var deviceType = document.createElement("span");
	deviceType.innerHTML = "<b>EXP:</b> " + player.XP;
	deviceDiv.appendChild(deviceType);
}

function login()
{
	var LoginAction = {
			action: "login",
			username: "Whatwasit",
			password: "reFUSE",
	};
	socket.send(JSON.stringify(LoginAction));
}

function showLobbies()
{	
	var BrowserAction = {
			action: "browser",
		};
		socket.send(JSON.stringify(BrowserAction));
}

function createLobby()
{
	var name = document.getElementById("addDeviceForm").elements["device_name"].value;
	
	var CreateLobbyAction = {
			action: "create-lobby",
			name: name,
		};
	socket.send(JSON.stringify(CreateLobbyAction));
	
	hideForm();
}

function joinLobby(id)
{
	var joinAction = {
			action: "join-lobby",
			id: id,
		};
		socket.send(JSON.stringify(joinAction));
}


/* --------- */

function showForm() {
	document.getElementById("addDeviceForm").style.display = '';
}

function hideForm() {
	document.getElementById("addDeviceForm").style.display = "none";
}

function init()
{
	hideForm();
	document.getElementById("refresh").style.display = "none";
	document.getElementById("create").style.display = "none";
}