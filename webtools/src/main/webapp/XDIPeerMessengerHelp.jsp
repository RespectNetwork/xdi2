<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="xdi2.core.properties.XDI2Properties" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>XDI Peer Messenger - Help</title>
<script type="text/javascript" src="tabber.js"></script>
<link rel="stylesheet" target="_blank" href="style.css" TYPE="text/css" MEDIA="screen">
</head>
<body>
	<div id="imgtop"><img id="imgtopleft" src="images/xdi2-topleft.png"><a href="http://projectdanube.org/"><img id="imgtopright" src="images/xdi2-topright.png"></a></div>
	<div id="main">
	<div class="header">
	<span id="appname"><img src="images/app20b.png"> XDI Peer Messenger Help</span>
	&nbsp;&nbsp;&nbsp;&nbsp;<a href="XDIPeerMessenger">&gt;&gt;&gt; Back...</a><br>
	This is version <%= XDI2Properties.properties.getProperty("project.version") %> <%= XDI2Properties.properties.getProperty("project.build.timestamp") %>, Git commit <%= XDI2Properties.properties.getProperty("git.commit.id").substring(0,6) %> <%= XDI2Properties.properties.getProperty("git.commit.time") %>.
	</div>

	<div class="tabber">

    <div class="tabbertab">

	<h2>Information</h2>

	<p>Here you can apply XDI messages to a peer XDI graph and view results.</p>

	<ul>
	<li>Messages can be entered in any format.</li>
	<li>$add, $mod and $del operation will modify the XDI document in the left input text area.</li>
	<li>$get operations will not modify the XDI document in the left input text area, but will yield a direct result.</li>
	<li>If you get the error "Unknown serialization format", use the XDI Validator to debug your input document and message.</li>
	</ul>

	</div>

    <div class="tabbertab">

	<h2>Versioning</h2>

	<p>Currently not supported.</p>

	</div>

    <div class="tabbertab">

	<h2>Link Contracts</h2>

	<p>Currently not supported.</p>

	</div>

    <div class="tabbertab">

	<h2>Sender Authentication</h2>

	<p>Currently not supported.</p>

	</div>
	
	</div>

</body>
</html>
