<!--<%@page contentType="text/html" pageEncoding="UTF-8"%>-->
<!DOCTYPE html>
<html>

<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Websocket Demo</title>
</head>
<body>
<h1 style="text-align:center">Agent Connector</h1>
<form action="/dispatch">
  user: <input type="text" name="user"><br>
  message: <input type="text" name="message"><br>
  <input type="submit" value="Submit">
</form>
<div id="editor">
    <textarea cols="200" rows="100" id="editArea" name="contactus"></textarea>
</div>
</body>
<script type="text/javascript" src="lib/jquery-2.0.2.min.js"></script>
<script type="text/javascript" src="lib/underscore-min.js"></script>
<script type="text/javascript" src="lib/backbone.js"></script>
<script type="text/javascript" src="agentConnector.js"></script>
</html>
