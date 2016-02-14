<?php

	$dsn = "mysql:host=localhost;dbname=sirs26";
	$username = "sirs26";
	$password = "y8TrawRu";

	$DBPrivK = get_DBPrivateKey('y8TrawRu');
	$DBPubK = get_DBPublicKey();
	$CLIENT_API_PRIVATE = get_DROIDPrivateKey('y8TrawRu');


	define("APP_NAME_HEADER","CLAppIST-1.0");
	define("WEBSITE_URL", "https://sirs26.loopingbit.com");
	define("TOKEN_LENGTH", 12);
	define("GOOGLE_API_KEY",     "AIzaSyDMI-YsNJY3DAH-M-ol75u5PSmoOq113RY");

	try {
	    $pdo = new PDO($dsn, $username, $password);
	    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
	}
	catch(PDOException $e) {
	    die("Could not connect to the database\n");
	}


	function get_DBPrivateKey($passphrase)
	  {
	    $privk = openssl_pkey_get_private('file://protected/Keys/DBcertificate.pem', $passphrase);
	    openssl_pkey_export($privk, $pkeyout);
	    return $pkeyout;
	  }

	function get_DBPublicKey()
	  {
	    $pubk = openssl_pkey_get_public('file://protected/Keys/DBpublic.pem');
	    $keyData = openssl_pkey_get_details($pubk);
	    return $keyData["key"];
	  }

	function get_DROIDPrivateKey($passphrase)
	{
		$privk = openssl_pkey_get_private('file://protected/Keys/client_api_certificate.pem', $passphrase);
	    openssl_pkey_export($privk, $pkeyout);
	    return $pkeyout;
	}
?>
