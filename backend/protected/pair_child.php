<?php

	if(empty($data["token"]) || strlen($data["token"])!=TOKEN_LENGTH)
		JSONResponse(array(
                'status' => 'error',
                'type'   => 'invalid_data',
                'message' => 'Data Params Missing/Invalid'
        ));

	$token = $data["token"];

	try {
		$stmt = $pdo->prepare("SELECT active FROM cl_child WHERE token = ?");
		$stmt->execute(array($token));

   	$result_active = $stmt->fetch(PDO::FETCH_ASSOC);
		$stmt = null;
		/**
		* If child is already active, we output a badass and ugly error
		**/
		if(!empty($result_active) && $result_active["active"]==1)
			JSONResponse(array(
                'status' => 'error',
                'type'   => 'pair_failed',
                'message' => 'Child Already paired. Delete it in Parent App, and add a new one'
    		));

		$stmt = $pdo->prepare("UPDATE cl_child SET active = 1 WHERE token = :token");
		$stmt->bindParam(':token', $token, PDO::PARAM_STR, TOKEN_LENGTH);
       	$stmt->execute();
       	if($stmt->rowCount()==1) {
       		/**
       		* Child was paired, now fetch basic info : id and name
       		**/
       		$stmt = $pdo->prepare("SELECT id,name FROM cl_child WHERE token = ?");
					$stmt->execute(array($token));

       		$result_child = $stmt->fetch(PDO::FETCH_ASSOC);
					$stmt = null;

       		JSONResponse(array(
                'status' => 'success',
                'type'   => 'success_pair_child',
                'message' => 'Child Succesfully paired',
                'data' => $result_child
        	));

       	} else {
       		 JSONResponse(array(
                'status' => 'error',
                'type'   => 'pair_failed',
                'message' => 'Error pairing child. Token is incorrect or has already expired'
        	));
       	}
    } catch(PDOException $error) {
        JSONResponse(array(
            'status' => 'error',
            'type'   => 'server_error',
            'message' => 'Internal Server Error'
        ));

    }

?>
