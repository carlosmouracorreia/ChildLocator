<?php
	/**
	* Action triggered each time the location in the child's mobile app is updated
	**/

	if(empty($data["child_id"]) || empty($data["lat"]) || empty($data["lon"]) || empty ($data["last_update"]) || empty($data["accuracy"]))
		JSONResponse(array(
                'status' => 'error',
                'type'   => 'invalid_data',
                'message' => 'Data Params Missing'
        ));

	$child_id = $data["child_id"];
	$lat = $data["lat"];
	$lon = $data["lon"];
	openssl_public_encrypt($lat, $secureLat, $DBPubK);
	openssl_public_encrypt($lon, $secureLon, $DBPubK);
	$accuracy = $data["accuracy"];
	$last_update = $data["last_update"];

	$stmt = $pdo->prepare("INSERT INTO cl_child_location (child_id, lat, lon, accuracy, last_update) VALUES(:child_id, :lat, :lon, :accuracy, :last_update)
							ON DUPLICATE KEY UPDATE lat=:lat, lon=:lon, accuracy=:accuracy, last_update=:last_update");

	$stmt->bindParam(':child_id', $child_id, PDO::PARAM_STR, 11);
	$stmt->bindParam(':lat', $secureLat, PDO::PARAM_STR);
	$stmt->bindParam(':lon', $secureLon, PDO::PARAM_STR);
	$stmt->bindParam(':accuracy', $accuracy, PDO::PARAM_STR);
	$stmt->bindParam(':last_update', $last_update, PDO::PARAM_STR);


    try {
        $stmt->execute();
    } catch(PDOException $error) {
			if($error->errorInfo[1] == 1452) {
					JSONResponse(array(
							'status' => 'error',
							'type'   => 'child_not_found',
							'message' => 'Child not found in DB'
					 ));
			} else {
					JSONResponse(array(
							'status' => 'error',
							'type'   => 'server_error',
							'message' => 'Internal Server Error'
					));
			}
    }

	JSONResponse(array(
            'status' => 'success',
            'type'   => 'location_update_sucess',
            'message' => 'Location successfully updated'
    ));

?>
