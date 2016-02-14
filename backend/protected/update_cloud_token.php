<?php

 	/**
    * Each time the parent logins, devices starts a background service to verify/create device id token in Google Cloud Messaging Service
    * Whenever it is created/changed, we issue a client HTTP Request to this Action, in order to save the device identifier in the DB
    * So the child can send SOS requests to the parent and the parent only, using the specific device
    **/
	if(empty($data["parent_id"]) || empty($data["token"]))
		JSONResponse(array(
                'status' => 'error',
                'type'   => 'invalid_data',
                'message' => 'Data Params Missing'
        ));

	$parent_id = $data["parent_id"];
	$token = $data["token"];

	$stmt = $pdo->prepare("UPDATE cl_parent SET gcm_token = ? WHERE id = ?");

    try {
		$stmt->execute(array($token,$parent_id));

        if(!$stmt->rowCount()) {
        	JSONResponse(array(
				'status' => 'error',
				'type'   => 'parent_not_exist',
				'message' => 'Parent Does Not Exist'
			));
        } 
    } catch(PDOException $error) {
		JSONResponse(array(
				'status' => 'error',
				'type'   => 'server_error',
				'message' => 'Internal Server Error'
		));
			
    }

	JSONResponse(array(
            'status' => 'success',
            'type'   => 'gcm_update_sucess',
            'message' => 'GCM Token Successfully updated'
    ));

?>
