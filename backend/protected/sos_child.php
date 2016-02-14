<?php
    /**
    * We use this controller to fetch parent GOOGLE CLOUD unique identifier -> device id, in order to direct notifications
    * to the parent device
    * After this, we issue a Curl HTTP Request to Google Servers
    **/
	if(empty($data["child_id"]))
		JSONResponse(array(
                'status' => 'error',
                'type'   => 'invalid_data',
                'message' => 'Data Params Missing'
        ));


	$child_id = $data["child_id"];
    try {

        $stmt = $pdo->prepare("SELECT name,parent_id FROM cl_child WHERE id = ?");
        $stmt->execute(array($child_id));
        $result_user = $stmt->fetch(PDO::FETCH_ASSOC);
        $stmt = null;

        if(empty($result_user))
            JSONResponse(array(
                    'status' => 'error',
                    'type'   => 'child_invalid',
                    'message' => 'Invalid Child'
            ));
        
        $parent_id = $result_user["parent_id"];
        $message = array("title" => "SOS RECEIVED from child ".$result_user["name"],
                         "child_id" => $child_id,
                         "parent_id" => $parent_id);  

        $stmt = $pdo->prepare("SELECT gcm_token FROM cl_parent WHERE id = ?");
        $stmt->execute(array($parent_id));
        $result_parent = $stmt->fetch(PDO::FETCH_ASSOC);
        $stmt = null;

        if(empty($result_parent))
            JSONResponse(array(
                    'status' => 'error',
                    'type'   => 'parent_invalid',
                    'message' => 'Invalid Parent'
            ));

        $token = $result_parent["gcm_token"];


        JSONResponse(array(
                        'status' => 'success',
                        'type'   => 'login_success',
                        'response' => sendPushNotificationToGCM($message,$token)
        ));


    } catch(PDOException $error) {
        JSONResponse(array(
            'status' => 'error',
            'type'   => 'server_error',
            'message' => 'Internal Server Error'
        ));
    }

    //generic php function to send GCM push notification
   	function sendPushNotificationToGCM($message,$parentToken) {
        //Google cloud messaging GCM-API url
        $url = 'https://android.googleapis.com/gcm/send';
        $fields = array(
            'to' => $parentToken,
            'data' => $message,
        );
        // Google Cloud Messaging GCM API Key
        
        $headers = array(
            'Authorization: key=' . GOOGLE_API_KEY,
            'Content-Type: application/json'
        );
        $ch = curl_init();
        curl_setopt($ch, CURLOPT_URL, $url);
        curl_setopt($ch, CURLOPT_POST, true);
        curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        curl_setopt ($ch, CURLOPT_SSL_VERIFYHOST, 0);   
        curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
        curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($fields));
        $result = curl_exec($ch);               
        if ($result === FALSE) {
            die('Curl failed: ' . curl_error($ch));
        }
        curl_close($ch);
        return $result;
    }

?>
