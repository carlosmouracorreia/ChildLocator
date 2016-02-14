<?php

	if(empty($data["parent_id"]) || empty($data["name"]))
		JSONResponse(array(
                'status' => 'error',
                'type'   => 'invalid_data',
                'message' => 'Data Params Missing'
        ));

	$parent_id = $data["parent_id"];
	$name = $data["name"];
	$size = 9;
	$token = null;


	$valid = false;
	while(!$valid){
		$tokenbytes = openssl_random_pseudo_bytes($size, $cstrong);
		$token = base64_encode($tokenbytes);

		if (ctype_alnum($token) && $cstrong == TRUE) // SO LETRAS E NUMEROS E SE FOR SECURE
			if(insertUniqueToken($pdo, $parent_id, $token, $name))
				$valid=true;
	}

	createTokenTTL($pdo, $token);

	JSONResponse(array(
            'status' => 'success',
            'type'   => 'token_sucess',
            'message' => $token
    ));


	function insertUniqueToken($pdo, $parent_id, $token, $name)
    {
	$stmt = $pdo->prepare("INSERT INTO cl_child(parent_id, token, name) VALUES (:pid, :token, :name)");
    $stmt->bindParam(':pid', $parent_id, PDO::PARAM_STR, 11);
    $stmt->bindParam(':token', $token, PDO::PARAM_STR, TOKEN_LENGTH);
    $stmt->bindParam(':name', $name, PDO::PARAM_STR, 254);
        try {
            $stmt->execute();
			return true;
        } catch(PDOException $error) {
            if($error->errorInfo[1] == 1062) {
				return false;
            } else {
                JSONResponse(array(
                    'status' => 'error',
                    'type'   => 'server_error',
                    'message' => 'Internal Server Error'
                ));
            }
        }
    }

	function createTokenTTL($pdo, $token)
    {
	$stmt = $pdo->prepare("
						CREATE EVENT TTL_".$token.
						" ON SCHEDULE AT CURRENT_TIMESTAMP + INTERVAL 5 MINUTE
						DO
							DELETE FROM cl_child WHERE token = :token AND active = 0;
						");

    $stmt->bindParam(':token', $token, PDO::PARAM_STR, TOKEN_LENGTH);
		try {
            $stmt->execute();
        } catch(PDOException $error) {

                JSONResponse(array(
                    'status' => 'error',
                    'type'   => 'server_error',
                    'message' => 'Internal Server Error'
                ));
        }
    }
?>
