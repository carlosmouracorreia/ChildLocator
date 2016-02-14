<?php

	if(empty($data["email"]) || empty($data["pwd"]))
		JSONResponse(array(
                'status' => 'error',
                'type'   => 'invalid_data',
                'message' => 'Data Params Missing'
        ));

	$email = $data["email"];
	$pwd = $data["pwd"];

	//user id

	try {

		$stmt = $pdo->prepare("SELECT id,person_name,active FROM cl_parent WHERE email = ? AND password = ?");
		$stmt->execute(array($email,$pwd));
		$result_user = $stmt->fetch(PDO::FETCH_ASSOC);
		$stmt = null;

		if(empty($result_user))
			JSONResponse(array(
	                'status' => 'error',
	                'type'   => 'login_incorrect',
	                'message' => 'Invalid Credentials'
	        ));

		if($result_user["active"]==0)
			JSONResponse(array(
			            'status' => 'error',
			            'type'   => 'login_inactive',
			            'message' => 'Account not active. Please check your mail to activate your account.'
			    ));

		$user_id = $result_user["id"];
		//user childs
		$stmt = $pdo->prepare("SELECT id, name, lat, lon, last_update
							   FROM cl_child
							   LEFT JOIN cl_child_location
							   ON cl_child.id = cl_child_location.child_id
							   WHERE parent_id = ? AND active = 1 AND cl_child_location.last_update IS NOT NULL");

		$stmt->execute(array($user_id));
		$result_child = $stmt->fetchAll(PDO::FETCH_ASSOC);
		$stmt = null;

		$data = [];
		$data["user"] = $result_user;

		// Decrypt Lat and Lon
		foreach ($result_child as &$child){
		 		openssl_private_decrypt($child["lat"], $decryptedLat, $DBPrivK);
				$child["lat"]=$decryptedLat;
				openssl_private_decrypt($child["lon"], $decryptedLon, $DBPrivK);
				$child["lon"]=$decryptedLon;
			}

		$data["childs"] = $result_child;

		JSONResponse(array(
	                'status' => 'success',
	                'type'   => 'login_success',
	                'data' => $data
	    ));

	} catch(PDOException $error) {
        JSONResponse(array(
            'status' => 'error',
            'type'   => 'server_error',
            'message' => 'Internal Server Error'
        ));
    }
?>
