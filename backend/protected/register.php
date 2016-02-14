<?php

	if(empty($data["email"]) || empty($data["pwd"]) || empty($data["salt"]) || empty($data["name"]))
		JSONResponse(array(
                'status' => 'error',
                'type'   => 'invalid_data',
                'message' => 'Data Params Missing'
        ));

	$email = $data["email"];
	$pwd = $data["pwd"];
	$salt = $data["salt"];
	$name = $data["name"];

	$token = null;
	$valid = false;
	while(!$valid){
	  $tokenbytes = openssl_random_pseudo_bytes(32, $cstrong);
	  $token = base64_encode($tokenbytes);

	  if ($cstrong == TRUE)
	    $valid=true;
	}

	$plaintext = $email.time().$token;
	$activation_code = hash('sha256', $plaintext);

	$stmt = $pdo->prepare("INSERT INTO cl_parent (email, password, salt, person_name, activation_code) VALUES (:email, :password, :salt, :person_name, :activation_code)");

    $stmt->bindParam(':email', $email, PDO::PARAM_STR, 254);
    $stmt->bindParam(':password', $pwd, PDO::PARAM_STR, 64);
		$stmt->bindParam(':salt', $salt, PDO::PARAM_STR, 25);
    $stmt->bindParam(':person_name', $name, PDO::PARAM_STR, 254);
		$stmt->bindParam(':activation_code', $activation_code, PDO::PARAM_STR, 64);

        try {
            $stmt->execute();
						$id = $pdo->lastInsertId();
						$stmt = null;
						activation_email($email, $name, $activation_code);
						createActivationTTL($pdo, $id, $email, $activation_code);
            JSONResponse(array(
                'status' => 'success',
                'type'   => 'success_register',
                'message' => 'User successfully registered. Please confirm your email.'
            ));

        } catch(PDOException $error) {
            if($error->errorInfo[1] == 1062) {
                JSONResponse(array(
                    'status' => 'error',
                    'type'   => 'email_exists',
                    'message' => 'Mail existent on the database'
                 ));
            } else {
                JSONResponse(array(
                    'status' => 'error',
                    'type'   => 'server_error',
                    'message' => 'Internal Server Error'
                ));
            }
        }

				function activation_email($email, $name, $activation_code)
					{
						$message = "Welcome to Secure Child Locator, ".$name."!\n\n";
						$message .= "To activate your account, please follow the link below. The link will remain valid for 24 hours.\n";
						$message .= WEBSITE_URL . '/activate.php?email=' . urlencode($email) . "&code=".$activation_code."\n\n";
						$message .= "Please do not reply to this email.\n\n";
						$message .= "Kind Regards,\n";
						$message .= "SIRS26 Team";
						mail($email, 'Registration Confirmation', $message, 'From: Child Locator SIRS26 <sirs26@loopingbit.com>');
					}

					function createActivationTTL($pdo, $id, $email, $activation_code)
						{
					$stmt = $pdo->prepare("
										CREATE EVENT TTL_".$id.
										" ON SCHEDULE AT CURRENT_TIMESTAMP + INTERVAL 24 HOUR
										DO
											DELETE FROM cl_parent WHERE email = :email AND activation_code = :activation_code AND active = 0;
										");

					$stmt->bindParam(':email', $email, PDO::PARAM_STR, 254);
					$stmt->bindParam(':activation_code', $activation_code, PDO::PARAM_STR, 64);

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
