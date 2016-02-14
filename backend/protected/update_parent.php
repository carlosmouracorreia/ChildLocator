<?php
	/**
	* Refresh parent main Activity, with child Names and Locations
	**/
	if(empty($data["user_id"]))
		JSONResponse(array(
                'status' => 'error',
                'type'   => 'invalid_data',
                'message' => 'Data Params Missing'
        ));

	$id = $data["user_id"];

	//user id
	$stmt = $pdo->prepare("SELECT id, person_name FROM cl_parent WHERE id = ?");
	$stmt->execute(array($id));
	$result_user = $stmt->fetch(PDO::FETCH_ASSOC);
	$stmt = null;

	if(empty($result_user))
		JSONResponse(array(
                'status' => 'error',
                'type'   => 'id_incorrect',
                'message' => 'Non Existent ID'
        ));

	//user childs
	$stmt = $pdo->prepare("SELECT id, name, lat, lon, last_update
						   FROM cl_child
						   LEFT JOIN cl_child_location
						   ON cl_child.id = cl_child_location.child_id
						   WHERE parent_id = ? AND active = 1 AND cl_child_location.last_update IS NOT NULL");

	$stmt->execute(array($id));
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
                'type'   => 'update_success',
                'data' => $data
    ));

?>
