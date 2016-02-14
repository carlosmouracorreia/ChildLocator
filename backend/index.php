<?php

	require_once("protected/config.php");

	if(!isset($_SERVER['HTTP_X_APP']) || $_SERVER['HTTP_X_APP']!=APP_NAME_HEADER) {
		JSONResponse(array(
                'status' => 'error',
                'type'   => 'bad_request',
                'message' => 'Bad Request'
            ));
	}

	if(!isset($_POST["query"]))
		 JSONResponse(array(
                'status' => 'error',
                'type'   => 'missing_request_content',
                'message' => 'Bad Request (Request Content)'
            ));

	$rawQuery = $_POST["query"];


	//START ENCRYPTING

	$decryptedContent = "";
  	if (!openssl_private_decrypt(base64_decode($rawQuery), $decryptedContent, $CLIENT_API_PRIVATE)){
        JSONResponse(array(
            'status' => 'authentication_error',
            'message' => 'Failed to decrypt data'
        ));
  	}

    $rawQuery = utf8_encode($decryptedContent);

	//END ENCRYPTING
	$structuredQuery = json_decode($rawQuery,true);


	if(empty($structuredQuery["data"]) || empty($structuredQuery["action"]))
		JSONResponse(array(
                'status' => 'error',
                'type'   => 'invalid_args',
                'message' => 'Arguments are invalid or missing'
        ));
	$data = $structuredQuery["data"];
	$action = $structuredQuery["action"];

	 switch($action) {
		case "register":
			require_once("protected/register.php");
		break;
		case "login":
			require_once("protected/login.php");
		break;
		case "get_token":
			require_once("protected/token.php");
		break;
		case "get_salt":
			require_once("protected/salt.php");
		break;
		case "update_parent":
			require_once("protected/update_parent.php");
		break;
		case "pair_child":
			require_once("protected/pair_child.php");
		break;
		case "update_location":
			require_once("protected/update_location.php");
		break;
		case "delete_child":
			require_once("protected/delete_child.php");
		break;
		case "sos_child":
			require_once("protected/sos_child.php");
		break;
		case "update_parent_cloud_token":
			require_once("protected/update_cloud_token.php");
		break;
		default:
			JSONResponse(array(
                'status' => 'error',
                'type'   => 'unknown_action',
                'message' => 'Unknown Action'
            ));
	}


	function JSONResponse($response)
    {
        echo json_encode($response,JSON_PRETTY_PRINT);
        DIE();
    }

?>
