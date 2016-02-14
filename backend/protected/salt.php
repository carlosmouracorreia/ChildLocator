<?php

if(empty($data["email"]) && empty($data["generate"]))
	JSONResponse(array(
            'status' => 'error',
            'type'   => 'invalid_data',
            'message' => 'Data Params Missing'
    ));

$email = $data["email"];
$generate = $data["generate"];

if($generate == 1){
		$salt = null;
		$size = 18;
		$valid = false;
		while(!$valid){
			$randombytes = openssl_random_pseudo_bytes($size, $cstrong);
			$salt = base64_encode($randombytes);

			if ($cstrong == TRUE)
					$valid=true;
		}

		JSONResponse(array(
	            'status' => 'success',
	            'type'   => 'salt_response',
	            'data' => $salt
	    ));
	}

	if(!empty($email))
	{
			try {

				$stmt = $pdo->prepare("SELECT salt,active FROM cl_parent WHERE email = ?");
				$stmt->execute(array($email));
				$result_user = $stmt->fetch(PDO::FETCH_ASSOC);

				if($result_user["active"]==0)
					JSONResponse(array(
					            'status' => 'error',
					            'type'   => 'login_inactive',
					            'message' => 'Account not active. Please check your mail to activate your account.'
					    ));


				if(empty($result_user))
					JSONResponse(array(
			                'status' => 'error',
			                'type'   => 'login_incorrect',
			                'message' => 'Invalid Credentials'
			        ));

					JSONResponse(array(
				                'status' => 'success',
				                'type'   => 'salt_success',
				                'data' => $result_user["salt"]
				    ));

			} catch(PDOException $error) {
		        JSONResponse(array(
		            'status' => 'error',
		            'type'   => 'server_error',
		            'message' => 'Internal Server Error'
		        ));
		    }


	}
?>
