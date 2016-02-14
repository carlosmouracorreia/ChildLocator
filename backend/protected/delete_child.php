<?php

	if(empty($data["child_id"]))
		JSONResponse(array(
                'status' => 'error',
                'type'   => 'invalid_data',
                'message' => 'Data Params Missing'
        ));

	$child_id = $data["child_id"];

	$stmt = $pdo->prepare("DELETE FROM cl_child WHERE id = :child_id");

	$stmt->bindParam(':child_id', $child_id, PDO::PARAM_STR, 11);

    try {
        $stmt->execute();
    } catch(PDOException $error) {
        JSONResponse(array(
            'status' => 'error',
            'type'   => 'server_error',
            'message' => 'Internal Server Error'
        ));
    }

	$num=$stmt->rowCount();

	if($num == 0)
			JSONResponse(array(
								'status' => 'error',
								'type'   => 'child_not_found',
								'message' => 'Child not found in DB'
			));

	JSONResponse(array(
            'status' => 'success',
            'type'   => 'child_delete_success',
            'message' => 'Child successfully removed'
    ));
?>
