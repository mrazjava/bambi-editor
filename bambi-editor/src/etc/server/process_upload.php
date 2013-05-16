<?php
include "db_connection.php"; // your db configuration

// Response Protocol:
// KEY|VALUE - one line, lines can be in any order
// Keys:
// STATUS - OK or ERROR, required
// RECEIVED - number of bytes received, required
// PROCESSED - number of bytes actually processed, required
// DATE - server date/time when request was processed, optional
// MSG - message from server (error or info), optional

$pid=$_SERVER['HTTP_USER_ID'];
$expected_bytes = (int) $_SERVER['HTTP_STREAM_SIZE'];
$f = $_FILES['filename']['tmp_name'];
$fsize = filesize($f);

if($pid != null) {
	$data = addslashes(fread(fopen($f, "r"), $fsize));

	if($expected_bytes == $fsize) {
		$sql = "select count(*) as c from user_pic_profile where pid = $pid";
		$result = mysql_query($sql) or die(mysql_error());
		$row=mysql_fetch_array($result);
		if($row['c'] > 0) {
			$sql = "update user_pic_profile set picture = '$data' where pid = $pid";
		}
		else {
			$sql = "insert into user_pic_profile values ($pid,'$data')";
		}

		mysql_query($sql) or die(mysql_error());
		
		echo "RECEIVED|$fsize\r\n";
		echo "PROCESSED|$fsize\r\n";
		echo "DATE|".date("Y-m-d H:i:s")."\r\n";
		echo "STATUS|OK";
	}
	else {
		echo "RECEIVED|$fsize\r\n";
		echo "PROCESSED|0\r\n";
		echo "STATUS|ERROR\r\n";
		echo "MSG|byte mismatch: [expected $expected_bytes, received: $fsize]\r\n";
		echo "DATE|".date("Y-m-d H:i:s");
	}
}
else {
	echo "RECEIVED|$fsize\r\n";
	echo "PROCESSED|0\r\n";
	echo "STATUS|ERROR\r\n";
	echo "MSG|Missing user_id\r\n";
	echo "DATE|".date("Y-m-d H:i:s");
}
?>
