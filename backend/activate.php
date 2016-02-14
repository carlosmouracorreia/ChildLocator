<?php

if(!empty($_GET['email']) && isset($_GET['email']) && !empty($_GET['code']) && isset($_GET['code']))
{
  require_once("protected/config.php");

  $email = urldecode($_GET['email']);
  $code = $_GET['code'];

  $stmt = $pdo->prepare("SELECT active FROM cl_parent WHERE email = :email AND activation_code = :code");
  $stmt->bindParam(':email', $email, PDO::PARAM_STR, 254);
  $stmt->bindParam(':code', $code, PDO::PARAM_STR, 64);
  $stmt->execute();

  $result_active = $stmt->fetch(PDO::FETCH_ASSOC);
  $stmt = null;

  if(!empty($result_active))
  {
    if($result_active["active"]==0)
    {
      $stmt = $pdo->prepare("UPDATE cl_parent SET active = 1 WHERE email = :email AND activation_code = :code");
      $stmt->bindParam(':email', $email, PDO::PARAM_STR, 254);
      $stmt->bindParam(':code', $code, PDO::PARAM_STR, 64);
      $stmt->execute();
      echo "Your account is now activated!";
    }
    else
    {
      echo "Your account is already active, no need to activate again.";
    }
  }
  else
  {
  echo "Wrong email or activation code. Please contact the admin.";
  }
}

die();
?>
